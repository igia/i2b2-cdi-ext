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

import psycopg2

def open_connection(host, port, database, user, password):
    conn = psycopg2.connect(database=database, user=user, password=password, host=host, port=port)
    cur = conn.cursor()
    return (conn, cur)
    
    #provide your i2b2 db host and port
def open_i2b2_connection():
    conn, cur = open_connection(database='i2b2', user='i2b2', password='demouser', host='i2b2-pg', port='5432')
    return (conn, cur)
    
def commit_conn(conn):
    conn.commit()
    
def close_conn(cur, conn):
    cur.close()
    conn.close()
    
def delete_concepts(cur):
    print('\nDeleting concepts:\n')
    print('delete from i2b2 metadata')
    query = 'delete from i2b2metadata.i2b2'
    cur.execute(query)
    
    print('delete from i2b2 concept dimension')    
    query = 'delete from i2b2demodata.concept_dimension'
    cur.execute(query)
    
    print('delete from i2b2 table access')
    query = 'delete from i2b2metadata.table_access'
    cur.execute(query)
        
def delete_i2b2_data(cur):
    print('\nDeleting i2b2 data:\n')
    print("Deleting data from observation_fact...")
    query = 'delete from i2b2demodata.observation_fact'
    cur.execute(query)

    print("Deleting data from visit_dimension...")
    query = 'delete from i2b2demodata.visit_dimension'
    cur.execute(query)
    
    print("Deleting data from provider_dimension...")
    query = 'delete from i2b2demodata.encounter_mapping'
    cur.execute(query)
    
    print("Deleting data from patient_dimension...")
    query = 'delete from i2b2demodata.patient_dimension'
    cur.execute(query)
    
    print("Deleting data from patient_mapping...")
    query = 'delete from i2b2demodata.patient_mapping'
    cur.execute(query)
    
    print("Deleting data from provider_dimension...")
    query = 'delete from i2b2demodata.provider_dimension'
    cur.execute(query)
    
def data_count(cur):
    print('\nRow counts:\n')
    query = 'select count(*) from i2b2demodata.patient_mapping'
    cur.execute(query)
    count = cur.fetchone()
    print('Count from patient_mapping : ' + str(count))
    
    query = 'select count(*) from i2b2demodata.patient_dimension'
    cur.execute(query)
    count = cur.fetchone()
    print('Count from patient_dimension : ' + str(count))
    
    query = 'select count(*) from i2b2demodata.encounter_mapping'
    cur.execute(query)
    count = cur.fetchone()
    print('Count from encounter_mapping : ' + str(count))
    
    query = 'select count(*) from i2b2demodata.visit_dimension'
    cur.execute(query)
    count = cur.fetchone()
    print('Count from visit_dimension : ' + str(count))
    
    query = 'select count(*) from i2b2demodata.provider_dimension'
    cur.execute(query)
    count = cur.fetchone()
    print('Count from provider_dimension : ' + str(count))
    
    query = 'select count(*) from i2b2demodata.observation_fact'
    cur.execute(query)
    count = cur.fetchone()
    print('Count from observation_fact : ' + str(count))
