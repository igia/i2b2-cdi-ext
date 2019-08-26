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

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.Size;

import io.igia.i2b2.cdi.common.util.DateUtil;

public class CsvDemographics extends Patient implements Serializable {

	private static final long serialVersionUID = -8781534572618061873L;
	
	private String birthDTS;
	private Date birthDate;
	
	@Size(max = 50)
	private String gender;
	
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getBirthDTS() {
		return birthDTS;
	}

	public void setBirthDTS(String birthDTS) {
		this.birthDTS = birthDTS;
		this.birthDate = DateUtil.getFormatedDateYearFirst(birthDTS);
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}
}
