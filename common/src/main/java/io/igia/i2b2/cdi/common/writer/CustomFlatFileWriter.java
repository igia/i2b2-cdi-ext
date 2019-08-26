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
 * Copyright (C) 2018-2019 Persistent Systems, Inc.
 */
package io.igia.i2b2.cdi.common.writer;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;

import org.springframework.batch.item.file.FlatFileHeaderCallback;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor;
import org.springframework.batch.item.file.transform.DelimitedLineAggregator;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class CustomFlatFileWriter {

	private CustomFlatFileWriter() {
	}

	public static <T> FlatFileItemWriter<T> getWriter(String writerName, String outputFile, String[] fields) {
		// Create writer instance
		FlatFileItemWriter<T> writer = new FlatFileItemWriter<>();
		writer.setName(writerName);
		writer.setHeaderCallback(new FlatFileHeaderCallback() {

			public void writeHeader(Writer writer) throws IOException {
				writer.write(Arrays.toString(fields).replace("[", "").replace("]", ""));
			}
		});

		// Set output file location
		Resource outputResource = new FileSystemResource(outputFile);
		writer.setResource(outputResource);

		// All job repetitions should "append" to same output file
		writer.setAppendAllowed(true);

		// Name field values sequence based on object properties
		BeanWrapperFieldExtractor<T> beanWrapperFieldExtractor = new BeanWrapperFieldExtractor<>();
		beanWrapperFieldExtractor.setNames(fields);

		DelimitedLineAggregator<T> delimitedLineAggregator = new DelimitedLineAggregator<>();
		delimitedLineAggregator.setDelimiter(",");
		delimitedLineAggregator.setFieldExtractor(beanWrapperFieldExtractor);

		writer.setLineAggregator(delimitedLineAggregator);
		return writer;
	}
}