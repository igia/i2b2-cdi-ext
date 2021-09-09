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
package io.igia.i2b2.cdi.conceptimport.joblistener;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.stereotype.Component;

import io.igia.i2b2.cdi.common.domain.DerivedConceptDefinition;

@Component
public class I2b2DerivedConceptSkipListener implements SkipListener<DerivedConceptDefinition, DerivedConceptDefinition> {

    private static final Logger log = LoggerFactory.getLogger(I2b2DerivedConceptSkipListener.class);
    private FlatFileItemWriter<DerivedConceptDefinition> flatFileItemWriter;
    private static final String DUPLICATE_RECORD = "Duplicate Record";
    private static final String DATA_INTEGRITY_VIOLATION = "Data integrity violation";
    private static final String DUPLICATE_KEY_EXCEPTION = "DuplicateKeyException"; 
    private static final String DATA_INTEGRITY_VIOLATION_EXCEPTION = "DataIntegrityViolationException";
    
    public I2b2DerivedConceptSkipListener(FlatFileItemWriter<DerivedConceptDefinition> writer) {
        this.flatFileItemWriter = writer;
    }

    @Override
    public void onSkipInRead(Throwable t) {
        // Not needed now
    }

    @Override
    public void onSkipInWrite(DerivedConceptDefinition item, Throwable t) {

        List<DerivedConceptDefinition> errConcepts = new ArrayList<>();
        if (t.getMessage().contains(DUPLICATE_KEY_EXCEPTION)) {
            item.setValidationErrorMessage(DUPLICATE_RECORD);
        } else if (t.getMessage().contains(DATA_INTEGRITY_VIOLATION_EXCEPTION)){
            item.setValidationErrorMessage(DATA_INTEGRITY_VIOLATION);
        }

        // Wrap all concepts fields with double quotes as this fields is used to
        // write in to csv file
        DerivedConceptDefinition concept = new DerivedConceptDefinition().wrapConceptFieldsWithDoubleQuotes(item);
        errConcepts.add(concept);
        log.info("ERROR_RECORD : {}, REASON : {}", concept, concept.getValidationErrorMessage());
        try {
            flatFileItemWriter.write(errConcepts);
        } catch (Exception e) {
            log.error("Error while writing error logs to file {}", e);
        }
    }

    @Override
    public void onSkipInProcess(DerivedConceptDefinition item, Throwable t) {
        // Not needed now
    }
}
