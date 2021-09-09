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

import io.igia.i2b2.cdi.common.cache.PatientMappingCache;
import io.igia.i2b2.cdi.common.cache.PatientNextValCache;
import io.igia.i2b2.cdi.common.domain.AppJobContextProperties;
import io.igia.i2b2.cdi.common.domain.CsvDemographics;
import io.igia.i2b2.cdi.common.exception.PatientAlreadyExistsException;
import io.igia.i2b2.cdi.common.helper.PatientHelper;
import io.igia.i2b2.cdi.common.util.AppJobContext;

public class DemographicsProcessor implements ItemProcessor<CsvDemographics,CsvDemographics>{

	private PatientMappingCache patientMappingCache;
	private PatientNextValCache patientNextValCache;
	private DataSource i2b2DemoDataSource;
	private JdbcTemplate template = null;
	private AppJobContextProperties appJobContextProperties;
	private PatientHelper patientHelper;
	private static final String PATIENT_ALREADY_EXISTS = "Duplicate record";
	
	public DemographicsProcessor (PatientHelper patientHelper) {
		this.patientHelper = patientHelper;;
	}
	
	public void setPatientNextValueCache(PatientNextValCache patientNextValCache) {
		this.patientNextValCache = patientNextValCache;
	}
	
	public void setPatientCache(PatientMappingCache patientMappingCache) {
		this.patientMappingCache = patientMappingCache;
	}
	
	public void setDataSource(DataSource i2b2DemoDataSource) {
		this.i2b2DemoDataSource = i2b2DemoDataSource;
	}
	
	@BeforeStep
	public void beforeStep(StepExecution stepExecution) {
		template = new JdbcTemplate(i2b2DemoDataSource);
		
		//get next patient id value
		Integer patientIdNextVal = patientHelper.getPatientIdNextValue(template);
		patientNextValCache.setNextValue(patientIdNextVal);
		
		// Get metadata properties that has calculated in precalculation step
		appJobContextProperties = new AppJobContext().getJobContextPropertiesFromJobParameters(stepExecution.getJobExecution());
	}
	
	@Override
	public CsvDemographics process(CsvDemographics item) throws Exception {
		
		//Get data from i2b2. Check if patient id exists.
		List<Integer> patientNums = patientHelper.getPatientNums(template, item.getPatientID());
		if (!patientNums.isEmpty()) {
			patientMappingCache.putData(item.getPatientID(), patientNums.get(0));
			throw new PatientAlreadyExistsException(PATIENT_ALREADY_EXISTS);
		}
			
		item.setSourceSystemCD(appJobContextProperties.getSourceSystemCd());
		item.setProjectID(appJobContextProperties.getProjectId());
		item.setUpdateDate(new Timestamp(System.currentTimeMillis()));
		
		int nextVal = patientNextValCache.getNextVal();
		item.setPatientNum(nextVal);
		patientMappingCache.putData(item.getPatientID(), nextVal);
		
		return item;
	}
}