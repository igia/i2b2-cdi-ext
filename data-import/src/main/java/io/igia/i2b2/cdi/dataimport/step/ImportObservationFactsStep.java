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
package io.igia.i2b2.cdi.dataimport.step;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;

import io.igia.i2b2.cdi.common.cache.EncounterNextValCache;
import io.igia.i2b2.cdi.common.cache.PatientMappingCache;
import io.igia.i2b2.cdi.common.cache.PatientNextValCache;
import io.igia.i2b2.cdi.common.config.AppBatchProperties;
import io.igia.i2b2.cdi.common.config.DataSourceMetaInfoConfig;
import io.igia.i2b2.cdi.common.domain.CsvObservationFact;
import io.igia.i2b2.cdi.common.domain.DataFileName;
import io.igia.i2b2.cdi.common.domain.DataJobStepName;
import io.igia.i2b2.cdi.common.helper.EncounterHelper;
import io.igia.i2b2.cdi.common.helper.PatientHelper;
import io.igia.i2b2.cdi.common.reader.CustomFlatFileReader;
import io.igia.i2b2.cdi.common.reader.CustomJdbcItemReader;
import io.igia.i2b2.cdi.common.util.CsvHeaders;
import io.igia.i2b2.cdi.common.util.CustomBeanValidator;
import io.igia.i2b2.cdi.common.util.TableFields;
import io.igia.i2b2.cdi.common.writer.CustomFlatFileWriter;
import io.igia.i2b2.cdi.common.writer.CustomJdbcBatchItemWriter;
import io.igia.i2b2.cdi.dataimport.joblistener.I2b2ObservationFactSkipListener;
import io.igia.i2b2.cdi.dataimport.joblistener.ObservationFactSkipListener;
import io.igia.i2b2.cdi.dataimport.processor.ObservationFactProcessor;

@Configuration
public class ImportObservationFactsStep {
	
	private static final String FILE_NAME = DataFileName.OBSERVATION_FACTS.getFileName();
	
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	private StepBuilderFactory i2b2StepBuilderFactory;
		
	@Autowired
	private AppBatchProperties batchProperties;
	
	@Autowired
	@Qualifier("postgresqlDataSource")
	DataSource postgresqlDataSource;
	
	@Autowired
	@Qualifier("i2b2DemoDataSource")
	private DataSource i2b2DemoDataSource;
	
	@Autowired
	CustomBeanValidator customBeanValidator;
	
	@Autowired
	PatientMappingCache patientMappingCache;
	
	@Autowired
	PatientNextValCache patientNextValCache;
	
	@Autowired
	EncounterNextValCache encounterNextValCache;
	
	@Autowired
	PatientHelper patientHelper;
	
	@Autowired
	EncounterHelper encounterHelper;
	
	@Autowired
	DataSourceMetaInfoConfig dataSourceMetaInfoConfig;
	
	// Load from csv into intermediate database...
	@Bean
	@StepScope
	public FlatFileItemReader<CsvObservationFact> csvObservationFactsReader(@Value("#{jobParameters['LOCAL_DATA_DIRECTORY_PATH']}") String filePath) {
		return CustomFlatFileReader.getReader("csvObservationFactsReader",
				filePath + FILE_NAME,
				CsvHeaders.getObservationFactHeaders(), CsvHeaders.getObservationFactColumnNumbers(), CsvObservationFact.class);
	}

	@Bean
	public JdbcBatchItemWriter<CsvObservationFact> intermediateDBObservationFactsWriter() {
		String insertSql = "INSERT into nhp_observation_facts " + "(" + TableFields.getObservationFactTableFields() + ")" + ""
				+ " values (:encounterID, :patientID, :conceptCD, :providerID, :startDTS, :modifierCD, :instanceNum, :value, :unitCD)";
		return CustomJdbcBatchItemWriter.getWriter(insertSql, postgresqlDataSource);
	}
	
	@Bean
	@StepScope
	public FlatFileItemWriter<CsvObservationFact> observationsSkippedRecordsWriter(@Value("#{jobParameters['ERROR_RECORDS_DIRECTORY_PATH']}") String filePath) {
		String fileName = DataFileName.OBSERVATION_FACTS_SKIPPED_RECORDS.getFileName();
		return CustomFlatFileWriter.getWriter("observationsSkippedRecordsWriter", filePath + fileName,
				CsvHeaders.getObservationFactErrorRecordHeaders());
	}
	
	public Step intermediateDBImportObservationFactsStep() {
		ObservationFactSkipListener skipListener = new ObservationFactSkipListener(observationsSkippedRecordsWriter(""));
		
		return stepBuilderFactory.get(DataJobStepName.IMPORT_CSV_OBSERVATION_FACT_STEP)
				.<CsvObservationFact, CsvObservationFact>chunk(batchProperties.getCommitInterval())
				.reader(csvObservationFactsReader(""))
				.processor(customBeanValidator.validator())
				.writer(intermediateDBObservationFactsWriter())
				.faultTolerant()
                .skipLimit(Integer.MAX_VALUE)
                .skip(FlatFileParseException.class)
                .skip(ValidationException.class)
                .listener(skipListener)
                .stream(observationsSkippedRecordsWriter(""))
				.build();
	}
	
	// Load from intermediate database to i2b2...
	@Bean
	public JdbcPagingItemReader<CsvObservationFact> intermediateDBObservationFactsReader() throws Exception {
		SqlPagingQueryProviderFactoryBean pagingQueryProvider = new SqlPagingQueryProviderFactoryBean();
		pagingQueryProvider.setSelectClause("SELECT * ");
		pagingQueryProvider.setFromClause("from nhp_observation_facts");
		Map<String, Order> sortKeys = new HashMap<>();
		sortKeys.put("id", Order.ASCENDING);
		pagingQueryProvider.setSortKeys(sortKeys);
		return CustomJdbcItemReader.getPagingReader(pagingQueryProvider, postgresqlDataSource, batchProperties.getJdbcCursorReaderFetchSize(), new RowMapper<CsvObservationFact>() {

			@Override
			public CsvObservationFact mapRow(ResultSet rs, int rowNum) throws SQLException {
				CsvObservationFact observationFact = new CsvObservationFact();
				observationFact.setEncounterID(rs.getString("encounter_id"));
				observationFact.setPatientID(rs.getString("patient_id"));
				observationFact.setConceptCD(rs.getString("concept_cd"));
				observationFact.setProviderID(rs.getString("provider_id"));
				observationFact.setStartDTS(rs.getString("start_date"));
				observationFact.setModifierCD(rs.getString("modifier_cd"));
				observationFact.setInstanceNum(rs.getString("instance_num"));
				observationFact.setValue(rs.getString("value"));
				observationFact.setUnitCD(rs.getString("unit_cd"));
				return observationFact;
			}
		});
	}
	
	@Bean
	public  ObservationFactProcessor i2b2ObservationFactProcessor() {
		ObservationFactProcessor observationFactProcessor = new ObservationFactProcessor(patientHelper, encounterHelper);
		observationFactProcessor.setDataSource(i2b2DemoDataSource);
		observationFactProcessor.setPatientCache(patientMappingCache);
		observationFactProcessor.setPatientNextValueCache(patientNextValCache);
		observationFactProcessor.setEncounterNextValCache(encounterNextValCache);
		return observationFactProcessor;
	}
	
	@Bean
	public JdbcBatchItemWriter<CsvObservationFact> i2b2PatientMappingWriter() {
		return patientHelper.getI2b2PatientMappingWriterWithExistsClause(i2b2DemoDataSource);
	}
	
	@Bean
	public JdbcBatchItemWriter<CsvObservationFact> i2b2PatientDimensionWriter() {		
		return patientHelper.getI2b2PatientDimensionBasicWriter(i2b2DemoDataSource);
	}
	
	@Bean
	public JdbcBatchItemWriter<CsvObservationFact> i2b2FactEncounterMappingWriter() {
		return encounterHelper.getI2b2EncounterMappingWriter(i2b2DemoDataSource);
	}
	
	@Bean
	public JdbcBatchItemWriter<CsvObservationFact> i2b2FactVisitDimensionWriter() {
		return encounterHelper.getI2b2VisitDimensionBasicWriter(i2b2DemoDataSource);
	}
	
	@Bean
	public CompositeItemWriter<CsvObservationFact> observationFactWriter() {
		CompositeItemWriter<CsvObservationFact> compositeWriter = new CompositeItemWriter<>();
		List<ItemWriter<? super CsvObservationFact>> delegates = new ArrayList<>();
		delegates.add(i2b2PatientMappingWriter());
		delegates.add(i2b2PatientDimensionWriter());
		delegates.add(i2b2FactEncounterMappingWriter());
		delegates.add(i2b2FactVisitDimensionWriter());
		delegates.add(i2b2ObservationFactsWriter());
		compositeWriter.setDelegates(delegates);
		return compositeWriter;
	}
	
	@Bean
	public JdbcBatchItemWriter<CsvObservationFact> i2b2ObservationFactsWriter() {

		String sql = "INSERT INTO " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "observation_fact "
				+ "(encounter_num, patient_num, concept_cd, provider_id, start_date, modifier_cd, instance_num, "
				+ "valtype_cd, tval_char, nval_num, units_cd, update_date, sourcesystem_cd)";
		
		if (batchProperties.isEnableNotExistsClause()) {
			sql += " select :encounterNum, :patientNum, :conceptCD, :providerID, :startDate, :modifierCD, "
					+ ":instanceNumeric, :valTypeCd, :value, :valueNumeric, :unitCD, :updateDate, :sourceSystemCD "
					+ " where not exists (select encounter_num from " + dataSourceMetaInfoConfig.getDemodataSchemaName()
					+ "observation_fact where encounter_num = :encounterNum and patient_num = :patientNum and"
					+ " concept_cd = :conceptCD and provider_id = :providerID and start_date = :startDate)";
		} else {
			sql += " VALUES (:encounterNum, :patientNum, :conceptCD, :providerID, :startDate, :modifierCD, "
					+ ":instanceNumeric, :valTypeCd, :value, :valueNumeric, :unitCD, :updateDate, :sourceSystemCD)";
		}
		return CustomJdbcBatchItemWriter.getWriter(sql, i2b2DemoDataSource);
	}

	@Bean
	public Step i2b2ImportObservationFactsStep() throws Exception {
        I2b2ObservationFactSkipListener skipListener = new I2b2ObservationFactSkipListener(
                observationsSkippedRecordsWriter(""));

	    return i2b2StepBuilderFactory.get(DataJobStepName.IMPORT_I2B2_OBSERVATION_FACT_STEP)
				.<CsvObservationFact, CsvObservationFact>chunk(batchProperties.getCommitInterval())
				.reader(intermediateDBObservationFactsReader())
				.processor(i2b2ObservationFactProcessor())
				.writer(observationFactWriter())
				.faultTolerant()
				.skip(DuplicateKeyException.class)
				.skipLimit(Integer.MAX_VALUE)
				.listener(skipListener)
				.stream(observationsSkippedRecordsWriter(""))
				.build();
	}
}