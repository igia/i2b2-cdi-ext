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
package io.igia.i2b2.cdi.common.domain;

import java.util.Objects;

public class DerivedConceptDependency {

    private Integer id;
    private Integer derivedConceptId;
    private String derivedConceptPath;
    private String parentConceptPath;
    private Integer derivedConceptPathIndex;
    private Integer parentConceptPathIndex;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getDerivedConceptId() {
        return derivedConceptId;
    }

    public void setDerivedConceptId(Integer derivedConceptId) {
        this.derivedConceptId = derivedConceptId;
    }

    public String getDerivedConceptPath() {
        return derivedConceptPath;
    }

    public void setDerivedConceptPath(String derivedConceptPath) {
        this.derivedConceptPath = derivedConceptPath;
    }

    public String getParentConceptPath() {
        return parentConceptPath;
    }

    public void setParentConceptPath(String parentConceptPath) {
        this.parentConceptPath = parentConceptPath;
    }

    public Integer getDerivedConceptPathIndex() {
        return derivedConceptPathIndex;
    }

    public void setDerivedConceptPathIndex(Integer derivedConceptPathIndex) {
        this.derivedConceptPathIndex = derivedConceptPathIndex;
    }

    public Integer getParentConceptPathIndex() {
        return parentConceptPathIndex;
    }

    public void setParentConceptPathIndex(Integer parentConceptPathIndex) {
        this.parentConceptPathIndex = parentConceptPathIndex;
    }
    
    @Override
    public int hashCode() {
    return Objects.hash(derivedConceptId, parentConceptPath);
    }

    @Override
    public boolean equals(Object obj) {
    if (this == obj) {
        return true;
    }
    if (obj == null) {
        return false;
    }
    if (!(obj instanceof DerivedConceptDependency)) {
        return false;
    }
    DerivedConceptDependency other = (DerivedConceptDependency) obj;
    return Objects.equals(derivedConceptId, other.derivedConceptId)
        && Objects.equals(parentConceptPath, other.parentConceptPath);
    }
}
