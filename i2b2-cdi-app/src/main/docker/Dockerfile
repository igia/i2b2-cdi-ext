#
# This Source Code Form is subject to the terms of the Mozilla Public License, v.
# 2.0 with a Healthcare Disclaimer.
# A copy of the Mozilla Public License, v. 2.0 with the Healthcare Disclaimer can
# be found under the top level directory, named LICENSE.
# If a copy of the MPL was not distributed with this file, You can obtain one at
# http://mozilla.org/MPL/2.0/.
# If a copy of the Healthcare Disclaimer was not distributed with this file, You
# can obtain one at the project website https://github.com/igia.
#
# Copyright (C) 2021-2022 Persistent Systems, Inc.
#

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
