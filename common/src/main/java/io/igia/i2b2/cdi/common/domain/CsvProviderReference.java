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

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class CsvProviderReference extends BaseModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5694033017747434984L;
	
	@NotEmpty(message = "Provider Id (column 1) should not be empty")
	@Size(max = 50, message = "Provider Id (column 1) size should not be greater than 50 characters")
	private String providerID;
	
	@NotEmpty(message = "Provider Path (column 2) should not be empty")
	@Size(max = 700, message = "Provider Path (column 2) size should not be greater than 700 characters")
	private String providerPath;
	
	@Size(max = 850, message = "User NM (column 3) size should not be greater than 850 characters")
	private String userNM;
	
	private String validationErrorMessage;
	
	public String getProviderID() {
		return providerID;
	}

	public void setProviderID(String providerID) {
		this.providerID = providerID;
	}
	
	public String getProviderPath() {
		return providerPath;
	}

	public void setProviderPath(String providerPath) {
		this.providerPath = providerPath;
	}

	public String getUserNM() {
		return userNM;
	}

	public void setUserNM(String userNM) {
		this.userNM = userNM;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

    public String getValidationErrorMessage() {
        return validationErrorMessage;
    }

    public void setValidationErrorMessage(String validationErrorMessage) {
        this.validationErrorMessage = validationErrorMessage;
    }
	
    /**
     * Wrap provider fields with double quotes. This method requires when there
     * is need to write into the csv file.
     * @param c - Provider reference
     * @return
     */
    public CsvProviderReference wrapConceptFieldsWithDoubleQuotes(CsvProviderReference c) {
        CsvProviderReference provider = new CsvProviderReference();
        provider.setProviderID("\"" + c.getProviderID() + "\"");
        provider.setProviderPath("\"" + c.getProviderPath() + "\"");
        provider.setUserNM("\"" + c.getUserNM() + "\"");
        provider.setValidationErrorMessage("\"" + c.getValidationErrorMessage() + "\"");
        return provider;
    }
}
