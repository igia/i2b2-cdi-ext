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
 * Copyright (C) 2018-2019 Persistent Systems, Inc.
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
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;

import io.igia.i2b2.cdi.dataimport.processor.EncounterProcessor;
import io.igia.i2b2.cdi.common.JobListener.CustomSkipListener;
import io.igia.i2b2.cdi.common.cache.EncounterNextValCache;
import io.igia.i2b2.cdi.common.cache.PatientMappingCache;
import io.igia.i2b2.cdi.common.config.AppBatchProperties;
import io.igia.i2b2.cdi.common.config.AppIntegrationProperties;
import io.igia.i2b2.cdi.common.domain.CsvEncounter;
import io.igia.i2b2.cdi.common.domain.DataFileName;
import io.igia.i2b2.cdi.common.helper.EncounterHelper;
import io.igia.i2b2.cdi.common.reader.CustomJdbcItemReader;
import io.igia.i2b2.cdi.common.reader.EncounterFlatFileReader;
import io.igia.i2b2.cdi.common.util.CsvHeaders;
import io.igia.i2b2.cdi.common.util.CustomBeanValidator;
import io.igia.i2b2.cdi.common.util.TableFields;
import io.igia.i2b2.cdi.common.writer.CustomFlatFileWriter;
import io.igia.i2b2.cdi.common.writer.CustomJdbcBatchItemWriter;
import io.igia.i2b2.cdi.common.writer.EncountersCompositeJdbcWriter;


@Configuration
public class ImportEncountersStep {
	
	
	@Autowired
	PatientMappingCache patientMappingCache;
	
	@Autowired
	EncounterNextValCache encounterNextValCache;
	
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	private AppBatchProperties batchProperties;
	
	@Autowired
	@Qualifier("postgresqlDataSource")
	private DataSource postgresqlDataSource;
	
	@Autowired
	@Qualifier("i2b2DemoDataSource")
	private DataSource i2b2DemoDataSource;
	
	@Autowired
	AppIntegrationProperties appIntegrationProperties;
	
	@Autowired
	CustomBeanValidator customBeanValidator;
		
	private static final String FILE_NAME = DataFileName.VISIT_DIMENSIONS.getFileName();
	
	// Load from csv into intermediate database...
	@Bean
	@StepScope
	public EncounterFlatFileReader csvEncounterReader(@Value("#{jobParameters['LOCAL_DATA_DIRECTORY_PATH']}") String filePath) {
		EncounterFlatFileReader encounterFlatFileReader = new EncounterFlatFileReader();
		encounterFlatFileReader.setReader("csvEncounterReader",
				filePath + FILE_NAME,
				CsvHeaders.getEncounterHeaders(), 
				CsvHeaders.getEncounterColumnNumbers(), 
				patientMappingCache);
		return encounterFlatFileReader;
	}
	
	@Bean
	public JdbcBatchItemWriter<CsvEncounter> intermediateDBEncounterWriter() {
		String sql = "INSERT INTO nhp_patient_encounters " + "(" + TableFields.getEncountersTableFields() + ")"
				+ " values(:encounterID, :patientID, :startDTS, :endDTS )";

		return CustomJdbcBatchItemWriter.getWriter(sql, postgresqlDataSource);
	}
	
	@Bean
	@StepScope
	public FlatFileItemWriter<CsvEncounter> encountersSkippedRecordsWriter(@Value("#{jobParameters['ERROR_RECORDS_DIRECTORY_PATH']}") String filePath) {
		String fileName = DataFileName.VISIT_DIMENSIONS_SKIPPED_RECORDS.getFileName();
		return CustomFlatFileWriter.getWriter("encountersSkippedRecordsWriter", filePath + fileName,
				CsvHeaders.getEncounterHeaders());
	}
	
	@Bean
	public Step intermediateDBImportEncountersStep() {
		CustomSkipListener<CsvEncounter, CsvEncounter> skipListener = new CustomSkipListener<>();
		skipListener.setFlatFileItemWriter(encountersSkippedRecordsWriter(""));
		
		return stepBuilderFactory.get("intermediateDBImportEncountersStep")
				.<CsvEncounter, CsvEncounter>chunk(batchProperties.getCommitInterval())
				.reader(csvEncounterReader(""))
				.processor(customBeanValidator.validator())
				.writer(intermediateDBEncounterWriter())
				.faultTolerant()
                .skipLimit(Integer.MAX_VALUE)
                .skip(FlatFileParseException.class)
                .skip(ValidationException.class)
                .listener(skipListener)
                .stream(encountersSkippedRecordsWriter(""))
				.build();
	}
	
	// Load from intermediate database into i2b2...
	@Bean
	public JdbcPagingItemReader<CsvEncounter> intermediateDBEncounterReader() throws Exception {
		SqlPagingQueryProviderFactoryBean pagingQueryProvider = new SqlPagingQueryProviderFactoryBean();
		pagingQueryProvider.setSelectClause("SELECT *");
		pagingQueryProvider.setFromClause("from nhp_patient_encounters");
		Map<String, Order> sortKeys = new HashMap<>();
		sortKeys.put("id", Order.ASCENDING);
		pagingQueryProvider.setSortKeys(sortKeys);
		return CustomJdbcItemReader.getPagingReader(pagingQueryProvider, postgresqlDataSource, batchProperties.getJdbcCursorReaderFetchSize(), new RowMapper<CsvEncounter>() {

			@Override
			public CsvEncounter mapRow(ResultSet rs, int rowNum) throws SQLException {
				CsvEncounter encounter = new CsvEncounter();
				encounter.setPatientID(rs.getString("patient_ide"));
				encounter.setEncounterID(rs.getString("encounter_ide"));
				encounter.setStartDTS(rs.getString("start_date"));
				encounter.setEndDTS(rs.getString("end_date"));
				return encounter;
			}
		});
	}
	
	@Bean
	public EncounterProcessor encounterProcessor() {
		EncounterProcessor encounterProcessor = new EncounterProcessor();
		encounterProcessor.setI2b2DataSource(i2b2DemoDataSource);
		encounterProcessor.setEncounterNextValCache(encounterNextValCache);
		encounterProcessor.setPatientMappingCache(patientMappingCache);
		return encounterProcessor;
	}

	@Bean
	public JdbcBatchItemWriter<CsvEncounter> i2b2EncounterMappingWriter() {
		return EncounterHelper.getI2b2EncounterMappingWriter(i2b2DemoDataSource);
	}
	
	@Bean
	public JdbcBatchItemWriter<CsvEncounter> i2b2EncounterWriter() {
		return EncounterHelper.getI2b2VisitDimensionWriter(i2b2DemoDataSource);
	}	
	
	@Bean
	public EncountersCompositeJdbcWriter encounterWriter() {
		EncountersCompositeJdbcWriter encounterWriter = new EncountersCompositeJdbcWriter();
		
		List<JdbcBatchItemWriter<CsvEncounter>> writers = new ArrayList<>();
		writers.add(i2b2EncounterMappingWriter());
		writers.add(i2b2EncounterWriter());
		
		encounterWriter.setDelegates(writers);
		return encounterWriter;
	}

	@Bean
	public Step i2b2ImportEncountersStep() throws Exception {
		return stepBuilderFactory.get("i2b2ImportEncountersStep")
				.<CsvEncounter, CsvEncounter>chunk(batchProperties.getCommitInterval())
				.reader(intermediateDBEncounterReader())
				.processor(encounterProcessor())
				.writer(encounterWriter())
				.faultTolerant()
				.skip(DuplicateKeyException.class)
				.skipLimit(Integer.MAX_VALUE)
				.build();
	}
}
