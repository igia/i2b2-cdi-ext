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

import java.util.List;

import javax.sql.DataSource;

import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import io.igia.i2b2.cdi.common.config.DataSourceMetaInfoConfig;
import io.igia.i2b2.cdi.common.util.TableFields;
import io.igia.i2b2.cdi.common.writer.CustomJdbcBatchItemWriter;

@Component
public class EncounterHelper {

	@Autowired
	DataSourceMetaInfoConfig dataSourceMetaInfoConfig;
	
	private EncounterHelper() {
	}

	public <T> JdbcBatchItemWriter<T> getI2b2EncounterMappingWriter(DataSource i2b2DemoDataSource) {
		String sql = "INSERT INTO " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "encounter_mapping " + "(" + TableFields.getI2b2EncounterMappingFields()
				+ ") select :encounterID, :sourceSystemCD, :encounterNum, :projectID, :patientID, "
				+ ":sourceSystemCD, :updateDate, :sourceSystemCD where not exists (select encounter_ide from "
				+ dataSourceMetaInfoConfig.getDemodataSchemaName() + "encounter_mapping where encounter_ide = :encounterID)";

		return CustomJdbcBatchItemWriter.getWriter(sql, i2b2DemoDataSource);
	}

	public <T> JdbcBatchItemWriter<T> getI2b2VisitDimensionWriter(DataSource i2b2DemoDataSource) {

		String sql = "INSERT INTO " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "visit_dimension(encounter_num, patient_num, start_date, end_date, update_date, sourcesystem_cd)"
				+ " values(:encounterNum, :patientNum, :startDate, :endDate, :updateDate, :sourceSystemCD) ";

		return CustomJdbcBatchItemWriter.getWriter(sql, i2b2DemoDataSource);
	}

	public <T> JdbcBatchItemWriter<T> getI2b2VisitDimensionBasicWriter(DataSource i2b2DemoDataSource) {

		String sql = "INSERT INTO " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "visit_dimension(encounter_num, patient_num, update_date, sourcesystem_cd)"
				+ " select :encounterNum, :patientNum, :updateDate, :sourceSystemCD where not exists "
				+ "(select encounter_num from " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "visit_dimension where encounter_num = :encounterNum) ";

		return CustomJdbcBatchItemWriter.getWriter(sql, i2b2DemoDataSource);
	}

	public Integer getEncounterIdNextValue(JdbcTemplate template) {
		String sql = "select coalesce(MAX(encounter_num),0) AS max_id from " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "encounter_mapping";
		return template.queryForObject(sql, Integer.class);
	}
	
	/**
	 * Check if patient encounter exists.
	 * @param template jdbc template to query into database
	 * @return
	 */
	public List<Integer> getEncounterNums(JdbcTemplate template, String patientId) {
		String sql = "select ENCOUNTER_NUM from " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "encounter_mapping where ENCOUNTER_IDE = ?";
		return template.queryForList(sql,new Object [] {patientId},Integer.class);
	}
}