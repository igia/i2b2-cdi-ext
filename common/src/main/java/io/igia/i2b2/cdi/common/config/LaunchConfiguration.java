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
package io.igia.i2b2.cdi.common.config;

import javax.sql.DataSource;

import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.configuration.support.MapJobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.support.DatabaseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@EnableBatchProcessing
public class LaunchConfiguration {

	@Autowired
	@Qualifier("postgresqlDataSource")
	DataSource postgresqlDataSource;
	
	@Autowired
	@Qualifier("i2b2DemoDataSource")
	DataSource i2b2DemoDataSource;
	
	@Bean
	public PlatformTransactionManager transactionManager() {
		return new DataSourceTransactionManager(postgresqlDataSource);
	}
	
	@Bean
	public PlatformTransactionManager i2b2TransactionManager() {
		return new DataSourceTransactionManager(i2b2DemoDataSource);
	}

	@Bean
	public JobOperator jobOperator() throws Exception {
		SimpleJobOperator jobOperator = new SimpleJobOperator();
		jobOperator.setJobLauncher(jobLauncher());
		jobOperator.setJobRepository(jobRepository());
		jobOperator.setJobExplorer(jobExplorer());
		jobOperator.setJobRegistry(jobRegistry());

		jobOperator.afterPropertiesSet();
		return jobOperator;
	}

	@Bean
	public JobExplorer jobExplorer() throws Exception {
		JobExplorerFactoryBean factory = new JobExplorerFactoryBean();
		factory.setDataSource(postgresqlDataSource);
		factory.afterPropertiesSet();

		return factory.getObject();
	}

	@Bean
	public JobRegistry jobRegistry() {
		return new MapJobRegistry();
	}

	@Bean
	public JobRegistryBeanPostProcessor jobRegistryBeanPostProcessor() {
		JobRegistryBeanPostProcessor postProcessor = new JobRegistryBeanPostProcessor();
		postProcessor.setJobRegistry(jobRegistry());
		return postProcessor;
	}

	@Bean
	public JobLauncher jobLauncher() throws Exception {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository(jobRepository());
		jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
		return jobLauncher;
	}

	@Bean
	public JobRepository jobRepository() throws Exception {

		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
		factory.setDataSource(postgresqlDataSource);
		factory.setDatabaseType(DatabaseType.POSTGRES.name());
		factory.setTransactionManager(transactionManager());
		factory.afterPropertiesSet();

		return factory.getObject();
	}
	
	@Bean
	public JobBuilderFactory jobBuilderFactory() throws Exception {
		return new JobBuilderFactory(jobRepository());
	}

	@Bean
	public StepBuilderFactory stepBuilderFactory() throws Exception {
		return new StepBuilderFactory(jobRepository(), transactionManager());
	}

	@Bean
	public StepBuilderFactory i2b2StepBuilderFactory() throws Exception {
		return new StepBuilderFactory(jobRepository(), i2b2TransactionManager());
	}
}
