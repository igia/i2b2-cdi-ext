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
package io.igia.i2b2.cdi.common.domain;

public class DataJobStepName {

    private DataJobStepName() {

    }

    public static final String IMPORT_CSV_PATIENT_DIMENSION_STEP = "importCsvPatientDimensionStep";
    public static final String IMPORT_I2B2_PATIENT_DIMENSION_STEP = "importI2b2PatientDimensionStep";
    public static final String IMPORT_CSV_ENCOUNTER_STEP = "importCsvEncounterStep";
    public static final String IMPORT_I2B2_ENCOUNTER_STEP = "importI2b2EncounterStep";
    public static final String IMPORT_CSV_OBSERVATION_FACT_STEP = "importCsvObservationFactStep";
    public static final String IMPORT_I2B2_OBSERVATION_FACT_STEP = "importI2b2ObservationFactStep";
    public static final String IMPORT_CSV_PROVIDER_STEP = "importCsvProviderStep";
    public static final String IMPORT_I2B2_PROVIDER_STEP = "importI2b2ProviderStep";

}
