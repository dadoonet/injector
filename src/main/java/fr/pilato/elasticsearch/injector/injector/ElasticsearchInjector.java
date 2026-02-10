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
import fr.pilato.elasticsearch.injector.bean.Person;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static fr.pilato.elasticsearch.injector.helper.SSLUtils.createTrustAllCertsContext;

/**
 * Injects generated persons into an Elasticsearch cluster via the bulk API.
 */
public class ElasticsearchInjector extends Injector {

    private static final Logger logger = LogManager.getLogger(ElasticsearchInjector.class);
    /** Elasticsearch Java API client. */
    private final ElasticsearchClient client;
    private final String index;
    /** Bulk ingester for batching index requests. */
    private final BulkIngester<Void> ingester;

    /**
     * Creates an Elasticsearch injector.
     * @param index target index name
     * @param bulkSize bulk size
     * @param host Elasticsearch host URL
     * @param username basic auth username (deprecated)
     * @param password basic auth password (deprecated)
     * @param apikey API key for authentication (optional)
     */
    public ElasticsearchInjector(String index, int bulkSize, String host, String username, String password, String apikey) {
        logger.info("Using Elasticsearch backend running at {} with bulk size of {} documents in index {}", host, bulkSize, index);
        this.index = index;

        if (apikey != null) {
            logger.debug("Using API Key authentication");
            client = ElasticsearchClient.of(b -> b
                    .host(host)
                    .sslContext(createTrustAllCertsContext())
                    .apiKey(apikey)
            );
        } else {
            logger.warn("Using basic authentication is deprecated. Please use API Key instead (--es.apikey).");
            client = ElasticsearchClient.of(b -> b
                    .host(host)
                    .sslContext(createTrustAllCertsContext())
                    .usernameAndPassword(username, password)
            );
        }

        // Create the new client (using the low level client)
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
                client.close();
            } catch (IOException e) {
                throw new RuntimeException("Can not close properly the elasticsearch Rest Client, e");
            }
        }
    }
}
