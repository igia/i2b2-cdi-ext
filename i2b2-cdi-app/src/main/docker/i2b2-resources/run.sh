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

sh docker-build.sh

# Need to improve by removing hardcoded sleep intervals
docker-compose -f app.yml -f postgres.yml -f sftp.yml up i2b2-cdi-postgresql -d
sleep 45
docker-compose -f app.yml -f postgres.yml -f sftp.yml up i2b2-cdi-sftp  -d
sleep 30
docker-compose -f app.yml -f postgres.yml -f sftp.yml up -d

