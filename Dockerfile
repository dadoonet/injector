# JRE instead of JDK to reduce image size; version aligned with java.compiler.version in pom.xml
FROM eclipse-temurin:${java.compiler.version}-jre

RUN mkdir -p /opt
ADD ${project.build.directory}/docker-extra/run-java/run-java.sh /opt
ADD ${project.build.directory}/${project.build.finalName}.jar /opt

ENTRYPOINT [ "/opt/run-java.sh" ]
