#!/bin/sh
. keys.sh

java -jar ${project.build.directory}/${project.build.finalName}.jar --debug

#java -jar ${project.build.directory}/${project.build.finalName}.jar --nb 1000000 --bulk 10000 --debug \
#    --elasticsearch --es.host $ES_HOST --es.user elastic --es.pass $ES_PASS --es.index person \
#    --appsearch --ap.host $AP_HOST --app.key $AP_KEY --ap.engine person
