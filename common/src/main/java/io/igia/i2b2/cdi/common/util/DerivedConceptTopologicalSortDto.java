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
package io.igia.i2b2.cdi.common.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DerivedConceptTopologicalSortDto {
    private List<Integer> order;
    private String message;
    private Map<String, Integer> pathMap = new HashMap<>();

    public Map<String, Integer> getPathMap() {
	return pathMap;
    }

    public void setPathMap(Map<String, Integer> pathMap) {
	this.pathMap = pathMap;
    }

    public List<Integer> getOrder() {
	return order;
    }

    public void setOrder(List<Integer> order) {
	this.order = order;
    }

    public String getMessage() {
	return message;
    }

    public void setMessage(String message) {
	this.message = message;
    }

}
