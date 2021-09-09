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
package io.igia.i2b2.cdi.common.JobListener;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

@Component
public class JobCompletionNotification {

	JobCompletionNotification () {
		
	}
	private static final Logger log = LoggerFactory.getLogger(JobCompletionNotification.class);

	/**
	 * This is a helper method to log job statistics
	 * 
	 * @param jobExecution
	 *            this is required for fetching the batch status, job parameters
	 *            etc.
	 */
	public static void afterJob(JobExecution jobExecution) {
		//log job statistics
		try {
			if(jobExecution.getStatus() == BatchStatus.COMPLETED){
				//job success
				log.info("JOB FINISHED! : {}", jobExecution);
			}
			else if(jobExecution.getStatus() == BatchStatus.FAILED){
				//job failure
				log.error("JOB FAILED! : {}", jobExecution);
			}
			Iterator<StepExecution> iteratorStepExecution = jobExecution.getStepExecutions().iterator();
			while(iteratorStepExecution.hasNext()) {
				StepExecution stepExecution = iteratorStepExecution.next();
				log.info("{}", stepExecution);
			}
		} catch (Exception e) {
			log.error("Exception reading job statistics! : {}", jobExecution );
			log.error("{}", e);
		}
	}
}
