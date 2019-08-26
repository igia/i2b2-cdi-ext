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

public enum ConceptFileName {

	CONCEPTS("concepts.csv"),
	CONCEPTS_SKIPPED_RECORDS("concepts_skippedrecords.csv"),
	CONCEPT_MAPPINGS("concept_mappings.csv"),
	CONCEPT_MAPPINGS_SKIPPED_RECORDS("conceptmapping_skippedrecords.csv");

	private String fileName;
	private static Map<String, Boolean> map;

	ConceptFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}

	public static Map<String, Boolean> getConceptFileMap() {
		if (map == null) {
			map = new HashMap<>();
			map.put(CONCEPTS.getFileName(), false);
			map.put(CONCEPT_MAPPINGS.getFileName(), false);
		}
		return map;
	}
}