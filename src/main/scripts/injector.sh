#!/bin/bash
source .env

time java -jar ${project.build.directory}/${project.build.finalName}.jar --debug

#time java -jar ${project.build.directory}/${project.build.finalName}.jar --nb 1000000 --bulk 10000 --debug \
#    --elasticsearch --es.host $ES_HOST --es.apikey $ES_APIKEY --es.index person
#    --console
