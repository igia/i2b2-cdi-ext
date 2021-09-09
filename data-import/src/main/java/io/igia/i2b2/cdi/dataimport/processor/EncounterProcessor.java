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
package io.igia.i2b2.cdi.dataimport.processor;

import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.JdbcTemplate;

import io.igia.i2b2.cdi.common.cache.EncounterNextValCache;
import io.igia.i2b2.cdi.common.cache.PatientMappingCache;
import io.igia.i2b2.cdi.common.domain.AppJobContextProperties;
import io.igia.i2b2.cdi.common.domain.CsvEncounter;
import io.igia.i2b2.cdi.common.helper.EncounterHelper;
import io.igia.i2b2.cdi.common.util.AppJobContext;

public class EncounterProcessor implements ItemProcessor<CsvEncounter,CsvEncounter>{

	private DataSource i2b2DemoDataSource;
	private EncounterNextValCache encounterNextValCache;
	private PatientMappingCache patientMappingCache;
	private JdbcTemplate template = null;
	private AppJobContextProperties appJobContextProperties;
	private EncounterHelper encounterHelper;
	
	public EncounterProcessor (EncounterHelper encounterHelper) {
		this.encounterHelper = encounterHelper;
	}
	
	public void setPatientMappingCache(PatientMappingCache patientMappingCache) {
		this.patientMappingCache = patientMappingCache;
	}
	
	public void setEncounterNextValCache(EncounterNextValCache encounterNextValCache) {
		this.encounterNextValCache = encounterNextValCache;
	}
	
	public void setI2b2DataSource(DataSource i2b2DemoDataSource) {
		this.i2b2DemoDataSource = i2b2DemoDataSource;
	}
	
	@BeforeStep
	public void beforeStep(StepExecution stepExecution) {
		template = new JdbcTemplate(i2b2DemoDataSource);
		
		//get next encounter id value
		Integer encounterIdNextVal = encounterHelper.getEncounterIdNextValue(template);
		encounterNextValCache.setNextValue(encounterIdNextVal);
		
		// Get metadata properties that has calculated in precalculation step
		appJobContextProperties = new AppJobContext().getJobContextPropertiesFromJobParameters(stepExecution.getJobExecution());
	}
	
	@Override
	public CsvEncounter process(CsvEncounter item) throws Exception {
		
		List<Integer> encounterNums = encounterHelper.getEncounterNums(template, item.getEncounterID());
		if (!encounterNums.isEmpty()) {
			return null;
		}
		
		item.setSourceSystemCD(appJobContextProperties.getSourceSystemCd());
		item.setProjectID(appJobContextProperties.getProjectId());
		item.setUpdateDate(new Timestamp(System.currentTimeMillis()));
		item.setEncounterNum(encounterNextValCache.getNextVal());
		item.setPatientNum(patientMappingCache.getData(item.getPatientID()));
		
		return item;
	}
}
