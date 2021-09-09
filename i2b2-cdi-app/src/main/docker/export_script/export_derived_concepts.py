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

import db_helper
import csv

derived_concepts_filename = 'derived_fact.cdi'
concepts_filename = 'concepts.csv'

conn1, cur1 = db_helper.open_i2b2demodata_connection()
concepts = db_helper.get_all_derived_concepts(cur1)

with open(derived_concepts_filename, "w", newline='' ) as csvfile:
    print("Exporting derived concepts...")
    writer = csv.writer(csvfile, quoting=csv.QUOTE_NONNUMERIC)
    writer.writerow(["ConceptPath", "Depends On", "Description", "SqlQuery", "UnitCd"])
        
    for concept in concepts:
        id = concept['id']
        concept_path = concept['concept_path']
        dependency = ''
        description = concept['description']
        sql_query = concept['sql_query']
        unit = concept['unit_cd']
        dependencies = db_helper.get_derived_concept_dependencies_by_id(cur1, id)
        for depdn in dependencies:
            parent_concept_path = depdn['parent_concept_path']
            if dependency == '':
                dependency = parent_concept_path
            else:
                dependency = dependency + ',' + parent_concept_path
                
        row = [concept_path, dependency, description, sql_query, unit ]
        writer.writerow(row)
        
db_helper.commit_conn(conn1)
db_helper.close_conn(cur1,conn1)

