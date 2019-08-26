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
package io.igia.i2b2.cdi.common.JobListener;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.stereotype.Component;

@Component
public class CustomSkipListener<T, S> implements SkipListener<T, S> {

	private static final Logger log = LoggerFactory.getLogger(CustomSkipListener.class);
	private FlatFileItemWriter<T> flatFileItemWriter;

	@Override
	public void onSkipInRead(Throwable t) {

	}

	@Override
	public void onSkipInWrite(S item, Throwable t) {

	}

	@Override
	public void onSkipInProcess(T item, Throwable t) {
		List<T> list = new ArrayList<>();
		list.add(item);

		try {
			flatFileItemWriter.write(list);
		} catch (Exception e) {
			log.error("Error while writing error logs to file {}", e);
		}
	}

	public void setFlatFileItemWriter(FlatFileItemWriter<T> flatFileItemWriter) {
		this.flatFileItemWriter = flatFileItemWriter;
	}
}
