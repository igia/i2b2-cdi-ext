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
package io.igia.i2b2.cdi.conceptimport.processor;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;

import io.igia.i2b2.cdi.common.config.I2b2SchemaProperties;
import io.igia.i2b2.cdi.common.domain.AppJobContextProperties;
import io.igia.i2b2.cdi.common.domain.CsvConceptMapping;
import io.igia.i2b2.cdi.common.util.AppJobContext;

public class ConceptMappingProcessor implements ItemProcessor<CsvConceptMapping, CsvConceptMapping> {

	private AppJobContextProperties appJobContextProperties;
	private DataSource dataSource;
	private I2b2SchemaProperties i2b2Properties;

	public ConceptMappingProcessor(DataSource i2b2DemoDataSource, I2b2SchemaProperties properties) {
		dataSource = i2b2DemoDataSource;
		i2b2Properties = properties;
	}

	@BeforeStep
	public void beforeStep(StepExecution stepExecution) {
		// Get metadata properties that has calculated in precalculation step
		appJobContextProperties = new AppJobContext()
				.getJobContextPropertiesFromJobParameters(stepExecution.getJobExecution());
	}

	@Override
	public CsvConceptMapping process(CsvConceptMapping item) throws Exception {

		NamedParameterJdbcTemplate template = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> oldI2b2ConceptList;
		Map<String, Object> oldI2b2Concept;

		String i2b2Query = "select * from i2b2metadata.i2b2 where c_fullname in ( select concept_path "
				+ "from i2b2demodata.concept_dimension where concept_cd = :conceptCd limit 1 )";
		MapSqlParameterSource i2b2Params = new MapSqlParameterSource();
		i2b2Params.addValue("conceptCd", item.getStdCode());

		oldI2b2ConceptList = template.queryForList(i2b2Query, i2b2Params);
		if (oldI2b2ConceptList.isEmpty()) {
			return null;
		}
		oldI2b2Concept = oldI2b2ConceptList.get(0);
		String path = oldI2b2Concept.get("c_fullname").toString();

		item.setLevel(getLevel(path) - 1);
		item.setFullPath(getFullPathName(path, item));
		item.setName(item.getLocalCodeName());
		item.setConceptCode(item.getLocalCode());
		item.setSynonymCode(i2b2Properties.getSynonymCd());
		item.setVisualAttributes("FA");
		item.setMetaDataXML(oldI2b2Concept.get("c_metadataxml").toString());
		item.setFactTableColumnName(oldI2b2Concept.get("c_facttablecolumn").toString());
		item.settableName(oldI2b2Concept.get("c_tablename").toString());
		item.setColumnName(oldI2b2Concept.get("c_columnname").toString());
		item.setColumnDataType(oldI2b2Concept.get("c_columndatatype").toString());
		item.setOperator(oldI2b2Concept.get("c_operator").toString());
		item.setDimensionCode(getDimCode(oldI2b2Concept, item));
		item.setToolTip(getToolTip(oldI2b2Concept.get("c_tooltip").toString(), item));
		item.setAppliedPath(oldI2b2Concept.get("m_applied_path").toString());
		item.setTimeStamp(new Timestamp(System.currentTimeMillis()));

		item.setSourceSystemCd(appJobContextProperties.getSourceSystemCd());
		return item;
	}

	protected Integer getLevel(String path) {
		return StringUtils.countOccurrencesOf(path, i2b2Properties.getI2b2Separator());
	}

	protected String getFullPathName(String path, CsvConceptMapping item) {
		return path + item.getLocalCodeName() + i2b2Properties.getI2b2Separator();
	}

	protected String getToolTip(String path, CsvConceptMapping item) {
		String toolTip = null;
		toolTip = path + " " + i2b2Properties.getI2b2Separator() + " " + item.getLocalCodeName();
		return toolTip;
	}

	private String getDimCode(Map<String, Object> oldI2b2Concept, CsvConceptMapping item) {
		return oldI2b2Concept.get("c_dimcode").toString() + item.getLocalCodeName()	+ i2b2Properties.getI2b2Separator();
	}
}