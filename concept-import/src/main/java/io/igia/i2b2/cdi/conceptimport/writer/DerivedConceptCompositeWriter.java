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
package io.igia.i2b2.cdi.conceptimport.writer;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import io.igia.i2b2.cdi.common.config.DataSourceMetaInfoConfig;
import io.igia.i2b2.cdi.common.config.I2b2SchemaProperties;
import io.igia.i2b2.cdi.common.domain.DerivedConceptDefinition;
import io.igia.i2b2.cdi.common.domain.DerivedConceptDependency;
import io.igia.i2b2.cdi.common.helper.ConceptHelper;
import io.igia.i2b2.cdi.common.rowmapper.DerivedConceptDependencyRowMapper;
import io.igia.i2b2.cdi.common.rowmapper.DerivedConceptRowMapper;
import io.igia.i2b2.cdi.common.util.CyclicDependencyException;
import io.igia.i2b2.cdi.common.util.TopologicalSortWrapper;

public class DerivedConceptCompositeWriter implements ItemWriter<DerivedConceptDefinition> {

    private static final Logger log = LoggerFactory.getLogger(DerivedConceptCompositeWriter.class);
    private DataSource dataSource = null;
    private DataSourceMetaInfoConfig dataSourceMetaInfoConfig;
    private I2b2SchemaProperties i2b2Properties;
    private JdbcBatchItemWriter<DerivedConceptDefinition> derivedConceptWriter;

    public DerivedConceptCompositeWriter(DataSource dataSource, DataSourceMetaInfoConfig dataSourceMetaInfoConfig,
            I2b2SchemaProperties i2b2Properties, JdbcBatchItemWriter<DerivedConceptDefinition> derivedConceptWriter) {
        this.dataSource = dataSource;
        this.dataSourceMetaInfoConfig = dataSourceMetaInfoConfig;
        this.i2b2Properties = i2b2Properties;
        this.derivedConceptWriter = derivedConceptWriter;
    }

    @Override
    public void write(List<? extends DerivedConceptDefinition> derivedConcepts) throws Exception {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        NamedParameterJdbcTemplate namedParamTemplate = new NamedParameterJdbcTemplate(dataSource);
        List<DerivedConceptDependency> dependencies = new ArrayList<>();

        for (DerivedConceptDefinition derivedConceptDefinition : derivedConcepts) {

            if (derivedConceptDefinition.getDependsOn().isEmpty()) {
                continue;
            }

            List<String> dependencyList = Arrays.asList(derivedConceptDefinition.getDependsOn().split(","));

            for (String dependency : dependencyList) {
                DerivedConceptDependency dependencyObj = new DerivedConceptDependency();
                dependencyObj.setDerivedConceptPath(derivedConceptDefinition.getConceptPath());

                dependency = dependency.trim();
                dependency = dependency.replace("/", i2b2Properties.getI2b2Separator());
                dependency = ConceptHelper.removeSeparatorAtFirstAndLastIfExists(dependency, i2b2Properties);
                dependency = ConceptHelper.getFullPathName(dependency, i2b2Properties);
                dependencyObj.setParentConceptPath(dependency.trim());
                dependencies.add(dependencyObj);
            }
        }

        // get the existing derived concepts list for cyclic dependency check
        List<DerivedConceptDefinition> existingDerivedConcepts = getExistingDerivedConcepts(jdbcTemplate);
        existingDerivedConcepts.addAll(derivedConcepts);

        // get the existing dependencies list for cyclic dependency check
        List<DerivedConceptDependency> existingDependencies = getExistingDependencies(jdbcTemplate);
        existingDependencies.addAll(dependencies);

        List<DerivedConceptDefinition> concepts = null;
        try {
            concepts = new TopologicalSortWrapper().generateDependencyList(existingDerivedConcepts,existingDependencies);
            derivedConceptWriter.write(derivedConcepts);
            
            // Get id's of recently added derived concepts for further processing
            Map<String, Integer> recentlyAddedConcepts = getDerivedConceptMap(namedParamTemplate, derivedConcepts);
            
            // Add derived concept id's for dependencies
            dependencies = getDependencies(dependencies, recentlyAddedConcepts);
            
            saveDependencyBatch(jdbcTemplate, dependencies);
        } catch (CyclicDependencyException e) {
            log.error("Skipping insertion of derived concepts and dependencies because of : {}", e.getMessage());
            log.debug("{}", concepts);
            log.error("{}", e);
        }
    }

    private List<DerivedConceptDependency> getDependencies(List<DerivedConceptDependency> dependencies,
            Map<String, Integer> recentlyAddedConcepts) {
        for (int i = 0; i < dependencies.size(); i++) {
            Integer derivedConceptId = recentlyAddedConcepts.get(dependencies.get(i).getDerivedConceptPath());
            if (derivedConceptId != null) {
                dependencies.get(i).setDerivedConceptId(derivedConceptId);
            }
        }
        return dependencies;
    }

    private Map<String, Integer> getDerivedConceptMap(NamedParameterJdbcTemplate namedParamTemplate,
            List<? extends DerivedConceptDefinition> derivedConcepts) {

        List<String> conceptPaths = new ArrayList<>();
        for (DerivedConceptDefinition concept : derivedConcepts) {
            conceptPaths.add(concept.getConceptPath());
        }

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("conceptPaths", conceptPaths);

        // Query for recently added derived concepts
        String sql = "SELECT id, concept_path from " + dataSourceMetaInfoConfig.getDemodataSchemaName()
                + "derived_concept_definition WHERE concept_path in (:conceptPaths)";
        List<DerivedConceptDefinition> concepts =  namedParamTemplate.query(sql, parameters, new RowMapper<DerivedConceptDefinition>() {

            @Override
            public DerivedConceptDefinition mapRow(ResultSet rs, int rowNum) throws SQLException {
                DerivedConceptDefinition concept = new DerivedConceptDefinition();
                concept.setId(rs.getInt("id"));
                concept.setConceptPath(rs.getString("concept_path"));
                return concept;
            }
        });
        
        // Convert concepts list to map
        Map<String, Integer> conceptsMap = new HashMap<>();
        for(DerivedConceptDefinition concept: concepts) {
            conceptsMap.put(concept.getConceptPath(), concept.getId());
        }
        return conceptsMap;
    }

    private List<DerivedConceptDefinition> getExistingDerivedConcepts(JdbcTemplate jdbcTemplate) {
        String sql = "SELECT * from " + dataSourceMetaInfoConfig.getDemodataSchemaName()
                + "derived_concept_definition ";
        return jdbcTemplate.query(sql, new DerivedConceptRowMapper());
    }

    private List<DerivedConceptDependency> getExistingDependencies(JdbcTemplate jdbcTemplate) {
        String sql = "SELECT dependency.id, dependency.derived_concept_id, dependency.parent_concept_path, "
                + "concept.concept_path FROM " + dataSourceMetaInfoConfig.getDemodataSchemaName() + ""
                + "derived_concept_dependency dependency " 
                + "INNER JOIN " + dataSourceMetaInfoConfig.getDemodataSchemaName() 
                + "derived_concept_definition concept "
                + "ON dependency.derived_concept_id = concept.id;";
        return jdbcTemplate.query(sql, new DerivedConceptDependencyRowMapper());
    }

    public void saveDependencyBatch(JdbcTemplate jdbcTemplate, final List<DerivedConceptDependency> dependencies) {
        jdbcTemplate.batchUpdate("INSERT INTO " + dataSourceMetaInfoConfig.getDemodataSchemaName()
                + "derived_concept_dependency (derived_concept_id, parent_concept_path) VALUES (?, ?)", new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setInt(1, dependencies.get(i).getDerivedConceptId());
                        ps.setString(2, dependencies.get(i).getParentConceptPath());
                    }

                    public int getBatchSize() {
                        return dependencies.size();
                    }

                });
    }
}