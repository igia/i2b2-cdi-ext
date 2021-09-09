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
package io.igia.i2b2.cdi.common.util;

import java.util.Iterator;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;

import io.igia.i2b2.cdi.common.domain.AppJobContextProperties;
import io.igia.i2b2.cdi.common.domain.ConceptFileName;
import io.igia.i2b2.cdi.common.domain.ConceptJobStepName;
import io.igia.i2b2.cdi.common.domain.DataFileName;
import io.igia.i2b2.cdi.common.domain.DataJobStepName;

public class BatchJobSummary {

    private BatchJobSummary() {
    }

    private static final String SKIPPED_STR = ", Skipped : ";
    private static final String DATABASE_REJECTED_STR = ", Database rejected : ";
    private static final String READ_STR = "Read : ";
    private static final String WRITE_STR = "Write : ";
    private static final String LOGGED_TO_FILE_STR = ", Logged to log file : ";

    /**
     * Get concept job summary. This method prints count of records read, write and skipped.
     * @param jobExecution - Job execution
     * @return
     */
    public static String getConceptJobSummary(JobExecution jobExecution) {

        // Get job parameters
        AppJobContextProperties appJobContextProperties = new AppJobContext()
                .getJobContextPropertiesFromJobParameters(jobExecution);

        final String fileBasePath = appJobContextProperties.getErrorRecordsDirectoryPath();
                
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append("--## Concept Batch Job Import Summary ##--").append("\n");
        builder.append("- Job Name : ").append(jobExecution.getJobInstance().getJobName()).append(" , Job Status : ")
                .append(jobExecution.getStatus());
        builder.append("\n");

        Iterator<StepExecution> iteratorStepExecution = jobExecution.getStepExecutions().iterator();
        while (iteratorStepExecution.hasNext()) {
            StepExecution stepExecution = iteratorStepExecution.next();

            switch (stepExecution.getStepName()) {
            case ConceptJobStepName.IMPORT_CSV_CONCEPT_STEP:
                int conceptsLoggedCount = FileHelper
                        .getRowCountOfCsv(fileBasePath + ConceptFileName.CONCEPTS_SKIPPED_RECORDS.getFileName());

                builder.append("- Concepts Read          | ").append(READ_STR).append(stepExecution.getReadCount())
                        .append(SKIPPED_STR).append(stepExecution.getReadSkipCount())
                        .append(LOGGED_TO_FILE_STR).append(conceptsLoggedCount);
                builder.append("\n");
                break;

            case ConceptJobStepName.IMPORT_I2B2_CONCEPT_STEP:
                builder.append("- Concepts Write         | ").append(WRITE_STR).append(stepExecution.getWriteCount())
                        .append(DATABASE_REJECTED_STR).append(stepExecution.getWriteSkipCount());
                builder.append("\n");
                break;

            case ConceptJobStepName.IMPORT_CSV_DERIVED_CONCEPT_STEP:
                int derivedConceptsLoggedCount = FileHelper.getRowCountOfCsv(
                        fileBasePath + ConceptFileName.DERIVED_CONCEPTS_SKIPPED_RECORDS.getFileName());
                
                builder.append("- Derived Concepts Read  | ").append(READ_STR).append(stepExecution.getReadCount())
                        .append(SKIPPED_STR).append(stepExecution.getReadSkipCount())
                        .append(LOGGED_TO_FILE_STR).append(derivedConceptsLoggedCount);
                builder.append("\n");
                break;

            case ConceptJobStepName.IMPORT_I2B2_DERIVED_CONCEPT_STEP:
                builder.append("- Derived Concepts Write | ").append(WRITE_STR).append(stepExecution.getWriteCount())
                        .append(DATABASE_REJECTED_STR).append(stepExecution.getWriteSkipCount());
                builder.append("\n");
                break;

            case ConceptJobStepName.IMPORT_CSV_CONCEPT_MAPPING_STEP:
                int conceptMappingsLoggedCount = FileHelper.getRowCountOfCsv(
                        fileBasePath + ConceptFileName.CONCEPT_MAPPINGS_SKIPPED_RECORDS.getFileName());
                builder.append("- Concept Mappings Read  | ").append(READ_STR).append(stepExecution.getReadCount())
                        .append(SKIPPED_STR).append(stepExecution.getReadSkipCount())
                        .append(LOGGED_TO_FILE_STR).append(conceptMappingsLoggedCount);
                builder.append("\n");
                break;

            case ConceptJobStepName.IMPORT_I2B2_CONCEPT_MAPPING_STEP:
                builder.append("- Concept Mappings Write | ").append(WRITE_STR).append(stepExecution.getWriteCount())
                        .append(DATABASE_REJECTED_STR).append(stepExecution.getWriteSkipCount());
                builder.append("\n");
                break;

            default:
                builder.append("");
                break;
            }
        }
        return builder.toString();
    }
    
    /**
     * Get data job summary. This method prints count of records read, write and skipped.
     * @param jobExecution - Job execution
     * @return
     */
    public static String getDataJobSummary(JobExecution jobExecution) {

        // Get job parameters
        AppJobContextProperties appJobContextProperties = new AppJobContext()
                .getJobContextPropertiesFromJobParameters(jobExecution);

        final String fileBasePath = appJobContextProperties.getErrorRecordsDirectoryPath();
                
        StringBuilder builder = new StringBuilder();
        builder.append("\n");
        builder.append("--## Data Batch Job Import Summary ##--").append("\n");
        builder.append("- Job Name : ").append(jobExecution.getJobInstance().getJobName()).append(" , Job Status : ")
                .append(jobExecution.getStatus());
        builder.append("\n");

        Iterator<StepExecution> iteratorStepExecution = jobExecution.getStepExecutions().iterator();
        while (iteratorStepExecution.hasNext()) {
            StepExecution stepExecution = iteratorStepExecution.next();

            switch (stepExecution.getStepName()) {
            case DataJobStepName.IMPORT_CSV_PATIENT_DIMENSION_STEP:
                int dimensionsLoggedCount = FileHelper
                        .getRowCountOfCsv(fileBasePath + DataFileName.PATIENT_DIMENSIONS_SKIPPED_RECORDS.getFileName());

                builder.append("- Patient Dimensions Read  | ").append(READ_STR).append(stepExecution.getReadCount())
                        .append(SKIPPED_STR).append(stepExecution.getReadSkipCount())
                        .append(LOGGED_TO_FILE_STR).append(dimensionsLoggedCount);
                builder.append("\n");
                break;

            case DataJobStepName.IMPORT_I2B2_PATIENT_DIMENSION_STEP:
                builder.append("- Patient Dimensions Write | ").append(WRITE_STR).append(stepExecution.getWriteCount())
                        .append(DATABASE_REJECTED_STR).append(stepExecution.getWriteSkipCount());
                builder.append("\n");
                break;

            case DataJobStepName.IMPORT_CSV_ENCOUNTER_STEP:
                int encountersLoggedCount = FileHelper.getRowCountOfCsv(
                        fileBasePath + DataFileName.VISIT_DIMENSIONS_SKIPPED_RECORDS.getFileName());
                
                builder.append("- Patient Encounters Read  | ").append(READ_STR).append(stepExecution.getReadCount())
                        .append(SKIPPED_STR).append(stepExecution.getReadSkipCount())
                        .append(LOGGED_TO_FILE_STR).append(encountersLoggedCount);
                builder.append("\n");
                break;

            case DataJobStepName.IMPORT_I2B2_ENCOUNTER_STEP:
                builder.append("- Patient Encounters Write | ").append(WRITE_STR).append(stepExecution.getWriteCount())
                        .append(DATABASE_REJECTED_STR).append(stepExecution.getWriteSkipCount());
                builder.append("\n");
                break;

            case DataJobStepName.IMPORT_CSV_PROVIDER_STEP:
                int providersLoggedCount = FileHelper.getRowCountOfCsv(
                        fileBasePath + DataFileName.PROVIDER_DIMENSIONS_SKIPPED_RECORDS.getFileName());
                builder.append("- Providers Read           | ").append(READ_STR).append(stepExecution.getReadCount())
                        .append(SKIPPED_STR).append(stepExecution.getReadSkipCount())
                        .append(LOGGED_TO_FILE_STR).append(providersLoggedCount);
                builder.append("\n");
                break;

            case DataJobStepName.IMPORT_I2B2_PROVIDER_STEP:
                builder.append("- Providers Write          | ").append(WRITE_STR).append(stepExecution.getWriteCount())
                        .append(DATABASE_REJECTED_STR).append(stepExecution.getWriteSkipCount());
                builder.append("\n");
                break;
                
            case DataJobStepName.IMPORT_CSV_OBSERVATION_FACT_STEP:
                int observationsLoggedCount = FileHelper.getRowCountOfCsv(
                        fileBasePath + DataFileName.OBSERVATION_FACTS_SKIPPED_RECORDS.getFileName());
                builder.append("- Observation Facts Read   | ").append(READ_STR).append(stepExecution.getReadCount())
                        .append(SKIPPED_STR).append(stepExecution.getReadSkipCount())
                        .append(LOGGED_TO_FILE_STR).append(observationsLoggedCount);
                builder.append("\n");
                break;

            case DataJobStepName.IMPORT_I2B2_OBSERVATION_FACT_STEP:
                builder.append("- Observation Facts Write  | ").append(WRITE_STR).append(stepExecution.getWriteCount())
                        .append(DATABASE_REJECTED_STR).append(stepExecution.getWriteSkipCount());
                builder.append("\n");
                break;

            default:
                builder.append("");
                break;
            }
        }
        return builder.toString();
    }
}
