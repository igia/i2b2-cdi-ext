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
package io.igia.i2b2.cdi.common.helper;

import java.sql.Timestamp;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import io.igia.i2b2.cdi.common.config.I2b2SchemaProperties;
import io.igia.i2b2.cdi.common.domain.I2b2Concept;
import io.igia.i2b2.cdi.common.domain.ValueType;

public class ConceptHelper {

    private ConceptHelper() {
    }

    private static final String META_DATA_XML = "<?xml version=\"1.0\"?><ValueMetadata><Version>3.02</Version><CreationDateTime>04/15/2007 01:22:23</CreationDateTime><TestID>Common</TestID><TestName>Common</TestName><DataType>PosFloat</DataType><CodeType>GRP</CodeType><Loinc>2090-9</Loinc><Flagstouse></Flagstouse><Oktousevalues>Y</Oktousevalues><MaxStringLength></MaxStringLength><LowofLowValue></LowofLowValue><HighofLowValue></HighofLowValue><LowofHighValue></LowofHighValue><HighofHighValue></HighofHighValue><LowofToxicValue></LowofToxicValue><HighofToxicValue></HighofToxicValue><EnumValues></EnumValues><CommentsDeterminingExclusion><Com></Com></CommentsDeterminingExclusion><UnitValues><NormalUnits></NormalUnits><EqualUnits></EqualUnits><ExcludingUnits></ExcludingUnits><ConvertingUnits><Units></Units><MultiplyingFactor></MultiplyingFactor></ConvertingUnits></UnitValues><Analysis><Enums /><Counts /><New /></Analysis></ValueMetadata>";
    private static final String NUMERIC = "N";
    private static final String CONCEPT_DIMENSION = "concept_dimension";
    private static final String MODIFIER_DIMENSION = "modifier_dimension";
    public static final String DEFAULT_MODIFIER_APPLIED_PATH = "@";

    public static String removeSeparatorAtFirstAndLastIfExists(String path, I2b2SchemaProperties i2b2Properties) {
	path = path.startsWith(i2b2Properties.getI2b2Separator()) ? path.substring(1) : path;
	path = path.endsWith(i2b2Properties.getI2b2Separator()) ? path.substring(0, path.length() - 1) : path;
	return path;
    }

    public static Integer getLevel(String path, I2b2SchemaProperties i2b2Properties) {
	return StringUtils.countOccurrencesOf(path, i2b2Properties.getI2b2Separator());
    }

    public static String getFullPathName(String path, I2b2SchemaProperties i2b2Properties) {
	return i2b2Properties.getI2b2Separator() + path + i2b2Properties.getI2b2Separator();
    }

    public static String getConceptName(String path, I2b2SchemaProperties i2b2Properties) {
	String[] components = path.split(Pattern.quote(i2b2Properties.getI2b2Separator()));
	return components[components.length - 1];
    }

    public static String getVisualAttributes(String path, I2b2SchemaProperties i2b2Properties,
	    String modifierAppliedPath) {
	if ((!StringUtils.isEmpty(modifierAppliedPath))
		&& modifierAppliedPath.equalsIgnoreCase(DEFAULT_MODIFIER_APPLIED_PATH))
	    return path.contains(i2b2Properties.getI2b2Separator()) ? "FA" : "CA";
	else
	    return "RA";
    }

    public static String getToolTip(String path, I2b2SchemaProperties i2b2Properties) {
	return path.replace(i2b2Properties.getI2b2Separator(), " " + i2b2Properties.getI2b2Separator() + " ");
    }

    public static String getDimCode(String dimCode, String path, I2b2SchemaProperties i2b2Properties) {
	return !StringUtils.isEmpty(dimCode) ? dimCode : getFullPathName(path, i2b2Properties);
    }

    public static String getOperator(String operator, I2b2SchemaProperties i2b2Properties) {
	return !StringUtils.isEmpty(operator) ? operator : i2b2Properties.getOperator();
    }

    public static String getColumnDataType(String columnDataType, String tableName,
	    I2b2SchemaProperties i2b2Properties) {
	if (columnDataType.equalsIgnoreCase(ValueType.NUMERIC.getValType())
		&& !tableName.equalsIgnoreCase(CONCEPT_DIMENSION) && !tableName.equalsIgnoreCase(MODIFIER_DIMENSION))
	    return columnDataType;
	else
	    return i2b2Properties.getColumnDataType();
    }

    public static String getColumnName(String columnName, I2b2SchemaProperties i2b2Properties) {
	return !StringUtils.isEmpty(columnName) ? columnName : i2b2Properties.getConceptPath();
    }

    public static String getTableName(String tableName, I2b2SchemaProperties i2b2Properties) {
	return !StringUtils.isEmpty(tableName) ? tableName : i2b2Properties.getTableName();
    }

    public static String getFactTableColumnName(String factTableColumn, I2b2SchemaProperties i2b2Properties) {
	return !StringUtils.isEmpty(factTableColumn) ? factTableColumn : i2b2Properties.getFactTableColumn();
    }

    public static I2b2Concept mapI2b2ConceptObject(String conceptPath, String conceptCode, String conceptType,
	    I2b2SchemaProperties i2b2Properties) {
	I2b2Concept i2b2Concept = new I2b2Concept();
	conceptPath = removeSeparatorAtFirstAndLastIfExists(conceptPath, i2b2Properties);
	conceptCode = conceptCode.replace("[]", "");

	i2b2Concept.setLevel(getLevel(conceptPath, i2b2Properties));
	i2b2Concept.setFullPath(getFullPathName(conceptPath, i2b2Properties));
	i2b2Concept.setName(getConceptName(conceptPath, i2b2Properties));
	i2b2Concept.setConceptCode(conceptCode);
	i2b2Concept.setSynonymCode(i2b2Properties.getSynonymCd());
	i2b2Concept
		.setVisualAttributes(getVisualAttributes(conceptPath, i2b2Properties, DEFAULT_MODIFIER_APPLIED_PATH));
	i2b2Concept.setMetaDataXML(getMetadataXml(conceptType));
	i2b2Concept.setFactTableColumnName(i2b2Properties.getFactTableColumn());
	i2b2Concept.settableName(i2b2Properties.getTableName());
	i2b2Concept.setColumnName(i2b2Properties.getColumnName());
	i2b2Concept.setColumnDataType(i2b2Properties.getColumnDataType());
	i2b2Concept.setOperator(i2b2Properties.getOperator());
	i2b2Concept.setDimensionCode(getFullPathName(conceptPath, i2b2Properties));
	i2b2Concept.setToolTip(getToolTip(conceptPath, i2b2Properties));
	i2b2Concept.setAppliedPath(DEFAULT_MODIFIER_APPLIED_PATH);
	i2b2Concept.setTimeStamp(new Timestamp(System.currentTimeMillis()));
	i2b2Concept.setcTableCode(i2b2Properties.getTableAccessCd());
	i2b2Concept.setcTableName(i2b2Properties.getTableAccessName());
	i2b2Concept.setcProtectedAccess(i2b2Properties.getProtectedAccess());
	return i2b2Concept;
    }

    public static String getMetadataXml(String conceptType) {
	return conceptType.equals(NUMERIC) ? META_DATA_XML : "";
    }

    public static String getAppliedPath(String mAppliedPath, I2b2SchemaProperties i2b2Properties) {
	return StringUtils.isEmpty(mAppliedPath) ? DEFAULT_MODIFIER_APPLIED_PATH
		: mAppliedPath.replace("/", i2b2Properties.getI2b2Separator());
    }

    public static String getModifierExclusionCode(String modifierExclusionCode) {
	return StringUtils.isEmpty(modifierExclusionCode) ? null : modifierExclusionCode;
    }
}