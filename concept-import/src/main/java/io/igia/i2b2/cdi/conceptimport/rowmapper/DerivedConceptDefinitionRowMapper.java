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
package io.igia.i2b2.cdi.conceptimport.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import io.igia.i2b2.cdi.common.config.I2b2SchemaProperties;
import io.igia.i2b2.cdi.common.domain.DerivedConceptDefinition;
import io.igia.i2b2.cdi.common.helper.ConceptHelper;

@Component
public class DerivedConceptDefinitionRowMapper implements RowMapper<DerivedConceptDefinition> {
    
    private I2b2SchemaProperties i2b2Properties;

    public DerivedConceptDefinitionRowMapper(@Qualifier("i2b2SchemaFields") I2b2SchemaProperties i2b2Properties) {
        this.i2b2Properties = i2b2Properties;
    }

    @Override
    public DerivedConceptDefinition mapRow(ResultSet rs, int rowNum) throws SQLException {
        DerivedConceptDefinition concept = new DerivedConceptDefinition();
        String conceptPath = rs.getString("concept_path");
        conceptPath = conceptPath.replace("/", i2b2Properties.getI2b2Separator());
        conceptPath = ConceptHelper.removeSeparatorAtFirstAndLastIfExists(conceptPath, i2b2Properties);
        
        concept.setDependsOn(rs.getString("parent_concept_path"));
        concept.setConceptPath(ConceptHelper.getFullPathName(conceptPath, i2b2Properties));
        concept.setDescription(rs.getString("description"));
        concept.setSqlQuery(rs.getString("sql_query"));
        concept.setUnitCd(rs.getString("unit_cd"));
        concept.setUpdateDate(new Timestamp(System.currentTimeMillis()));
        return concept;
    }
}