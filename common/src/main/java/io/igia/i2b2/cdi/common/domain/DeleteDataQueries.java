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

public enum DeleteDataQueries {

	CONCEPTS("delete from concepts"),
	CONCEPT_MAPPINGS("delete from concept_mapping"),
	DEMOGRAPHICS("delete from nhp_patient_demographics"),
	ENCOUNTERS("delete from nhp_patient_encounters"),
	PROVIDER_REFERENCES("delete from nhp_patient_provider_reference"),
	OBSERVATION_FACTS("delete from nhp_observation_facts");
	
	private String query;

	DeleteDataQueries(String query) {
		this.query = query;
	}

	public String getQuery() {
		return query;
	}
}