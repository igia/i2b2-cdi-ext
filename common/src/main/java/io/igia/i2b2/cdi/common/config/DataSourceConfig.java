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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import io.igia.i2b2.cdi.common.domain.DataSourceType;
import liquibase.integration.spring.SpringLiquibase;

@Configuration
public class DataSourceConfig {
	
	@Autowired
	private DataSourceMetaInfoConfig dataSourceMetaInfoConfig;
	
	/**
	 * This method reads the properties from the yml for local database. These
	 * are primary dataSource properties.
	 * 
	 * @return the dataSource properties object for local database.
	 */
	@Bean
	@Primary
	@ConfigurationProperties("app.datasource.postgresql")
	public DataSourceProperties posgresqlDataSourceProperties() {
		return new DataSourceProperties();
	}

	/**
	 * This method sets the database properties required for the i2b2meta dataSource and
	 * returns the dataSource. This is primary dataSource.
	 * 
	 * @return the dataSource for the local database.
	 */
	@Bean(name = "postgresqlDataSource")
	@Primary
	@ConfigurationProperties("app.datasource.postgresql")
	public DataSource postgresqlDataSource() {
		return posgresqlDataSourceProperties().initializeDataSourceBuilder().build();
	}

	/**
	 * Reads the properties from the yml for i2b2demo.
	 * 
	 * @return the dataSource properties object for i2b2demo.
	 */
	@Bean
	@ConfigurationProperties("app.datasource.i2b2demodata")
	public DataSourceProperties i2b2DemoDataSourceProperties() {
		return new DataSourceProperties();
	}

	/**
	 * This method sets the database properties required for the i2b2demo dataSource and
	 * returns the dataSource. This method also set the schema name if db type is postgres and empty
	 * schema name if db type is sqlserver.
	 * 
	 * @return the dataSource for the i2b2 demo.
	 */
	@Bean(name = "i2b2DemoDataSource")
	@ConfigurationProperties("app.datasource.i2b2demodata")
	public DataSource i2b2DemoDataSource() {
		String dbUrl = i2b2DemoDataSourceProperties().getUrl().toLowerCase();
		if (dbUrl.contains(DataSourceType.POSTGRES.getDbType())) {
			dataSourceMetaInfoConfig.setDemodataSchemaName(dataSourceMetaInfoConfig.getDemodataSchemaName() + ".");
		}
		return i2b2DemoDataSourceProperties().initializeDataSourceBuilder().build();
	}
	
	/**
	 * Reads the properties from the yml for i2b2meta.
	 * 
	 * @return the dataSource properties object for i2b2meta.
	 */
	@Bean
	@ConfigurationProperties("app.datasource.i2b2metadata")
	public DataSourceProperties i2b2MetaDataSourceProperties() {
		return new DataSourceProperties();
	}
	
	/**
	 * This method sets the database properties required for the i2b2demo dataSource and
	 * returns the dataSource. This method also set the schema name if db type is postgres and empty
	 * schema name if db type is sqlserver.
	 * 
	 * @return the dataSource for the i2b2 meta.
	 */
	@Bean(name = "i2b2MetaDataSource")
	@ConfigurationProperties("app.datasource.i2b2metadata")
	public DataSource i2b2MetaDataSource() {
		String dbUrl = i2b2DemoDataSourceProperties().getUrl().toLowerCase();
		if (dbUrl.contains(DataSourceType.POSTGRES.getDbType())) {
			dataSourceMetaInfoConfig.setMetadataSchemaName(dataSourceMetaInfoConfig.getMetadataSchemaName() + ".");
		}
		return i2b2MetaDataSourceProperties().initializeDataSourceBuilder().build();
	}
	
	/**
	 * Returns the liquibase. This method takes local postgres data source and
	 * create the initial database schema.
	 * 
	 * @return the liquibase object from SpringLiquibase.
	 */
	@Bean
	public SpringLiquibase liquibase() {
		SpringLiquibase liquibase = new SpringLiquibase();
		liquibase.setDataSource(postgresqlDataSource());
		liquibase.setChangeLog("classpath:config/liquibase/db.changelog-master.xml");
		return liquibase;
	}
	
	@Bean
	public SpringLiquibase i2b2Liquibase() {
		SpringLiquibase liquibase = new SpringLiquibase();
		liquibase.setDataSource(i2b2DemoDataSource());
		liquibase.setChangeLog("classpath:config/liquibase/db.i2b2.changelog-master.xml");
		return liquibase;
	}
}
