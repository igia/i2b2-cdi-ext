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

import db_helper_pgsql
import csv

conn1, cur1 = db_helper_pgsql.open_i2b2_connection()

db_helper_pgsql.delete_concepts(cur1)
db_helper_pgsql.delete_i2b2_data(cur1)

#Row count of all tables
db_helper_pgsql.data_count(cur1) 
    
db_helper_pgsql.commit_conn(conn1)

db_helper_pgsql.close_conn(cur1,conn1)
