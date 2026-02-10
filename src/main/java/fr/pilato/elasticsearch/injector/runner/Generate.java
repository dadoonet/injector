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

import fr.pilato.elasticsearch.injector.bean.Person;
import fr.pilato.elasticsearch.injector.injector.ElasticsearchInjector;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import fr.pilato.elasticsearch.injector.helper.PersonGenerator;
import fr.pilato.elasticsearch.injector.injector.ConsoleInjector;
import fr.pilato.elasticsearch.injector.injector.Injector;
import fr.pilato.elasticsearch.injector.serializer.MetaParser;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Main entry point to generate person documents and inject them to console or Elasticsearch.
 * <p>
 * Command-line parsing is implemented with <a href="https://picocli.info/">Picocli</a>.
 * </p>
 */
public class Generate {
    private static final Logger logger = LogManager.getLogger(Generate.class);

    /**
     * Command-line options for the generate command (Picocli).
     */
    @Command(name = "injector",
            description = "Generate person documents and inject to console or Elasticsearch.",
            usageHelpAutoWidth = true)
    public static class GenerateCommand {

        /** Default constructor for Picocli. */
        public GenerateCommand() {
        }

        // Common options

        @Option(names = "--debug", description = "Debug mode")
        private boolean debug;

        @Option(names = "--trace", description = "Trace mode")
        private boolean trace;

        @Option(names = "--silent", description = "Silent mode")
        private boolean silent;

        @Option(names = {"-h", "--help"}, description = "display current help", usageHelp = true, hidden = true)
        private boolean help;

        @Option(names = {"--nb"}, description = "Number of documents to inject (default: ${DEFAULT-VALUE}).",
                defaultValue = "1000000")
        private Integer nb;

        @Option(names = {"--bulk"}, description = "Size of each bulk (default: ${DEFAULT-VALUE}).",
                defaultValue = "10000")
        private Integer bulkSize;

        // Elasticsearch specific options

        @Option(names = {"--elasticsearch"}, description = "Use it when you want to inject in Elasticsearch")
        private boolean elasticsearch;

        @Option(names = {"--es.host"}, description = "Elasticsearch host (default: ${DEFAULT-VALUE}).",
                defaultValue = "https://127.0.0.1:9200")
        private String esHost;

        @Option(names = {"--es.index"}, description = "Elasticsearch index name (default: ${DEFAULT-VALUE}).",
                defaultValue = "person")
        private String esIndex;

        @Option(names = {"--es.apikey"}, description = "Elasticsearch API Key.", arity = "0..1", interactive = true)
        private String esApikey;

        @Option(names = {"--es.user"}, description = "Elasticsearch user name (default: ${DEFAULT-VALUE}). Deprecated",
                defaultValue = "elastic")
        @Deprecated
        private String esUsername;

        @Option(names = {"--es.pass"}, description = "Elasticsearch user password (default: ${DEFAULT-VALUE}). Deprecated",
                defaultValue = "changeme", arity = "0..1", interactive = true)
        @Deprecated
        private String esPassword;

        // Console specific options

        @Option(names = {"--console"}, description = "Use it when you want to just generate dataset to the console")
        private boolean console;

        @Option(names = {"--cs.pretty"}, description = "Use pretty mode if set")
        private boolean csPretty;
    }

    /** Private constructor for main class. */
    private Generate() {
    }

    /**
     * Main entry point.
     *
     * @param args command-line arguments (see {@link GenerateCommand})
     */
    public static void main(String[] args) {
        GenerateCommand commands = new GenerateCommand();
        CommandLine cmd = new CommandLine(commands);
        try {
            cmd.parseArgs(args);
        } catch (CommandLine.ParameterException e) {
            try {
                cmd.getParameterExceptionHandler().handleParseException(e, args);
            } catch (Exception ex) {
                logger.error("Invalid arguments: {}", e.getMessage());
            }
            return;
        }

        if (cmd.isUsageHelpRequested()) {
            cmd.usage(System.out);
            return;
        }

        // Change debug level if needed
        if (commands.debug || commands.trace || commands.silent) {
            if (commands.silent) {
                // We change the full rootLogger level
                Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.OFF);
            } else {
                Configurator.setAllLevels("fr.pilato.elasticsearch.injector", commands.debug ? Level.DEBUG : Level.TRACE);
            }
        }

        if (!commands.elasticsearch && !commands.console) {
            // Let's suppose we want to use elasticsearch by default
            commands.elasticsearch = true;
        }

        List<Injector> injectors = new ArrayList<>();
        if (commands.elasticsearch) {
            injectors.add(new ElasticsearchInjector(commands.esIndex, commands.bulkSize, commands.esHost,
                    commands.esUsername, commands.esPassword, commands.esApikey));
        }
        if (commands.console) {
            injectors.add(new ConsoleInjector(commands.csPretty));
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
                cmd.usage(System.out);
            }

        } catch (Throwable t) {
            logger.error("We got an error while injecting...", t);
        } finally {
            injectors.forEach(Injector::close);
        }
    }
}
