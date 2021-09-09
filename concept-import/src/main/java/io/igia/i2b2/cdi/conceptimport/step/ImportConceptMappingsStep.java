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
package io.igia.i2b2.cdi.conceptimport.step;

import java.util.HashMap;
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
import org.springframework.batch.item.validator.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DuplicateKeyException;

import io.igia.i2b2.cdi.common.config.AppBatchProperties;
import io.igia.i2b2.cdi.common.config.AppIntegrationProperties;
import io.igia.i2b2.cdi.common.config.DataSourceMetaInfoConfig;
import io.igia.i2b2.cdi.common.config.I2b2SchemaProperties;
import io.igia.i2b2.cdi.common.domain.ConceptFileName;
import io.igia.i2b2.cdi.common.domain.ConceptJobStepName;
import io.igia.i2b2.cdi.common.domain.CsvConceptMapping;
import io.igia.i2b2.cdi.common.domain.I2b2Concept;
import io.igia.i2b2.cdi.common.exception.ConceptNotFoundException;
import io.igia.i2b2.cdi.common.reader.CustomFlatFileReader;
import io.igia.i2b2.cdi.common.reader.CustomJdbcItemReader;
import io.igia.i2b2.cdi.common.tasklet.DeleteConceptTasklet;
import io.igia.i2b2.cdi.common.util.CsvHeaders;
import io.igia.i2b2.cdi.common.util.CustomBeanValidator;
import io.igia.i2b2.cdi.common.writer.CustomFlatFileWriter;
import io.igia.i2b2.cdi.common.writer.CustomJdbcBatchItemWriter;
import io.igia.i2b2.cdi.conceptimport.joblistener.ConceptJobCompletionNotificationListener;
import io.igia.i2b2.cdi.conceptimport.joblistener.ConceptMappingSkipListener;
import io.igia.i2b2.cdi.conceptimport.joblistener.I2b2ConceptMappingSkipListener;
import io.igia.i2b2.cdi.conceptimport.processor.ConceptMappingProcessor;
import io.igia.i2b2.cdi.conceptimport.rowmapper.I2b2ConceptMappingRowMapper;
import io.igia.i2b2.cdi.conceptimport.rowmapper.I2b2ConceptRowMapper;
import io.igia.i2b2.cdi.conceptimport.writer.I2b2ConceptMappingCompositeWriter;

@Configuration
public class ImportConceptMappingsStep {

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
    @Qualifier("i2b2MetaDataSource")
    DataSource i2b2MetaDataSource;

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
    @Qualifier("i2b2SchemaFields")
    private I2b2SchemaProperties i2b2Properties;

    @Autowired
    DataSourceMetaInfoConfig dataSourceMetaInfoConfig;
    
    @Autowired
    ImportConceptsStep importConceptsStep;
    
    @Bean
    @StepScope
    public FlatFileItemReader<CsvConceptMapping> getCsvConceptMappingReader(
            @Value("#{jobParameters['LOCAL_DATA_DIRECTORY_PATH']}") String filePath) {
        return CustomFlatFileReader.getReader("csvconceptmappingreader",
                filePath + ConceptFileName.CONCEPT_MAPPINGS.getFileName(), CsvHeaders.getConceptMappingHeaders(),
                CsvHeaders.getConceptMappingColumnNumbers(), CsvConceptMapping.class);
    }

    @Bean
    public JdbcBatchItemWriter<CsvConceptMapping> csvConceptMappingWriter() {
        String sql = "INSERT INTO concept_mapping (std_cd, local_cd, local_cd_name) "
                + "VALUES (:stdCode, :localCode, :localCodeName)";
        return CustomJdbcBatchItemWriter.getWriter(sql, postgresqlDataSource);
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<CsvConceptMapping> conceptMappingSkippedRecordsWriter(
            @Value("#{jobParameters['ERROR_RECORDS_DIRECTORY_PATH']}") String filePath) {
        String fileName = ConceptFileName.CONCEPT_MAPPINGS_SKIPPED_RECORDS.getFileName();
        return CustomFlatFileWriter.getWriter("conceptMappingSkippedRecordsWriter", filePath + fileName,
                CsvHeaders.getConceptMappingErrorRecordHeaders());
    }

    public Step importConceptMappingStep() {
        ConceptMappingSkipListener skipListener = new ConceptMappingSkipListener(conceptMappingSkippedRecordsWriter(""));

        return i2b2StepBuilderFactory.get(ConceptJobStepName.IMPORT_CSV_CONCEPT_MAPPING_STEP)
                .<CsvConceptMapping, CsvConceptMapping> chunk(batchProperties.getCommitInterval())
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
        return new ConceptMappingProcessor(i2b2DemoDataSource, i2b2MetaDataSource, i2b2Properties,
                dataSourceMetaInfoConfig);
    }

    @Bean
    public ItemWriter<I2b2Concept> i2b2CompositeConceptMappingWriter() {
        I2b2ConceptMappingCompositeWriter i2b2ConceptWriter = new I2b2ConceptMappingCompositeWriter();
        i2b2ConceptWriter.setMetaDataWriter(importConceptsStep.i2b2ConceptMetaDataWriter());
        i2b2ConceptWriter.setConceptDimensionWriter(importConceptsStep.i2b2ConceptConceptDimensionWriter());
        return i2b2ConceptWriter;
    }

    @Bean
    public Step importI2b2ConceptMappingStep() throws Exception {
        I2b2ConceptMappingSkipListener skipListener = new I2b2ConceptMappingSkipListener(conceptMappingSkippedRecordsWriter(""));
        return i2b2StepBuilderFactory.get(ConceptJobStepName.IMPORT_I2B2_CONCEPT_MAPPING_STEP)
                .<CsvConceptMapping, CsvConceptMapping> chunk(batchProperties.getCommitInterval())
                .reader(i2b2ConceptMappingReader())
                .processor(i2b2ConceptMappingProcessor())
                .writer(i2b2CompositeConceptMappingWriter())
                .faultTolerant()
                .skip(DuplicateKeyException.class)
                .skip(ConceptNotFoundException.class)
                .skipLimit(Integer.MAX_VALUE)
                .listener(skipListener)
                .stream(conceptMappingSkippedRecordsWriter(""))
                .build();
    }
}
