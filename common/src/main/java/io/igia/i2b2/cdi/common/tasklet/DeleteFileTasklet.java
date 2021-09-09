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
package io.igia.i2b2.cdi.common.tasklet;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import io.igia.i2b2.cdi.common.domain.AppJobContextProperties;
import io.igia.i2b2.cdi.common.util.AppJobContext;

@Component
public class DeleteFileTasklet implements Tasklet {

	private static final Logger log = LoggerFactory.getLogger(DeleteFileTasklet.class);

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		JobExecution jobExecution = AppJobContext.getJobExecutionFromChunkContext(chunkContext);
		AppJobContextProperties appJobContextProperties = new AppJobContext()
				.getJobContextPropertiesFromJobParameters(jobExecution);

		try {
			File file = new File(appJobContextProperties.getLocalDataDirectoryPath()).getParentFile();
			FileUtils.deleteDirectory(file);
		} catch (Exception e) {
			log.error("Error while deleting files recursively {}", e);
		}
		return RepeatStatus.FINISHED;
	}
}
