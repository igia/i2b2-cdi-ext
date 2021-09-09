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
package io.igia.i2b2.cdi.derivedfact.rowmapper;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.springframework.jdbc.core.RowMapper;

import io.igia.i2b2.cdi.common.domain.DerivedConceptDefinition;
import io.igia.i2b2.cdi.common.domain.I2b2DefaultData;
import io.igia.i2b2.cdi.common.domain.ObservationFact;

public class ObservationFactRowMapper implements RowMapper<ObservationFact>{
	
	private static final String PATIENT_NUM_STR = "patient_num";
	private static final String ENCOUNTER_NUM_STR = "encounter_num";
	private static final String PROVIDER_ID_STR = "provider_id";
	private static final String START_DATE_STR = "start_date";
	private static final String MODIFIER_CD_STR = "modifier_cd";
	private static final String INSTANCE_NUM_STR = "instance_num";
	private static final String VALTYPE_CD_STR = "valtype_cd";
	private static final String TVAL_CHAR_STR = "tval_char";	
	private static final String NVAL_NUM_STR = "nval_num";
	private static final String END_DATE_STR = "end_date";
	DerivedConceptDefinition derivedConceptDefinition = null;
	
	public ObservationFactRowMapper(DerivedConceptDefinition derivedConceptDefinition){
		this.derivedConceptDefinition = derivedConceptDefinition;
	}

	@Override
	public ObservationFact mapRow(ResultSet rs, int rowNum) throws SQLException {
		ObservationFact obsFact = new ObservationFact();
		Timestamp t = new Timestamp(System.currentTimeMillis());
		
		boolean isPatientNumKeyExists = hasColumn(rs, PATIENT_NUM_STR);
		if (!isPatientNumKeyExists) {
			return null;
		}
		
		obsFact.setEncounterNum(getEncounterNum(rs));
		obsFact.setPatientNum(rs.getInt(PATIENT_NUM_STR));
		obsFact.setConceptCd(derivedConceptDefinition.getConceptCode());
		obsFact.setProviderId(getProviderId(rs));
		obsFact.setStartDate(getStartDate(rs, t));
		obsFact.setModifierCd(getModifierCd(rs));
		obsFact.setInstanceNum(getInstanceNum(rs));
		obsFact.setValTypeCd(getValTypeCd(rs));
		obsFact.settValChar(getTvalChar(rs));
		obsFact.setnValNum(getNvalNum(rs));
		obsFact.setUnitsCd(derivedConceptDefinition.getUnitCd());
		obsFact.setEndDate(getEndDate(rs, t));
		obsFact.setUpdateDate(t);
		return obsFact;		 
	}
	
	private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
	    ResultSetMetaData rsmd = rs.getMetaData();
	    int columns = rsmd.getColumnCount();
	    for (int x = 1; x <= columns; x++) {
	        if (columnName.equalsIgnoreCase(rsmd.getColumnName(x))) {
	            return true;
	        }
	    }
	    return false;
	}
	
	private Integer getEncounterNum(ResultSet rs) throws SQLException {
		boolean isEncounetrKeyExists = hasColumn(rs, ENCOUNTER_NUM_STR);
		return isEncounetrKeyExists ? rs.getInt(ENCOUNTER_NUM_STR) : I2b2DefaultData.getMissingEncounter();
	}
	
	private String getProviderId(ResultSet rs) throws SQLException {
		boolean isProviderKeyExists = hasColumn(rs, PROVIDER_ID_STR);
		return isProviderKeyExists ? rs.getString(PROVIDER_ID_STR) : I2b2DefaultData.getMissingProvider();
	}
	
	private Timestamp getStartDate(ResultSet rs, Timestamp t) throws SQLException {
		boolean isStartDateKeyExists = hasColumn(rs, START_DATE_STR);
		return isStartDateKeyExists ? rs.getTimestamp(START_DATE_STR) : t;
	}
	
	private String getModifierCd(ResultSet rs) throws SQLException {
		boolean isModifierKeyExists = hasColumn(rs, MODIFIER_CD_STR);
		return isModifierKeyExists ? rs.getString(MODIFIER_CD_STR) : I2b2DefaultData.getMissingModifierCd();
	}
	
	private Integer getInstanceNum(ResultSet rs) throws SQLException {
		boolean isInstanceNumKeyExists = hasColumn(rs, INSTANCE_NUM_STR);
		return isInstanceNumKeyExists ? rs.getInt(INSTANCE_NUM_STR) : I2b2DefaultData.getMissingInstanceNum();
	}
	
	private String getValTypeCd(ResultSet rs) throws SQLException {
		String valTypeCd = "";
		boolean isValtypeCdKeyExists = hasColumn(rs, VALTYPE_CD_STR);
		if (isValtypeCdKeyExists) {
			valTypeCd =  rs.getString(VALTYPE_CD_STR);
		} else {
			boolean isNvalNumKeyExists = hasColumn(rs, NVAL_NUM_STR);
			if (isNvalNumKeyExists) {
				rs.getDouble(NVAL_NUM_STR);
				valTypeCd = rs.wasNull() ? "T" : "N";
			}
		}
		return valTypeCd;
	}
	
	private String getTvalChar(ResultSet rs) throws SQLException {
		String tValChar = "";
		boolean isTvalCharKeyExists = hasColumn(rs, TVAL_CHAR_STR);
		if (isTvalCharKeyExists) {
			tValChar = rs.getString(TVAL_CHAR_STR);
		} else {
			boolean isNvalNumKeyExists = hasColumn(rs, NVAL_NUM_STR);
			if (isNvalNumKeyExists) {
				rs.getDouble(NVAL_NUM_STR);
				tValChar = rs.wasNull() ? "" : "E";
			}
		}
		return tValChar;
	}
	
	private Double getNvalNum(ResultSet rs) throws SQLException {
		boolean isNvalNumKeyExists = hasColumn(rs, NVAL_NUM_STR);
		return isNvalNumKeyExists ? rs.getDouble(NVAL_NUM_STR) : null;
	}
	
	private Timestamp getEndDate(ResultSet rs, Timestamp t) throws SQLException {
		boolean isEndDateKeyExists = hasColumn(rs, END_DATE_STR);
		return isEndDateKeyExists ? rs.getTimestamp(END_DATE_STR) : t;
	}
}