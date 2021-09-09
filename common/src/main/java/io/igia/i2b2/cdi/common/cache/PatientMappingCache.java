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
package io.igia.i2b2.cdi.common.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class PatientMappingCache {
	private Map<String, Integer> patientMapping = new ConcurrentHashMap<>();
	
	public void putData(String patientIDE, int patientNum) {
		patientMapping.put(patientIDE, patientNum);
	}
	
	public int getData(String patientID) {
		return patientMapping.get(patientID);
	}
	
	public boolean containsPatientID(String patientID) {
		return patientMapping.containsKey(patientID);
	}
	
	public void clearCache() {
		patientMapping.clear();
	}
}
