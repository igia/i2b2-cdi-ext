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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import io.igia.i2b2.cdi.common.config.I2b2ModifierProperties;
import io.igia.i2b2.cdi.common.config.I2b2SchemaProperties;
import io.igia.i2b2.cdi.common.domain.I2b2Concept;
import io.igia.i2b2.cdi.common.helper.ConceptHelper;

@Component
public class I2b2ConceptRowMapper implements RowMapper<I2b2Concept> {

    @Autowired
    @Qualifier("i2b2SchemaFields")
    private I2b2SchemaProperties i2b2Properties;

    @Autowired
    @Qualifier("modifierProperties")
    private I2b2ModifierProperties i2b2ModifierProperties;

    @Override
    public I2b2Concept mapRow(ResultSet rs, int rowNum) throws SQLException {
	I2b2Concept concept;
	final String modifierAppliedPath = ConceptHelper.getAppliedPath(rs.getString("m_applied_path"), i2b2Properties);
	if (modifierAppliedPath.equalsIgnoreCase(ConceptHelper.DEFAULT_MODIFIER_APPLIED_PATH)) {
	    concept = buildConceptMapper(i2b2Properties, rs);
	} else {
	    concept = buildConceptMapper(i2b2ModifierProperties, rs);
	}

	return concept;
    }

    private I2b2Concept buildConceptMapper(I2b2SchemaProperties i2b2Properties, ResultSet rs) throws SQLException {
	I2b2Concept concept = new I2b2Concept();
	final String modifierAppliedPath = ConceptHelper.getAppliedPath(rs.getString("m_applied_path"), i2b2Properties);

	String path = rs.getString("concept_path");
	String conceptCode = rs.getString("concept_code");
	path = path.replace("/", i2b2Properties.getI2b2Separator());
	path = ConceptHelper.removeSeparatorAtFirstAndLastIfExists(path, i2b2Properties);
	String metaDataXml = rs.getString("metadata_xml");
	String columnDataType = rs.getString("column_datatype");
	String tableName = ConceptHelper.getTableName(rs.getString("table_name"), i2b2Properties);

	concept.setLevel(ConceptHelper.getLevel(path, i2b2Properties));
	concept.setFullPath(ConceptHelper.getFullPathName(path, i2b2Properties));
	concept.setName(ConceptHelper.getConceptName(path, i2b2Properties));
	concept.setConceptCode(conceptCode);
	concept.setSynonymCode(i2b2Properties.getSynonymCd());
	concept.setVisualAttributes(ConceptHelper.getVisualAttributes(path, i2b2Properties, modifierAppliedPath));
	concept.setMetaDataXML(!metaDataXml.isEmpty() ? metaDataXml : ConceptHelper.getMetadataXml(columnDataType));
	concept.setFactTableColumnName(
		ConceptHelper.getFactTableColumnName(rs.getString("facttable_column"), i2b2Properties));
	concept.settableName(tableName);
	concept.setColumnName(ConceptHelper.getColumnName(rs.getString("column_name"), i2b2Properties));
	concept.setColumnDataType(ConceptHelper.getColumnDataType(columnDataType, tableName, i2b2Properties));
	concept.setOperator(ConceptHelper.getOperator(rs.getString("operator"), i2b2Properties));
	concept.setDimensionCode(ConceptHelper.getDimCode(rs.getString("dimcode"), path, i2b2Properties));
	concept.setToolTip(ConceptHelper.getToolTip(path, i2b2Properties));
	concept.setAppliedPath(modifierAppliedPath); // @ does not work in yml file...TBD
	concept.setModifierExclusionCode(ConceptHelper.getModifierExclusionCode(rs.getString("m_exclusion_cd")));
	concept.setTimeStamp(new Timestamp(System.currentTimeMillis()));
	concept.setcTableCode(i2b2Properties.getTableAccessCd());
	concept.setcTableName(i2b2Properties.getTableAccessName());
	concept.setcProtectedAccess(i2b2Properties.getProtectedAccess());

	return concept;
    }
}