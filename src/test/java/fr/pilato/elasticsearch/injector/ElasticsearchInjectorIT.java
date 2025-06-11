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
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.InfoResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import fr.pilato.elasticsearch.injector.bean.Person;
import fr.pilato.elasticsearch.injector.runner.Generate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.*;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static fr.pilato.elasticsearch.injector.helper.SSLUtils.createContextFromCaCert;
import static fr.pilato.elasticsearch.injector.helper.SSLUtils.createTrustAllCertsContext;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class ElasticsearchInjectorIT {

    private static final Logger logger = LogManager.getLogger();
    private ElasticsearchClient client = null;
    private String apikey;
    private static final String PASSWORD = "changeme";
    private static ElasticsearchContainer container;
    private static byte[] certAsBytes;

    private static final String INDEX_NAME_DEFAULT = "person";
    private static final String INDEX_NAME = "anotherindexname";

    @BeforeAll
    static void startTestContainers() throws IOException {
        Properties props = new Properties();
        props.load(ElasticsearchInjectorIT.class.getResourceAsStream("/version.properties"));
        String version = props.getProperty("elasticsearch.version");
        logger.info("Starting testcontainers with Elasticsearch {}.", version);
        // Start the container. This step might take some time...
        container = new ElasticsearchContainer(
                DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch")
                        .withTag(version))
                .withPassword(PASSWORD)
                .withReuse(true);
        container.start();
        certAsBytes = container.copyFileFromContainer(
                "/usr/share/elasticsearch/config/certs/http_ca.crt",
                InputStream::readAllBytes);
    }
    
    @BeforeEach
    void createClientAndCleanData() throws IOException {
        client = getClient("https://" + container.getHttpHostAddress(), certAsBytes);

        apikey = client.security().createApiKey(cak -> cak.name("injector")).encoded();

        // Recreate the client with the api key
        client = getClient("https://" + container.getHttpHostAddress(), certAsBytes);

        removeData(INDEX_NAME);
        removeData(INDEX_NAME_DEFAULT);
    }

    @AfterEach
    void stopElasticsearchClient() throws IOException {
        if (client != null) {
            client._transport().close();
        }
    }

    @Test
    void testInjector() throws IOException {
        testWithArgs(INDEX_NAME_DEFAULT, new String[]{
                "--elasticsearch",
                "--nb", "100",
                "--es.host", "https://" + container.getHttpHostAddress(),
                "--es.apikey", apikey,
            });
    }

    @Test
    void testInjectorWithIndexName() throws IOException {
        testWithArgs(INDEX_NAME, new String[]{
                "--elasticsearch",
                "--nb", "100",
                "--es.host", "https://" + container.getHttpHostAddress(),
                "--es.apikey", apikey,
                "--es.index", INDEX_NAME
            });
    }

    private void testWithArgs(String indexName, String[] args) throws IOException {
        Generate.main(args);
        client.indices().refresh();
        SearchResponse<Person> response = client.search(sr -> sr.index(indexName), Person.class);
        assertThat(response.hits().total().value(), is(100L));
        // Check that the index template exists
        assertThat(client.indices().getIndexTemplate(itb -> itb.name(indexName)).indexTemplates(), not(emptyIterable()));
    }

    private ElasticsearchClient getClient(String elasticsearchServiceAddress, byte[] certificate) throws IOException {
        logger.debug("Trying to connect to {} {}.", elasticsearchServiceAddress,
                certificate == null ? "with no ssl checks": "using the provided SSL certificate");

        // Create the client
        if (apikey != null) {
            logger.debug("Using API Key authentication");
            client = ElasticsearchClient.of(b -> b
                    .host(elasticsearchServiceAddress)
                    .sslContext(certificate != null ?
                            createContextFromCaCert(certificate) : createTrustAllCertsContext())
                    .apiKey(apikey)
            );
        } else {
            logger.warn("Using basic authentication is deprecated. Please use API Key instead (--es.apikey).");
            client = ElasticsearchClient.of(b -> b
                    .host(elasticsearchServiceAddress)
                    .sslContext(certificate != null ?
                            createContextFromCaCert(certificate) : createTrustAllCertsContext())
                    .usernameAndPassword("elastic", PASSWORD)
            );
        }

        InfoResponse info = client.info();
        logger.debug("Client connected to a cluster running version {} at {}.", info.version().number(), elasticsearchServiceAddress);
        return client;
    }

    private void removeData(String indexName) throws IOException {
        logger.debug("Removing index [{}]", indexName);
        client.indices().delete(dir -> dir.index(indexName).ignoreUnavailable(true));
        try {
            logger.debug("Removing index template data [{}]", indexName);
            client.indices().deleteIndexTemplate(itb -> itb.name(indexName));
        } catch (ElasticsearchException ignored) {}
    }
}
