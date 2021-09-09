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

public class CsvConceptMapping extends I2b2Concept implements Serializable {

	private static final long serialVersionUID = -8435954617533322751L;

	@NotEmpty(message = "Std code (column 1) should not be empty")
	@Size(max = 50, message = "Std code (column 1) size should not be greater than 50 characters")
	private String stdCode;
	
	@NotEmpty(message = "Local code (column 2) should not be empty")
	@Size(max = 50, message = "Local code (column 2) size should not be greater than 50 characters")
	private String localCode;
	
	@NotEmpty(message = "Local code name (column 3) should not be empty")
	@Size(max = 2000, message = "Local code name (column 3) size should not be greater than 2000 characters")
	private String localCodeName;

	private String validationErrorMessage;
	
	public String getStdCode() {
		return stdCode;
	}

	public void setStdCode(String stdCode) {
		this.stdCode = stdCode;
	}

	public String getLocalCode() {
		return localCode;
	}

	public void setLocalCode(String localCode) {
		this.localCode = localCode;
	}

	public String getLocalCodeName() {
		return localCodeName;
	}

	public void setLocalCodeName(String localCodeName) {
		this.localCodeName = localCodeName;
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
    
    @Override
    public String toString() {
        return "CsvConceptMapping [stdCode=" + stdCode + ", localCode=" + localCode + ", localCodeName=" + localCodeName
                + ", validationErrorMessage=" + validationErrorMessage + "]";
    }

    /**
     * Wrap concept mapping fields with double quotes. This method requires when there
     * is need to write into the csv file.
     * @param c - Concept mapping
     * @return
     */
    public CsvConceptMapping wrapConceptFieldsWithDoubleQuotes(CsvConceptMapping c) {
        CsvConceptMapping conceptMapping = new CsvConceptMapping();
        conceptMapping.setStdCode("\"" + c.getStdCode() + "\"");
        conceptMapping.setLocalCode("\"" + c.getLocalCode() + "\"");
        conceptMapping.setLocalCodeName("\"" + c.getLocalCodeName() + "\"");
        conceptMapping.setValidationErrorMessage("\"" + c.getValidationErrorMessage() + "\"");
        return conceptMapping;
    }
}
