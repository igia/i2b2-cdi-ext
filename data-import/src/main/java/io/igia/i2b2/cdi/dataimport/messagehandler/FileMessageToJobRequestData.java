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
package io.igia.i2b2.cdi.dataimport.messagehandler;

import java.util.ArrayList;
import java.util.Date;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.integration.launch.JobLaunchRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.Transformer;
import org.springframework.messaging.Message;

import io.igia.i2b2.cdi.common.domain.AppJobContextProperties;
import io.igia.i2b2.cdi.common.domain.DataFileName;
import io.igia.i2b2.cdi.common.util.FileHelper;

public class FileMessageToJobRequestData {

	private static final String PROJECT_ID = "PROJECT_ID";
	private static final String SOURCE_SYSTEM_CD = "SOURCE_SYSTEM_CD";
	private static final String LOCAL_DATA_DIRECTORY_PATH = "LOCAL_DATA_DIRECTORY_PATH";
	private static final String ERROR_RECORDS_DIRECTORY_PATH = "ERROR_RECORDS_DIRECTORY_PATH";

	@Autowired
	private Job importDataJob;

	/**
	 * This method is used to transfer the message with job parameters.
	 * 
	 * @param message
	 *            this is the path of the file from where the batch wil read the
	 *            data.
	 * @return the message to passed to the Job Launch Gateway.
	 */

	@Transformer
	public JobLaunchRequest toRequest(Message<ArrayList<Object>> message) {
		JobLaunchRequest jobLaunchRequest = null;
		JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();

		// Unpack zip file and delete zip after successful unpack
		String zipDirectoryPath = FileHelper.unpack(message.getPayload());
		
		// Derive job properties from zip file path
		AppJobContextProperties properties = FileHelper.getJobContextProperties(zipDirectoryPath);
		
		// Create error records directory
		FileHelper.createErrorRecordsDirectory(properties.getErrorRecordsDirectoryPath());
		
		// Create missing files for data
		FileHelper.createMissingFiles(properties.getLocalDataDirectoryPath(), DataFileName.getDataFileMap());

		jobParametersBuilder.addString(LOCAL_DATA_DIRECTORY_PATH, properties.getLocalDataDirectoryPath());
		jobParametersBuilder.addString(PROJECT_ID, properties.getProjectId());
		jobParametersBuilder.addString(SOURCE_SYSTEM_CD, properties.getSourceSystemCd());
		jobParametersBuilder.addString(ERROR_RECORDS_DIRECTORY_PATH, properties.getErrorRecordsDirectoryPath());
		jobParametersBuilder.addDate("jobStartDateTime", new Date());
		jobLaunchRequest = new JobLaunchRequest(importDataJob, jobParametersBuilder.toJobParameters());
		return jobLaunchRequest;
	}
}