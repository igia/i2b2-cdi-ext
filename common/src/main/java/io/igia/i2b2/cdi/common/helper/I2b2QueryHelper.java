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
package io.igia.i2b2.cdi.common.helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.igia.i2b2.cdi.common.config.DataSourceMetaInfoConfig;
import io.igia.i2b2.cdi.common.util.TableFields;

@Component
public class I2b2QueryHelper {

	@Autowired
	DataSourceMetaInfoConfig dataSourceMetaInfoConfig;
	
	private I2b2QueryHelper() {
	}

	public String getObservationFactQuery() {
		return "INSERT INTO " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "observation_fact "
				+ "(encounter_num, patient_num, concept_cd, provider_id, start_date, modifier_cd, instance_num, "
				+ "valtype_cd, tval_char, nval_num, units_cd, update_date, sourcesystem_cd)"
				+ " select ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? "
				+ " where not exists (select encounter_num from " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "observation_fact where encounter_num = ? and patient_num = ? and"
				+ " concept_cd = ? and provider_id = ? and start_date = ? and modifier_cd = ?)";
	}

	public String getPatientMappingQuery() {
		return "INSERT INTO " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "patient_mapping " + "(" + TableFields.getI2b2PatientMappingFields() + ")"
				+ " select ?, ?, ?, ?, ?, ?"
				+ " where not exists (select patient_ide from " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "patient_mapping where patient_ide = ?)";
	}

	public String getPatientDimensionQuery() {
		return "INSERT INTO " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "patient_dimension(patient_num, patient_blob, update_date, sourcesystem_cd)"
				+ "select ?, ?, ?, ? where not exists (select patient_num from "
				+ dataSourceMetaInfoConfig.getDemodataSchemaName() + "patient_dimension where patient_num = ?)";
	}

	public String getConceptMetadataQuery() {
		return "INSERT INTO " + dataSourceMetaInfoConfig.getMetadataSchemaName() + "i2b2 " + "(c_hlevel,c_fullname,c_name,c_synonym_cd,c_visualattributes"
				+ ",c_metadataxml,c_facttablecolumn,c_tablename,c_columnname,c_columndatatype,c_operator,c_dimcode,"
				+ "c_tooltip,m_applied_path,update_date,sourcesystem_cd) select "
				+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? where not exists (select c_fullname from "
				+ dataSourceMetaInfoConfig.getMetadataSchemaName() + "i2b2 where c_fullname = ?)";
	}

	public String getConceptDimensionQuery() {
		return "INSERT INTO " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "concept_dimension "
				+ "(concept_path, concept_cd, name_char, update_date, sourcesystem_cd) select "
				+ "?, ?, ?, ?, ? where not exists (select concept_path from "
				+ "" + dataSourceMetaInfoConfig.getDemodataSchemaName() + "concept_dimension where concept_path = ?)";
	}

	public String getConceptTableAccessQuery() {
		return "INSERT INTO " + dataSourceMetaInfoConfig.getMetadataSchemaName() + "table_access "
				+ "(c_table_cd, c_table_name, c_protected_access, c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes,"
				+ "c_metadataxml,c_facttablecolumn, c_dimtablename, c_columnname, c_columndatatype, c_operator, c_dimcode,"
				+ "c_tooltip) select ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? where not exists (select c_fullname from "
				+ dataSourceMetaInfoConfig.getMetadataSchemaName() + "table_access where c_fullname = ?)";
	}
	
	public String getConceptCodeFromPath() {
		return "select concept_cd from " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "concept_dimension "
				+ "where concept_path = ?";
	}
}
