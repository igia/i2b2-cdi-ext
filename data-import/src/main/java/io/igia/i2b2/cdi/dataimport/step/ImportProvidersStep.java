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
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemReader;
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

import io.igia.i2b2.cdi.common.cache.EncounterNextValCache;
import io.igia.i2b2.cdi.common.cache.PatientMappingCache;
import io.igia.i2b2.cdi.common.cache.ProviderCache;
import io.igia.i2b2.cdi.common.config.AppBatchProperties;
import io.igia.i2b2.cdi.common.config.DataSourceMetaInfoConfig;
import io.igia.i2b2.cdi.common.domain.CsvProviderReference;
import io.igia.i2b2.cdi.common.domain.DataFileName;
import io.igia.i2b2.cdi.common.domain.DataJobStepName;
import io.igia.i2b2.cdi.common.reader.CustomFlatFileReader;
import io.igia.i2b2.cdi.common.reader.CustomJdbcItemReader;
import io.igia.i2b2.cdi.common.util.CsvHeaders;
import io.igia.i2b2.cdi.common.util.CustomBeanValidator;
import io.igia.i2b2.cdi.common.util.TableFields;
import io.igia.i2b2.cdi.common.writer.CustomFlatFileWriter;
import io.igia.i2b2.cdi.common.writer.CustomJdbcBatchItemWriter;
import io.igia.i2b2.cdi.dataimport.joblistener.I2b2ProviderSkipListener;
import io.igia.i2b2.cdi.dataimport.joblistener.ProviderSkipListener;
import io.igia.i2b2.cdi.dataimport.processor.ProviderProcessor;


@Configuration
public class ImportProvidersStep {
	
	@Autowired
	ProviderCache providerCache;
	
	@Autowired
	PatientMappingCache patientMappingCache;
	
	@Autowired
	EncounterNextValCache encounterNextValCache;
	
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
	DataSourceMetaInfoConfig dataSourceMetaInfoConfig;
	
	private static final String FILE_NAME = DataFileName.PROVIDER_DIMENSIONS.getFileName();
	
	// Load from csv into intermediate database...
	@Bean
	@StepScope
	public FlatFileItemReader<CsvProviderReference> csvProviderReferenceReader(@Value("#{jobParameters['LOCAL_DATA_DIRECTORY_PATH']}") String filePath) {
		return CustomFlatFileReader.getReader("csvProviderReferenceReader",
				filePath + FILE_NAME,
				CsvHeaders.getProviderReferenceHeaders(), CsvHeaders.getProviderReferenceColumnNumbers(), CsvProviderReference.class);
	}

	@Bean
	public JdbcBatchItemWriter<CsvProviderReference> providerReferenceWriter() {
		String sql = "INSERT INTO nhp_patient_provider_reference " + "(" + TableFields.getProviderReferenceTableFields() + ")"
				+ " values (:providerID, :providerPath, :userNM)";

		return CustomJdbcBatchItemWriter.getWriter(sql,postgresqlDataSource);
	}
	
	@Bean
	@StepScope
	public FlatFileItemWriter<CsvProviderReference> providersSkippedRecordsWriter(@Value("#{jobParameters['ERROR_RECORDS_DIRECTORY_PATH']}") String filePath) {
		String fileName = DataFileName.PROVIDER_DIMENSIONS_SKIPPED_RECORDS.getFileName();
		return CustomFlatFileWriter.getWriter("providersSkippedRecordsWriter", filePath + fileName,
				CsvHeaders.getProviderReferenceErrorRecordHeaders());
	}
	
	@Bean
	public Step intermediateDBImportProvidersStep() {
		ProviderSkipListener skipListener = new ProviderSkipListener(providersSkippedRecordsWriter(""));
		
		return stepBuilderFactory.get(DataJobStepName.IMPORT_CSV_PROVIDER_STEP)
				.<CsvProviderReference, CsvProviderReference>chunk(batchProperties.getCommitInterval())
				.reader(csvProviderReferenceReader(""))
				.processor(customBeanValidator.validator())
				.writer(providerReferenceWriter())
				.faultTolerant()
                .skipLimit(Integer.MAX_VALUE)
                .skip(FlatFileParseException.class)
                .skip(ValidationException.class)
                .listener(skipListener)
                .stream(providersSkippedRecordsWriter(""))
				.build();
	}
	
	// Load from intermediate database to i2b2...
	@Bean
	public JdbcPagingItemReader<CsvProviderReference> intermediateDBProvidersReader() throws Exception {
		SqlPagingQueryProviderFactoryBean pagingQueryProvider = new SqlPagingQueryProviderFactoryBean();
		pagingQueryProvider.setSelectClause("SELECT *");
		pagingQueryProvider.setFromClause("from nhp_patient_provider_reference");
		Map<String, Order> sortKeys = new HashMap<>();
		sortKeys.put("id", Order.ASCENDING);
		pagingQueryProvider.setSortKeys(sortKeys);
		return CustomJdbcItemReader.getPagingReader(pagingQueryProvider, postgresqlDataSource, batchProperties.getJdbcCursorReaderFetchSize(), new RowMapper<CsvProviderReference>() {

			@Override
			public CsvProviderReference mapRow(ResultSet rs, int rowNum) throws SQLException {
				CsvProviderReference providerReference = new CsvProviderReference();
				providerReference.setProviderID(rs.getString("provider_id"));
				providerReference.setProviderPath(rs.getString("provider_path"));
				providerReference.setUserNM(rs.getString("user_nm"));
				return providerReference;
			}
		});
	}
	
	@Bean
	public ProviderProcessor providerProcessor() {
		return new ProviderProcessor(providerCache, providersSkippedRecordsWriter(""));
	}
	
	@Bean
	public JdbcBatchItemWriter<CsvProviderReference> i2b2ProviderDimensionWriter() {

		String sql = "INSERT INTO " + dataSourceMetaInfoConfig.getDemodataSchemaName() 
		+ "provider_dimension(provider_id, provider_path, name_char, update_date, sourcesystem_cd)" + 
		" values (:providerID, :providerPath, :userNM, :updateDate, :sourceSystemCD)";
		
		return CustomJdbcBatchItemWriter.getWriter(sql, i2b2DemoDataSource);
	}

	@Bean
	public Step i2b2ImportProviderReferenceStep() throws Exception {
	    I2b2ProviderSkipListener skipListener = new I2b2ProviderSkipListener(providersSkippedRecordsWriter(""));
	    
		return i2b2StepBuilderFactory.get(DataJobStepName.IMPORT_I2B2_PROVIDER_STEP)
				.<CsvProviderReference, CsvProviderReference>chunk(batchProperties.getCommitInterval())
				.reader(intermediateDBProvidersReader())
				.processor(providerProcessor())
				.writer(i2b2ProviderDimensionWriter())
				.faultTolerant()
                .skipLimit(Integer.MAX_VALUE)
                .skip(DuplicateKeyException.class)
                .listener(skipListener)
                .stream(providersSkippedRecordsWriter(""))
				.build();
	}
}
