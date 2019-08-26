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
package io.igia.i2b2.cdi.conceptimport.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import io.igia.i2b2.cdi.common.domain.CsvConceptMapping;

@Component
public class I2b2ConceptMappingRowMapper implements RowMapper<CsvConceptMapping> {
	
	@Override
	public CsvConceptMapping mapRow(ResultSet rs, int rowNum) throws SQLException {		
		CsvConceptMapping concept = new CsvConceptMapping();
		
		concept.setStdCode(rs.getString("std_cd"));
		concept.setLocalCode(rs.getString("local_cd"));
		concept.setLocalCodeName(rs.getString("local_cd_name"));
		
		return concept;
	}
}