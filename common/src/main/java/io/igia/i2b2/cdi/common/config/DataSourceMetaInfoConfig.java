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

@ConfigurationProperties(prefix = "app.datasource.i2b2-db-metainfo")
@Configuration("dbMetaInfoConfig")
public class DataSourceMetaInfoConfig {

    private String demodataSchemaName;
    private String metadataSchemaName;
	public String getDemodataSchemaName() {
		return demodataSchemaName;
	}
	public void setDemodataSchemaName(String demodataSchemaName) {
		this.demodataSchemaName = demodataSchemaName;
	}
	public String getMetadataSchemaName() {
		return metadataSchemaName;
	}
	public void setMetadataSchemaName(String metadataSchemaName) {
		this.metadataSchemaName = metadataSchemaName;
	}
}