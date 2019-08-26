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
import java.sql.Timestamp;

public class ObservationFact extends BaseModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2721356852917329141L;
	
	private Integer encounterNum;
	private Integer patientNum;
	private String conceptCd;
	private String providerId;
	private Timestamp startDate;
	private String modifierCd;
	private Integer instanceNum;
	private String valTypeCd;
	private String tValChar;
	private Double nValNum;
	private String unitsCd;
	private Timestamp endDate;
	public Integer getEncounterNum() {
		return encounterNum;
	}
	public void setEncounterNum(Integer encounterNum) {
		this.encounterNum = encounterNum;
	}
	public Integer getPatientNum() {
		return patientNum;
	}
	public void setPatientNum(Integer patientNum) {
		this.patientNum = patientNum;
	}
	public String getConceptCd() {
		return conceptCd;
	}
	public void setConceptCd(String conceptCd) {
		this.conceptCd = conceptCd;
	}
	public String getProviderId() {
		return providerId;
	}
	public void setProviderId(String providerId) {
		this.providerId = providerId;
	}
	public Timestamp getStartDate() {
		return startDate;
	}
	public void setStartDate(Timestamp startDate) {
		this.startDate = startDate;
	}
	public String getModifierCd() {
		return modifierCd;
	}
	public void setModifierCd(String modifierCd) {
		this.modifierCd = modifierCd;
	}
	public Integer getInstanceNum() {
		return instanceNum;
	}
	public void setInstanceNum(Integer instanceNum) {
		this.instanceNum = instanceNum;
	}
	public String getValTypeCd() {
		return valTypeCd;
	}
	public void setValTypeCd(String valTypeCd) {
		this.valTypeCd = valTypeCd;
	}
	public String gettValChar() {
		return tValChar;
	}
	public void settValChar(String tValChar) {
		this.tValChar = tValChar;
	}
	public Double getnValNum() {
		return nValNum;
	}
	public void setnValNum(Double nValNum) {
		this.nValNum = nValNum;
	}
	public String getUnitsCd() {
		return unitsCd;
	}
	public void setUnitsCd(String unitsCd) {
		this.unitsCd = unitsCd;
	}
	public Timestamp getEndDate() {
		return endDate;
	}
	public void setEndDate(Timestamp endDate) {
		this.endDate = endDate;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
