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
package io.igia.i2b2.cdi.common.util;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;

import io.igia.i2b2.cdi.common.domain.AppJobContextProperties;

public class AppJobContext {

	private static final String PROJECT_ID = "PROJECT_ID";
	private static final String SOURCE_SYSTEM_CD = "SOURCE_SYSTEM_CD";
	private static final String LOCAL_DATA_DIRECTORY_PATH = "LOCAL_DATA_DIRECTORY_PATH";
	private static final String ERROR_RECORDS_DIRECTORY_PATH = "ERROR_RECORDS_DIRECTORY_PATH";
	
	public AppJobContextProperties getJobContextPropertiesFromJobParameters(JobExecution jobExecution) {
		AppJobContextProperties appJobContextProperties = new AppJobContextProperties();
		JobParameters jobParameters = jobExecution.getJobParameters();
		appJobContextProperties.setSourceSystemCd(jobParameters.getString(SOURCE_SYSTEM_CD));
		appJobContextProperties.setProjectId(jobParameters.getString(PROJECT_ID));
		appJobContextProperties.setLocalDataDirectoryPath(jobParameters.getString(LOCAL_DATA_DIRECTORY_PATH));
		appJobContextProperties.setErrorRecordsDirectoryPath(jobParameters.getString(ERROR_RECORDS_DIRECTORY_PATH));
		return appJobContextProperties;
	}
	
	public static JobExecution getJobExecutionFromChunkContext(ChunkContext chunkContext) {
		StepContext stepContext = chunkContext.getStepContext();
		StepExecution stepExecution = stepContext.getStepExecution();
		return stepExecution.getJobExecution();
	}
	
	public static JobParameters getJobParameters(ChunkContext chunkContext) {
		StepContext stepContext = chunkContext.getStepContext();
		StepExecution stepExecution = stepContext.getStepExecution();
		JobExecution jobExecution = stepExecution.getJobExecution();
		return jobExecution.getJobParameters();
	}
}