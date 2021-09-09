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
FROM alpine:latest

ENV SFTP_USER igia_sftp
ENV SFTP_PASSWORD h1psftpPwd
ENV SFTP_HOME /data

# Install OpenSSH and
# ensure key creation
RUN apk add --no-cache openssh && \
    rm -rf /etc/ssh/ssh_host_rsa_key /etc/ssh/ssh_host_dsa_key /etc/ssh/ssh_host_ecdsa_key

# Copy entrypoint script
COPY entrypoint.sh /

# SSH Server configuration file
COPY sshd_config /etc/ssh/sshd_config

EXPOSE 22

# Run entrypoint
RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]