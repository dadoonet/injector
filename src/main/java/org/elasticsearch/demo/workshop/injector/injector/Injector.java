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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.demo.workshop.injector.bean.Person;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public abstract class Injector implements Closeable {

    private static final Logger logger = LogManager.getLogger(Injector.class);

    boolean started = false;

    /**
     * Start the engine. Check that it's running and prepare what is needed to make it work properly.
     * @throws Exception In case something goes wrong
     */
    public abstract void internalStart() throws Exception;

    /**
     * Start the engine. Check that it's running and prepare what is needed to make it work properly.
     */
    public void start() {
        logger.trace("Validating settings for engine {}", this.getClass().getSimpleName());
        try {
            List<IllegalArgumentException> exceptions = validateSettings();
            if (!exceptions.isEmpty()) {
                logger.error("We can not start {} engine because of {} error{}", this.getClass().getSimpleName(), exceptions.size(), exceptions.size() > 1 ? "s" : "");
                exceptions.forEach(e -> logger.error(" - {}", e.getMessage()));
                throw new Exception("Error while validating settings");
            }
            logger.debug("Starting engine {}", this.getClass().getSimpleName());
            internalStart();
            started = true;
        } catch (Exception e) {
            logger.warn("Can not start engine {}: {}", this.getClass().getSimpleName(), e.getMessage());
            logger.debug("Stacktrace for engine {}", this.getClass().getSimpleName(), e);
        }
    }

    /**
     * Check that settings are correctly set when launching the injector
     */
    abstract List<IllegalArgumentException> validateSettings() throws IllegalArgumentException;

    /**
     * Close the injector
     */
    abstract public void close();

    /**
     * Inject a document to the search engine
     * @param num     entity number
     * @param person  person bean to index
     */
    abstract public void inject(int num, Person person);


    /**
     * Read a file in classpath and return its content. If the file is not found, the error is logged, but null
     * is returned so that the user is aware of what happened.
     *
     * @param url File URL Example : /es/twitter/_settings.json
     * @return File content or null if file doesn't exist
     */
    static String readFileInClasspath(String url) {
        StringBuilder bufferJSON = new StringBuilder();

        try (InputStreamReader ipsr = new InputStreamReader(Injector.class.getResource(url).openStream());
             BufferedReader br = new BufferedReader(ipsr)) {
            String line;

            while ((line = br.readLine()) != null) {
                bufferJSON.append(line);
            }
        } catch (IOException e) {
            logger.error("Failed to load file from url [{}]: {}", url, e.getMessage());
            return null;
        }

        return bufferJSON.toString();
    }

    public boolean isStarted() {
        return started;
    }
}
