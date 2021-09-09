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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;

import io.igia.i2b2.cdi.common.config.AppBatchProperties;
import io.igia.i2b2.cdi.common.config.DataSourceMetaInfoConfig;
import io.igia.i2b2.cdi.common.config.I2b2SchemaProperties;
import io.igia.i2b2.cdi.common.domain.ConceptFileName;
import io.igia.i2b2.cdi.common.domain.ConceptJobStepName;
import io.igia.i2b2.cdi.common.domain.DerivedConceptDefinition;
import io.igia.i2b2.cdi.common.reader.CustomFlatFileReader;
import io.igia.i2b2.cdi.common.reader.CustomJdbcItemReader;
import io.igia.i2b2.cdi.common.util.CsvHeaders;
import io.igia.i2b2.cdi.common.util.CustomBeanValidator;
import io.igia.i2b2.cdi.common.writer.CustomFlatFileWriter;
import io.igia.i2b2.cdi.common.writer.CustomJdbcBatchItemWriter;
import io.igia.i2b2.cdi.conceptimport.joblistener.DerivedConceptSkipListener;
import io.igia.i2b2.cdi.conceptimport.joblistener.I2b2DerivedConceptSkipListener;
import io.igia.i2b2.cdi.conceptimport.rowmapper.DerivedConceptDefinitionRowMapper;
import io.igia.i2b2.cdi.conceptimport.writer.DerivedConceptCompositeWriter;

@Configuration
public class ImportDerivedConceptsStep {

    @Autowired
    public StepBuilderFactory i2b2StepBuilderFactory;

    @Autowired
    @Qualifier("postgresqlDataSource")
    DataSource postgresqlDataSource;

    @Autowired
    @Qualifier("i2b2DemoDataSource")
    DataSource i2b2DemoDataSource;

    @Autowired
    AppBatchProperties batchProperties;

    @Autowired
    CustomBeanValidator customBeanValidator;

    @Autowired
    @Qualifier("i2b2SchemaFields")
    private I2b2SchemaProperties i2b2Properties;

    @Autowired
    DataSourceMetaInfoConfig dataSourceMetaInfoConfig;

    @Bean
    @StepScope
    public FlatFileItemReader<DerivedConceptDefinition> derivedConceptDefinitionCsvReader(
            @Value("#{jobParameters['LOCAL_DATA_DIRECTORY_PATH']}") String filePath) {
        return CustomFlatFileReader.getReader("csvderivedconceptreader",
                filePath + ConceptFileName.DERIVED_CONCEPTS.getFileName(), CsvHeaders.getDerivedFactDefinitionHeaders(),
                CsvHeaders.getDerivedFactDefinitionColumnNumbers(), DerivedConceptDefinition.class);
    }

    @Bean
    public JdbcBatchItemWriter<DerivedConceptDefinition> derivedConceptDefinitionWriter() {
        String sql = "INSERT INTO derived_concept_definition (concept_path, parent_concept_path, description, sql_query, unit_cd) "
                + "values (:conceptPath, :dependsOn, :description, :sqlQuery, :unitCd);";
        return CustomJdbcBatchItemWriter.getWriter(sql, postgresqlDataSource);
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<DerivedConceptDefinition> derivedFactsSkippedRecordsWriter(
            @Value("#{jobParameters['ERROR_RECORDS_DIRECTORY_PATH']}") String filePath) {
        String fileName = ConceptFileName.DERIVED_CONCEPTS_SKIPPED_RECORDS.getFileName();
        return CustomFlatFileWriter.getWriter("derivedFactsSkippedRecordsWriter", filePath + fileName,
                CsvHeaders.getDerivedFactDefinitionHeaders());
    }

    public Step importCsvDerivedConceptDefinitionStep() {
        DerivedConceptSkipListener skipListener = new DerivedConceptSkipListener(derivedFactsSkippedRecordsWriter(""));

        return i2b2StepBuilderFactory.get(ConceptJobStepName.IMPORT_CSV_DERIVED_CONCEPT_STEP)
                .<DerivedConceptDefinition, DerivedConceptDefinition> chunk(batchProperties.getCommitInterval())
                .reader(derivedConceptDefinitionCsvReader(""))
                .processor(customBeanValidator.validator())
                .writer(derivedConceptDefinitionWriter())
                .faultTolerant()
                .skip(FlatFileParseException.class)
                .skip(ValidationException.class)
                .skipLimit(Integer.MAX_VALUE)
                .listener(skipListener)
                .stream(derivedFactsSkippedRecordsWriter(""))
                .build();
    }

    @Bean
    public JdbcPagingItemReader<DerivedConceptDefinition> i2b2DerivedConceptDefinitionReader() throws Exception {
        SqlPagingQueryProviderFactoryBean pagingQueryProvider = new SqlPagingQueryProviderFactoryBean();
        pagingQueryProvider.setSelectClause("SELECT *");
        pagingQueryProvider.setFromClause("from derived_concept_definition");
        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.ASCENDING);
        pagingQueryProvider.setSortKeys(sortKeys);
        return CustomJdbcItemReader.getPagingReader(pagingQueryProvider, postgresqlDataSource,
                batchProperties.getJdbcCursorReaderFetchSize(), new DerivedConceptDefinitionRowMapper(i2b2Properties));
    }

    @Bean
    public JdbcBatchItemWriter<DerivedConceptDefinition> i2b2DerivedConceptDefinitionWriter() {
        String sql = "INSERT INTO derived_concept_definition (concept_path, description, sql_query, unit_cd, update_date) "
                + "values (:conceptPath, :description, :sqlQuery, :unitCd, :updateDate);";
        return CustomJdbcBatchItemWriter.getWriter(sql, i2b2DemoDataSource);
    }

    @Bean
    public ItemWriter<DerivedConceptDefinition> i2b2DerivedConceptDefinitionCompositeWriter() {
        return new DerivedConceptCompositeWriter(i2b2DemoDataSource, dataSourceMetaInfoConfig, i2b2Properties,
                i2b2DerivedConceptDefinitionWriter());
    }

    public Step importI2b2DerivedConceptDefinitionStep() throws Exception {
        I2b2DerivedConceptSkipListener skipListener = new I2b2DerivedConceptSkipListener(derivedFactsSkippedRecordsWriter(""));

        return i2b2StepBuilderFactory.get(ConceptJobStepName.IMPORT_I2B2_DERIVED_CONCEPT_STEP)
                .<DerivedConceptDefinition, DerivedConceptDefinition> chunk(batchProperties.getCommitInterval())
                .reader(i2b2DerivedConceptDefinitionReader())
                .writer(i2b2DerivedConceptDefinitionCompositeWriter())
                .faultTolerant()
                .skip(DuplicateKeyException.class)
                .skip(DataIntegrityViolationException.class)
                .skipLimit(Integer.MAX_VALUE)
                .listener(skipListener)
                .stream(derivedFactsSkippedRecordsWriter(""))
                .build();
    }
}
