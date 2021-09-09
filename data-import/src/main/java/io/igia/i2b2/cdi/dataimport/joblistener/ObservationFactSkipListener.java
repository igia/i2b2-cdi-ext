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
package io.igia.i2b2.cdi.dataimport.joblistener;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.stereotype.Component;

import io.igia.i2b2.cdi.common.domain.CsvObservationFact;
import io.igia.i2b2.cdi.common.util.ValidationErrorMessageUtil;

@Component
public class ObservationFactSkipListener implements SkipListener<CsvObservationFact, CsvObservationFact> {

	private static final Logger log = LoggerFactory.getLogger(ObservationFactSkipListener.class);
	private FlatFileItemWriter<CsvObservationFact> flatFileItemWriter;

	public ObservationFactSkipListener (FlatFileItemWriter<CsvObservationFact> writer) {
	    this.flatFileItemWriter = writer;
	}
	
	@Override
	public void onSkipInRead(Throwable t) {
	    // Not needed now
	}

	@Override
	public void onSkipInWrite(CsvObservationFact item, Throwable t) {
	    // Not needed now
	}

    @Override
    public void onSkipInProcess(CsvObservationFact item, Throwable t) {
        List<CsvObservationFact> errFacts = new ArrayList<>();
        item.setValidationErrorMessage(ValidationErrorMessageUtil.trim(t.getMessage()));

        // Wrap all observation fact fields with double quotes as this fields is used to
        // write in to csv file
        CsvObservationFact fact = new CsvObservationFact().wrapConceptFieldsWithDoubleQuotes(item);
        errFacts.add(fact);
        try {
            flatFileItemWriter.write(errFacts);
        } catch (Exception e) {
            log.error("Error while writing error logs to file {}", e);
        }
    }
}
