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
package io.igia.i2b2.cdi.conceptimport.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
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
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;

import io.igia.i2b2.cdi.conceptimport.jobListener.ConceptJobCompletionNotificationListener;
import io.igia.i2b2.cdi.conceptimport.processor.ConceptMappingProcessor;
import io.igia.i2b2.cdi.conceptimport.processor.ConceptProcessor;
import io.igia.i2b2.cdi.conceptimport.rowmapper.I2b2ConceptMappingRowMapper;
import io.igia.i2b2.cdi.conceptimport.rowmapper.I2b2ConceptRowMapper;
import io.igia.i2b2.cdi.conceptimport.writer.I2b2ConceptCompositeWriter;
import io.igia.i2b2.cdi.conceptimport.writer.I2b2ConceptMappingCompositeWriter;
import io.igia.i2b2.cdi.common.JobListener.CustomSkipListener;
import io.igia.i2b2.cdi.common.config.AppBatchProperties;
import io.igia.i2b2.cdi.common.config.AppIntegrationProperties;
import io.igia.i2b2.cdi.common.config.I2b2SchemaProperties;
import io.igia.i2b2.cdi.common.config.LaunchConfiguration;
import io.igia.i2b2.cdi.common.domain.ConceptFileName;
import io.igia.i2b2.cdi.common.domain.CsvConcept;
import io.igia.i2b2.cdi.common.domain.CsvConceptMapping;
import io.igia.i2b2.cdi.common.domain.I2b2Concept;
import io.igia.i2b2.cdi.common.reader.CustomFlatFileReader;
import io.igia.i2b2.cdi.common.reader.CustomJdbcItemReader;
import io.igia.i2b2.cdi.common.tasklet.DeleteConceptTasklet;
import io.igia.i2b2.cdi.common.tasklet.DeleteFileTasklet;
import io.igia.i2b2.cdi.common.util.CsvHeaders;
import io.igia.i2b2.cdi.common.util.CustomBeanValidator;
import io.igia.i2b2.cdi.common.writer.CustomFlatFileWriter;
import io.igia.i2b2.cdi.common.writer.CustomJdbcBatchItemWriter;

@Configuration
@EnableBatchProcessing
@ComponentScan(basePackageClasses = DefaultBatchConfigurer.class)
@Import({ LaunchConfiguration.class })
public class ConceptBatchConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	public StepBuilderFactory i2b2StepBuilderFactory;
	
	@Autowired
	ConceptJobCompletionNotificationListener listener;

	@Autowired
	@Qualifier("postgresqlDataSource")
	DataSource postgresqlDataSource;

	@Autowired
	@Qualifier("i2b2DemoDataSource")
	DataSource i2b2DemoDataSource;
	
	@Autowired
	I2b2ConceptRowMapper i2b2ConceptRowMapper;
	
	@Autowired
	I2b2ConceptMappingRowMapper i2b2ConceptMappingRowMapper;

	@Autowired
	AppBatchProperties batchProperties;

	@Autowired
	AppIntegrationProperties integrationProperties;
	
	@Autowired
	DeleteConceptTasklet deleteConceptTasklet;
	
	@Autowired
	CustomBeanValidator customBeanValidator;
	
	@Autowired
	private I2b2SchemaProperties i2b2Properties;
	
	private static final Logger log = LoggerFactory.getLogger(ConceptBatchConfiguration.class);

	//Concept csv import step...
	@Bean
	@StepScope
	public FlatFileItemReader<CsvConcept> getCsvConceptReader(@Value("#{jobParameters['LOCAL_DATA_DIRECTORY_PATH']}") String filePath) {
		return CustomFlatFileReader.getReader("csvconceptreader",
				filePath + ConceptFileName.CONCEPTS.getFileName(),
				CsvHeaders.getConceptsHeaders(), CsvHeaders.getConceptsColumnNumbers(), CsvConcept.class);
	}
	
	@Bean
	public JdbcBatchItemWriter<CsvConcept> csvConceptWriter() {
		String sql = "INSERT INTO concepts (concept_code, concept_path, metadata_xml, facttable_column, "
				+ "table_name, column_name, column_datatype, operator, dimcode) "
				+ "VALUES (:key, :path, :metaDataXml, :factTableColumn, :tableName, :columnName, "
				+ ":columnDataType, :operator, :dimcode)";
		return CustomJdbcBatchItemWriter.getWriter(sql, postgresqlDataSource);
	}
	
	@Bean
	@StepScope
	public FlatFileItemWriter<CsvConcept> conceptsSkippedRecordsWriter(@Value("#{jobParameters['ERROR_RECORDS_DIRECTORY_PATH']}") String filePath) {
		String fileName = ConceptFileName.CONCEPTS_SKIPPED_RECORDS.getFileName();
		return CustomFlatFileWriter.getWriter("conceptsSkippedRecordsWriter", filePath + fileName,
				CsvHeaders.getConceptsHeaders());
	}

	@Bean
	public Step importCsvConceptStep() {
		CustomSkipListener<CsvConcept, CsvConcept> skipListener = new CustomSkipListener<>();
		skipListener.setFlatFileItemWriter(conceptsSkippedRecordsWriter(""));
		
		return stepBuilderFactory.get("importCsvConceptStep")
				.<CsvConcept, CsvConcept>chunk(batchProperties.getCommitInterval())
				.reader(getCsvConceptReader(""))
				.processor(customBeanValidator.validator())
				.writer(csvConceptWriter())
				.faultTolerant()
                .skipLimit(Integer.MAX_VALUE)
                .skip(FlatFileParseException.class)
                .skip(ValidationException.class)
                .listener(skipListener)
                .stream(conceptsSkippedRecordsWriter(""))
				.build();
	}

	//Concept i2b2 import step...
	@Bean
	public JdbcPagingItemReader<I2b2Concept> i2b2ConceptReader() throws Exception {
		SqlPagingQueryProviderFactoryBean pagingQueryProvider = new SqlPagingQueryProviderFactoryBean();
		pagingQueryProvider.setSelectClause("SELECT *");
		pagingQueryProvider.setFromClause("from concepts");
		Map<String, Order> sortKeys = new HashMap<>();
		sortKeys.put("id", Order.ASCENDING);
		pagingQueryProvider.setSortKeys(sortKeys);
		return CustomJdbcItemReader.getPagingReader(pagingQueryProvider, postgresqlDataSource, batchProperties.getJdbcCursorReaderFetchSize(), i2b2ConceptRowMapper);
	}

	@Bean
	public JdbcBatchItemWriter<I2b2Concept> i2b2ConceptMetaDataWriter() {

		String sql = "INSERT INTO i2b2metadata.i2b2 "
				+ "(c_hlevel,c_fullname,c_name,c_synonym_cd,c_visualattributes,c_metadataxml,"
				+ "c_facttablecolumn,c_tablename,c_columnname,c_columndatatype,c_operator,c_dimcode,"
				+ "c_tooltip,m_applied_path,update_date, sourcesystem_cd) VALUES ("
				+ ":level, :fullPath, :name, :synonymCode, :visualAttributes, :metaDataXML, :factTableColumnName,"
				+ ":tableName, :columnName, :columnDataType, :operator, :dimensionCode, :toolTip, :appliedPath, :timeStamp, :sourceSystemCd)";
		
		return CustomJdbcBatchItemWriter.getWriter(sql, i2b2DemoDataSource);
	}

	@Bean
	public JdbcBatchItemWriter<I2b2Concept> i2b2ConceptConceptDimensionWriter() {

		String sql = "INSERT INTO i2b2demodata.concept_dimension "
				+ "(concept_path, concept_cd, name_char, update_date, sourcesystem_cd) VALUES"
				+ "(:fullPath, :conceptCode, :name, :timeStamp, :sourceSystemCd)";

		return CustomJdbcBatchItemWriter.getWriter(sql, i2b2DemoDataSource);
	}

	@Bean
	public JdbcBatchItemWriter<I2b2Concept> i2b2ConceptTableAccesWriter() {

		String sql = "INSERT INTO i2b2metadata.table_access "
				+ "(c_table_cd, c_table_name, c_protected_access, c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes,"
				+ "c_facttablecolumn, c_dimtablename, c_columnname, c_columndatatype, c_operator, c_dimcode,"
				+ "c_tooltip) VALUES (" + ":cTableCode, :cTableName, :cProtectedAccess, "
				+ ":level, :fullPath, :name, :synonymCode, :visualAttributes, :factTableColumnName, "
				+ ":tableName, :columnName, :columnDataType, :operator, :dimensionCode, :toolTip)";
		
		return CustomJdbcBatchItemWriter.getWriter(sql, i2b2DemoDataSource);
	}
	
	private ConceptProcessor i2b2ConceptProcessor() {
		return new ConceptProcessor();
	}

	@Bean
	public ItemWriter<I2b2Concept> i2b2CompositeConceptWriter() {
		I2b2ConceptCompositeWriter i2b2ConceptWriter = new I2b2ConceptCompositeWriter();
		i2b2ConceptWriter.setMetaDataWriter(i2b2ConceptMetaDataWriter());
		i2b2ConceptWriter.setConceptDimensionWriter(i2b2ConceptConceptDimensionWriter());
		i2b2ConceptWriter.setTableccessWriter(i2b2ConceptTableAccesWriter());
		return i2b2ConceptWriter;
	}

	@Bean
	public Step importI2b2ConceptStep() throws Exception {
		return i2b2StepBuilderFactory.get("importI2b2ConceptStep")
				.<I2b2Concept, I2b2Concept>chunk(batchProperties.getCommitInterval())
				.reader(i2b2ConceptReader())
				.processor(i2b2ConceptProcessor())
				.writer(i2b2CompositeConceptWriter())
				.faultTolerant()
				.skip(DuplicateKeyException.class)
				.skipLimit(Integer.MAX_VALUE)
				.build();
	}
	
	@Bean
	public Step preDeleteConceptTaskletStep() {
		return stepBuilderFactory.get("preDeleteConceptTaskletStep")
				.tasklet(deleteConceptTasklet)
				.build();
	}
	
	@Bean
	public Step postDeleteConceptTaskletStep() {
		return stepBuilderFactory.get("postDeleteConceptTaskletStep")
				.tasklet(deleteConceptTasklet)
				.build();
	}
	
	@Bean
	public DeleteFileTasklet deleteConceptFileTasklet() {
		return new DeleteFileTasklet();
	}
		
	@Bean
	public Step deleteConceptFileTaskletStep() {
		return stepBuilderFactory.get("deleteConceptFileTaskletStep")
				.tasklet(deleteConceptFileTasklet())
				.build();
	}
	
	@Bean
	@StepScope
	public FlatFileItemReader<CsvConceptMapping> getCsvConceptMappingReader(@Value("#{jobParameters['LOCAL_DATA_DIRECTORY_PATH']}") String filePath) {
		return CustomFlatFileReader.getReader("csvconceptmappingreader",
				filePath + ConceptFileName.CONCEPT_MAPPINGS.getFileName(),
				CsvHeaders.getConceptMappingHeaders(), CsvHeaders.getConceptMappingColumnNumbers(), CsvConceptMapping.class);
	}
	
	@Bean
	public JdbcBatchItemWriter<CsvConceptMapping> csvConceptMappingWriter() {
		String sql = "INSERT INTO concept_mapping (std_cd, local_cd, local_cd_name) "
				+ "VALUES (:stdCode, :localCode, :localCodeName)";
		return CustomJdbcBatchItemWriter.getWriter(sql, postgresqlDataSource);
	}
	
	@Bean
	@StepScope
	public FlatFileItemWriter<CsvConceptMapping> conceptMappingSkippedRecordsWriter(@Value("#{jobParameters['ERROR_RECORDS_DIRECTORY_PATH']}") String filePath) {
		String fileName = ConceptFileName.CONCEPT_MAPPINGS_SKIPPED_RECORDS.getFileName();
		return CustomFlatFileWriter.getWriter("conceptMappingSkippedRecordsWriter", filePath + fileName,
				CsvHeaders.getConceptMappingHeaders());
	}
	
	public Step importConceptMappingStep() {
		CustomSkipListener<CsvConceptMapping, CsvConceptMapping> skipListener = new CustomSkipListener<>();
		skipListener.setFlatFileItemWriter(conceptMappingSkippedRecordsWriter(""));
		
		return i2b2StepBuilderFactory.get("importConceptMappingStep")
				.<CsvConceptMapping, CsvConceptMapping>chunk(batchProperties.getCommitInterval())
				.reader(getCsvConceptMappingReader(""))
				.processor(customBeanValidator.validator())
				.writer(csvConceptMappingWriter())
				.faultTolerant()
                .skipLimit(Integer.MAX_VALUE)
                .skip(FlatFileParseException.class)
                .skip(ValidationException.class)
                .listener(skipListener)
                .stream(conceptMappingSkippedRecordsWriter(""))
				.build();
	}
	
	@Bean
	public JdbcPagingItemReader<CsvConceptMapping> i2b2ConceptMappingReader() throws Exception {
		SqlPagingQueryProviderFactoryBean pagingQueryProvider = new SqlPagingQueryProviderFactoryBean();
		pagingQueryProvider.setSelectClause("SELECT *");
		pagingQueryProvider.setFromClause("from concept_mapping");
		Map<String, Order> sortKeys = new HashMap<>();
		sortKeys.put("id", Order.ASCENDING);
		pagingQueryProvider.setSortKeys(sortKeys);
		return CustomJdbcItemReader.getPagingReader(pagingQueryProvider, postgresqlDataSource,
				batchProperties.getJdbcCursorReaderFetchSize(), i2b2ConceptMappingRowMapper);
	}
	
	private ConceptMappingProcessor i2b2ConceptMappingProcessor() {
		return new ConceptMappingProcessor(i2b2DemoDataSource, i2b2Properties);
	}
	
	@Bean
	public ItemWriter<I2b2Concept> i2b2CompositeConceptMappingWriter() {
		I2b2ConceptMappingCompositeWriter i2b2ConceptWriter = new I2b2ConceptMappingCompositeWriter();
		i2b2ConceptWriter.setMetaDataWriter(i2b2ConceptMetaDataWriter());
		i2b2ConceptWriter.setConceptDimensionWriter(i2b2ConceptConceptDimensionWriter());
		return i2b2ConceptWriter;
	}
	
	@Bean
	public Step importI2b2ConceptMappingStep() throws Exception {
		return i2b2StepBuilderFactory.get("importI2b2ConceptMappingStep")
				.<CsvConceptMapping, CsvConceptMapping>chunk(batchProperties.getCommitInterval())
				.reader(i2b2ConceptMappingReader())
				.processor(i2b2ConceptMappingProcessor())
				.writer(i2b2CompositeConceptMappingWriter())
				.faultTolerant()
				.skip(DuplicateKeyException.class)
				.skipLimit(Integer.MAX_VALUE)
				.build();
	}

	@Bean
	public Job importConceptJob() {
		try {
			return jobBuilderFactory.get("importConceptJob")
					.incrementer(new RunIdIncrementer())
					.listener(listener)
					.flow(preDeleteConceptTaskletStep())
					.next(importCsvConceptStep())
					.next(importI2b2ConceptStep())
					.next(importConceptMappingStep())
					.next(importI2b2ConceptMappingStep())
					.next(postDeleteConceptTaskletStep())
					.next(deleteConceptFileTaskletStep())
					.end()
					.build();
		} catch (Exception e) {
			log.error("Error while executing importConceptJob : {}", e);
			return null;
		}
	}
}