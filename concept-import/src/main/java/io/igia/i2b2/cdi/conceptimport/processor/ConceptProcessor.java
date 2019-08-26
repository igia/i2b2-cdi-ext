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

import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;

import io.igia.i2b2.cdi.common.domain.AppJobContextProperties;
import io.igia.i2b2.cdi.common.domain.I2b2Concept;
import io.igia.i2b2.cdi.common.util.AppJobContext;

public class ConceptProcessor implements ItemProcessor<I2b2Concept, I2b2Concept> {

	private AppJobContextProperties appJobContextProperties;

	@BeforeStep
	public void beforeStep(StepExecution stepExecution) {
		// Get metadata properties that has calculated in precalculation step
		appJobContextProperties = new AppJobContext().getJobContextPropertiesFromJobParameters(stepExecution.getJobExecution());
	}

	@Override
	public I2b2Concept process(I2b2Concept item) throws Exception {
		item.setSourceSystemCd(appJobContextProperties.getSourceSystemCd());
		return item;
	}
}