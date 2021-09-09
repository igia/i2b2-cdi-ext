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
package io.igia.i2b2.cdi.conceptimport.writer;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.util.StringUtils;

import io.igia.i2b2.cdi.common.domain.I2b2Concept;

public class I2b2ConceptCompositeWriter implements ItemWriter<I2b2Concept> {
    private static final String CONCEPT_PLACEHOLDER = "@";
    private static final String MODIFIER_EXCLUSION = "X";
    private JdbcBatchItemWriter<I2b2Concept> metaDataWriter;
    private JdbcBatchItemWriter<I2b2Concept> tableAccessWriter;
    private JdbcBatchItemWriter<I2b2Concept> conceptDimensionWriter;
    private JdbcBatchItemWriter<I2b2Concept> modifierDimensionWriter;
    private JdbcBatchItemWriter<I2b2Concept> modifierExclusionMetaDataWriter;

    public void setMetaDataWriter(JdbcBatchItemWriter<I2b2Concept> metaDataWriter) {
	this.metaDataWriter = metaDataWriter;
    }

    public void setTableccessWriter(JdbcBatchItemWriter<I2b2Concept> tableAccessWriter) {
	this.tableAccessWriter = tableAccessWriter;
    }

    public void setConceptDimensionWriter(JdbcBatchItemWriter<I2b2Concept> conceptDimensionWriter) {
	this.conceptDimensionWriter = conceptDimensionWriter;
    }

    public void setModifierDimensionWriter(JdbcBatchItemWriter<I2b2Concept> modifierDimensionWriter) {
	this.modifierDimensionWriter = modifierDimensionWriter;
    }

    @Override
    public void write(List<? extends I2b2Concept> items) throws Exception {
	ArrayList<I2b2Concept> metaDataEntries = new ArrayList<>();
	ArrayList<I2b2Concept> tableAccessEntries = new ArrayList<>();
	ArrayList<I2b2Concept> conceptDimensionEntries = new ArrayList<>();
	ArrayList<I2b2Concept> modifierDimensionEntries = new ArrayList<>();
	ArrayList<I2b2Concept> metaDataEntriesForModifierExclusion = new ArrayList<>();

	for (I2b2Concept item : items) {
	    if (item.getAppliedPath().equalsIgnoreCase(CONCEPT_PLACEHOLDER)) {
		boolean check = (item.getModifierExclusionCode() != null) ? metaDataEntries.add(item)
			: metaDataEntriesForModifierExclusion.add(item);
		conceptDimensionEntries.add(item);
	    } else if (!StringUtils.isEmpty(item.getAppliedPath())
		    && !item.getAppliedPath().equalsIgnoreCase(CONCEPT_PLACEHOLDER)
		    && item.getModifierExclusionCode() == null) {
		metaDataEntriesForModifierExclusion.add(item);
		modifierDimensionEntries.add(item);
	    } else if (!StringUtils.isEmpty(item.getAppliedPath())
		    && !StringUtils.isEmpty(item.getModifierExclusionCode())
		    && item.getModifierExclusionCode().equalsIgnoreCase(MODIFIER_EXCLUSION)) {
		metaDataEntries.add(item);
	    }

	    if (item.getLevel() == 0) {
		tableAccessEntries.add(item);
	    }
	}
	conceptDimensionWriter.write(conceptDimensionEntries);
	metaDataWriter.write(metaDataEntries);
	tableAccessWriter.write(tableAccessEntries);
	modifierDimensionWriter.write(modifierDimensionEntries);
	modifierExclusionMetaDataWriter.write(metaDataEntriesForModifierExclusion);
    }

    public void setModifierExclusionMetaDataWriter(JdbcBatchItemWriter<I2b2Concept> modifierExclusionMetaDataWriter) {
	this.modifierExclusionMetaDataWriter = modifierExclusionMetaDataWriter;
    }

}
