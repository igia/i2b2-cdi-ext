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
package io.igia.i2b2.cdi.common.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "i2b2.schema")
@Configuration("i2b2SchemaFields")
public class I2b2SchemaProperties {

	private String synonymCd; 
	private String factTableColumn; 
	private String tableName;
	private String columnName;
	private String columnDataType;
	private String operator;
	private String tableAccessCd;
	private String tableAccessName;
	private String protectedAccess;
	private String i2b2Separator;
	private String conceptPath;
	private String appliedPath;
	
	public String getAppliedPath() {
		return appliedPath;
	}
	public void setAppliedPath(String appliedPath) {
		this.appliedPath = appliedPath;
	}
	public String getConceptPath() {
		return conceptPath;
	}
	public void setConceptPath(String conceptPath) {
		this.conceptPath = conceptPath;
	}
	public String getSynonymCd() {
		return synonymCd;
	}
	public String getI2b2Separator() {
		return i2b2Separator;
	}
	public void setI2b2Separator(String i2b2Separator) {
		this.i2b2Separator = i2b2Separator;
	}
	public void setSynonymCd(String synonymCd) {
		this.synonymCd = synonymCd;
	}
	public String getFactTableColumn() {
		return factTableColumn;
	}
	public void setFactTableColumn(String factTableColumn) {
		this.factTableColumn = factTableColumn;
	}
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
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
	public String getTableAccessCd() {
		return tableAccessCd;
	}
	public void setTableAccessCd(String tableAccessCd) {
		this.tableAccessCd = tableAccessCd;
	}
	public String getTableAccessName() {
		return tableAccessName;
	}
	public void setTableAccessName(String tableAccessName) {
		this.tableAccessName = tableAccessName;
	}
	public String getProtectedAccess() {
		return protectedAccess;
	}
	public void setProtectedAccess(String protectedAccess) {
		this.protectedAccess = protectedAccess;
	}
}
