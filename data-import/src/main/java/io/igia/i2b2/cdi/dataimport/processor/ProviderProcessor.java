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
import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.file.FlatFileItemWriter;

import io.igia.i2b2.cdi.common.cache.ProviderCache;
import io.igia.i2b2.cdi.common.domain.AppJobContextProperties;
import io.igia.i2b2.cdi.common.domain.CsvProviderReference;
import io.igia.i2b2.cdi.common.util.AppJobContext;

public class ProviderProcessor implements ItemProcessor<CsvProviderReference,CsvProviderReference> {
	
	private ProviderCache providerCache;
	private AppJobContextProperties appJobContextProperties;
	private FlatFileItemWriter<CsvProviderReference> flatFileItemWriter;
	private static final String DUPLICATE_PROVIDER = "Duplicate record";
	
	public ProviderProcessor (ProviderCache providerCache, FlatFileItemWriter<CsvProviderReference> flatFileItemWriter) {
	    this.providerCache = providerCache;
	    this.flatFileItemWriter = flatFileItemWriter;
	}
	
	@BeforeStep
	public void beforeStep(StepExecution stepExecution) {	
		// Get metadata properties that has calculated in precalculation step
		appJobContextProperties = new AppJobContext().getJobContextPropertiesFromJobParameters(stepExecution.getJobExecution());
	}
	
	@Override
	public CsvProviderReference process(CsvProviderReference item) throws Exception {
		
		if (providerCache.containsProvider(item.getProviderID())) {
			List<CsvProviderReference> errProviders = new ArrayList<>();
	        item.setValidationErrorMessage(DUPLICATE_PROVIDER);

	        // Wrap all provider fields with double quotes as this fields is used to
	        // write in to csv file
	        CsvProviderReference provider = new CsvProviderReference().wrapConceptFieldsWithDoubleQuotes(item);
	        errProviders.add(provider);
	        flatFileItemWriter.write(errProviders);
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