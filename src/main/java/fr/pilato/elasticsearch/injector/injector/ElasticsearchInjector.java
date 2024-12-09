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
import co.elastic.clients.elasticsearch._helpers.bulk.BulkIngester;
import co.elastic.clients.elasticsearch._helpers.bulk.BulkListener;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.InfoResponse;
import co.elastic.clients.elasticsearch.indices.PutIndexTemplateResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import fr.pilato.elasticsearch.injector.bean.Person;

import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;

public class ElasticsearchInjector extends Injector {

    private static final Logger logger = LogManager.getLogger(ElasticsearchInjector.class);
    private final ElasticsearchClient client;
    private final ElasticsearchTransport transport;
    private final String index;
    private final BulkIngester<Void> ingester;

    public ElasticsearchInjector(String index, int bulkSize, String host, String username, String password, String apikey) {
        logger.info("Using Elasticsearch backend running at {} with bulk size of {} documents in index {}", host, bulkSize, index);
        this.index = index;

        try {
            SSLContextBuilder sslBuilder = SSLContexts.custom().loadTrustMaterial(null, (x509Certificates, s) -> true);
            final SSLContext sslContext = sslBuilder.build();

            RestClientBuilder lowLevelClientBuilder = RestClient.builder(HttpHost.create(host));
            if (apikey != null) {
                lowLevelClientBuilder.setHttpClientConfigCallback(hcb -> hcb
                        .setSSLContext(sslContext)
                        .setSSLHostnameVerifier(new NoopHostnameVerifier())
                );
                lowLevelClientBuilder.setDefaultHeaders(new Header[] {
                    new BasicHeader("Authorization", "ApiKey " + apikey)
                });
            } else {
                logger.warn("Using basic authentication is deprecated. Please use API Key instead (--es.apikey).");
                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
                lowLevelClientBuilder.setHttpClientConfigCallback(hcb -> hcb
                        .setSSLContext(sslContext)
                        .setSSLHostnameVerifier(new NoopHostnameVerifier())
                        .setDefaultCredentialsProvider(credentialsProvider)
                );
            }

            // Create the new client (using the low level client)
            transport = new RestClientTransport(lowLevelClientBuilder.build(), new JacksonJsonpMapper());
            client = new ElasticsearchClient(transport);

            ingester = BulkIngester.of(b -> b
                    .client(client)
                    .listener(new BulkListener<>() {
                        @Override
                        public void beforeBulk(long executionId, BulkRequest request, List<Void> voids) {
                            logger.debug("going to execute bulk of {} requests", request.operations().size());
                        }

                        @Override
                        public void afterBulk(long executionId, BulkRequest request, List<Void> voids, BulkResponse response) {
                            logger.debug("bulk executed {} errors", response.errors() ? "with" : "without");
                        }

                        @Override
                        public void afterBulk(long executionId, BulkRequest request, List<Void> voids, Throwable failure) {
                            logger.warn("error while executing bulk", failure);
                        }
                    })
                    .maxOperations(bulkSize)
            );

        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void internalStart() throws IOException {
        InfoResponse info = client.info();
        logger.info("Injector connected to a node running elasticsearch {}", info.version().number());

        // Create or Update the template
        try (InputStream stream = Injector.class.getResource("/template.json").openStream()) {
            PutIndexTemplateResponse response = client.indices().putIndexTemplate(itb -> itb.name(index).withJson(stream));
            if (response.acknowledged()) {
                logger.info("Template [{}] created or updated", index);
            } else {
                logger.fatal("Can not create template [{}]", index);
            }
        }
    }

    @Override
    List<IllegalArgumentException> validateSettings() {
        return Collections.emptyList();
    }

    @Override
    public void inject(int finalI, Person person) {
        ingester.add(bo -> bo.index(
                io -> io.index(index).document(person)
        ));
    }

    @Override
    public void close() {
        logger.debug("Closing the injector");
        if (ingester != null) {
            ingester.close();
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
