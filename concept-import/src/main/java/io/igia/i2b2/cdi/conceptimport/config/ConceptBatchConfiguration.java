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
package io.igia.i2b2.cdi.conceptimport.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import io.igia.i2b2.cdi.common.config.LaunchConfiguration;
import io.igia.i2b2.cdi.common.tasklet.DeleteConceptTasklet;
import io.igia.i2b2.cdi.common.tasklet.DeleteFileTasklet;
import io.igia.i2b2.cdi.conceptimport.joblistener.ConceptJobCompletionNotificationListener;
import io.igia.i2b2.cdi.conceptimport.step.ImportConceptMappingsStep;
import io.igia.i2b2.cdi.conceptimport.step.ImportConceptsStep;
import io.igia.i2b2.cdi.conceptimport.step.ImportDerivedConceptsStep;

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
    ConceptJobCompletionNotificationListener listener;

    @Autowired
    DeleteConceptTasklet deleteConceptTasklet;
   
    @Autowired
    ImportConceptsStep importConceptsStep;
    
    @Autowired
    ImportDerivedConceptsStep importDerivedConceptsStep;
    
    @Autowired
    ImportConceptMappingsStep importConceptMappingsStep;

    private static final Logger log = LoggerFactory.getLogger(ConceptBatchConfiguration.class);
    private static final String CONCEPT_JOB_NAME = "importConceptJob";
    private static final String PRE_DELETE_CONCEPT_TASKLET = "preDeleteConceptTaskletStep";
    private static final String POST_DELETE_CONCEPT_TASKLET = "postDeleteConceptTaskletStep";
    private static final String DELETE_CONCEPT_FILE_TASKLET = "deleteConceptFileTaskletStep";

    @Bean
    public Step preDeleteConceptTaskletStep() {
        return stepBuilderFactory.get(PRE_DELETE_CONCEPT_TASKLET)
                .tasklet(deleteConceptTasklet)
                .build();
    }

    @Bean
    public Step postDeleteConceptTaskletStep() {
        return stepBuilderFactory.get(POST_DELETE_CONCEPT_TASKLET)
                .tasklet(deleteConceptTasklet)
                .build();
    }

    @Bean
    public DeleteFileTasklet deleteConceptFileTasklet() {
        return new DeleteFileTasklet();
    }

    @Bean
    public Step deleteConceptFileTaskletStep() {
        return stepBuilderFactory.get(DELETE_CONCEPT_FILE_TASKLET)
                .tasklet(deleteConceptFileTasklet())
                .build();
    }

    @Bean
    public Job importConceptJob() {
        try {
            return jobBuilderFactory.get(CONCEPT_JOB_NAME)
	            .incrementer(new RunIdIncrementer())
	            .listener(listener)
	            .flow(preDeleteConceptTaskletStep())
	            .next(importConceptsStep.importCsvConceptStep())
	            .next(importConceptsStep.importI2b2ConceptStep())
	            .next(importDerivedConceptsStep.importCsvDerivedConceptDefinitionStep())
	            .next(importDerivedConceptsStep.importI2b2DerivedConceptDefinitionStep())
	            .next(importConceptMappingsStep.importConceptMappingStep())
	            .next(importConceptMappingsStep.importI2b2ConceptMappingStep())
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