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

import io.igia.i2b2.cdi.common.cache.PatientMappingCache;
import io.igia.i2b2.cdi.common.cache.PatientNextValCache;
import io.igia.i2b2.cdi.common.config.AppBatchProperties;
import io.igia.i2b2.cdi.common.domain.CsvDemographics;
import io.igia.i2b2.cdi.common.domain.DataFileName;
import io.igia.i2b2.cdi.common.domain.DataJobStepName;
import io.igia.i2b2.cdi.common.exception.PatientAlreadyExistsException;
import io.igia.i2b2.cdi.common.helper.PatientHelper;
import io.igia.i2b2.cdi.common.reader.CustomFlatFileReader;
import io.igia.i2b2.cdi.common.reader.CustomJdbcItemReader;
import io.igia.i2b2.cdi.common.util.CsvHeaders;
import io.igia.i2b2.cdi.common.util.CustomBeanValidator;
import io.igia.i2b2.cdi.common.util.TableFields;
import io.igia.i2b2.cdi.common.writer.CustomFlatFileWriter;
import io.igia.i2b2.cdi.common.writer.CustomJdbcBatchItemWriter;
import io.igia.i2b2.cdi.dataimport.joblistener.DemographicSkipListener;
import io.igia.i2b2.cdi.dataimport.joblistener.I2b2DemographicSkipListener;
import io.igia.i2b2.cdi.dataimport.processor.DemographicsProcessor;

@Configuration
public class ImportDemographicsStep {
	@Autowired
	PatientMappingCache patientMappingCache;
	
	@Autowired
	PatientNextValCache patientNextValCache;
	
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	private StepBuilderFactory i2b2StepBuilderFactory;
		
	@Autowired
	private AppBatchProperties batchProperties;
	
	@Autowired
	@Qualifier("postgresqlDataSource")
	private DataSource postgresqlDataSource;
	
	@Autowired
	@Qualifier("i2b2DemoDataSource")
	private DataSource i2b2DemoDataSource;
	
	@Autowired
	CustomBeanValidator customBeanValidator;
	
	@Autowired
	PatientHelper patientHelper;
	
	private static final String FILE_NAME = DataFileName.PATIENT_DIMENSIONS.getFileName();
		
	// Load from csv into intermediate database...
	@Bean
	@StepScope
	public FlatFileItemReader<CsvDemographics> csvDemographicsReader(@Value("#{jobParameters['LOCAL_DATA_DIRECTORY_PATH']}") String filePath) {
		return CustomFlatFileReader.getReader("csvDemographicsReader",
				filePath + FILE_NAME,
				CsvHeaders.getDemographicHeaders(), CsvHeaders.getDemographicColumnNumbers(), CsvDemographics.class);
	}
		
	@Bean
	public JdbcBatchItemWriter<CsvDemographics> intermediateDBDemographicsWriter() {
		String sql = "INSERT INTO nhp_patient_demographics " + "(" + TableFields.getDemographicsTableFields() + ")"
				+ " values(:patientID, :birthDTS, :gender)";
		return CustomJdbcBatchItemWriter.getWriter(sql, postgresqlDataSource);
	}
	
	@Bean
	@StepScope
	public FlatFileItemWriter<CsvDemographics> demographicsSkippedRecordsWriter(@Value("#{jobParameters['ERROR_RECORDS_DIRECTORY_PATH']}") String filePath) {
		String fileName = DataFileName.PATIENT_DIMENSIONS_SKIPPED_RECORDS.getFileName();
		return CustomFlatFileWriter.getWriter("demographicsSkippedRecordsWriter", filePath + fileName,
				CsvHeaders.getDemographicErrorRecordHeaders());
	}
	
	@Bean
	public Step intermediateDBImportDemographicsStep() {
		DemographicSkipListener skipListener = new DemographicSkipListener(demographicsSkippedRecordsWriter(""));
		
		return stepBuilderFactory.get(DataJobStepName.IMPORT_CSV_PATIENT_DIMENSION_STEP)
				.<CsvDemographics, CsvDemographics>chunk(batchProperties.getCommitInterval())
				.reader(csvDemographicsReader(""))
				.processor(customBeanValidator.validator())
				.writer(intermediateDBDemographicsWriter())
				.faultTolerant()
                .skipLimit(Integer.MAX_VALUE)
                .skip(FlatFileParseException.class)
                .skip(DuplicateKeyException.class)
                .skip(ValidationException.class)
                .listener(skipListener)
                .stream(demographicsSkippedRecordsWriter(""))
				.build();
	}
	
	// Load from intermediate database to i2b2...
	@Bean
	public JdbcPagingItemReader<CsvDemographics> intermediateDBDemographicsReader() throws Exception {
		SqlPagingQueryProviderFactoryBean pagingQueryProvider = new SqlPagingQueryProviderFactoryBean();
		pagingQueryProvider.setSelectClause("SELECT *");
		pagingQueryProvider.setFromClause("from nhp_patient_demographics");
		Map<String, Order> sortKeys = new HashMap<>();
		sortKeys.put("id", Order.ASCENDING);
		pagingQueryProvider.setSortKeys(sortKeys);
		return CustomJdbcItemReader.getPagingReader(pagingQueryProvider, postgresqlDataSource, batchProperties.getJdbcCursorReaderFetchSize(), new RowMapper<CsvDemographics>() {

			@Override
			public CsvDemographics mapRow(ResultSet rs, int rowNum) throws SQLException {
				CsvDemographics demographics = new CsvDemographics();
				demographics.setPatientID(rs.getString("patient_ide"));
				demographics.setBirthDTS(rs.getString("birth_dts"));
				demographics.setGender(rs.getString("gender"));
				return demographics;
			}
		});
	}
	
	@Bean
	public DemographicsProcessor demographicsProcessor() {
		DemographicsProcessor demographicProcessor = new DemographicsProcessor(patientHelper);
		demographicProcessor.setDataSource(i2b2DemoDataSource);
		demographicProcessor.setPatientCache(patientMappingCache);
		demographicProcessor.setPatientNextValueCache(patientNextValCache);
		return demographicProcessor;
	}
	
	@Bean
	public JdbcBatchItemWriter<CsvDemographics> i2b2DemographicsPatientMappingWriter() {
		return patientHelper.getI2b2PatientMappingWriter(i2b2DemoDataSource);
	}
	
	@Bean
	public JdbcBatchItemWriter<CsvDemographics> i2b2DemographicsPatientDimensionWriter() {		
		return patientHelper.getI2b2PatientDimensionWriter(i2b2DemoDataSource);
	}
	
	@Bean
	public CompositeItemWriter<CsvDemographics> demographicsWriter() {
		CompositeItemWriter<CsvDemographics> compositeWriter = new CompositeItemWriter<>();
		List<ItemWriter<? super CsvDemographics>> delegates = new ArrayList<>();
		delegates.add(i2b2DemographicsPatientMappingWriter());
		delegates.add(i2b2DemographicsPatientDimensionWriter());
		compositeWriter.setDelegates(delegates);
		return compositeWriter;
	}
	
	@Bean
	public Step i2b2ImportDemographicsStep() throws Exception {
	    I2b2DemographicSkipListener skipListener = new I2b2DemographicSkipListener(demographicsSkippedRecordsWriter(""));
	    
		return i2b2StepBuilderFactory.get(DataJobStepName.IMPORT_I2B2_PATIENT_DIMENSION_STEP)
				.<CsvDemographics, CsvDemographics>chunk(batchProperties.getCommitInterval())
				.reader(intermediateDBDemographicsReader())
				.processor(demographicsProcessor())
				.writer(demographicsWriter())
				.faultTolerant()
                .skipLimit(Integer.MAX_VALUE)
                .skip(DuplicateKeyException.class)
                .skip(PatientAlreadyExistsException.class)
                .listener(skipListener)
                .stream(demographicsSkippedRecordsWriter(""))
				.build();
	}
}