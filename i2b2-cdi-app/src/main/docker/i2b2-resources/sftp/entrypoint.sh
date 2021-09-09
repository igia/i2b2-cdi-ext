#!/bin/sh
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

set -e

# Checks for SFTP_USER variable
if [ -z "$SFTP_USER" ]; then
  echo >&2 'Please set a SFTP_USER variable (ie.: -e SFTP_USER=john).'
  exit 1
fi

# Checks for SFTP_PASSWORD variable
if [ -z "$SFTP_PASSWORD" ]; then
  echo >&2 'Please set a SFTP_PASSWORD variable (ie.: -e SFTP_PASSWORD=someRandomPwd).'
  exit 1
fi

# Checks for SFTP_HOME variable
if [ -z "$SFTP_HOME" ]; then
  echo >&2 'Please set a SFTP_HOME variable (ie.: -e SFTP_HOME=/data).'
  exit 1
fi


if /usr/bin/id -u ${SFTP_USER} 2> /dev/null; then
  echo "User ${SFTP_USER} already exists"
else
  echo "Creating user ${SFTP_USER} with home ${SFTP_HOME}/${SFTP_USER}"
  adduser -D -h ${SFTP_HOME}/${SFTP_USER} ${SFTP_USER}
  echo "${SFTP_USER}:${SFTP_PASSWORD}" | chpasswd
fi

if [ ! -d ${SFTP_HOME}/${SFTP_USER} ]; then
  echo "Creating ${SFTP_HOME}/${SFTP_USER}"
  mkdir -p ${SFTP_HOME}/${SFTP_USER}
fi

# The folder itself must be owned by root, the contents
# by the user
echo "Fixing permissions for user ${SFTP_USER} in ${SFTP_HOME}/${SFTP_USER}"
chown -Rv ${SFTP_USER}:${SFTP_USER} ${SFTP_HOME}/${SFTP_USER}
chmod -Rv 644 ${SFTP_HOME}/${SFTP_USER}
chown root.root ${SFTP_HOME}/${SFTP_USER}
chmod 777 ${SFTP_HOME}/${SFTP_USER}

echo "Fixing permission to root in ${SFTP_HOME}/${SFTP_USER}"
chown root.root ${SFTP_HOME}/${SFTP_USER}
chmod 755   ${SFTP_HOME}

# Generate unique ssh keys for this container, if needed
ssh-keygen -A

# do not detach (-D), log to stderr (-e), passthrough other arguments
# exec /usr/sbin/sshd -D -e $@
exec /usr/sbin/sshd -D -e