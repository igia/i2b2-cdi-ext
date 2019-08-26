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
package io.igia.i2b2.cdi.common.reader;

import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.core.io.FileSystemResource;

public class CustomFlatFileReader {

	private CustomFlatFileReader() {
		
	}
	
	/**
	 * Returns a FlatFileItemReader object which will be further passed to
	 * processor or writer. In this method FlatFileItemReader reads a csv file
	 * row by row in chunks. In our case chunk size is 1.
	 * 
	 * @param <T>
	 * 
	 * @param filePath
	 *            A path to read the csv file.
	 * @return The FlatFileItemReader object.
	 */
	public static <T> FlatFileItemReader<T> getReader(String readerName, String filePath, String[] headers, int [] includedFields, Class<? extends T> c) {

		DelimitedLineTokenizer delimitedLineTokenizer = new DelimitedLineTokenizer();
		delimitedLineTokenizer.setNames(headers);
		delimitedLineTokenizer.setDelimiter(",");
		if (includedFields.length > 0) {
			delimitedLineTokenizer.setIncludedFields(includedFields);
		}
		
		BeanWrapperFieldSetMapper<T> beanWrapperFieldSetMapper = new BeanWrapperFieldSetMapper<>();
		beanWrapperFieldSetMapper.setTargetType(c);
		
		DefaultLineMapper<T> defaultLineMapper = new DefaultLineMapper<>();
		defaultLineMapper.setLineTokenizer(delimitedLineTokenizer);
		defaultLineMapper.setFieldSetMapper(beanWrapperFieldSetMapper);

		
		FlatFileItemReader<T> flatFileItemReader = new FlatFileItemReader<>();
		flatFileItemReader.setName(readerName);
		flatFileItemReader.setResource(new FileSystemResource(filePath));
		flatFileItemReader.setLinesToSkip(1);
		flatFileItemReader.setLineMapper(defaultLineMapper);

		return flatFileItemReader;
	}
}
