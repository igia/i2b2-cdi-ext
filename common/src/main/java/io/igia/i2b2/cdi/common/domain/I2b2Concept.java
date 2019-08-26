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

public class I2b2Concept implements Serializable{
	private static final long serialVersionUID = 2L;
	
	private int level;
	private String conceptCode;
	private String fullPath;
	private String name;
	private String synonymCode;
	private String visualAttributes;
	private String metaDataXML;
	private String factTableColumnName;
	private String tableName;
	private String columnName;
	private String columnDataType;
	private String operator;
	private String dimensionCode;
	private String toolTip;
	private String appliedPath;
	private Timestamp timeStamp;
	private String sourceSystemCd;
	private Integer uploadId;
	private String cTableCode;
	private String cTableName;
	private String cProtectedAccess;
	
	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public String getConceptCode() {
		return conceptCode;
	}

	public void setConceptCode(String conceptCode) {
		this.conceptCode = conceptCode;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSynonymCode() {
		return synonymCode;
	}

	public void setSynonymCode(String synonymCode) {
		this.synonymCode = synonymCode;
	}

	public String getVisualAttributes() {
		return visualAttributes;
	}

	public void setVisualAttributes(String visualAttributes) {
		this.visualAttributes = visualAttributes;
	}

	public String getMetaDataXML() {
		return metaDataXML;
	}

	public void setMetaDataXML(String metaDataXML) {
		this.metaDataXML = metaDataXML;
	}

	public String getFactTableColumnName() {
		return factTableColumnName;
	}

	public void setFactTableColumnName(String factTableColumnName) {
		this.factTableColumnName = factTableColumnName;
	}

	public String getTableName() {
		return tableName;
	}

	public void settableName(String tableName) {
		this.tableName = tableName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getColumnDataType() {
		return columnDataType;
	}

	public void setColumnDataType(String columnDataType) {
		this.columnDataType = columnDataType;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getDimensionCode() {
		return dimensionCode;
	}

	public void setDimensionCode(String dimensionCode) {
		this.dimensionCode = dimensionCode;
	}

	public String getToolTip() {
		return toolTip;
	}

	public void setToolTip(String toolTip) {
		this.toolTip = toolTip;
	}

	public String getAppliedPath() {
		return appliedPath;
	}

	public void setAppliedPath(String appliedPath) {
		this.appliedPath = appliedPath;
	}

	public Timestamp getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(Timestamp timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getcTableCode() {
		return cTableCode;
	}

	public void setcTableCode(String cTableCode) {
		this.cTableCode = cTableCode;
	}

	public String getcTableName() {
		return cTableName;
	}

	public void setcTableName(String cTableName) {
		this.cTableName = cTableName;
	}

	public String getcProtectedAccess() {
		return cProtectedAccess;
	}

	public void setcProtectedAccess(String cProtectedAccess) {
		this.cProtectedAccess = cProtectedAccess;
	}

	public String getSourceSystemCd() {
		return sourceSystemCd;
	}

	public void setSourceSystemCd(String sourceSystemCd) {
		this.sourceSystemCd = sourceSystemCd;
	}

	public Integer getUploadId() {
		return uploadId;
	}

	public void setUploadId(Integer uploadId) {
		this.uploadId = uploadId;
	}
}
