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

import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import io.igia.i2b2.cdi.common.config.DataSourceMetaInfoConfig;
import io.igia.i2b2.cdi.common.domain.DerivedConceptDefinition;
import io.igia.i2b2.cdi.common.domain.Status;

public class StepListener implements StepExecutionListener {

    private static final String JOB_ID = "jobId";
    private static final String DERIVED_CONCEPT_ID = "derivedConceptId";
    private static final String DERIVED_CONCEPT_CD = "derivedConceptCd";
    private static final String STATUS = "status";
    private static final String DERIVED_CONCEPT_SQL = "derivedConceptSql";
    private static final String STARTED_ON = "startedOn";
    private static final String COMPLETED_ON = "completedOn";
    private static final String ERROR_STACK = "errorStack";
    private DerivedConceptDefinition derivedConceptDefinition;
    private DataSource i2b2DemoDataSource;
    private NamedParameterJdbcTemplate jdbcTemplate;
    private DataSourceMetaInfoConfig dataSourceMetaInfoConfig;

    public StepListener(DerivedConceptDefinition derivedConceptDefinition, DataSource i2b2DemoDataSource, DataSourceMetaInfoConfig dataSourceMetaInfoConfig) {
        this.derivedConceptDefinition = derivedConceptDefinition;
        this.i2b2DemoDataSource = i2b2DemoDataSource;

        this.jdbcTemplate = new NamedParameterJdbcTemplate(this.i2b2DemoDataSource);
        this.dataSourceMetaInfoConfig = dataSourceMetaInfoConfig;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        
        // Delete derived facts associated with the derived concept
        deleteDerivedFactsByConceptCode(derivedConceptDefinition.getConceptCode());
        
        // Check if fact job details already exists with 'PROCESSING' status.
        List<Integer> jobDetailsIds = getJobDetailsIds(derivedConceptDefinition, Status.PROCESSING);
        if (jobDetailsIds.isEmpty()) {
            // Insert fact job details for derived fact calculation as processing
            insertJobDetails(derivedConceptDefinition, Status.PROCESSING);
        } else {
            // Update fact job details for derived fact calculation as processing
            Integer jobId = jobDetailsIds.get(0);
            updateJobDetailsById(jobId, derivedConceptDefinition, Status.PROCESSING);
        }
    }

    /**
     * Delete derived facts by concept code
     * @param conceptCode - code to delete the facts
     */
    private void deleteDerivedFactsByConceptCode(String conceptCode) {
        MapSqlParameterSource i2b2Params = new MapSqlParameterSource();
        i2b2Params.addValue(DERIVED_CONCEPT_CD, conceptCode);
        String sql = "DELETE FROM observation_fact WHERE concept_cd = :" + DERIVED_CONCEPT_CD;
        jdbcTemplate.update(sql, i2b2Params);
    }

    /**
     * While processing derived concepts, update job details by id, set status 'PROSSESING' and sql used.
     * @param jobId - Unique id of the job
     * @param derivedConceptDefinition - Derived concept definition object
     * @param status - Status of the job in execution
     */
    private void updateJobDetailsById(Integer jobId, DerivedConceptDefinition inDerivedConceptDefinition,
            Status status) {
        MapSqlParameterSource i2b2Params = new MapSqlParameterSource();
        i2b2Params.addValue(JOB_ID, jobId);
        i2b2Params.addValue(DERIVED_CONCEPT_SQL, inDerivedConceptDefinition.getSqlQuery());
        i2b2Params.addValue(STATUS, status.toString());
        i2b2Params.addValue(STARTED_ON, new Timestamp(System.currentTimeMillis()));
        String sql = "UPDATE " + dataSourceMetaInfoConfig.getDemodataSchemaName()
                + "derived_concept_job_details set derived_concept_sql = :" + DERIVED_CONCEPT_SQL + ", status = :"
                + STATUS + ", started_on = :" + STARTED_ON + " WHERE id = :" + JOB_ID;
        jdbcTemplate.update(sql, i2b2Params);
    }

    /**
     * Get Job details ids by status 
     * @param inDerivedConceptDefinition - Derived concept definition object
     * @param status - Status of the job. In this case status = 'PENDING'.
     * @return
     */
    private List<Integer> getJobDetailsIds(DerivedConceptDefinition inDerivedConceptDefinition, Status status) {
        MapSqlParameterSource i2b2Params = new MapSqlParameterSource();
        i2b2Params.addValue(DERIVED_CONCEPT_ID, inDerivedConceptDefinition.getId());
        i2b2Params.addValue(STATUS, status.toString());
        String sql = "SELECT id from " + dataSourceMetaInfoConfig.getDemodataSchemaName()
                + "derived_concept_job_details WHERE derived_concept_id = :" + DERIVED_CONCEPT_ID + " and status = :"
                + STATUS;
        return jdbcTemplate.queryForList(sql, i2b2Params, Integer.class);
    }

    /**
     * Insert Job details. 
     * @param inDerivedConceptDefinition - Derived concept definition object
     * @param status - Status of the job
     */
    private void insertJobDetails(DerivedConceptDefinition inDerivedConceptDefinition, Status status) {
        MapSqlParameterSource i2b2Params = new MapSqlParameterSource();
        i2b2Params.addValue(DERIVED_CONCEPT_ID, inDerivedConceptDefinition.getId());
        i2b2Params.addValue(DERIVED_CONCEPT_SQL, inDerivedConceptDefinition.getSqlQuery());
        i2b2Params.addValue(STATUS, status.toString());
        i2b2Params.addValue(STARTED_ON, new Timestamp(System.currentTimeMillis()));
        String sql = "INSERT INTO " + dataSourceMetaInfoConfig.getDemodataSchemaName()
                + "derived_concept_job_details (derived_concept_id, derived_concept_sql, "
                + "status, started_on) VALUES ( :" + DERIVED_CONCEPT_ID + ", :" + DERIVED_CONCEPT_SQL + ", :" + STATUS
                + ", :" + STARTED_ON + ")";
        jdbcTemplate.update(sql, i2b2Params);
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        ExitStatus exitStatus = stepExecution.getExitStatus();
        Integer derivedConceptId = derivedConceptDefinition.getId();
        if (stepExecution.getStatus() == BatchStatus.COMPLETED) {
            // Derived fact calculation is completed for that derived concept.
            updateFactJobDetailsById(derivedConceptId, Status.COMPLETED, exitStatus.getExitDescription());
        } else {
            updateFactJobDetailsById(derivedConceptId, Status.ERROR, exitStatus.getExitDescription());
        }
        return ExitStatus.COMPLETED;
    }

    /**
     * 
     * @param derivedConceptId
     * @param status
     * @param errorStack
     */
    private void updateFactJobDetailsById(Integer derivedConceptId, Status status, String errorStack) {

        // Get derived concept job execution details
        List<Integer> jobIds = getFactJobDetailsIds(derivedConceptId);

        // Update status of the derived concept
        if (!jobIds.isEmpty()) {
            MapSqlParameterSource i2b2Params = new MapSqlParameterSource();
            i2b2Params.addValue(JOB_ID, jobIds.get(0));
            i2b2Params.addValue(STATUS, status.toString());
            i2b2Params.addValue(ERROR_STACK, errorStack);
            i2b2Params.addValue(COMPLETED_ON, new Timestamp(System.currentTimeMillis()));
            String sql = "UPDATE " + dataSourceMetaInfoConfig.getDemodataSchemaName()
                    + "derived_concept_job_details set error_stack = :" + ERROR_STACK + ", status = :" + STATUS
                    + ", completed_on = :" + COMPLETED_ON + " WHERE id = :" + JOB_ID;
            jdbcTemplate.update(sql, i2b2Params);
        }
    }

    /**
     * Get last record of job details by filter by derived_concept_id and order by id desc.
     * @param derivedConceptId - Unique identifier of the derived concept
     * @return
     */
    private List<Integer> getFactJobDetailsIds(Integer derivedConceptId) {
        MapSqlParameterSource i2b2Params = new MapSqlParameterSource();
        i2b2Params.addValue(DERIVED_CONCEPT_ID, derivedConceptId);

        String sql = "SELECT id from " + dataSourceMetaInfoConfig.getDemodataSchemaName()
                + "derived_concept_job_details WHERE derived_concept_id = :" + DERIVED_CONCEPT_ID
                + " ORDER BY id desc";
        return jdbcTemplate.queryForList(sql, i2b2Params, Integer.class);
    }
}
