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

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.Size;

import io.igia.i2b2.cdi.common.util.DateUtil;

public class CsvDemographics extends Patient implements Serializable {

	private static final long serialVersionUID = -8781534572618061873L;
	
	private String birthDTS;
	private Date birthDate;
	
	@Size(max = 50, message = "Gender (column 3) size should not be greater than 50 characters")
	private String gender;
	
	private String validationErrorMessage;
	
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

    public String getValidationErrorMessage() {
        return validationErrorMessage;
    }

    public void setValidationErrorMessage(String validationErrorMessage) {
        this.validationErrorMessage = validationErrorMessage;
    }

    @Override
    public String toString() {
        return "CsvDemographics [birthDTS=" + birthDTS + ", birthDate=" + birthDate + ", gender=" + gender
                + ", validationErrorMessage=" + validationErrorMessage + "]";
    }
	
    /**
     * Wrap demographic fields with double quotes. This method requires when there
     * is need to write into the csv file.
     * @param c - Demographic
     * @return
     */
    public CsvDemographics wrapConceptFieldsWithDoubleQuotes(CsvDemographics c) {
        CsvDemographics demographic = new CsvDemographics();
        demographic.setPatientID("\"" + c.getPatientID() + "\"");
        demographic.setBirthDTS("\"" + c.getBirthDTS() + "\"");
        demographic.setGender("\"" + c.getGender() + "\"");
        demographic.setValidationErrorMessage("\"" + c.getValidationErrorMessage() + "\"");
        return demographic;
    }
}
