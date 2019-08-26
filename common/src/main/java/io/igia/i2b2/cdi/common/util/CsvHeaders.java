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
package io.igia.i2b2.cdi.common.util;

public class CsvHeaders {

	private static final String CSV_HEADER_PATIENT_ID_STR = "patientID";
	private static final String CSV_HEADER_PATIENT_ENCOUNTER_ID_STR = "encounterID";
	private CsvHeaders() {
	}

	public static String[] getDemographicHeaders() {
		return new String[] { CSV_HEADER_PATIENT_ID_STR, "birthDTS", "Gender"};
	}
	
	public static int[] getDemographicColumnNumbers() {
		return new int[] { 0, 1, 2 };
	}

	public static String[] getConceptsHeaders() {
		return new String[] { "path", "key", "columnDataType", "metaDataXml", "factTableColumn", "tableName",
				"columnName", "operator", "dimcode" };
	}
	
	public static int[] getConceptsColumnNumbers() {
		return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8};
	}
	
	public static String[] getConceptMappingHeaders() {
		return new String[] { "stdCode", "localCode", "localCodeName" };
	}
	
	public static int[] getConceptMappingColumnNumbers() {
		return new int[] { 0, 1, 2};
	}

	public static String[] getEncounterHeaders() {
		return new String[] { CSV_HEADER_PATIENT_ENCOUNTER_ID_STR, CSV_HEADER_PATIENT_ID_STR, "startDTS", "endDTS"};
	}
	
	public static int[] getEncounterColumnNumbers() {
		return new int[] { 0, 1, 2, 3 };
	}

	public static String[] getProviderReferenceHeaders() {
		return new String[] { "providerID", "providerPath","userNM" };
	}
	
	public static int[] getProviderReferenceColumnNumbers() {
		return new int[] { 0, 1, 2 };
	}
	
	public static String[] getObservationFactHeaders() {
		return new String[] { CSV_HEADER_PATIENT_ENCOUNTER_ID_STR, CSV_HEADER_PATIENT_ID_STR, "conceptCD", "providerID", "startDTS", "modifierCD", "instanceNum", "value", "unitCD"};
	}
	
	public static int[] getObservationFactColumnNumbers() {
		return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
	}
}
