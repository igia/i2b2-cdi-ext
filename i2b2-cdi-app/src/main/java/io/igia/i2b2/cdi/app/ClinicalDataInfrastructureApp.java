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
package io.igia.i2b2.cdi.app;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.dsl.channel.MessageChannels;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.dsl.Sftp;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

import com.jcraft.jsch.ChannelSftp.LsEntry;

import io.igia.i2b2.cdi.common.config.AppIntegrationProperties;

@SpringBootApplication
@IntegrationComponentScan
@EnableIntegration
@ComponentScan({"io.igia.i2b2.cdi.*"})
public class ClinicalDataInfrastructureApp {
	
	@Autowired
	AppIntegrationProperties integrationProperties;
	
	public static void main(String[] args) {
		SpringApplication.run(ClinicalDataInfrastructureApp.class, args);
	}

	@Bean
	public SessionFactory<LsEntry> sftpSessionFactory() {	
        DefaultSftpSessionFactory factory = new DefaultSftpSessionFactory(true);
        factory.setHost(integrationProperties.getHost());
        factory.setPort(integrationProperties.getPort());
        factory.setUser(integrationProperties.getUser());
        factory.setPassword(integrationProperties.getPassword());
        factory.setAllowUnknownKeys(true);
        return new CachingSessionFactory<>(factory);
    }
	
	@Bean
    public IntegrationFlow sftpInboundFlowConcept() {		
        return IntegrationFlows
            .from(Sftp.inboundAdapter(sftpSessionFactory())
            		.deleteRemoteFiles(true)
                    .preserveTimestamp(true)
                    .remoteDirectory(integrationProperties.getRemoteDirPathConcept())
                    .regexFilter(integrationProperties.getRegexFilter())
                    .localDirectory(new File(integrationProperties.getLocalDirPathConcept())),
                 e -> e.id("sftpInboundAdapter1")
                    .autoStartup(true)
                    .poller(Pollers.fixedDelay(integrationProperties.getPollersFixedDelay())))
            .channel(MessageChannels.queue("sftpInboundResultChannel1"))
            .get();
    }
	
	@Bean
    public IntegrationFlow sftpInboundFlowData() {
        return IntegrationFlows
            .from(Sftp.inboundAdapter(sftpSessionFactory())
            		.deleteRemoteFiles(true)
                    .preserveTimestamp(true)
                    .remoteDirectory(integrationProperties.getRemoteDirPathData())
                    .regexFilter(integrationProperties.getRegexFilter())
                    .localDirectory(new File(integrationProperties.getLocalDirPathData())),
                 e -> e.id("sftpInboundAdapter2")
                    .autoStartup(true)
                    .poller(Pollers.fixedDelay(integrationProperties.getPollersFixedDelay())))
            .channel(MessageChannels.queue("sftpInboundResultChannel2"))
            .get();
    }
	
	@Bean
    public IntegrationFlow sftpOutboundFlowConcept() {
        return IntegrationFlows.from("sftpOutboundChannelConcept")
            .handle(Sftp.outboundAdapter(sftpSessionFactory())
                         .useTemporaryFileName(true)
                         .remoteDirectory(integrationProperties.getRemoteDirPathConcept())
            ).get();
    }
	
	@Bean
    public IntegrationFlow sftpOutboundFlowData() {
        return IntegrationFlows.from("sftpOutboundChannelData")
            .handle(Sftp.outboundAdapter(sftpSessionFactory())
                         .useTemporaryFileName(true)
                         .remoteDirectory(integrationProperties.getRemoteDirPathData())
            ).get();
    }
}