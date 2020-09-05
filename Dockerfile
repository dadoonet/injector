FROM maven:latest
ENV source /source
WORKDIR ${source}   # WORKDIR /source
ADD . $source       # ADD . /source
CMD ["mvn", "clean", "install"]
