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
package io.igia.i2b2.cdi.common.domain;

import java.security.SecureRandom;

import io.igia.i2b2.cdi.common.util.NumberUtil;

public class I2b2DefaultData {

	private static final int PROJECT_ID = 1;
	private static final String MISSING_PROVIDER = "0";
	private static final String MISSING_DATE = "1940-01-01 00:00:00";
	private static final String MISSING_MODIFIER_CD = "@";
	private static final int MISSING_INSTANCE_NUM = 1;
	private static SecureRandom randNumber = NumberUtil.getRandomNumber();
	
	private I2b2DefaultData() {
	}
	
	public static int getProjectId() {
		return PROJECT_ID;
	}

	public static String getMissingProvider() {
		return MISSING_PROVIDER;
	}

	public static String getMissingDate() {
		return MISSING_DATE;
	}
	
	public static String getMissingModifierCd() {
		return MISSING_MODIFIER_CD;
	}
	
	public static int getMissingInstanceNum() {
		return MISSING_INSTANCE_NUM;
	}
	
	public static Integer getMissingEncounter() {
		return randNumber.nextInt() * -1;
	}
}
