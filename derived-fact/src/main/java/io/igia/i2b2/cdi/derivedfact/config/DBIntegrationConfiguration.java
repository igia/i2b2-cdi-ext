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
package io.igia.i2b2.cdi.derivedfact.config;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.Pollers;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.integration.jdbc.JdbcPollingChannelAdapter;
import org.springframework.jdbc.core.RowMapper;

import io.igia.i2b2.cdi.common.config.AppJdbcIntegrationProperties;
import io.igia.i2b2.cdi.derivedfact.messagehandler.JdbcMessageToJobRequest;

@Configuration
@EnableIntegration
public class DBIntegrationConfiguration {
			
    private static final String INTEGRATION_SQL_QUERY = "SELECT derived_concept_id FROM derived_concept_job_details WHERE status = 'PENDING'";

    @Autowired
    @Qualifier("i2b2DemoDataSource")
    private DataSource i2b2DemoDataSource;
     
	@Bean
	public IntegrationFlow pollingFlow(AppJdbcIntegrationProperties integrationProperties) {
	    return IntegrationFlows.from(jdbcMessageSource(i2b2DemoDataSource),
	                c -> c.poller(Pollers.fixedRate(integrationProperties.getPollersFixedRate())
	                        .maxMessagesPerPoll(1)))
	            .transform(jdbcMessageToAsyncJobLaunchRequests())
	            .log(LoggingHandler.Level.WARN, "headers.id + ': ' + payload")
	            .get();
	}

    @Bean
    public MessageSource<Object> jdbcMessageSource(DataSource i2b2DemoDataSource) {

        JdbcPollingChannelAdapter adapter = new JdbcPollingChannelAdapter(i2b2DemoDataSource, INTEGRATION_SQL_QUERY);
        adapter.setRowMapper(new RowMapper<Integer>() {
            @Override
            public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
                return rs.getInt("derived_concept_id");
            }
        });
        return adapter;
    }

	@Bean
    public JdbcMessageToJobRequest jdbcMessageToAsyncJobLaunchRequests() {
        return new JdbcMessageToJobRequest(i2b2DemoDataSource);
    }
}
