FROM openjdk:8-jre-alpine

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JAVA_OPTS=""

# Allow root group the access on directory (needed for OpenShift, if you are writing to a file) 
RUN mkdir -p /opt/igia && \
    chgrp -R 0 /opt/igia && \
    chmod -R g=u /opt/igia

WORKDIR /opt/igia

COPY *.war /opt/igia/app.war

CMD java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar app.war
