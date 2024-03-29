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
package io.igia.i2b2.cdi.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.igia.i2b2.cdi.common.processor.BeanValidatingItemProcessor;

@Component
public class CustomBeanValidator {
	
	private static final Logger log = LoggerFactory.getLogger(CustomBeanValidator.class);
	
	public <T> BeanValidatingItemProcessor<T> validator() {
		BeanValidatingItemProcessor<T> validator = new BeanValidatingItemProcessor<>();
		validator.setFilter(false);
		try {
			validator.afterPropertiesSet();
		} catch (Exception e) {
			log.error("Error while creating validator bean" ,e);
		}
		return validator;
	}
}
