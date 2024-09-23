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

package fr.pilato.elasticsearch.injector;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import fr.pilato.elasticsearch.injector.bean.Person;
import fr.pilato.elasticsearch.injector.runner.Generate;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ElasticsearchInjectorIT {
    private ElasticsearchClient client;
    private ElasticsearchTransport transport;

    @BeforeEach
    void initClient() {
        try {
            SSLContextBuilder sslBuilder = SSLContexts.custom().loadTrustMaterial(null, (x509Certificates, s) -> true);
            final SSLContext sslContext = sslBuilder.build();

            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "changeme"));
            RestClient lowLevelClient = RestClient
                    .builder(HttpHost.create("https://127.0.0.1:9200"))
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
    }

    @Test
    void testInjector() throws IOException {
        client.indices().delete(dir -> dir.index("person").ignoreUnavailable(true));
        Generate.main(new String[]{"--elasticsearch", "--nb", "100"});
        client.indices().refresh();
        SearchResponse<Person> response = client.search(sr -> sr.index("person"), Person.class);
        assertThat(response.hits().total().value(), is(100L));
        // Check that the index template exists
        assertThat(client.indices().getIndexTemplate(itb -> itb.name("person")).indexTemplates(), not(emptyIterable()));
    }

    @AfterEach
    void stopClient() {
        if (client != null) {
            try {
                transport.close();
            } catch (IOException e) {
                throw new RuntimeException("Can not close properly the elasticsearch Rest Client, e");
            }
        }
    }
}
