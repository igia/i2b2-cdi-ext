/**
 * This Source Code Form is subject to the terms of the Mozilla Public License, v.
 * 2.0 with a Healthcare Disclaimer.
 * A copy of the Mozilla Public License, v. 2.0 with the Healthcare Disclaimer can
 * be found under the top level directory, named LICENSE.
 * If a copy of the MPL was not distributed with this file, You can obtain one at
 * http://mozilla.org/MPL/2.0/.
 * If a copy of the Healthcare Disclaimer was not distributed with this file, You
 * can obtain one at the project website https://github.com/igia.
 *
 * Copyright (C) 2021-2022 Persistent Systems, Inc.
 */
package io.igia.i2b2.cdi.common.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import io.igia.i2b2.cdi.common.domain.DerivedConceptDefinition;

@Component
public class DerivedConceptRowMapper implements RowMapper<DerivedConceptDefinition> {

    @Override
    public DerivedConceptDefinition mapRow(ResultSet rs, int rowNum) throws SQLException {
        DerivedConceptDefinition derivedConceptDefinition = new DerivedConceptDefinition();
        derivedConceptDefinition.setId(rs.getInt("id"));
        derivedConceptDefinition.setConceptPath(rs.getString("concept_path"));
        derivedConceptDefinition.setSqlQuery(rs.getString("sql_query"));
        derivedConceptDefinition.setUnitCd(rs.getString("unit_cd"));
        return derivedConceptDefinition;
    }
}