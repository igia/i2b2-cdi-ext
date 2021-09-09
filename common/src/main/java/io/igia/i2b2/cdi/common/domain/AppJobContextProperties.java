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
package io.igia.i2b2.cdi.common.domain;

import org.springframework.stereotype.Component;

@Component
public class AppJobContextProperties {

	private String sourceSystemCd;
	private String projectId;
	private Integer uploadId;
	private String localDataDirectoryPath;
	private String errorRecordsDirectoryPath;
	
	public String getSourceSystemCd() {
		return sourceSystemCd;
	}
	public void setSourceSystemCd(String sourceSystemCd) {
		this.sourceSystemCd = sourceSystemCd;
	}
	public String getProjectId() {
		return projectId;
	}
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}
	public Integer getUploadId() {
		return uploadId;
	}
	public void setUploadId(Integer uploadId) {
		this.uploadId = uploadId;
	}
	public String getLocalDataDirectoryPath() {
		return localDataDirectoryPath;
	}
	public void setLocalDataDirectoryPath(String localDataDirectoryPath) {
		this.localDataDirectoryPath = localDataDirectoryPath;
	}
	public String getErrorRecordsDirectoryPath() {
		return errorRecordsDirectoryPath;
	}
	public void setErrorRecordsDirectoryPath(String errorRecordsDirectoryPath) {
		this.errorRecordsDirectoryPath = errorRecordsDirectoryPath;
	}
	@Override
	public String toString() {
		return "AppJobContextProperties [sourceSystemCd=" + sourceSystemCd + ", projectId=" + projectId + ", uploadId="
				+ uploadId + ", localDataDirectoryPath=" + localDataDirectoryPath + ", errorRecordsDirectoryPath="
				+ errorRecordsDirectoryPath + "]";
	}
	
}