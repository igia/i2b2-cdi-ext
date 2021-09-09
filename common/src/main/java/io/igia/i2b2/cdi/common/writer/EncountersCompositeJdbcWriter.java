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

import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;

import io.igia.i2b2.cdi.common.domain.CsvEncounter;

public class EncountersCompositeJdbcWriter implements ItemWriter<CsvEncounter> {

	private List<JdbcBatchItemWriter<CsvEncounter>> delegates;

	public void setDelegates(List<JdbcBatchItemWriter<CsvEncounter>> writers) {
		this.delegates = writers;
	}

	@Override
	public void write(List<? extends CsvEncounter> items) throws Exception {

		for (JdbcBatchItemWriter<CsvEncounter> writer : delegates) {
			writer.write(items);
		}
	}
}