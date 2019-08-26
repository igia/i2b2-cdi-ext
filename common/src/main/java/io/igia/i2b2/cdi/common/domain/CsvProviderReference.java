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

public class CsvProviderReference extends BaseModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5694033017747434984L;
	
	@NotEmpty
	@Size(max = 50)
	private String providerID;
	
	@NotEmpty
	@Size(max = 700)
	private String providerPath;
	
	@Size(max = 850)
	private String userNM;
	
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
}
