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
package io.igia.i2b2.cdi.common.processor;

import javax.validation.Validator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.validator.SpringValidator;
import org.springframework.batch.item.validator.ValidatingItemProcessor;
import org.springframework.util.Assert;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.validation.beanvalidation.SpringValidatorAdapter;

/**
 * A {@link ValidatingItemProcessor} that uses the Bean Validation API (JSR-303)
 * to validate items.
 *
 * @param <T> type of items to validate
 * @author Mahmoud Ben Hassine
 * @since 4.1
 */
public class BeanValidatingItemProcessor<T> extends ValidatingItemProcessor<T> {
	
	private static final Logger log = LoggerFactory.getLogger(BeanValidatingItemProcessor.class);

	private Validator validator;

	/**
	 * Create a new instance of {@link BeanValidatingItemProcessor} with the
	 * default configuration.
	 */
	public BeanValidatingItemProcessor() {
		try (LocalValidatorFactoryBean localValidatorFactoryBean = new LocalValidatorFactoryBean();) {
			localValidatorFactoryBean.afterPropertiesSet();
			this.validator = localValidatorFactoryBean.getValidator();
		} catch (Exception e) {
			log.error("Error while creting validator", e);
		}
	}

	/**
	 * Create a new instance of {@link BeanValidatingItemProcessor}.
	 * @param localValidatorFactoryBean used to configure the Bean Validation validator
	 */
	public BeanValidatingItemProcessor(LocalValidatorFactoryBean localValidatorFactoryBean) {
		Assert.notNull(localValidatorFactoryBean, "localValidatorFactoryBean must not be null");
		this.validator = localValidatorFactoryBean.getValidator();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		SpringValidatorAdapter springValidatorAdapter = new SpringValidatorAdapter(this.validator);
		SpringValidator<T> springValidator = new SpringValidator<>();
		springValidator.setValidator(springValidatorAdapter);
		springValidator.afterPropertiesSet();
		setValidator(springValidator);
		super.afterPropertiesSet();
	}
}
