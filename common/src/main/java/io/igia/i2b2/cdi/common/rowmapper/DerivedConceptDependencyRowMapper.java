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
package io.igia.i2b2.cdi.common.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import io.igia.i2b2.cdi.common.domain.DerivedConceptDependency;

@Component
public class DerivedConceptDependencyRowMapper implements RowMapper<DerivedConceptDependency> {

    @Override
    public DerivedConceptDependency mapRow(ResultSet rs, int rowNum) throws SQLException {
        DerivedConceptDependency derivedConceptDependency = new DerivedConceptDependency();
        derivedConceptDependency.setId(rs.getInt("id"));
        derivedConceptDependency.setDerivedConceptId(rs.getInt("derived_concept_id"));
        derivedConceptDependency.setDerivedConceptPath(rs.getString("concept_path"));
        derivedConceptDependency.setParentConceptPath(rs.getString("parent_concept_path"));
        return derivedConceptDependency;
    }
}