/*
 * Licensed to Elasticsearch (the "Author") under one
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

package fr.pilato.elasticsearch.injector.runner;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import fr.pilato.elasticsearch.injector.bean.Person;
import fr.pilato.elasticsearch.injector.injector.ElasticsearchInjector;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import fr.pilato.elasticsearch.injector.helper.PersonGenerator;
import fr.pilato.elasticsearch.injector.injector.AppSearchInjector;
import fr.pilato.elasticsearch.injector.injector.ConsoleInjector;
import fr.pilato.elasticsearch.injector.injector.Injector;
import fr.pilato.elasticsearch.injector.serializer.MetaParser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Generate {
    private static final Logger logger = LogManager.getLogger(Generate.class);

    public static class GenerateCommand {
        // Common options

        @Parameter(names = "--debug", description = "Debug mode")
        private boolean debug = false;

        @Parameter(names = "--trace", description = "Trace mode", hidden = true)
        private boolean trace = false;

        @Parameter(names = "--silent", description = "Silent mode", hidden = true)
        private boolean silent = false;

        @Parameter(names = "--help", description = "display current help", help = true, hidden = true)
        boolean help;

        @Parameter(names = "--nb", description = "Number of documents to inject.")
        private Integer nb = 1000000;

        @Parameter(names = "--bulk", description = "Size of each bulk.")
        private Integer bulkSize = 10000;

        // Elasticsearch specific options

        @Parameter(names = "--elasticsearch", description = "Use it when you want to inject in a local cluster")
        private boolean elasticsearch = false;

        @Parameter(names = "--es.host", description = "Elasticsearch host.")
        private String esHost = "http://127.0.0.1:9200";

        @Parameter(names = "--es.index", description = "Elasticsearch index name.")
        private String esIndex = "person";

        @Parameter(names = "--es.user", description = "Elasticsearch user name.")
        private String esUsername = "elastic";

        @Parameter(names = "--es.pass", description = "Elasticsearch user password.", password = true)
        private String esPassword = "changeme";

        // App Search specific options

        @Parameter(names = "--appsearch", description = "Use it when you want to inject in a app search service")
        private boolean appsearch = false;

        @Parameter(names = "--ap.engine", description = "App Search Engine name.")
        private String apEngine = "person";

        @Parameter(names = "--ap.host", description = "App Search host identifier.")
        private String apHost = null;

        @Parameter(names = "--app.key", description = "App Search API key.", password = true, echoInput = true)
        private String apKey = null;

        // Console specific options

        @Parameter(names = "--console", description = "Use it when you want to just generate dataset to the console")
        private boolean console = false;

        @Parameter(names = "--cs.pretty", description = "Use pretty mode if set")
        private boolean csPretty = false;

        @Parameter(names = "--cs.appsearch", description = "Generate data using the app search model instead of the normal one")
        private boolean csAppSearch = false;
    }

    public static void main(String[] args) {
        GenerateCommand commands = new GenerateCommand();
        JCommander jCommander = new JCommander(commands);
        jCommander.parse(args);

        // Change debug level if needed
        if (commands.debug || commands.trace || commands.silent) {

            if (commands.silent) {
                // We change the full rootLogger level
                Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.OFF);
            } else {
                Configurator.setAllLevels("fr.pilato.elasticsearch.injector", commands.debug ? Level.DEBUG : Level.TRACE);
            }
        }

        if (commands.help) {
            jCommander.usage();
            return;
        }

        if (!commands.elasticsearch && !commands.appsearch && !commands.console) {
            // Let's suppose we want to use elasticsearch by default
            commands.elasticsearch = true;
        }

        List<Injector> injectors = new ArrayList<>();
        if (commands.elasticsearch) {
            injectors.add(new ElasticsearchInjector(commands.esIndex, commands.bulkSize, commands.esHost,
                    commands.esUsername, commands.esPassword));
        }
        if (commands.appsearch) {
            injectors.add(new AppSearchInjector(commands.apEngine, commands.apHost, commands.apKey, commands.bulkSize));
        }
        if (commands.console) {
            injectors.add(new ConsoleInjector(commands.csPretty, commands.csAppSearch));
        }
        try {
            injectors.forEach(Injector::start);
            AtomicInteger numStarted = new AtomicInteger();
            injectors.forEach(injector -> { if (injector.isStarted()) numStarted.incrementAndGet();});

            if (numStarted.get() > 0) {
                logger.info("Generating [{}] persons", commands.nb);
                Person person = PersonGenerator.personGenerator();
                logger.info("Sample document: {}", MetaParser.prettyMapper.writeValueAsString(person));

                for (int i = 0; i < commands.nb; i++) {
                    int finalI = i;
                    injectors.forEach(injector -> {
                        if (injector.isStarted()) {
                            injector.inject(finalI, PersonGenerator.personGenerator());
                        }
                    });
                }
                logger.info("Done injecting [{}] persons", commands.nb);
            } else {
                logger.warn("No injector has been started correctly. Read help.");
                jCommander.usage();
            }

        } catch (Throwable t) {
            logger.error("We got an error while injecting...", t);
        } finally {
            injectors.forEach(Injector::close);
        }
    }
}
