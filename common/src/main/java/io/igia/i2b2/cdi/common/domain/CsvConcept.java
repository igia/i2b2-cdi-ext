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

public class CsvConcept implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -901955878668441429L;

    @NotEmpty(message = "Concept code (column 2) should not be empty")
    @Size(max = 50, message = "Concept code (column 2) size should not be greater than 50 characters")
    private String key;

    @NotEmpty(message = "Concept path (coulmn 1) should not be empty")
    @Size(max = 700, message = "Concept path (coulmn 1) size should not be greater than 700 characters")
    private String path;

    private String metaDataXml;

    @Size(max = 50, message = "FactTableColumn (coulmn 5) size should not be greater than 50 characters")
    private String factTableColumn;

    @Size(max = 50, message = "Table Name (coulmn 6) size should not be greater than 50 characters")
    private String tableName;

    @Size(max = 50, message = "Column Name (coulmn 7) size should not be greater than 50 characters")
    private String columnName;

    @NotEmpty(message = "Concept type (coulmn 3) should not be empty")
    @Size(max = 50, message = "Concept type (column 3) size should not be greater than 50 characters")
    private String columnDataType;

    @Size(max = 10, message = "Operator (coulmn 8) size should not be greater than 10 characters")
    private String operator;

    @Size(max = 700, message = "Dimcode (coulmn 9) size should not be greater than 700 characters")
    private String dimcode;

    @Size(max = 700, message = "ModifierAppliedPath (coulmn 10) size should not be greater than 700 characters")
    private String modifierAppliedPath;

    @Size(max = 10, message = "ModifierExclusionCd (coulmn 11) size should not be greater than 10 characters")
    private String modifierExclusionCd;

    private String validationErrorMessage;

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

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getModifierAppliedPath() {
        return modifierAppliedPath;
    }

    public void setModifierAppliedPath(String modifierAppliedPath) {
        this.modifierAppliedPath = modifierAppliedPath;
    }

    public String getModifierExclusionCd() {
        return modifierExclusionCd;
    }

    public void setModifierExclusionCd(String modifierExclusionCd) {
        this.modifierExclusionCd = modifierExclusionCd;
    }

    public String getValidationErrorMessage() {
        return validationErrorMessage;
    }

    public void setValidationErrorMessage(String validationErrorMessage) {
        this.validationErrorMessage = validationErrorMessage;
    }

    @Override
    public String toString() {
        return "CsvConcept [key=" + key + ", path=" + path + ", metaDataXml=" + metaDataXml + ", factTableColumn="
                + factTableColumn + ", tableName=" + tableName + ", columnName=" + columnName + ", columnDataType="
                + columnDataType + ", operator=" + operator + ", dimcode=" + dimcode + ", modifierAppliedPath="
                + modifierAppliedPath + ", modifierExclusionCd=" + modifierExclusionCd + ", validationErrorMessage="
                + validationErrorMessage + "]";
    }

    /**
     * Wrap concept fields with double quotes. This method requires when there
     * is need to write into the csv file.
     * @param c - Concept
     * @return
     */
    public CsvConcept wrapConceptFieldsWithDoubleQuotes(CsvConcept c) {
        CsvConcept concept = new CsvConcept();
        concept.setPath("\"" + c.getPath() + "\"");
        concept.setKey("\"" + c.getKey() + "\"");
        concept.setColumnDataType("\"" + c.getColumnDataType() + "\"");
        concept.setMetaDataXml("\"" + c.getMetaDataXml() + "\"");
        concept.setFactTableColumn("\"" + c.getFactTableColumn() + "\"");
        concept.setTableName("\"" + c.getTableName() + "\"");
        concept.setColumnName("\"" + c.getColumnName() + "\"");
        concept.setOperator("\"" + c.getOperator() + "\"");
        concept.setDimcode("\"" + c.getDimcode() + "\"");
        concept.setModifierAppliedPath("\"" + c.getModifierAppliedPath() + "\"");
        concept.setModifierExclusionCd("\"" + c.getModifierExclusionCd() + "\"");
        concept.setValidationErrorMessage("\"" + c.getValidationErrorMessage() + "\"");
        return concept;
    }
}
