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

public class TableFields {

	private static final String LOCAL_DB_PATIENT_ID_STR = "patient_ide";
	private static final String LOCAL_DB_PATIENT_ENCOUNTER_ID_STR = "encounter_ide";
	
	private TableFields() {
	}

	public static String getDemographicsTableFields() {

		String[] strArr = new String[] { LOCAL_DB_PATIENT_ID_STR, "birth_dts", "gender"};

		return String.join(",", strArr);
	}

	public static String getEncountersTableFields() {

		String[] strArr = new String[] { LOCAL_DB_PATIENT_ENCOUNTER_ID_STR, LOCAL_DB_PATIENT_ID_STR, "start_date", "end_date"};
		return String.join(",", strArr);
	}

	public static String getProviderReferenceTableFields() {

		String[] strArr = new String[] { "provider_id", "provider_path", "user_nm"};
		return String.join(",", strArr);
	}
	
	public static String getI2b2PatientMappingFields() {
		String [] strArr = new String [] {"PATIENT_IDE","PATIENT_IDE_SOURCE","PATIENT_NUM","PROJECT_ID","UPDATE_DATE", "SOURCESYSTEM_CD"};
		return String.join(",", strArr);
	}
	
	public static String getI2b2EncounterMappingFields() {
		String[] strArr = new String[] { "ENCOUNTER_IDE", "ENCOUNTER_IDE_SOURCE", "ENCOUNTER_NUM", "PROJECT_ID",
				"PATIENT_IDE", "PATIENT_IDE_SOURCE", "UPDATE_DATE", "SOURCESYSTEM_CD" };
		return String.join(",", strArr);
	}
	
	public static String getObservationFactTableFields() {
		String [] strArr = new String [] {"encounter_id","patient_id","concept_cd","provider_id","start_date", "modifier_cd", "instance_num", "value", "unit_cd"};
		return String.join(",", strArr);
	}
}
