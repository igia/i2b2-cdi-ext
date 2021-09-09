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
package io.igia.i2b2.cdi.derivedfact.messagehandler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.annotation.Transformer;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.messaging.Message;

import io.igia.i2b2.cdi.common.config.DataSourceMetaInfoConfig;
import io.igia.i2b2.cdi.common.domain.DerivedConceptDefinition;
import io.igia.i2b2.cdi.common.domain.DerivedConceptDependency;
import io.igia.i2b2.cdi.common.domain.Status;
import io.igia.i2b2.cdi.common.util.TopologicalSortWrapper;
import io.igia.i2b2.cdi.derivedfact.joblistener.DerivedFactTriggerListener;
import io.igia.i2b2.cdi.derivedfact.rowmapper.I2b2DerivedConceptDefinitionRowMapper;
import io.igia.i2b2.cdi.derivedfact.util.DerivedConceptDependencyHierarchy;

public class JdbcMessageToJobRequest {
    
    private static final Logger log = LoggerFactory.getLogger(JdbcMessageToJobRequest.class);

    private static final String PROJECT_ID = "PROJECT_ID";
    private static final String SOURCE_SYSTEM_CD = "SOURCE_SYSTEM_CD";
    private static final String JOB_START_DATETIME = "jobStartDateTime";
    private static final String STATUS = "status";
    private static final String DERIVED_CONCEPT_ID = "derivedConceptId";
    private NamedParameterJdbcTemplate template;
    private DataSource dataSource;
    
    @Autowired
    DataSourceMetaInfoConfig dataSourceMetaInfoConfig;
    
    @Autowired
    DerivedFactTriggerListener derivedFactTriggerListener;
    
    @Autowired
    private JobLauncher jobLauncher;
    
    public JdbcMessageToJobRequest (DataSource i2b2DemoDataSource) {
        this.dataSource = i2b2DemoDataSource;
        this.template = new NamedParameterJdbcTemplate(dataSource);
    }

    @Transformer
    public JobLauncher toRequest(Message<ArrayList<Object>> message) {
        
        // Get derived concept definitions with 'PENDING' status.
        @SuppressWarnings("unchecked")
        List<Integer> pendingDerivedConceptIds = (List<Integer>) (Object)message.getPayload();
        
        //Update status to 'PROCESSING' so that db poller should not take it again
        updateJobDetails(pendingDerivedConceptIds);
        
        // Create map of ids
        Set<Integer> pendingIds = new HashSet<>(pendingDerivedConceptIds);
        
        List<DerivedConceptDefinition> pendingDerivedConcepts = getDerivedConceptDefinitions(pendingDerivedConceptIds);
        
        // Fail the job launch request if derived concepts is empty
        if(pendingDerivedConcepts.isEmpty()) {
            return jobLauncher;
        }
        
        // Get all dependency hierarchies
        List<Set<DerivedConceptDependency>> dependencyHierarchies = new DerivedConceptDependencyHierarchy(dataSource)
                .getAllDerivedConceptDependencyHierarchy();
        List<List<DerivedConceptDefinition>> sortedDerivedCocepts = new ArrayList<>();
        dependencyHierarchies.forEach(dependencyHierarchy -> {
            List<Integer> derivedConceptIds = new ArrayList<>();
            dependencyHierarchy.forEach(dependency -> derivedConceptIds.add(dependency.getDerivedConceptId()));
            List<DerivedConceptDefinition> inDerivedConceptsList = getDerivedConceptDefinitions(pendingDerivedConceptIds);
            List<DerivedConceptDefinition> sortedConcepts = new TopologicalSortWrapper()
                    .getDerivedConceptTopologicalSequence(inDerivedConceptsList, new ArrayList<>(dependencyHierarchy));
            sortedDerivedCocepts.add(sortedConcepts);
        });
        
        // Get derived concepts sequence as per dependencies
        if (!sortedDerivedCocepts.isEmpty()) {
            sortedDerivedCocepts.forEach(derivedConcept -> {
                final List<DerivedConceptDefinition> derivedConceptDefinitions = new ArrayList<>();
                derivedConcept.forEach(concept -> {
                    if (pendingIds.contains(concept.getId())) {
                        derivedConceptDefinitions.add(concept);
                        pendingIds.remove(concept.getId());
                    }
                    if (pendingDerivedConcepts.contains(concept)) {
                        pendingDerivedConcepts.remove(concept);
                    }
                });
                if (!derivedConceptDefinitions.isEmpty()) {
                    buildAndLaunchJob(derivedConceptDefinitions);
                }
            });
        }
        
        // Calculate erived concepts that was not part of the dependencies and
        // can be calculated independently
        if (!pendingDerivedConcepts.isEmpty()) {
            buildAndLaunchJob(pendingDerivedConcepts);
        }
        return jobLauncher;
    }

    /**
     * Build and launch the derived concept calculation job
     * @param derivedConceptDefinitions - List of derived concepts for calculation
     */
    private void buildAndLaunchJob(List<DerivedConceptDefinition> derivedConceptDefinitions) {
     // Build job
        SimpleJobBuilder jobBuilder = derivedFactTriggerListener
                .buildDerivedConceptCalculationJob(derivedConceptDefinitions);
        // Run async job
        if (jobBuilder != null) {
            JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
            jobParametersBuilder.addString(PROJECT_ID, "");
            jobParametersBuilder.addString(SOURCE_SYSTEM_CD, "");
            jobParametersBuilder.addDate(JOB_START_DATETIME, new Date());
            try {
                jobLauncher.run(jobBuilder.build(), jobParametersBuilder.toJobParameters());
            } catch (Exception e) {
                log.error("Error while launching async batch jobs : ", e);
            }
        }
    }
    
    /**
     * Get derived concepts which has status 'PENDING'
     * @param ids
     * @return
     */
    private List<DerivedConceptDefinition> getDerivedConceptDefinitions(List<Integer> ids) {
        MapSqlParameterSource i2b2Params = new MapSqlParameterSource();
        i2b2Params.addValue("derivedConceptIds", ids);
        String sql = "SELECT derivedconcept.*, concept.concept_cd FROM "
                + dataSourceMetaInfoConfig.getDemodataSchemaName() + "derived_concept_definition derivedconcept "
                + "INNER JOIN " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "concept_dimension concept "
                + "ON derivedconcept.concept_path = concept.concept_path "
                + "WHERE derivedconcept.id in (:derivedConceptIds)";
        return template.query(sql, i2b2Params, new I2b2DerivedConceptDefinitionRowMapper());
    }
        
    /**
     * Before start of derived concept calculation, Update job details status as 'PROSSESING' so that jdbc poller
     * should not get it again.
     * @param derivedConcepts - List of derived concepts ids
     */
    private void updateJobDetails(List<Integer> pendingDerivedConceptIds) {
        MapSqlParameterSource i2b2Params = new MapSqlParameterSource();
        i2b2Params.addValue(STATUS, Status.PROCESSING.toString());
        i2b2Params.addValue(DERIVED_CONCEPT_ID, pendingDerivedConceptIds);
        String sql = "UPDATE " + dataSourceMetaInfoConfig.getDemodataSchemaName()
                + "derived_concept_job_details set status = :" + STATUS 
                + " WHERE derived_concept_id in (:" + DERIVED_CONCEPT_ID + ")";
        template.update(sql, i2b2Params);
    }
}