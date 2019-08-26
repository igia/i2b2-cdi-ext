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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class ProviderCache {
	private Map<String, Integer> providers = new ConcurrentHashMap<>();
	
	public void putData(String provider) {
		providers.put(provider, 0);
	}
	
	public int getData(String provider) {
		return providers.get(provider);
	}
	
	public boolean containsProvider(String provider) {
		return providers.containsKey(provider);
	}
	
	public void clearCache() {
		providers.clear();
	}
}
