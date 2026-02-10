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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.pilato.elasticsearch.injector.bean.Person;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import fr.pilato.elasticsearch.injector.serializer.MetaParser;

import java.util.Collections;
import java.util.List;

/**
 * Used to print out every single generated element to the console.
 */
public class ConsoleInjector extends Injector {

    private static final Logger logger = LogManager.getLogger(ConsoleInjector.class);

    /** Jackson mapper for serializing persons to JSON (pretty or compact). */
    private final ObjectMapper mapper;

    /**
     * Creates a console injector.
     * @param pretty if true, output is pretty-printed JSON
     */
    public ConsoleInjector(boolean pretty) {
        logger.info("Using Console to generate the dataset with pretty set to {}", pretty);
        mapper = pretty ? MetaParser.prettyMapper : MetaParser.mapper;
    }

    @Override
    public void internalStart() {
    }

    @Override
    List<IllegalArgumentException> validateSettings() {
        return Collections.emptyList();
    }

    @Override
    public void inject(int finalI, Person person) {
        try {
            System.out.println(mapper.writeValueAsString(person));
        } catch (JsonProcessingException e) {
            logger.warn("Can not serialize to JSON", e);
        }
    }

    @Override
    public void close() {
    }
}
