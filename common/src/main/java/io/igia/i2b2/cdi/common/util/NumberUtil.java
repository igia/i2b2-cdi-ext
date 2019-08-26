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

import java.security.SecureRandom;

public class NumberUtil {
	
	private NumberUtil() {
	
	}
	
	private static SecureRandom randNumber;
	
	static {
		randNumber = new SecureRandom();
	}
	
	public static SecureRandom getRandomNumber() {
		return randNumber;
	}
	
	public static double getDoubleFromString(String input) {
		Double value = 999999.0;
		
		try {
			value = Double.parseDouble(input);
		}
		catch(Exception ex) {
			
		}
		
		return value;
	}
	
	public static int getIntegerFromString(String input) {
		
		int value = 999999; 
				
		try {
			value = Integer.parseInt(input);
		}
		catch (Exception ex) {
			
		}
		
		return value;
	}
	
	public static boolean isNumeric(String value) {
		try {
			Double.parseDouble(value);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
}
