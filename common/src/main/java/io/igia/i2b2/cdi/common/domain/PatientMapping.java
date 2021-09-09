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

public class PatientMapping {
	private String patientIDE;
	private int patientNum;
	private String source;
	
	public String getPatientIDE() {
		return patientIDE;
	}
	public void setPatientIDE(String patientIDE) {
		this.patientIDE = patientIDE;
	}
	public int getPatientNum() {
		return patientNum;
	}
	public void setPatientNum(int patientNum) {
		this.patientNum = patientNum;
	}
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
}
