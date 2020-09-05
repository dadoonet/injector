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
import com.swiftype.appsearch.Client;
import com.swiftype.appsearch.ClientException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.demo.workshop.injector.bean.AppSearchPerson;
import org.elasticsearch.demo.workshop.injector.bean.Person;
import org.elasticsearch.demo.workshop.injector.serializer.MetaParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AppSearchInjector extends Injector {

    private static final Logger logger = LogManager.getLogger(AppSearchInjector.class);
    private static final int MAX_DOCUMENTS = 100000;
    private final int MAX_BULK_SIZE = 100;
    private final String host;
    private final String key;
    private final String engineName;
    private final int bulkSize;
    private Client client;
    private List<String> documents;

    public AppSearchInjector(String engine, String host, String key, int bulkSize) {
        logger.info("Using App Search service running on {} with bulk size of {} documents in engine named {}", host, bulkSize, engine);
        // Hard limit of the App Search
        if (bulkSize > MAX_BULK_SIZE) {
            logger.warn("Forcing bulk size for app search to {} as this is its maximum possible value.", MAX_BULK_SIZE);
            this.bulkSize = MAX_BULK_SIZE;
        } else {
            this.bulkSize = bulkSize;
        }
        this.host = host;
        this.key = key;
        this.engineName = engine;
        this.documents = new ArrayList<>();
    }

    @Override
    public void internalStart() throws IOException {
        // We check our engine is available
        try {
            client = new Client(host, key);
            client.getEngine(engineName);
        } catch (ClientException e) {
            if (e.getMessage().contains("404")) {
                // Engine does not exist. Let's create it.
                try {
                    client.createEngine(engineName);
                } catch (ClientException e1) {
                    throw new IOException("Can not create engine " + engineName +
                            ". Please check https://app.swiftype.com/as/engines/ and/or create it manually.");
                }
            } else {
                throw new IOException("Can not access to the AppSearch platform", e);
            }
        }
        started = true;
    }

    @Override
    List<IllegalArgumentException> validateSettings() {
        List<IllegalArgumentException> errors = new ArrayList<>();
        if (this.bulkSize > MAX_BULK_SIZE) {
            errors.add(new IllegalArgumentException("bulk size can not exceed " + MAX_BULK_SIZE + " but is set to " + this.bulkSize));
        }
        if (StringUtils.isBlank(this.host)) {
            errors.add(new IllegalArgumentException("ap.host must be set when using App Search"));
        }
        if (StringUtils.isBlank(this.key)) {
            errors.add(new IllegalArgumentException("ap.key must be set when using App Search"));
        }
        return errors;
    }

    private boolean skipping = false;

    @Override
    public void inject(int num, Person person) {
        // We need to skip documents if we reach the maximum limit of App Search
        if (skipping || num > MAX_DOCUMENTS) {
            if (!skipping) {
                logger.warn("We reached the maximum number of documents we can have on AppSearch ({}). Skipping the next ones.", MAX_DOCUMENTS);
            }
            skipping = true;
        } else {
            try {
                documents.add(MetaParser.mapper.writeValueAsString(new AppSearchPerson(person)));
                if (documents.size() >= bulkSize) {
                    internalIndex(documents);
                }
            } catch (JsonProcessingException e) {
                logger.warn("Can not serialize to JSON", e);
            }
        }
    }

    @Override
    public void close() {
        logger.debug("Closing the injector");
        // If we do have some remaining documents, let's index then
        internalIndex(documents);
    }

    private void internalIndex(List<String> documents) {
        if (documents.size() > 0) {
            logger.debug("Calling app search engine to index {} documents", documents.size());
            this.documents = new ArrayList<>();
            try {
                client.indexJsonDocuments(engineName, documents);
            } catch (ClientException e) {
                logger.error("Error while injecting documents", e);
            }
        }
    }
}
