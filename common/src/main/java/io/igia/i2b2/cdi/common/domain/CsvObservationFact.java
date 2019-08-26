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

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import io.igia.i2b2.cdi.common.util.DateUtil;

public class CsvObservationFact extends Patient implements Serializable {

	private static final long serialVersionUID = 3886600583050051942L;

	@Size(max = 200)
	private String encounterID;
	
	private int encounterNum;
	
	@NotEmpty
	@Size(max = 50)
	private String conceptCD;
	
	@Size(max = 50)
	private String providerID;
	
	@NotEmpty
	@Size(max = 50)
	private String startDTS;
	
	private Date startDate;
	private String modifierCD;
	private String instanceNum;
	private Integer instanceNumeric;
	private String valTypeCd;
	
	@Size(max = 255)
	private String value;
	
	private Double valueNumeric;
	
	@Size(max = 50)
	private String unitCD;
	
	public String getEncounterID() {
		return encounterID;
	}

	public void setEncounterID(String encounterID) {
		this.encounterID = encounterID;
	}

	public int getEncounterNum() {
		return encounterNum;
	}

	public void setEncounterNum(int encounterNum) {
		this.encounterNum = encounterNum;
	}

	public String getConceptCD() {
		return conceptCD;
	}

	public void setConceptCD(String conceptCD) {
		this.conceptCD = conceptCD;
	}

	public String getProviderID() {
		return providerID;
	}

	public void setProviderID(String providerID) {
		this.providerID = providerID;
	}

	public String getStartDTS() {
		return startDTS;
	}

	public void setStartDTS(String startDTS) {
		this.startDTS = startDTS;
		this.startDate = DateUtil.getFormatedDateYearFirst(startDTS);
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getModifierCD() {
		return modifierCD;
	}

	public void setModifierCD(String modifierCD) {
		this.modifierCD = modifierCD;
	}

	public String getInstanceNum() {
		return instanceNum;
	}

	public void setInstanceNum(String instanceNum) {
		this.instanceNum = instanceNum;
	}

	public Integer getInstanceNumeric() {
		return instanceNumeric;
	}

	public void setInstanceNumeric(Integer instanceNumeric) {
		this.instanceNumeric = instanceNumeric;
	}

	public String getValTypeCd() {
		return valTypeCd;
	}

	public void setValTypeCd(String valTypeCd) {
		this.valTypeCd = valTypeCd;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Double getValueNumeric() {
		return valueNumeric;
	}

	public void setValueNumeric(Double valueNumeric) {
		this.valueNumeric = valueNumeric;
	}

	public String getUnitCD() {
		return unitCD;
	}

	public void setUnitCD(String unitCD) {
		this.unitCD = unitCD;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
}
