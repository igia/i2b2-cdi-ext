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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeChunk;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.jdbc.core.JdbcTemplate;

import io.igia.i2b2.cdi.common.cache.EncounterNextValCache;
import io.igia.i2b2.cdi.common.cache.PatientMappingCache;
import io.igia.i2b2.cdi.common.cache.PatientNextValCache;
import io.igia.i2b2.cdi.common.domain.AppJobContextProperties;
import io.igia.i2b2.cdi.common.domain.CsvObservationFact;
import io.igia.i2b2.cdi.common.domain.I2b2DefaultData;
import io.igia.i2b2.cdi.common.domain.RelationalOperator;
import io.igia.i2b2.cdi.common.domain.ValueType;
import io.igia.i2b2.cdi.common.helper.EncounterHelper;
import io.igia.i2b2.cdi.common.helper.PatientHelper;
import io.igia.i2b2.cdi.common.util.AppJobContext;
import io.igia.i2b2.cdi.common.util.NumberUtil;

public class ObservationFactProcessor implements ItemProcessor<CsvObservationFact, CsvObservationFact> {

	private PatientMappingCache patientMappingCache;
	private PatientNextValCache patientNextValCache;
	private EncounterNextValCache encounterNextValCache;
	private DataSource i2b2DemoDataSource;
	private JdbcTemplate template = null;
	private AppJobContextProperties appJobContextProperties;
	private Map<String, Integer> previouslySeenEncounter = new HashMap<>();
	private PatientHelper patientHelper;
	private EncounterHelper encounterHelper;
	
	public ObservationFactProcessor (PatientHelper patientHelper, EncounterHelper encounterHelper) {
		this.patientHelper = patientHelper;
		this.encounterHelper = encounterHelper;
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
	
	public void setEncounterNextValCache(EncounterNextValCache encounterNextValCache) {
		this.encounterNextValCache = encounterNextValCache;
	}
	
	@BeforeChunk
	public void beforeChunk(ChunkContext context) {
		previouslySeenEncounter.clear();
	}
	
	@BeforeStep
	public void beforeStep(StepExecution stepExecution) {
		
		template = new JdbcTemplate(i2b2DemoDataSource);
		
		//get next patient id value
		Integer patientIdNextVal = patientHelper.getPatientIdNextValue(template);
		patientNextValCache.setNextValue(patientIdNextVal);
		
		//get next encounter id value
		Integer encounterIdNextVal = encounterHelper.getEncounterIdNextValue(template);
		encounterNextValCache.setNextValue(encounterIdNextVal);
		
		// Get metadata properties that has calculated in precalculation step
		appJobContextProperties = new AppJobContext().getJobContextPropertiesFromJobParameters(stepExecution.getJobExecution());
	}

	@Override
	public CsvObservationFact process(CsvObservationFact item) throws Exception {
		
		//Get data from i2b2. Check if patient id exists.
		if (patientMappingCache.containsPatientID(item.getPatientID())) {
			item.setPatientNum(patientMappingCache.getData(item.getPatientID()));
		} else {
			List<Integer> patientNums = patientHelper.getPatientNums(template, item.getPatientID());
			if (!patientNums.isEmpty()) {
				item.setPatientNum(patientNums.get(0));
			}
			else {
				int nextVal = patientNextValCache.getNextVal();
				item.setPatientNum(nextVal);
			}
			patientMappingCache.putData(item.getPatientID(), item.getPatientNum());
		}
		
		//Check if patient encounter id exists.
		if (item.getEncounterID().isEmpty()) {
			item.setEncounterNum(I2b2DefaultData.getMissingEncounter());
		} else {
			if (previouslySeenEncounter.containsKey(item.getEncounterID())) {
				item.setEncounterNum(previouslySeenEncounter.get(item.getEncounterID()));
			} else {
				List<Integer> encounterNums = encounterHelper.getEncounterNums(template, item.getEncounterID());
				if (!encounterNums.isEmpty()) {
					item.setEncounterNum(encounterNums.get(0));
				}
				else {
					item.setEncounterNum(encounterNextValCache.getNextVal());
				}
				previouslySeenEncounter.put(item.getEncounterID(), item.getEncounterNum());
			}
		}
		
		if (item.getProviderID().isEmpty()) {
			item.setProviderID(I2b2DefaultData.getMissingProvider());
		}
		
		if(item.getModifierCD().isEmpty()) {
			item.setModifierCD(I2b2DefaultData.getMissingModifierCd());
		}
		if(item.getInstanceNum().isEmpty()) {
			item.setInstanceNumeric(I2b2DefaultData.getMissingInstanceNum());
		} else {
			item.setInstanceNumeric(Integer.parseInt(item.getInstanceNum()));
		}
		
		if (!item.getValue().isEmpty()) {
			if (NumberUtil.isNumeric(item.getValue())) {
				item.setValueNumeric(Double.parseDouble((item.getValue())));
				item.setValTypeCd(ValueType.NUMERIC.getValType());
				item.setValue(RelationalOperator.EQUALS.getOperator());
			} else {
				item.setValue(item.getValue());
				item.setValTypeCd(ValueType.TEXT.getValType());
			}
		}
		
		item.setUpdateDate(new Timestamp(System.currentTimeMillis()));
		item.setSourceSystemCD(appJobContextProperties.getSourceSystemCd());
		item.setProjectID(appJobContextProperties.getProjectId());
		
		return item;
	}
}