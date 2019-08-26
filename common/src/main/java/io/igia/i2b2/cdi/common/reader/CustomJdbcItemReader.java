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
package io.igia.i2b2.cdi.common.reader;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.jdbc.core.RowMapper;

public class CustomJdbcItemReader {
	private CustomJdbcItemReader() {
		
	}
	
	private static final Logger log = LoggerFactory.getLogger(CustomJdbcItemReader.class);
	
	public static <T> JdbcPagingItemReader<T> getPagingReader(SqlPagingQueryProviderFactoryBean pagingQueryProvider, DataSource dataSource, Integer fetchSize, RowMapper<T> rowMapper) throws Exception {
		JdbcPagingItemReader<T> reader = new JdbcPagingItemReader<>();
		try {
			reader.setDataSource(dataSource);
			pagingQueryProvider.setDataSource(dataSource);
			reader.setPageSize(fetchSize);
			reader.setQueryProvider(pagingQueryProvider.getObject());
			reader.setRowMapper(rowMapper);
			reader.afterPropertiesSet();
			reader.setSaveState(true);
		} catch (Exception e) {
			log.error("Error while setting JdbcPagingItemReader object : {}", e);
			throw e;
		}
		return reader;
	}
	
	public static <T> JdbcCursorItemReader<T> getReader(String sql, DataSource dataSource, Integer fetchSize, RowMapper<T> rowMapper) {
		JdbcCursorItemReader<T> reader = new JdbcCursorItemReader<>();
		reader.setDataSource(dataSource);
		reader.setSql(sql);
		reader.setFetchSize(fetchSize);
		reader.setRowMapper(rowMapper);
		return reader;
	}
}