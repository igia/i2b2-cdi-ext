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
package io.igia.i2b2.cdi.common.tasklet;

import javax.sql.DataSource;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import io.igia.i2b2.cdi.common.domain.DeleteDataQueries;

@Component
public class DeleteConceptTasklet implements Tasklet {

	@Autowired
	@Qualifier("postgresqlDataSource")
	DataSource postgresqlDataSource;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

		JdbcTemplate jdbcTemplate;
		jdbcTemplate = new JdbcTemplate(postgresqlDataSource);

		jdbcTemplate.update(DeleteDataQueries.CONCEPTS.getQuery());
		jdbcTemplate.update(DeleteDataQueries.CONCEPT_MAPPINGS.getQuery());
		return RepeatStatus.FINISHED;
	}
}
