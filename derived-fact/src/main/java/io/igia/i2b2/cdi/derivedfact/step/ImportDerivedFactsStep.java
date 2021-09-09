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
package io.igia.i2b2.cdi.derivedfact.step;

import javax.sql.DataSource;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DuplicateKeyException;

import io.igia.i2b2.cdi.common.config.AppBatchProperties;
import io.igia.i2b2.cdi.common.config.DataSourceMetaInfoConfig;
import io.igia.i2b2.cdi.common.domain.DerivedConceptDefinition;
import io.igia.i2b2.cdi.common.domain.ObservationFact;
import io.igia.i2b2.cdi.common.reader.CustomJdbcItemReader;
import io.igia.i2b2.cdi.common.writer.CustomJdbcBatchItemWriter;
import io.igia.i2b2.cdi.derivedfact.joblistener.StepListener;
import io.igia.i2b2.cdi.derivedfact.processor.DerivedFactProcessor;
import io.igia.i2b2.cdi.derivedfact.rowmapper.ObservationFactRowMapper;

@Configuration
public class ImportDerivedFactsStep {
		
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
		
	@Autowired
	private AppBatchProperties batchProperties;
		
	@Autowired
	@Qualifier("i2b2DemoDataSource")
	private DataSource i2b2DemoDataSource;
	
	@Autowired
	private DataSourceMetaInfoConfig dataSourceMetaInfoConfig;
	
	// fetch data from derived facts definition table...
	public JdbcCursorItemReader<ObservationFact> intermediateDerivedFactsReader(DerivedConceptDefinition derivedConceptDefinition) throws Exception {
		return CustomJdbcItemReader.getReader(derivedConceptDefinition.getSqlQuery(), i2b2DemoDataSource,
				batchProperties.getJdbcCursorReaderFetchSize(), new ObservationFactRowMapper(derivedConceptDefinition));
	}
	
	private DerivedFactProcessor derivedFactProcessor() {
		return new DerivedFactProcessor();
	}
	
	@Bean
	public JdbcBatchItemWriter<ObservationFact> i2b2DerivedFactsWriter() {

		String insertSql = "INSERT into " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "observation_fact "
				+ "(encounter_num, patient_num, concept_cd, provider_id, start_date, modifier_cd, instance_num, "
				+ "valtype_cd, tval_char, nval_num, units_cd, end_date, update_date, sourcesystem_cd) "
				+ "VALUES (:encounterNum, :patientNum, :conceptCd, :providerId, :startDate, :modifierCd, "
				+ ":instanceNum, :valTypeCd, :tValChar, :nValNum, :unitsCd, :endDate, :updateDate, "
				+ ":sourceSystemCD)";
		return CustomJdbcBatchItemWriter.getWriter(insertSql, i2b2DemoDataSource);
	}
	
	public Step derivedObservationFactsStep(DerivedConceptDefinition derivedConceptDefinition, String stepName) throws Exception {
		return stepBuilderFactory.get(stepName)
				.<ObservationFact, ObservationFact>chunk(batchProperties.getCommitInterval())
				.reader(intermediateDerivedFactsReader(derivedConceptDefinition))
				.processor(derivedFactProcessor())
				.writer(i2b2DerivedFactsWriter())
				.faultTolerant()
                .skip(DuplicateKeyException.class)
                .skipLimit(Integer.MAX_VALUE)
				.listener(new StepListener(derivedConceptDefinition, i2b2DemoDataSource, dataSourceMetaInfoConfig))
				.build();
	}
}