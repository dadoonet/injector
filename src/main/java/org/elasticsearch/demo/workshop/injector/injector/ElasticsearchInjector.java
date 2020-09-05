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

package org.elasticsearch.demo.workshop.injector.injector;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.main.MainResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.PutIndexTemplateRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.demo.workshop.injector.bean.Person;
import org.elasticsearch.demo.workshop.injector.serializer.MetaParser;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ElasticsearchInjector extends Injector {

    private static final Logger logger = LogManager.getLogger(ElasticsearchInjector.class);
    private final RestHighLevelClient client;
    private final BulkProcessor bulkProcessor;
    private final String index;

    public ElasticsearchInjector(String index, int bulkSize, String host, String username, String password) {
        logger.info("Using Elasticsearch backend running at {} with bulk size of {} documents in index {}", host, bulkSize, index);
        this.index = index;
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        RestClientBuilder builder = RestClient
                .builder(HttpHost.create(host))
                .setHttpClientConfigCallback(hcb -> hcb.setDefaultCredentialsProvider(credentialsProvider));

        client = new RestHighLevelClient(builder);
        bulkProcessor = BulkProcessor.builder(
                (request, bulkListener) -> client.bulkAsync(request, RequestOptions.DEFAULT, bulkListener),
                new SimpleLoggerListener())
                .setBulkActions(bulkSize)
                .setFlushInterval(TimeValue.timeValueSeconds(5))
                .build();
    }

    @Override
    public void internalStart() throws IOException {
        // We check the cluster is running
        MainResponse info = client.info(RequestOptions.DEFAULT);
        logger.info("Injector connected to a node running elasticsearch {}", info.getVersion().toString());

        // Create or Update the template
        PutIndexTemplateRequest request = new PutIndexTemplateRequest(index);
        String schema = readFileInClasspath("/template.json");
        request.source(schema, XContentType.JSON);
        logger.info("Creating or updating template [{}]", index);
        client.indices().putTemplate(request, RequestOptions.DEFAULT);
    }

    @Override
    List<IllegalArgumentException> validateSettings() {
        return Collections.emptyList();
    }

    @Override
    public void inject(int finalI, Person person) {
        try {
            bulkProcessor.add(new IndexRequest(index).source(MetaParser.mapper.writeValueAsString(person), XContentType.JSON));
        } catch (JsonProcessingException e) {
            logger.warn("Can not serialize to JSON", e);
        }
    }

    @Override
    public void close() {
        logger.debug("Closing the injector");
        if (bulkProcessor != null) {
            bulkProcessor.flush();
            try {
                bulkProcessor.awaitClose(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException("Can not close properly the bulk processor, e");
            }
        }
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                throw new RuntimeException("Can not close properly the elasticsearch Rest Client, e");
            }
        }
    }

    static class SimpleLoggerListener implements BulkProcessor.Listener {
        @Override
        public void beforeBulk(long executionId, BulkRequest request) {
            logger.debug("Going to execute new bulk composed of {} actions", request.numberOfActions());
        }

        @Override
        public void afterBulk(long executionId, BulkRequest request, BulkResponse response) {
            logger.debug("Executed bulk composed of {} actions", request.numberOfActions());
            if (response.hasFailures()) {
                logger.warn("There was failures while executing bulk: {}", response.buildFailureMessage());
                if (logger.isDebugEnabled()) {
                    for (BulkItemResponse item : response.getItems()) {
                        if (item.isFailed()) {
                            logger.debug("Error for {}/{}/{} for {} operation: {}", item.getIndex(),
                                    item.getType(), item.getId(), item.getOpType(), item.getFailureMessage());
                        }
                    }
                }
            }
        }

        @Override
        public void afterBulk(long executionId, BulkRequest request, Throwable failure) {
            logger.warn("Error executing bulk", failure);
        }
    }
}
