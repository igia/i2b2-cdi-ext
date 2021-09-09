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
package io.igia.i2b2.cdi.common.writer;

import javax.sql.DataSource;

import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;

public class CustomJdbcBatchItemWriter {
	private CustomJdbcBatchItemWriter() {
		
	}
	
	public static <T> JdbcBatchItemWriter<T> getWriter(String sql, DataSource dataStore) {
		JdbcBatchItemWriter<T> jdbcBatchItemWriter = new JdbcBatchItemWriter<>();
		jdbcBatchItemWriter
				.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<T>());
		jdbcBatchItemWriter.setSql(sql);
		jdbcBatchItemWriter.setDataSource(dataStore);
		jdbcBatchItemWriter.setAssertUpdates(false);
		return jdbcBatchItemWriter;
	}	
}
