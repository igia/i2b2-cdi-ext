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
package io.igia.i2b2.cdi.dataimport.joblistener;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.igia.i2b2.cdi.common.JobListener.JobCompletionNotification;
import io.igia.i2b2.cdi.common.config.ErrorLogProperties;
import io.igia.i2b2.cdi.common.domain.AppJobContextProperties;
import io.igia.i2b2.cdi.common.util.AppJobContext;
import io.igia.i2b2.cdi.common.util.BatchJobSummary;
import io.igia.i2b2.cdi.common.util.FileHelper;
import io.igia.i2b2.cdi.dataimport.messagehandler.SftpOutboundMessageHandlerData;
import io.igia.i2b2.cdi.derivedfact.joblistener.DerivedFactTriggerListener;

@Component
public class DataJobCompletionNotificationListener extends JobExecutionListenerSupport {

	private static final Logger log = LoggerFactory.getLogger(DataJobCompletionNotificationListener.class);
	
	@Autowired
	SftpOutboundMessageHandlerData sftpOutboundMessageHandler;
	
	@Autowired
	ErrorLogProperties errorLogProperties;
	
	@Autowired
	DerivedFactTriggerListener derivedFactTriggerListener;

	private DataJobCompletionNotificationListener() {
	}

	/**
	 * This is override method of the JobExecutionListenerSupport class. This is
	 * listener method which executes after the job is completed with success or
	 * fail.
	 * 
	 * @param jobExecution
	 *            this is required for fetching the batch status, job parameters
	 *            etc.
	 */
	@Override
	public void afterJob(JobExecution jobExecution) {
		JobCompletionNotification.afterJob(jobExecution);
		
		// Print summary of concept batch import job
        log.info(BatchJobSummary.getDataJobSummary(jobExecution));
		
		// Get metadata properties from previous job context and set to new job context
		AppJobContextProperties appJobContextProperties = new AppJobContext().getJobContextPropertiesFromJobParameters(jobExecution);
		
		//Send errors records zip file to the sftp
		String zipDir = FileHelper.toZipFolder(appJobContextProperties.getErrorRecordsDirectoryPath(), errorLogProperties.getErrorLogFileExtension());
		boolean sendFile = sftpOutboundMessageHandler.sftpOutboundChannelData(zipDir);
		if(sendFile){
			//Delete .txt appended file from local dir
			File deleteDir = new File(zipDir).getParentFile();
			try {
				FileUtils.deleteDirectory(deleteDir);
			} catch (IOException e) {
				log.error("Error while deleting directory : {}", e);
			}
		}
		
		// On success of data import job, Trigger derived fact calculation module
		if (jobExecution.getStatus() == BatchStatus.COMPLETED) {
			derivedFactTriggerListener.triggerDerivedFactModule(appJobContextProperties);
		}
	}

	/**
	 * This is override method of the JobExecutionListenerSupport class. This is
	 * listener method which executes before the job execution is started.
	 * 
	 * @param jobExecution
	 *            this is required for fetching the job parameters which will be
	 *            used to get the file path.
	 */
	@Override
	public void beforeJob(JobExecution jobExecution) {
		JobParameters parameters = jobExecution.getJobParameters();
		log.info("Data batch job parameters : {}", parameters);
	}
}
