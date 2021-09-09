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

public class ConceptJobStepName {

    private ConceptJobStepName() {

    }

    public static final String IMPORT_CSV_CONCEPT_STEP = "importCsvConceptStep";
    public static final String IMPORT_I2B2_CONCEPT_STEP = "importI2b2ConceptStep";
    public static final String IMPORT_CSV_DERIVED_CONCEPT_STEP = "importCsvDerivedConceptDefinitionStep";
    public static final String IMPORT_I2B2_DERIVED_CONCEPT_STEP = "importI2b2DerivedConceptDefinitionStep";
    public static final String IMPORT_CSV_CONCEPT_MAPPING_STEP = "importCsvConceptMappingStep";
    public static final String IMPORT_I2B2_CONCEPT_MAPPING_STEP = "importI2b2ConceptMappingStep";

}
