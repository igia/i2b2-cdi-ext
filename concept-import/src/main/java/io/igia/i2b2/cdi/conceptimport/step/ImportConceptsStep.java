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
import io.igia.i2b2.cdi.common.config.DataSourceMetaInfoConfig;
import io.igia.i2b2.cdi.common.domain.ConceptFileName;
import io.igia.i2b2.cdi.common.domain.ConceptJobStepName;
import io.igia.i2b2.cdi.common.domain.CsvConcept;
import io.igia.i2b2.cdi.common.domain.I2b2Concept;
import io.igia.i2b2.cdi.common.reader.CustomFlatFileReader;
import io.igia.i2b2.cdi.common.reader.CustomJdbcItemReader;
import io.igia.i2b2.cdi.common.util.CsvHeaders;
import io.igia.i2b2.cdi.common.util.CustomBeanValidator;
import io.igia.i2b2.cdi.common.writer.CustomFlatFileWriter;
import io.igia.i2b2.cdi.common.writer.CustomJdbcBatchItemWriter;
import io.igia.i2b2.cdi.conceptimport.joblistener.ConceptJobCompletionNotificationListener;
import io.igia.i2b2.cdi.conceptimport.joblistener.ConceptSkipListener;
import io.igia.i2b2.cdi.conceptimport.joblistener.I2b2ConceptSkipListener;
import io.igia.i2b2.cdi.conceptimport.processor.ConceptProcessor;
import io.igia.i2b2.cdi.conceptimport.rowmapper.I2b2ConceptRowMapper;
import io.igia.i2b2.cdi.conceptimport.writer.I2b2ConceptCompositeWriter;

@Configuration
public class ImportConceptsStep {

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
    AppBatchProperties batchProperties;

    @Autowired
    CustomBeanValidator customBeanValidator;

    @Autowired
    DataSourceMetaInfoConfig dataSourceMetaInfoConfig;
    
    // Concept csv import step...
    @Bean
    @StepScope
    public FlatFileItemReader<CsvConcept> getCsvConceptReader(
            @Value("#{jobParameters['LOCAL_DATA_DIRECTORY_PATH']}") String filePath) {
        return CustomFlatFileReader.getReader("csvconceptreader", filePath + ConceptFileName.CONCEPTS.getFileName(),
                CsvHeaders.getConceptsHeaders(), CsvHeaders.getConceptsColumnNumbers(), CsvConcept.class);
    }

    @Bean
    public JdbcBatchItemWriter<CsvConcept> csvConceptWriter() {
        String sql = "INSERT INTO concepts (concept_code, concept_path, metadata_xml, facttable_column, "
                + "table_name, column_name, column_datatype, operator, dimcode, m_applied_path, m_exclusion_cd) "
                + "VALUES (:key, :path, :metaDataXml, :factTableColumn, :tableName, :columnName, "
                + ":columnDataType, :operator, :dimcode, :modifierAppliedPath, :modifierExclusionCd)";
        return CustomJdbcBatchItemWriter.getWriter(sql, postgresqlDataSource);
    }

    @Bean
    @StepScope
    public FlatFileItemWriter<CsvConcept> conceptsSkippedRecordsWriter(
            @Value("#{jobParameters['ERROR_RECORDS_DIRECTORY_PATH']}") String filePath) {
        String fileName = ConceptFileName.CONCEPTS_SKIPPED_RECORDS.getFileName();
        return CustomFlatFileWriter.getWriter("conceptsSkippedRecordsWriter", filePath + fileName,
                CsvHeaders.getConceptsErrorRecordHeaders());
    }

    @Bean
    public Step importCsvConceptStep() {
        ConceptSkipListener skipListener = new ConceptSkipListener(conceptsSkippedRecordsWriter(""));

        return stepBuilderFactory.get(ConceptJobStepName.IMPORT_CSV_CONCEPT_STEP)
                .<CsvConcept, CsvConcept> chunk(batchProperties.getCommitInterval())
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

    // Concept i2b2 import step...
    @Bean
    public JdbcPagingItemReader<I2b2Concept> i2b2ConceptReader() throws Exception {
        SqlPagingQueryProviderFactoryBean pagingQueryProvider = new SqlPagingQueryProviderFactoryBean();
        pagingQueryProvider.setSelectClause("SELECT *");
        pagingQueryProvider.setFromClause("from concepts");
        Map<String, Order> sortKeys = new HashMap<>();
        sortKeys.put("id", Order.ASCENDING);
        pagingQueryProvider.setSortKeys(sortKeys);
        return CustomJdbcItemReader.getPagingReader(pagingQueryProvider, postgresqlDataSource,
                batchProperties.getJdbcCursorReaderFetchSize(), i2b2ConceptRowMapper);
    }

    @Bean
    public JdbcBatchItemWriter<I2b2Concept> i2b2ConceptMetaDataWriter() {

        String sql = "INSERT INTO " + dataSourceMetaInfoConfig.getMetadataSchemaName() + "i2b2 "
                + "(c_hlevel,c_fullname,c_name,c_synonym_cd,c_visualattributes,c_metadataxml,"
                + "c_facttablecolumn,c_tablename,c_columnname,c_columndatatype,c_operator,c_dimcode,"
                + "c_tooltip,m_applied_path,update_date, sourcesystem_cd, m_exclusion_cd) " + "SELECT * FROM "
                + "(SELECT :level AS c_hlevel, :fullPath AS c_fullname, :name AS c_name, :synonymCode AS c_synonym_cd, :visualAttributes AS c_visualattributes, :metaDataXML AS c_metadataxml, :factTableColumnName AS c_facttablecolumn, "
                + ":tableName AS c_tablename, :columnName AS c_columnname, :columnDataType AS c_columndatatype, :operator AS c_operator, :dimensionCode AS c_dimcode, :toolTip AS c_tooltip, :appliedPath AS m_applied_path, :timeStamp AS update_date, :sourceSystemCd AS sourcesystem_cd, :modifierExclusionCode AS m_exclusion_cd) AS temp"
                + " WHERE NOT EXISTS (SELECT 1 FROM i2b2 WHERE c_fullname =:fullPath AND m_applied_path =:appliedPath AND m_exclusion_cd = :modifierExclusionCode)";

        return CustomJdbcBatchItemWriter.getWriter(sql, i2b2MetaDataSource);
    }

    @Bean
    public JdbcBatchItemWriter<I2b2Concept> i2b2ConceptMetaDataWriterForModifierExclusion() {

        String sql = "INSERT INTO " + dataSourceMetaInfoConfig.getMetadataSchemaName() + "i2b2 "
                + "(c_hlevel,c_fullname,c_name,c_synonym_cd,c_visualattributes,c_metadataxml,"
                + "c_facttablecolumn,c_tablename,c_columnname,c_columndatatype,c_operator,c_dimcode,"
                + "c_tooltip,m_applied_path,update_date, sourcesystem_cd, m_exclusion_cd) " + "SELECT * FROM "
                + "(SELECT :level AS c_hlevel, :fullPath AS c_fullname, :name AS c_name, :synonymCode AS c_synonym_cd, :visualAttributes AS c_visualattributes, :metaDataXML AS c_metadataxml, :factTableColumnName AS c_facttablecolumn, "
                + ":tableName AS c_tablename, :columnName AS c_columnname, :columnDataType AS c_columndatatype, :operator AS c_operator, :dimensionCode AS c_dimcode, :toolTip AS c_tooltip, :appliedPath AS m_applied_path, :timeStamp AS update_date, :sourceSystemCd AS sourcesystem_cd, :modifierExclusionCode AS m_exclusion_cd) AS temp"
                + " WHERE NOT EXISTS (SELECT 1 FROM i2b2 WHERE c_fullname =:fullPath AND m_applied_path =:appliedPath AND m_exclusion_cd is null)";

        return CustomJdbcBatchItemWriter.getWriter(sql, i2b2MetaDataSource);
    }

    @Bean
    public JdbcBatchItemWriter<I2b2Concept> i2b2ConceptConceptDimensionWriter() {

        String sql = "INSERT INTO " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "concept_dimension "
                + "(concept_path, concept_cd, name_char, update_date, sourcesystem_cd) VALUES"
                + "(:fullPath, :conceptCode, :name, :timeStamp, :sourceSystemCd)";

        return CustomJdbcBatchItemWriter.getWriter(sql, i2b2DemoDataSource);
    }

    @Bean
    public JdbcBatchItemWriter<I2b2Concept> i2b2ModifierDimensionWriter() {

        String sql = "INSERT INTO " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "modifier_dimension "
                + "(modifier_path, modifier_cd, name_char, update_date, sourcesystem_cd) VALUES"
                + "(:fullPath, :conceptCode, :name, :timeStamp, :sourceSystemCd)";

        return CustomJdbcBatchItemWriter.getWriter(sql, i2b2DemoDataSource);
    }

    @Bean
    public JdbcBatchItemWriter<I2b2Concept> i2b2ConceptTableAccesWriter() {

        String sql = "INSERT INTO " + dataSourceMetaInfoConfig.getMetadataSchemaName() + "table_access "
                + "(c_table_cd, c_table_name, c_protected_access, c_hlevel, c_fullname, c_name, c_synonym_cd, c_visualattributes,"
                + "c_facttablecolumn, c_dimtablename, c_columnname, c_columndatatype, c_operator, c_dimcode,"
                + "c_tooltip) " + "SELECT * FROM "
                + "(SELECT :name AS c_table_cd, :cTableName AS c_table_name, :cProtectedAccess AS c_protected_access, "
                + ":level AS c_hlevel, :fullPath AS c_fullname, :name AS c_name, :synonymCode AS c_synonym_cd, :visualAttributes AS c_visualattributes, :factTableColumnName AS c_facttablecolumn, "
                + ":tableName AS c_dimtablename, :columnName AS c_columnname, :columnDataType AS c_columndatatype, :operator AS c_operator, :dimensionCode AS c_dimcode, :toolTip AS c_tooltip) AS temp "
                + "WHERE NOT EXISTS (SELECT 1 FROM table_access WHERE c_fullname=:fullPath)";

        return CustomJdbcBatchItemWriter.getWriter(sql, i2b2MetaDataSource);
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
        i2b2ConceptWriter.setModifierDimensionWriter(i2b2ModifierDimensionWriter());
        i2b2ConceptWriter.setModifierExclusionMetaDataWriter(i2b2ConceptMetaDataWriterForModifierExclusion());
        return i2b2ConceptWriter;
    }

    @Bean
    public Step importI2b2ConceptStep() throws Exception {
        I2b2ConceptSkipListener skipListener = new I2b2ConceptSkipListener(conceptsSkippedRecordsWriter(""));
        return i2b2StepBuilderFactory.get(ConceptJobStepName.IMPORT_I2B2_CONCEPT_STEP)
                .<I2b2Concept, I2b2Concept> chunk(batchProperties.getCommitInterval())
                .reader(i2b2ConceptReader())
                .processor(i2b2ConceptProcessor())
                .writer(i2b2CompositeConceptWriter())
                .faultTolerant()
                .skipLimit(Integer.MAX_VALUE)
                .skip(DuplicateKeyException.class)
                .listener(skipListener)
                .stream(conceptsSkippedRecordsWriter(""))
                .build();
    }
}
