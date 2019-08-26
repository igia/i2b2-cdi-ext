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
package io.igia.i2b2.cdi.common.util;

import java.io.File;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.igia.i2b2.cdi.common.domain.I2b2DefaultData;

public class DateUtil {

	private DateUtil() {
	}

	private static final Logger log = LoggerFactory.getLogger(DateUtil.class);

	public static Date getFormatedDateYearFirst(String dateStr) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date date = null;
		try {
			date = sdf.parse(dateStr);
		} catch (ParseException e) {
			try {
				date = sdf.parse(I2b2DefaultData.getMissingDate());
			} catch (ParseException e1) {
				log.error("Error while parsing date {}" ,e1);
			}
		}
		return date;
	}
	
	public static Timestamp getLastModifiedDateOfFile(String filePath) {

		File file = new File(filePath);
		Timestamp date = null;
		try {
			date = new Timestamp(file.lastModified());
		} catch (Exception e) {
			log.error("Error while parsing date {}" ,e);
		}
		return date;
	}
}
