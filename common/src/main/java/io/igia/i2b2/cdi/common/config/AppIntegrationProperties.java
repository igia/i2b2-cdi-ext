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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "integration.sftp")
@Configuration("integrationProperties")
public class AppIntegrationProperties {

	private String remoteDirPathConcept;
	private String remoteDirPathData;
	private String localDirPathConcept;
	private String localDirPathData;
	private String host;
	private Integer port;
	private String user;
	private String password;
	private Long pollersFixedDelay;
	private String regexFilter;
	private Long pollersFixedRate;
	private Long maxMessagesPerPoll;
	private String correlationExpression;
	private Integer appGroupTimeout;
	private String dataFileReleaseExpression;
	private String conceptFileReleaseExpression;
		
	public String getRemoteDirPathConcept() {
		return remoteDirPathConcept;
	}
	public void setRemoteDirPathConcept(String remoteDirPathConcept) {
		this.remoteDirPathConcept = remoteDirPathConcept;
	}
	public String getRemoteDirPathData() {
		return remoteDirPathData;
	}
	public void setRemoteDirPathData(String remoteDirPathData) {
		this.remoteDirPathData = remoteDirPathData;
	}
	public String getLocalDirPathConcept() {
		return localDirPathConcept;
	}
	public void setLocalDirPathConcept(String localDirPathConcept) {
		this.localDirPathConcept = localDirPathConcept;
	}
	public String getLocalDirPathData() {
		return localDirPathData;
	}
	public void setLocalDirPathData(String localDirPathData) {
		this.localDirPathData = localDirPathData;
	}
	public String getHost() {
		return host;
	}
	public void setHost(String host) {
		this.host = host;
	}
	public Integer getPort() {
		return port;
	}
	public void setPort(Integer port) {
		this.port = port;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public Long getPollersFixedDelay() {
		return pollersFixedDelay;
	}
	public void setPollersFixedDelay(Long pollersFixedDelay) {
		this.pollersFixedDelay = pollersFixedDelay;
	}
	public String getRegexFilter() {
		return regexFilter;
	}
	public void setRegexFilter(String regexFilter) {
		this.regexFilter = regexFilter;
	}
	public Long getPollersFixedRate() {
		return pollersFixedRate;
	}
	public void setPollersFixedRate(Long pollersFixedRate) {
		this.pollersFixedRate = pollersFixedRate;
	}
	public Long getMaxMessagesPerPoll() {
		return maxMessagesPerPoll;
	}
	public void setMaxMessagesPerPoll(Long maxMessagesPerPoll) {
		this.maxMessagesPerPoll = maxMessagesPerPoll;
	}
	public String getCorrelationExpression() {
		return correlationExpression;
	}
	public void setCorrelationExpression(String correlationExpression) {
		this.correlationExpression = correlationExpression;
	}
	public Integer getAppGroupTimeout() {
		return appGroupTimeout;
	}
	public void setAppGroupTimeout(Integer appGroupTimeout) {
		this.appGroupTimeout = appGroupTimeout;
	}
	public String getDataFileReleaseExpression() {
		return dataFileReleaseExpression;
	}
	public void setDataFileReleaseExpression(String dataFileReleaseExpression) {
		this.dataFileReleaseExpression = dataFileReleaseExpression;
	}
	public String getConceptFileReleaseExpression() {
		return conceptFileReleaseExpression;
	}
	public void setConceptFileReleaseExpression(String conceptFileReleaseExpression) {
		this.conceptFileReleaseExpression = conceptFileReleaseExpression;
	}
}
