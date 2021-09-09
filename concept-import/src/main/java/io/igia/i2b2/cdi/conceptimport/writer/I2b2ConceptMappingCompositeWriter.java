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
package io.igia.i2b2.cdi.conceptimport.writer;

import java.util.ArrayList;
import java.util.List;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;

import io.igia.i2b2.cdi.common.domain.I2b2Concept;

public class I2b2ConceptMappingCompositeWriter implements ItemWriter<I2b2Concept> {

	private JdbcBatchItemWriter<I2b2Concept> metaDataWriter;
	private JdbcBatchItemWriter<I2b2Concept> conceptDimensionWriter;

	public void setMetaDataWriter(JdbcBatchItemWriter<I2b2Concept> metaDataWriter) {
		this.metaDataWriter = metaDataWriter;
	}

	public void setConceptDimensionWriter(JdbcBatchItemWriter<I2b2Concept> conceptDimensionWriter) {
		this.conceptDimensionWriter = conceptDimensionWriter;
	}

	@Override
	public void write(List<? extends I2b2Concept> items) throws Exception {
		ArrayList<I2b2Concept> metaDataEntries = new ArrayList<>();
		ArrayList<I2b2Concept> conceptDimensionEntries = new ArrayList<>();

		for (I2b2Concept item : items) {
			metaDataEntries.add(item);
			conceptDimensionEntries.add(item);
		}

		metaDataWriter.write(metaDataEntries);
		conceptDimensionWriter.write(conceptDimensionEntries);
	}
}