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
package io.igia.i2b2.cdi.dataimport.processor;

import java.sql.Timestamp;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;

import io.igia.i2b2.cdi.common.cache.ProviderCache;
import io.igia.i2b2.cdi.common.domain.AppJobContextProperties;
import io.igia.i2b2.cdi.common.domain.CsvProviderReference;
import io.igia.i2b2.cdi.common.util.AppJobContext;

public class ProviderProcessor implements ItemProcessor<CsvProviderReference,CsvProviderReference> {
	
	private ProviderCache providerCache;
	private AppJobContextProperties appJobContextProperties;
	
	public void setProviderCache(ProviderCache providerCache) {
		this.providerCache = providerCache;
	}
	
	@BeforeStep
	public void beforeStep(StepExecution stepExecution) {	
		// Get metadata properties that has calculated in precalculation step
		appJobContextProperties = new AppJobContext().getJobContextPropertiesFromJobParameters(stepExecution.getJobExecution());
	}
	
	@Override
	public CsvProviderReference process(CsvProviderReference item) throws Exception {
		
		if (providerCache.containsProvider(item.getProviderID())) {
			return null;
		}
		
		providerCache.putData(item.getProviderID());
		if(item.getProviderPath().isEmpty()) {
			item.setProviderPath(item.getUserNM());
		}
		item.setSourceSystemCD(appJobContextProperties.getSourceSystemCd());
		item.setUpdateDate(new Timestamp(System.currentTimeMillis()));
		return item;
	}
}