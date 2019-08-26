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

public class CsvEncounter extends Patient implements Serializable {

	private static final long serialVersionUID = -4997639823226935309L;

	@NotEmpty
	@Size(max = 200)
	private String encounterID;

	private String startDTS;
	private Date startDate;
	private String endDTS;
	private Date endDate;
	private int encounterNum;

	public CsvEncounter() {

	}

	public CsvEncounter(CsvEncounter of) {
		this.setSourceSystemCD(of.getSourceSystemCD());
		this.setProjectID(of.getProjectID());
		this.setUpdateDate(of.getUpdateDate());
		this.setPatientID(of.getPatientID());
		this.setPatientNum(of.getPatientNum());
		this.encounterID = of.encounterID;
		this.startDTS = of.startDTS;
		this.startDate = of.startDate;
		this.endDTS = of.endDTS;
		this.endDate = of.endDate;
		this.encounterNum = of.encounterNum;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public int getEncounterNum() {
		return encounterNum;
	}

	public void setEncounterNum(int encounterNum) {
		this.encounterNum = encounterNum;
	}

	public String getEncounterID() {
		return encounterID;
	}

	public void setEncounterID(String encounterID) {
		this.encounterID = encounterID;
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

	public String getEndDTS() {
		return endDTS;
	}

	public void setEndDTS(String endDTS) {
		this.endDTS = endDTS;
		this.endDate = DateUtil.getFormatedDateYearFirst(endDTS);
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}
