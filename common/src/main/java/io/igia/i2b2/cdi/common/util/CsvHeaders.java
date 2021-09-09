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
package io.igia.i2b2.cdi.common.util;

public class CsvHeaders {

	private static final String CSV_HEADER_PATIENT_ID_STR = "patientID";
	private static final String CSV_HEADER_PATIENT_ENCOUNTER_ID_STR = "encounterID";
	private static final String VALIDATION_ERROR_MSG_STR = "validationErrorMessage";
	private static final String CSV_PROVIDER_ID_STR = "providerID";
	private CsvHeaders() {
	}

	public static String[] getDemographicHeaders() {
		return new String[] { CSV_HEADER_PATIENT_ID_STR, "birthDTS", "Gender"};
	}
	
	public static String[] getDemographicErrorRecordHeaders() {
        return new String[] { CSV_HEADER_PATIENT_ID_STR, "birthDTS", "Gender", VALIDATION_ERROR_MSG_STR};
    }
	
	public static int[] getDemographicColumnNumbers() {
		return new int[] { 0, 1, 2 };
	}

	public static String[] getConceptsHeaders() {
		return new String[] { "path", "key", "columnDataType", "metaDataXml", "factTableColumn", "tableName",
				"columnName", "operator", "dimcode", "modifierAppliedPath", "modifierExclusionCd" };
	}
	
	public static String[] getConceptsErrorRecordHeaders() {
        return new String[] { "path", "key", "columnDataType", "metaDataXml", "factTableColumn", "tableName",
                "columnName", "operator", "dimcode", "modifierAppliedPath", "modifierExclusionCd", VALIDATION_ERROR_MSG_STR };
    }
	
	public static int[] getConceptsColumnNumbers() {
		return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
	}
	
	public static String[] getConceptMappingHeaders() {
		return new String[] { "stdCode", "localCode", "localCodeName" };
	}
	
	public static String[] getConceptMappingErrorRecordHeaders() {
        return new String[] { "stdCode", "localCode", "localCodeName", VALIDATION_ERROR_MSG_STR };
    }
	
	public static int[] getConceptMappingColumnNumbers() {
		return new int[] { 0, 1, 2};
	}
	
	public static String[] getDerivedFactDefinitionHeaders() {
		return new String[] { "conceptPath", "dependsOn", "description", "sqlQuery", "unitCd"};
	}
	
	public static int[] getDerivedFactDefinitionColumnNumbers() {
		return new int[] { 0, 1, 2, 3, 4 };
	}

	public static String[] getEncounterHeaders() {
		return new String[] { CSV_HEADER_PATIENT_ENCOUNTER_ID_STR, CSV_HEADER_PATIENT_ID_STR, "startDTS", "endDTS"};
	}
	
	public static String[] getEncounterErrorRecordHeaders() {
        return new String[] { CSV_HEADER_PATIENT_ENCOUNTER_ID_STR, CSV_HEADER_PATIENT_ID_STR, "startDTS", "endDTS"
                , VALIDATION_ERROR_MSG_STR };
    }
	
	public static int[] getEncounterColumnNumbers() {
		return new int[] { 0, 1, 2, 3 };
	}

	public static String[] getProviderReferenceHeaders() {
		return new String[] { CSV_PROVIDER_ID_STR, "providerPath","userNM" };
	}
	
	public static String[] getProviderReferenceErrorRecordHeaders() {
        return new String[] { CSV_PROVIDER_ID_STR, "providerPath","userNM", VALIDATION_ERROR_MSG_STR };
    }
	
	public static int[] getProviderReferenceColumnNumbers() {
		return new int[] { 0, 1, 2 };
	}
	
    public static String[] getObservationFactHeaders() {
        return new String[] { CSV_HEADER_PATIENT_ENCOUNTER_ID_STR, CSV_HEADER_PATIENT_ID_STR, "conceptCD", CSV_PROVIDER_ID_STR,
                "startDTS", "modifierCD", "instanceNum", "value", "unitCD" };
    }

    public static String[] getObservationFactErrorRecordHeaders() {
        return new String[] { CSV_HEADER_PATIENT_ENCOUNTER_ID_STR, CSV_HEADER_PATIENT_ID_STR, "conceptCD", CSV_PROVIDER_ID_STR,
                "startDTS", "modifierCD", "instanceNum", "value", "unitCD" , VALIDATION_ERROR_MSG_STR};
    }
	
	public static int[] getObservationFactColumnNumbers() {
		return new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8 };
	}
}
