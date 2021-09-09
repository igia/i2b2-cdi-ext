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
package io.igia.i2b2.cdi.derivedfact.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import io.igia.i2b2.cdi.common.domain.DerivedConceptDependency;
import io.igia.i2b2.cdi.derivedfact.util.DerivedConceptDependencyHierarchy;

@Component
public class DerivedConceptDependencyMapper implements RowMapper<DerivedConceptDependency> {

    @Override
    public DerivedConceptDependency mapRow(ResultSet rs, int rowNum) throws SQLException {
        DerivedConceptDependency derivedConceptDependencyDto = new DerivedConceptDependency();
        derivedConceptDependencyDto.setId(rs.getInt(DerivedConceptDependencyHierarchy.ID));
        derivedConceptDependencyDto
                .setDerivedConceptId(rs.getInt(DerivedConceptDependencyHierarchy.DERIVED_CONCEPT_ID));
        derivedConceptDependencyDto
                .setParentConceptPath(rs.getString(DerivedConceptDependencyHierarchy.PARENT_CONCEPT_PATH));
        derivedConceptDependencyDto
                .setDerivedConceptPath(rs.getString(DerivedConceptDependencyHierarchy.DERIVED_CONCEPT_PATH));
        return derivedConceptDependencyDto;
    }
}
