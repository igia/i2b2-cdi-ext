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
package io.igia.i2b2.cdi.dataimport.config;

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
import io.igia.i2b2.cdi.common.tasklet.ClearCacheTasklet;
import io.igia.i2b2.cdi.common.tasklet.DeleteDataTasklet;
import io.igia.i2b2.cdi.common.tasklet.DeleteFileTasklet;
import io.igia.i2b2.cdi.dataimport.joblistener.DataJobCompletionNotificationListener;
import io.igia.i2b2.cdi.dataimport.step.ImportDemographicsStep;
import io.igia.i2b2.cdi.dataimport.step.ImportEncountersStep;
import io.igia.i2b2.cdi.dataimport.step.ImportObservationFactsStep;
import io.igia.i2b2.cdi.dataimport.step.ImportProvidersStep;

@Configuration
@EnableBatchProcessing
@ComponentScan(basePackageClasses = DefaultBatchConfigurer.class)
@Import({ LaunchConfiguration.class })
public class DataBatchConfiguration {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	DataJobCompletionNotificationListener listener;
		
	@Autowired
	DeleteDataTasklet deleteDataTasklet;
	
	@Autowired
	ImportDemographicsStep importDemographicsStep;
	
	@Autowired
	ImportEncountersStep importEncountersStep;	
	
	@Autowired
	ImportProvidersStep importProvidersStep;

	@Autowired
	ImportObservationFactsStep importObservationFactsStep;
	
	@Autowired
	ClearCacheTasklet clearCacheTasklet;
	
	private static final String DATA_JOB_NAME = "importDataJob";
    private static final String PRE_DELETE_DATA_TASKLET = "preDeleteDataTaskletStep";
    private static final String POST_DELETE_DATA_TASKLET = "postDeleteDataTaskletStep";
    private static final String DELETE_DATA_FILE_TASKLET = "deleteDataFileTaskletStep";
    private static final String CLEAR_CACHE_TASKLET = "clearCacheTaskletStep";
	
	private static final Logger log = LoggerFactory.getLogger(DataBatchConfiguration.class);
	
	@Bean
	public Step preDeleteDataTaskletStep() {
		return stepBuilderFactory.get(PRE_DELETE_DATA_TASKLET)
				.tasklet(deleteDataTasklet)
				.build();
	}
	
	@Bean
	public Step postDeleteDataTaskletStep() {
		return stepBuilderFactory.get(POST_DELETE_DATA_TASKLET)
				.tasklet(deleteDataTasklet)
				.build();
	}
	
	@Bean
	public DeleteFileTasklet deleteDataFileTasklet() {
		return new DeleteFileTasklet();		
	}

	@Bean
	public Step deleteDataFileTaskletStep() {
		return stepBuilderFactory.get(DELETE_DATA_FILE_TASKLET)
				.tasklet(deleteDataFileTasklet())
				.build();
	}
	
	@Bean
	public Step clearCacheTaskletStep() {
		return stepBuilderFactory.get(CLEAR_CACHE_TASKLET)
				.tasklet(clearCacheTasklet)
				.build();
	}
	
	@Bean
	public Job importDataJob() {

		try {
			return jobBuilderFactory
					.get(DATA_JOB_NAME).incrementer(new RunIdIncrementer()).listener(listener)
					.flow(preDeleteDataTaskletStep())
					.next(importDemographicsStep.intermediateDBImportDemographicsStep())
					.next(importDemographicsStep.i2b2ImportDemographicsStep())
					.next(importProvidersStep.intermediateDBImportProvidersStep())
					.next(importProvidersStep.i2b2ImportProviderReferenceStep())
					.next(importEncountersStep.intermediateDBImportEncountersStep())
					.next(importEncountersStep.i2b2ImportEncountersStep())
					.next(importObservationFactsStep.intermediateDBImportObservationFactsStep())
					.next(importObservationFactsStep.i2b2ImportObservationFactsStep())
					.next(postDeleteDataTaskletStep())
					.next(deleteDataFileTaskletStep())
					.next(clearCacheTaskletStep())
					.end()
					.build();
		} catch (Exception e) {
			log.error("Error while executing importDataJob : {}", e);
			return null;
		}
	}
}