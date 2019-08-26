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
package io.igia.i2b2.cdi.common.cache;

import org.springframework.stereotype.Component;

@Component
public class EncounterNextValCache {
	private int nextValue;
	
	public synchronized void setNextValue(int val) {
		nextValue = val;
	}
	
	public synchronized int getNextVal() {
		nextValue = nextValue + 1;
		return nextValue;
	}
}