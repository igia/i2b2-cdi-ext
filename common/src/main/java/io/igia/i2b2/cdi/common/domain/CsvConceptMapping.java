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

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class CsvConceptMapping extends I2b2Concept implements Serializable {

	private static final long serialVersionUID = -8435954617533322751L;

	@NotEmpty
	@Size(max = 50)
	private String stdCode;
	
	@NotEmpty
	@Size(max = 50)
	private String localCode;
	
	@NotEmpty
	@Size(max = 2000)
	private String localCodeName;

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
}
