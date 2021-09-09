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
package io.igia.i2b2.cdi.common.reader;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.springframework.batch.core.annotation.BeforeChunk;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.file.FlatFileItemReader;

import io.igia.i2b2.cdi.common.cache.PatientMappingCache;
import io.igia.i2b2.cdi.common.domain.CsvEncounter;

public class EncounterFlatFileReader implements ItemReader<CsvEncounter>, ItemStream {

	private FlatFileItemReader<CsvEncounter> reader;
	private PatientMappingCache patientMappingCache;
	private Map<String, Boolean> previouslySeen = new HashMap<>();

	public void setReader(String readerName, String filePath, String[] headers, int[] includedFileds,
			PatientMappingCache patientMappingCache) {
		this.reader = CustomFlatFileReader.getReader(readerName, filePath, headers, includedFileds, CsvEncounter.class);
		this.patientMappingCache = patientMappingCache;
	}

	@BeforeChunk
	public void beforeChunk(ChunkContext context) {
		previouslySeen.clear();
	}

	@Override
	public CsvEncounter read() throws Exception {

		CsvEncounter item = null;
		Timestamp t = new Timestamp(System.currentTimeMillis());

		while (true) {
			item = reader.read();

			if (item == null) {
				break;
			}

			if (!patientMappingCache.containsPatientID(item.getPatientID())) {
				continue;
			}

			if (previouslySeen.containsKey(item.getEncounterID())) {
				continue;
			}

			previouslySeen.put(item.getEncounterID(), true);

			item.setUpdateDate(t);
			item.setPatientNum(patientMappingCache.getData(item.getPatientID()));
			break;
		}
		return item;
	}

	@Override
	public void open(ExecutionContext executionContext) {
		reader.open(executionContext);
	}

	@Override
	public void update(ExecutionContext executionContext) {
		reader.update(executionContext);
	}

	@Override
	public void close() {
		reader.close();
	}
}