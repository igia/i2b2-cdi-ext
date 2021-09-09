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
    conn, cur = open_connection(database='i2b2demodata', user='sa', password='<YourStrong@Passw0rd>', host='i2b2-mssql', port='1433')
    return (conn, cur)

def open_i2b2metadata_connection():
    conn, cur = open_connection(database='i2b2metadata', user='sa', password='<YourStrong@Passw0rd>', host='i2b2-mssql', port='1433')
    return (conn, cur)
    
def commit_conn(conn):
    conn.commit()
    
def close_conn(cur, conn):
    cur.close()
    conn.close()

def delete_i2b2_demodata(cur):
    print('\nDeleting data from i2b2demodata:\n')
    print('truncate table derived_concept_dependency')
    query = 'truncate table derived_concept_dependency'
    cur.execute(query)

    print('truncate table derived_concept_job_details')
    query = 'truncate table derived_concept_job_details'
    cur.execute(query)
    
    print('delete from i2b2 concept dimension')    
    query = 'delete from concept_dimension'
    cur.execute(query)
    
    print('delete from i2b2 derived concept definition')
    query = 'delete from derived_concept_definition'
    cur.execute(query)

    print('truncate table PATIENT_DIMENSION;')
    query = 'truncate table PATIENT_DIMENSION'
    cur.execute(query)

    print('truncate table PATIENT_MAPPING;')
    query = 'truncate table PATIENT_MAPPING'
    cur.execute(query)

    print('truncate table OBSERVATION_FACT;')
    query = 'truncate table OBSERVATION_FACT'
    cur.execute(query)

    print('truncate table visit_dimension;')
    query = 'truncate table visit_dimension'
    cur.execute(query)

    print('truncate table encounter_mapping;')
    query = 'truncate table encounter_mapping'
    cur.execute(query)

    print('truncate table provider_dimension;')
    query = 'truncate table provider_dimension'
    cur.execute(query)

    print('truncate table MODIFIER_DIMENSION;')
    query = 'truncate table MODIFIER_DIMENSION'
    cur.execute(query)

def delete_i2b2_metadata(cur):
    print('\nDeleting data from i2b2metadata:\n')
    print('truncate table i2b2;')
    query = 'truncate table i2b2'
    cur.execute(query)

    print('truncate table table_access;')
    query = 'truncate table table_access'
    cur.execute(query)
    
def data_count(cur):
    print('\nRow counts:\n')
    query = 'select count(*) from patient_mapping'
    cur.execute(query)
    count = cur.fetchone()
    print('Count from patient_mapping : ' + str(count))
    
    query = 'select count(*) from patient_dimension'
    cur.execute(query)
    count = cur.fetchone()
    print('Count from patient_dimension : ' + str(count))
    
    query = 'select count(*) from encounter_mapping'
    cur.execute(query)
    count = cur.fetchone()
    print('Count from encounter_mapping : ' + str(count))
    
    query = 'select count(*) from visit_dimension'
    cur.execute(query)
    count = cur.fetchone()
    print('Count from visit_dimension : ' + str(count))
    
    query = 'select count(*) from provider_dimension'
    cur.execute(query)
    count = cur.fetchone()
    print('Count from provider_dimension : ' + str(count))
    
    query = 'select count(*) from observation_fact'
    cur.execute(query)
    count = cur.fetchone()
    print('Count from observation_fact : ' + str(count))
