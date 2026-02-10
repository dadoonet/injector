/*
 * Licensed to David Pilato (the "Author") under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. Author licenses this
 * file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package fr.pilato.elasticsearch.injector.serializer;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Helper to parse from and to Json
 */
public class MetaParser {

    /** ObjectMapper with pretty-printed JSON output (snake_case). */
    public static final ObjectMapper prettyMapper;
    /** ObjectMapper for compact JSON output (lower case). */
    public static final ObjectMapper mapper;

    /** Private constructor for utility class. */
    private MetaParser() {
    }

    static {
        SimpleModule injector = new SimpleModule("injector", new Version(5, 0, 0, null,
                "fr.pilato.elasticsearch.injector", "injector"));

        prettyMapper = new ObjectMapper();
        prettyMapper.registerModule(new JavaTimeModule());
        prettyMapper.registerModule(injector);
        prettyMapper.configure(SerializationFeature.INDENT_OUTPUT, true);
        prettyMapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        prettyMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        prettyMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        prettyMapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);

        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.registerModule(injector);
        mapper.configure(SerializationFeature.INDENT_OUTPUT, false);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CASE);
        mapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
    }

}
