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

import pymssql

def open_connection(host, port, database, user, password):
    conn = pymssql.connect(database=database, user=user, password=password, host=host, port=port)
    cur = conn.cursor()
    return (conn, cur)
    
    #provide your i2b2 db host and port
def open_i2b2demodata_connection():
    conn, cur = open_connection(database='i2b2demodata', user='sa', password='<YourStrong@Passw0rd>', host='localhost', port='1432')
    cur = conn.cursor(as_dict=True)
    return (conn, cur)

def open_i2b2metadata_connection():
    conn, cur = open_connection(database='i2b2metadata', user='sa', password='<YourStrong@Passw0rd>', host='localhost', port='1432')
    return (conn, cur)
    
def commit_conn(conn):
    conn.commit()
    
def close_conn(cur, conn):
    cur.close()
    conn.close()
    
def get_all_derived_concepts(cur):
    query = 'SELECT * from derived_concept_definition'
    cur.execute(query)
    result = cur.fetchall()
    return result

    
def get_derived_concept_dependencies_by_id(cur, id):
    query = 'SELECT * from derived_concept_dependency where derived_concept_id = %d'
    cur.execute(query, (id))
    result = cur.fetchall()
    return result

