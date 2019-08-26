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
package io.igia.i2b2.cdi.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "spring.batch.job")
@Configuration("batchProperties")
public class AppBatchProperties {

    private Integer commitInterval;
    private Integer jdbcCursorReaderFetchSize;
    private Integer validationFailSkipLimit;

	public Integer getCommitInterval() {
		return commitInterval;
	}
	public void setCommitInterval(Integer commitInterval) {
		this.commitInterval = commitInterval;
	}
	public Integer getJdbcCursorReaderFetchSize() {
		return jdbcCursorReaderFetchSize;
	}
	public void setJdbcCursorReaderFetchSize(Integer jdbcCursorReaderFetchSize) {
		this.jdbcCursorReaderFetchSize = jdbcCursorReaderFetchSize;
	}
	public Integer getValidationFailSkipLimit() {
		return validationFailSkipLimit;
	}
	public void setValidationFailSkipLimit(Integer validationFailSkipLimit) {
		this.validationFailSkipLimit = validationFailSkipLimit;
	}
}