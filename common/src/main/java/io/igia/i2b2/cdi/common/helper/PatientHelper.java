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
public class PatientHelper {
	
	@Autowired
	DataSourceMetaInfoConfig dataSourceMetaInfoConfig;
	
	private PatientHelper() {
		
	}	

	public <T> JdbcBatchItemWriter<T> getI2b2PatientMappingWriterWithExistsClause(DataSource i2b2DemoDataSource) {
		String sql = "INSERT INTO " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "patient_mapping " + "(" + TableFields.getI2b2PatientMappingFields() + ")"
				+ " select :patientID, :sourceSystemCD, :patientNum, :projectID, :updateDate, :sourceSystemCD"
				+ " where not exists (select patient_ide from " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "patient_mapping where patient_ide = :patientID)";

		return CustomJdbcBatchItemWriter.getWriter(sql, i2b2DemoDataSource);
	}
	
	public <T> JdbcBatchItemWriter<T> getI2b2PatientMappingWriter(DataSource i2b2DemoDataSource) {
        String sql = "INSERT INTO " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "patient_mapping " + "(" + TableFields.getI2b2PatientMappingFields() + ")"
                + " values (:patientID, :sourceSystemCD, :patientNum, :projectID, :updateDate, :sourceSystemCD)";
        return CustomJdbcBatchItemWriter.getWriter(sql, i2b2DemoDataSource);
    }

	public <T> JdbcBatchItemWriter<T> getI2b2PatientDimensionWriter(DataSource i2b2DemoDataSource) {

		String sql = "INSERT INTO " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "patient_dimension(patient_num, birth_date, sex_cd, patient_blob, update_date, sourcesystem_cd)"
				+ "values (:patientNum, :birthDate, :gender, :patientID, :updateDate, :sourceSystemCD)";

		return CustomJdbcBatchItemWriter.getWriter(sql, i2b2DemoDataSource);
	}
	
	public <T> JdbcBatchItemWriter<T> getI2b2PatientDimensionBasicWriter(DataSource i2b2DemoDataSource) {

		String sql = "INSERT INTO " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "patient_dimension(patient_num, patient_blob, update_date, sourcesystem_cd)"
				+ "select :patientNum, :patientID, :updateDate, :sourceSystemCD where not exists (select patient_num from "
				+ dataSourceMetaInfoConfig.getDemodataSchemaName() + "patient_dimension where patient_num = :patientNum)";

		return CustomJdbcBatchItemWriter.getWriter(sql, i2b2DemoDataSource);
	}
	
	public Integer getPatientIdNextValue(JdbcTemplate template) {
		String sql = "select coalesce(MAX(patient_num),0) AS max_id from " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "patient_mapping";
		return template.queryForObject(sql, Integer.class);
	}
	
	/**
	 * Check if patient exists.
	 * @param template jdbc template to query into database
	 * @return
	 */
	public List<Integer> getPatientNums(JdbcTemplate template, String patientId) {
		String sql = "select PATIENT_NUM from " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "patient_mapping where PATIENT_IDE = ?";	
		return template.queryForList(sql,new Object [] {patientId},Integer.class);
	}
}