# Injector for demos

<!-- Documentation generated from `src/main/documentation/README.markdown`. Do not edit directly. -->

This injector is used to demo
[elasticsearch](https://www.elastic.co/products/elasticsearch) and [Kibana](https://www.elastic.co/products/kibana).

| Injector     | elasticsearch | Release date |
|:-------------|:--------------|:------------:|
| ${project.version} | ${elasticsearch.version}          |              |
| 8.0          | 8.0.0         |  2022-02-16  |
| 7.15         | 7.15.1        |  2021-10-28  |
| 7.0          | 7.0.0         |  2019-04-11  |
| 6.5          | 6.5.1         |  2018-12-03  |
| 6.4.2        | 6.4.2         |  2018-10-03  |
| 6.4          | 6.4.0         |  2018-09-06  |
| 6.2          | 6.2.3         |  2018-04-09  |
| 6.0          | 6.0.0-alpha1  |  2017-05-10  |
| 5.3          | 5.3.2         |  2017-05-05  |
| 5.0          | 5.0.0         |  2016-10-03  |
| 5.0          | 5.0.0         |  2016-03-31  |
| 5.0          | 5.0.0-alpha1  |  2016-03-30  |
| 3.2          | 2.2.0         |  2016-02-05  |
| 3.1          | 2.1.0         |  2015-11-27  |
| 3.0          | 2.0.0         |  2015-11-12  |
| 2.10         | 1.7.1         |  2015-09-07  |
| 2.9          | 1.6.0         |  2015-06-15  |
| 2.8          | 1.5.2         |  2015-06-01  |
| 2.7          | 1.4.4         |  2015-02-24  |
| 2.6          | 1.4.0         |  2014-11-19  |
| 2.5          | 1.3.2         |  2014-09-02  |
| 2.4          | 1.2.0         |  2014-05-26  |
| 2.3          | 1.1.1         |  2014-05-06  |
| 2.2          | 1.1.0         |  2014-04-01  |
| 2.1          | 1.0.0         |  2014-03-21  |
| 2.1.RC2      | 1.0.0.RC2     |  2014-02-04  |
| 2.1.RC1      | 1.0.0.RC1     |  2014-01-23  |
| 1.1          | 0.90.6        |  2013-11-05  |
| 1.0          | 0.90.5        |  2013-10-01  |

`*` Broken version 

Status
======

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/fr.pilato.elasticsearch.injector/injector/badge.svg?style=flat-square)](https://maven-badges.herokuapp.com/maven-central/fr.pilato.elasticsearch.injector/injector/)
[![Build and Deploy the main branch](https://github.com/dadoonet/injector/actions/workflows/main.yml/badge.svg)](https://github.com/dadoonet/injector/actions/workflows/main.yml)

Usage
=====

If you are looking for a SNAPSHOT version, please look into 
https://s01.oss.sonatype.org/content/repositories/snapshots/fr/pilato/elasticsearch/injector/injector/${project.version}/

```sh
# Download it from maven central
wget https://repo1.maven.org/maven2/fr/pilato/elasticsearch/injector/${project.version}/${project.build.finalName}.jar

# Launch it (with all default settings)
java -jar ${project.build.finalName}.jar
```

With no option, it will inject `1000000` documents with a bulk size of `10000` in a local cluster running at
https://127.0.0.1:9200 with default password `changeme` for user named `elastic`.

Injector comes with the following implementations:

* Elasticsearch (`--elasticsearch` flag), where you want to index generated data to an elasticsearch cluster running locally or on [cloud](https://cloud.elastic.co/).
* App Search (`--appsearch` flag), where you want to index generated data to the [App Search service](https://app.swiftype.com/as).
* Console (`--console` flag), where you want to just print on the console generated data.

For both, you can configure `--nb` option to define the number of documents you'd like to generate
(defaults to `1000000`).
Also `--bulk` option can be set to define how many documents should be sent at once (defaults to `10000`).

For example:

```sh
java -jar ${project.build.finalName}.jar --nb 1000 --bulk 100
```

If no implementation is set, the injector will assume by default that you want to index your data in Elasticsearch
and will set `--elasticsearch` flag for you.

Elasticsearch service
---------------------

When running an Elasticsearch instance, local or on [cloud.elastic.co](https://cloud.elastic.co/), you can define other options.

To define the host to send the data to, set `--es.host` option (defaults to `https://127.0.0.1:9200`):

```sh
java -jar ${project.build.finalName}.jar --elasticsearch --es.host https://cloud_id.europe-west1.gcp.cloud.es.io:9243
```

If your cluster is secured, which is what will happen most likely on cloud.elastic.co, use `--es.user`
(defaults to `elastic`) and `--es.pass` to define your credentials.

```sh
java -jar ${project.build.finalName}.jar --elasticsearch --es.user elastic --es.pass changeme
```

If you don't provide the `--es.pass` you'll be prompted to enter it.

If you'd like to index your data in another index than `person` (default one), use `--es.index`:

```sh
java -jar ${project.build.finalName}.jar --elasticsearch --es.index person
```

If you want to use Kibana to display the information that have been generated, you can import the following
files into Kibana using `Stack Management -> Kibana -> Saved Objects` menu and click on "Import":

* Index pattern, Visualizations, Maps, Dashboard: https://github.com/dadoonet/injector/blob/main/src/main/resources/kibana-dashboard.ndjson
* Canvas example: https://github.com/dadoonet/injector/blob/main/src/main/resources/kibana-canvas.ndjson

Example of the Persons dataset dashboard:

![Persons dataset dashboard](images/dashboard.png "Persons dataset dashboard")

Example of the Canvas presentation:

![Canvas presentation](images/canvas.png "Canvas presentation")


App Search service
------------------

When sending documents to the [App Search service](https://www.elastic.co/guide/en/app-search/), you probably need to
specify `--ap.host`, `--es.user` and `--es.pass`:

```sh
java -jar ${project.build.finalName}.jar --appsearch --ap.host http://localhost:3002/api/as/v1/ --es.user elastic --es.pass changeme
```

If you don't provide the `--es.pass` you'll be prompted to enter it.

Optionally you can set [the engine](https://www.elastic.co/guide/en/app-search/current/engines.html) you wish to use by 
using `--ap.engine` option. It defaults to `person` and if not existing when the injector starts, it will be created 
automatically.

```sh
java -jar ${project.build.finalName}.jar --appsearch --ap.host http://localhost:3002/api/as/v1/ --es.user elastic --es.pass changeme --ap.engine person
```

The `--nb` and `--bulk` options are also used by this injector. Note that if you set them above the limits
of the App Search service, the injector will automatically adapt itself to use respectively `100000` and `100` (the limits).

Console
-------

When printing documents to the console, you can choose to prettify the documents first by using `cs.pretty` option:

```sh
java -jar ${project.build.finalName}.jar --console --cs.pretty
```

By default, JSON documents are generated using their default model (the one used by Elasticsearch implementation).
If you want to generate documents according to the App Search model, you can pass the `--cs.appsearch` option:

```sh
java -jar ${project.build.finalName}.jar --console --cs.appsearch
```

Using all services together
---------------------------

You can start the injector like this (all options together):

```sh
java -jar ${project.build.finalName}.jar \
    --nb 1000 --bulk 100 \
    --debug \
    --elasticsearch --es.host https://cloud_id.europe-west1.gcp.cloud.es.io:9243 --es.user elastic --es.pass changeme --es.index person \
    --appsearch --ap.host http://localhost:3002/api/as/v1/ --es.user elastic --es.pass changeme --ap.engine person \
    --console --cs.pretty --cs.appsearch
```

When you build the project with maven, you'll see in `target/scripts` dir an example
of scripts which you can adapt to your needs.

Debug options
-------------

You can use `--silent`, `--debug` or `--trace` to change the log level when using the injector.
That can give you more information when something is failing for example.


Documentation
=============

If you wish to edit the documentation, please edit it in `src/main/documentation`.

Then run the following commands to update the README in the root of this project and commit your changes:

```shell
mvn clean process-resources
git commit -a -m "Update documentation"
```

Developer Guide
===============

If you want to build it yourself or update to a new elasticsearch version, modify `pom.xml` file:

```xml
<elasticsearch.version>${elasticsearch.version}</elasticsearch.version>
```

Then compile the project:

```sh
# Compile
mvn clean install
```

Just get the final jar from `target/${project.build.finalName}.jar`. Or:

```sh
cd target
```

And launch all the examples from this dir.

Release guide
=============

To release the project you need to run the release plugin with the `release` profile as you need to sign the artifacts:

```sh
mvn release:prepare
git push --tags
git push
mvn release:perform -Prelease
```

If you need to skip the tests, run:

```sh
mvn release:perform -Prelease -Darguments="-DskipTests"
```

If everything is ok in https://s01.oss.sonatype.org/#stagingRepositories, you can perform the release with:

```sh
mvn nexus-staging:release
mvn nexus-staging:drop
```

License
=======

```
This software is licensed under the Apache 2 license, quoted below.

Copyright 2009-2022 Elastic <https://www.elastic.co>

Licensed under the Apache License, Version 2.0 (the "License"); you may not
use this file except in compliance with the License. You may obtain a copy of
the License at

    https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
License for the specific language governing permissions and limitations under
the License.
```
