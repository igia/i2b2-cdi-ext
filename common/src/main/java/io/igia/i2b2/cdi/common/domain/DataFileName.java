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
package io.igia.i2b2.cdi.common.domain;

import java.util.HashMap;
import java.util.Map;

public enum DataFileName {

	PATIENT_DIMENSIONS("patient_dimensions.csv"),
	PATIENT_DIMENSIONS_SKIPPED_RECORDS("patient_dimensions_skippedrecords.csv"),
	VISIT_DIMENSIONS("visit_dimensions.csv"),
	VISIT_DIMENSIONS_SKIPPED_RECORDS("visit_dimensions_skippedrecords.csv"),
	PROVIDER_DIMENSIONS("provider_dimensions.csv"),
	PROVIDER_DIMENSIONS_SKIPPED_RECORDS("provider_dimensions_skippedrecords.csv"),
	OBSERVATION_FACTS("observation_facts.csv"),
	OBSERVATION_FACTS_SKIPPED_RECORDS("observation_facts_skippedrecords.csv");

	private String fileName;
	private static Map<String, Boolean> map;

	DataFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}
	
	public static Map<String, Boolean> getDataFileMap() {
		if(map == null) {
			map = new HashMap<>();
			map.put(PATIENT_DIMENSIONS.getFileName(), false);
			map.put(VISIT_DIMENSIONS.getFileName(), false);
			map.put(PROVIDER_DIMENSIONS.getFileName(), false);
			map.put(OBSERVATION_FACTS.getFileName(), false);
		}
		return map;
	}
}