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

import io.igia.i2b2.cdi.common.domain.CsvConceptMapping;
import io.igia.i2b2.cdi.common.util.ValidationErrorMessageUtil;

@Component
public class ConceptMappingSkipListener implements SkipListener<CsvConceptMapping, CsvConceptMapping> {

	private static final Logger log = LoggerFactory.getLogger(ConceptMappingSkipListener.class);
	private FlatFileItemWriter<CsvConceptMapping> flatFileItemWriter;

	public ConceptMappingSkipListener (FlatFileItemWriter<CsvConceptMapping> writer) {
	    this.flatFileItemWriter = writer;
	}
	
	@Override
	public void onSkipInRead(Throwable t) {
	    // Not needed now
	}

	@Override
	public void onSkipInWrite(CsvConceptMapping item, Throwable t) {
	    // Not needed now
	}

    @Override
    public void onSkipInProcess(CsvConceptMapping item, Throwable t) {
        List<CsvConceptMapping> errConceptMappings = new ArrayList<>();
        item.setValidationErrorMessage(ValidationErrorMessageUtil.trim(t.getMessage()));

        // Wrap all concepts fields with double quotes as this fields is used to
        // write in to csv file
        CsvConceptMapping conceptMapping = new CsvConceptMapping().wrapConceptFieldsWithDoubleQuotes(item);
        errConceptMappings.add(conceptMapping);
        log.info("ERROR_RECORD : {}, REASON : {}", conceptMapping, conceptMapping.getValidationErrorMessage());
        try {
            flatFileItemWriter.write(errConceptMappings);
        } catch (Exception e) {
            log.error("Error while writing error logs to file {}", e);
        }
    }
}
