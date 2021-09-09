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
import java.sql.Timestamp;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

public class DerivedConceptDefinition implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -2197593091193269337L;

    private Integer id;

    @NotEmpty(message = "Concept path (column 1) should not be empty")
    @Size(max = 700, message = "Concept path (column 1) size should not be greater than 700 characters")
    private String conceptPath;
    
    private String dependsOn;
    
    private String description;
    
    private String conceptCode;
    
    @NotEmpty(message = "Sql query (column 4) should not be empty")
    private String sqlQuery;

    @Size(max = 50, message = "Unit Cd (column 5) size should not be greater than 50 characters")
    private String unitCd;
    
    private Timestamp updateDate;
    
    private String validationErrorMessage;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getConceptPath() {
        return conceptPath;
    }

    public void setConceptPath(String conceptPath) {
        this.conceptPath = conceptPath;
    }

    public String getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(String dependsOn) {
        this.dependsOn = dependsOn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConceptCode() {
        return conceptCode;
    }

    public void setConceptCode(String conceptCode) {
        this.conceptCode = conceptCode;
    }

    public String getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public String getUnitCd() {
        return unitCd;
    }

    public void setUnitCd(String unitCd) {
        this.unitCd = unitCd;
    }

    public Timestamp getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Timestamp updateDate) {
        this.updateDate = updateDate;
    }

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getValidationErrorMessage() {
        return validationErrorMessage;
    }

    public void setValidationErrorMessage(String validationErrorMessage) {
        this.validationErrorMessage = validationErrorMessage;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DerivedConceptDefinition other = (DerivedConceptDefinition) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DerivedConceptDefinition [id=" + id + ", conceptPath=" + conceptPath + ", dependsOn=" + dependsOn
                + ", description=" + description + ", conceptCode=" + conceptCode + ", sqlQuery=" + sqlQuery
                + ", unitCd=" + unitCd + ", updateDate=" + updateDate + ", validationErrorMessage="
                + validationErrorMessage + "]";
    }
    
    /**
     * Wrap Derived concept fields with double quotes. This method requires when there
     * is need to write into the csv file.
     * @param c - Derived concept definition
     * @return
     */
    public DerivedConceptDefinition wrapConceptFieldsWithDoubleQuotes(DerivedConceptDefinition c) {
        DerivedConceptDefinition concept = new DerivedConceptDefinition();
        concept.setConceptPath("\"" + c.getConceptPath() + "\"");
        concept.setDependsOn("\"" + c.getDependsOn() + "\"");
        concept.setDescription("\"" + c.getDescription() + "\"");
        concept.setSqlQuery("\"" + c.getSqlQuery() + "\"");
        concept.setUnitCd("\"" + c.getUnitCd() + "\"");
        concept.setValidationErrorMessage("\"" + c.getValidationErrorMessage() + "\"");
        return concept;
    }
}
