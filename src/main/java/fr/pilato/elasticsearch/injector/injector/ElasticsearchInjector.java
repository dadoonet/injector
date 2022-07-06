/*
 * Licensed to Elasticsearch under one or more contributor
 * license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright
 * ownership. Elasticsearch licenses this file to you under
 * the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package fr.pilato.elasticsearch.injector.injector;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.InfoResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.core.bulk.IndexOperation;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import co.elastic.clients.util.ApiTypeHelper;
import fr.pilato.elasticsearch.injector.bean.Person;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ElasticsearchInjector extends Injector {

    private static final Logger logger = LogManager.getLogger(ElasticsearchInjector.class);
    private final RestClient lowLevelClient;
    private final ElasticsearchClient client;
    private final ElasticsearchTransport transport;
    private final String index;
    private final int bulkSize;
    private List<BulkOperation> bulkOperations;

    public ElasticsearchInjector(String index, int bulkSize, String host, String username, String password) {
        logger.info("Using Elasticsearch backend running at {} with bulk size of {} documents in index {}", host, bulkSize, index);
        this.index = index;
        this.bulkSize = bulkSize;

        try {
            SSLContextBuilder sslBuilder = SSLContexts.custom().loadTrustMaterial(null, (x509Certificates, s) -> true);
            final SSLContext sslContext = sslBuilder.build();

            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
            lowLevelClient = RestClient
                    .builder(HttpHost.create(host))
                    .setHttpClientConfigCallback(hcb -> hcb
                            .setSSLContext(sslContext)
                            .setSSLHostnameVerifier(new NoopHostnameVerifier())
                            .setDefaultCredentialsProvider(credentialsProvider)
                    )
                    .build();

            // Create the new client (using the low level client)
            transport = new RestClientTransport(lowLevelClient, new JacksonJsonpMapper());
            client = new ElasticsearchClient(transport);
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new RuntimeException(e);
        }

        bulkOperations = new ArrayList<>(bulkSize);
    }

    @Override
    public void internalStart() throws IOException {
        // We check the cluster is running
        ApiTypeHelper.DANGEROUS_disableRequiredPropertiesCheck(true);
        InfoResponse info = client.info();
        ApiTypeHelper.DANGEROUS_disableRequiredPropertiesCheck(false);
        logger.info("Injector connected to a node running elasticsearch {}", info.version().number());

        // Create or Update the template
        String schema = readFileInClasspath("/template.json");
        Request request = new Request("PUT", "/_index_template/" + index);
        request.setJsonEntity(schema);
        lowLevelClient.performRequest(request);
        logger.info("Template [{}] created or updated", index);
    }

    @Override
    List<IllegalArgumentException> validateSettings() {
        return Collections.emptyList();
    }

    @Override
    public void inject(int finalI, Person person) {
        bulkOperations.add(IndexOperation.of(c -> c.document(person))._toBulkOperation());
        if (bulkOperations.size() >= bulkSize) {
            doExecute();
            bulkOperations = new ArrayList<>(bulkSize);
        }
    }

    private void doExecute() {
        try {
            logger.debug("Going to execute new bulk composed of {} actions", bulkOperations.size());
            BulkResponse response = client.bulk(r -> r.index(index).operations(bulkOperations));
            logger.debug("Executed bulk composed of {} actions", bulkOperations.size());
            if (response.errors()) {
                logger.warn("There was failures while executing bulk");
                if (logger.isDebugEnabled()) {
                    response.items()
                            .stream()
                            .filter(i -> i.error() != null)
                            .forEach(item -> logger.debug("Error for {}/{} for {} operation: {}",
                                    item.index(), item.id(), item.operationType(), item.error().reason()));
                }
            }

        } catch (Throwable t) {
            logger.warn("Error executing bulk", t);
        }
    }

    @Override
    public void close() {
        logger.debug("Closing the injector");
        if (!bulkOperations.isEmpty()) {
            doExecute();
        }

        if (client != null) {
            try {
                transport.close();
            } catch (IOException e) {
                throw new RuntimeException("Can not close properly the elasticsearch Rest Client, e");
            }
        }
    }
}
