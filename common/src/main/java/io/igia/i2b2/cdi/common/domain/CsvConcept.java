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

public class CsvConcept implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -901955878668441429L;

	@NotEmpty
	@Size(max = 50)
	private String key;
	
	@NotEmpty
	@Size(max = 700)
	private String path;
	
	private String metaDataXml;
	
	@Size(max = 50)
	private String factTableColumn;
	
	@Size(max = 50)
	private String tableName;
	
	@Size(max = 50)
	private String columnName;
	
	@NotEmpty
	@Size(max = 50)
	private String columnDataType;
	
	@Size(max = 10)
	private String operator;
	
	@Size(max = 700)
	private String dimcode;
	
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}	
	public String getMetaDataXml() {
		return metaDataXml;
	}
	public void setMetaDataXml(String metaDataXml) {
		this.metaDataXml = metaDataXml;
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
	public String getDimcode() {
		return dimcode;
	}
	public void setDimcode(String dimcode) {
		this.dimcode = dimcode;
	}
	
	@Override
	public String toString() {
		return "CsvConcept [key=" + key + ", path=" + path + ", metaDataXml=" + metaDataXml + ", factTableColumn="
				+ factTableColumn + ", tableName=" + tableName + ", columnName=" + columnName + ", columnDataType="
				+ columnDataType + ", operator=" + operator + ", dimcode=" + dimcode + "]";
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
