FROM eclipse-temurin:17-jdk

RUN mkdir -p /opt
ADD ${project.build.directory}/docker-extra/run-java/run-java.sh /opt
ADD ${project.build.directory}/${project.build.finalName}.jar /opt

ENTRYPOINT [ "/opt/run-java.sh" ]
