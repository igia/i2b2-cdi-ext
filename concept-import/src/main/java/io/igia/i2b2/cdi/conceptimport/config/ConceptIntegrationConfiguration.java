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
package io.igia.i2b2.cdi.conceptimport.config;

import java.io.File;

import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.integration.launch.JobLaunchingGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.file.dsl.Files;
import org.springframework.integration.handler.LoggingHandler;

import io.igia.i2b2.cdi.conceptimport.messagehandler.FileMessageToJobRequestConcept;
import io.igia.i2b2.cdi.common.config.AppIntegrationProperties;

@Configuration
@EnableIntegration
public class ConceptIntegrationConfiguration {
		
	@Autowired
	private JobLauncher jobLauncher;
	
	@Autowired
	AppIntegrationProperties integrationProperties;
	
	/**
	 * Returns the object of FileMessageToJobRequest. This will set the
	 * file_path and job which will be passed to the batch execution.
	 * 
	 * @return the object of FileMessageToJobRequest.
	 */	
	@Bean
	public FileMessageToJobRequestConcept fileMessageToJobRequestConcept() {
		return new FileMessageToJobRequestConcept();
	}
	
	protected DirectChannel inputChannel() {
		return new DirectChannel();
	}

	@Bean(name = "jobLaunchingGatewayConcept")
	public JobLaunchingGateway jobLaunchingGatewayConcept() {
	    return new JobLaunchingGateway(jobLauncher);
	}
	
	/**
	 * This is spring integration method, listens for the file at the given SFTP
	 * location. When the file found at the location, will be copid to local
	 * disk and remote file will be deleted after copy.
	 * 
	 * @param jobLaunchingGateway
	 *            from where the job will be launched.
	 * @return the IntegrationFlow.
	 */
	@Bean
	public IntegrationFlow integrationFlowConcept(@Qualifier("jobLaunchingGatewayConcept") JobLaunchingGateway jobLaunchingGateway) {
	    return IntegrationFlows.from(Files.inboundAdapter(new File(integrationProperties.getLocalDirPathConcept()))
	    		.regexFilter(integrationProperties.getRegexFilter())
	    		.useWatchService(true).preventDuplicates(false),
	           c -> c.poller(Pollers.fixedRate(integrationProperties.getPollersFixedRate()).maxMessagesPerPoll(integrationProperties.getMaxMessagesPerPoll())))
	    		.channel(inputChannel())
	    		.aggregate(a -> a.correlationExpression(integrationProperties.getCorrelationExpression()).groupTimeout(integrationProperties.getAppGroupTimeout()).releaseExpression(integrationProperties.getConceptFileReleaseExpression())
              		  .expireGroupsUponCompletion(true))
	    		.transform(fileMessageToJobRequestConcept())
	    		.handle(jobLaunchingGateway)
	    		.log(LoggingHandler.Level.WARN, "headers.id + ': ' + payload")
	    		.channel("nullChannel")
	    		.get();
	}
}
