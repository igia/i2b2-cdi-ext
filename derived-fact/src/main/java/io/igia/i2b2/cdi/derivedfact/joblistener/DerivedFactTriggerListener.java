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
package io.igia.i2b2.cdi.derivedfact.joblistener;

import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import io.igia.i2b2.cdi.common.config.DataSourceMetaInfoConfig;
import io.igia.i2b2.cdi.common.domain.AppJobContextProperties;
import io.igia.i2b2.cdi.common.domain.DerivedConceptDefinition;
import io.igia.i2b2.cdi.common.domain.DerivedConceptDependency;
import io.igia.i2b2.cdi.common.rowmapper.DerivedConceptDependencyRowMapper;
import io.igia.i2b2.cdi.common.util.CyclicDependencyException;
import io.igia.i2b2.cdi.common.util.TopologicalSortWrapper;
import io.igia.i2b2.cdi.derivedfact.rowmapper.I2b2DerivedConceptDefinitionRowMapper;
import io.igia.i2b2.cdi.derivedfact.step.ImportDerivedFactsStep;

@Component
public class DerivedFactTriggerListener
{
	
	private static final Logger log = LoggerFactory.getLogger(DerivedFactTriggerListener.class);
	private static final String JOB_NAME = "importDerivedObservationfactJob";
	private static final String STEP_NAME_PREFIX1 = "step1";
	private static final String STEP_NAME_PREFIX2 = "step";
	private static final String STEP_NAME_DELIMITER = "_";
	private static final String PROJECT_ID = "PROJECT_ID";
	private static final String SOURCE_SYSTEM_CD = "SOURCE_SYSTEM_CD";
	
	@Autowired
	@Qualifier("i2b2DemoDataSource")
	private DataSource i2b2DemoDataSource;
	
	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	private JobLauncher jobLauncher;
	
	@Autowired
	private ImportDerivedFactsStep importDerivedFactsStep;
	
	@Autowired
    DataSourceMetaInfoConfig dataSourceMetaInfoConfig;
	
    public void triggerDerivedFactModule(AppJobContextProperties appJobContextProperties) {
    	
    	JdbcTemplate template = new JdbcTemplate(i2b2DemoDataSource);
    	
        String sql = "SELECT derivedconcept.id, derivedconcept.concept_path, derivedconcept.description, "
                + "derivedconcept.sql_query, derivedconcept.unit_cd , concept.concept_cd "
                + "FROM " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "derived_concept_definition derivedconcept "
                + "INNER JOIN " + dataSourceMetaInfoConfig.getDemodataSchemaName() + "concept_dimension concept "
                + "ON derivedconcept.concept_path = concept.concept_path";
		List<DerivedConceptDefinition> allDerivedConcepts = template.query(sql, new I2b2DerivedConceptDefinitionRowMapper());

		if(allDerivedConcepts.isEmpty()) {
            return;
        }
		
		// Get derived concept sequence as per dependencies
		List<DerivedConceptDefinition> derivedConcepts;
        try {
            derivedConcepts = getDependencyList(template, allDerivedConcepts);
            
            //Add derived concepts that was not part of the dependencies and can be calculated independently
            for(DerivedConceptDefinition derivedConceptDefinition : allDerivedConcepts) {
                if (!derivedConcepts.contains(derivedConceptDefinition)) {
                    derivedConcepts.add(derivedConceptDefinition);
                }
            }
            if (!derivedConcepts.isEmpty()) {
                triggerDerivedFactJob(appJobContextProperties, derivedConcepts);
            }
        } catch (CyclicDependencyException e1) {
            log.error("Skipping Derived fact calculation because of : {}" , e1.getMessage());
            log.error("{}", e1);
        }
    }
    
    public void triggerDerivedFactJob(AppJobContextProperties appJobContextProperties, List<DerivedConceptDefinition> derivedConcepts) {
     
        JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
        jobParametersBuilder.addDate("jobStartDateTime", new Date());
        jobParametersBuilder.addString(SOURCE_SYSTEM_CD, appJobContextProperties.getSourceSystemCd());
        jobParametersBuilder.addString(PROJECT_ID, appJobContextProperties.getProjectId());
        
        try {
            SimpleJobBuilder jobBuilder = buildDerivedConceptCalculationJob(derivedConcepts);
            if (jobBuilder != null) {
                jobLauncher.run(jobBuilder.build(), jobParametersBuilder.toJobParameters());
            }
        } catch (Exception e) {
            log.error("Derived facts job failed with exception {} ", e);
        }
    }

    public SimpleJobBuilder buildDerivedConceptCalculationJob(List<DerivedConceptDefinition> derivedConcepts) {
        String firstStepName = getDerivedFactStepName(derivedConcepts.get(0).getConceptCode());
        SimpleJobBuilder jobBuilder = null;
        try {
            jobBuilder = jobBuilderFactory.get(JOB_NAME)
                    .start(importDerivedFactsStep.derivedObservationFactsStep(derivedConcepts.get(0), firstStepName));
            for (int i = 1; i < derivedConcepts.size(); i++) {
                String stepNameInner = getNextDerivedFactStepName(derivedConcepts.get(i).getConceptCode(), i + 1);
                jobBuilder.next(importDerivedFactsStep.derivedObservationFactsStep(derivedConcepts.get(i), stepNameInner));
            }
            // Add job completion notification listener
            jobBuilder.listener(new DerivedConceptJobListener());
        } catch (Exception e) {
            log.error("Derived concepts job build failed with exception {} ", e);
        }
        return jobBuilder;
    }

    public List<DerivedConceptDefinition> getDependencyList(JdbcTemplate template, List<DerivedConceptDefinition> derivedConceptsList) throws CyclicDependencyException {
        String dependencySql = "SELECT dependency.id, dependency.derived_concept_id, dependency.parent_concept_path, "
                + "concept.concept_path FROM " + dataSourceMetaInfoConfig.getDemodataSchemaName() + ""
                + "derived_concept_dependency dependency " 
                + "INNER JOIN " + dataSourceMetaInfoConfig.getDemodataSchemaName() 
                + "derived_concept_definition concept "
                + "ON dependency.derived_concept_id = concept.id;";
        List<DerivedConceptDependency> dependencyList = template.query(dependencySql,
                new DerivedConceptDependencyRowMapper());
        
        return new TopologicalSortWrapper().generateDependencyList(derivedConceptsList, dependencyList);        
    }

    private static String getNextDerivedFactStepName(String conceptCd, int i) {
		return STEP_NAME_PREFIX2 + i + STEP_NAME_DELIMITER + conceptCd;
	}

	private static String getDerivedFactStepName(String conceptCd) {
		return STEP_NAME_PREFIX1 + STEP_NAME_DELIMITER + conceptCd;
	}
}
