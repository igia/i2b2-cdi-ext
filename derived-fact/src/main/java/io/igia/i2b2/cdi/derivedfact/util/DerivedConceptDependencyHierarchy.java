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
package io.igia.i2b2.cdi.derivedfact.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import io.igia.i2b2.cdi.common.domain.DerivedConceptDependency;
import io.igia.i2b2.cdi.derivedfact.rowmapper.DerivedConceptDependencyMapper;

public class DerivedConceptDependencyHierarchy {

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public DerivedConceptDependencyHierarchy(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    public static final String ID = "id";
    public static final String DERIVED_CONCEPT_ID = "derivedConceptId";
    public static final String PARENT_CONCEPT_PATH = "parentConceptPath";
    public static final String DERIVED_CONCEPT_PATH = "derivedConceptPath";

    public List<Set<DerivedConceptDependency>> getAllDerivedConceptDependencyHierarchy() {
        List<DerivedConceptDependency> derivedConceptDependencies = getAllDerivedConceptDependencies();
        Set<Set<DerivedConceptDependency>> dependencyHierarchies = new HashSet<>();
        derivedConceptDependencies.parallelStream().forEach(dependency -> {
            List<DerivedConceptDependency> dependencies = new ArrayList<>();
            dependencies.add(dependency);
            Set<DerivedConceptDependency> dependencyHierarchy = getDerivedConceptDependencyHierarchy(dependencies);
            dependencyHierarchies.add(dependencyHierarchy);
        });
        return new ArrayList<>(dependencyHierarchies);
    }

    private List<DerivedConceptDependency> getAllDerivedConceptDependencies() {
        final String query = String.join(" ",
                "SELECT dependency.id as " + ID + ", dependency.derived_concept_id as " + DERIVED_CONCEPT_ID,
                ", dependency.parent_concept_path as " + PARENT_CONCEPT_PATH + ", derivedconcept.concept_path as "
                        + DERIVED_CONCEPT_PATH,
                "FROM derived_concept_dependency dependency INNER JOIN derived_concept_definition derivedconcept",
                "ON dependency.derived_concept_id = derivedconcept.id");

        return this.namedParameterJdbcTemplate.query(query, new DerivedConceptDependencyMapper());
    }

    private Set<DerivedConceptDependency> getDerivedConceptDependencyHierarchy(
            List<DerivedConceptDependency> derivedConceptDependencies) {
        Set<String> conceptPath = new HashSet<>();

        derivedConceptDependencies.stream().forEach(dependency -> {
            conceptPath.add(dependency.getDerivedConceptPath());
            conceptPath.add(dependency.getParentConceptPath());
        });

        List<DerivedConceptDependency> allDerivedConceptDependencies = new ArrayList<>();
        allDerivedConceptDependencies.addAll(derivedConceptDependencies);

        Set<DerivedConceptDependency> derivedConceptDependencyHierarchy = new HashSet<>();
        boolean[] dependencyFound = { true };
        while (dependencyFound[0]) {
            dependencyFound[0] = false;

            List<DerivedConceptDependency> derivedConceptDependenciesByPath = getDerivedConceptDependency(conceptPath);

            allDerivedConceptDependencies.addAll(derivedConceptDependenciesByPath);

            if (!allDerivedConceptDependencies.isEmpty()) {
                allDerivedConceptDependencies.forEach(dependency -> {
                    if (derivedConceptDependencyHierarchy.add(dependency)) {
                        dependencyFound[0] = true;
                        conceptPath.add(dependency.getParentConceptPath());
                        conceptPath.add(dependency.getDerivedConceptPath());
                    }
                });
            }
        }
        return derivedConceptDependencyHierarchy;
    }

    private List<DerivedConceptDependency> getDerivedConceptDependency(Set<String> conceptPath) {
        List<DerivedConceptDependency> derivedConceptDependencies = new ArrayList<>();
        if (conceptPath != null && !conceptPath.isEmpty()) {
            MapSqlParameterSource parameterSource = new MapSqlParameterSource();
            final String query = String.join(" ",
                    "SELECT dependency.id as " + ID + ", dependency.derived_concept_id as " + DERIVED_CONCEPT_ID,
                    ", dependency.parent_concept_path as " + PARENT_CONCEPT_PATH + ", derivedconcept.concept_path as "
                            + DERIVED_CONCEPT_PATH,
                    "FROM derived_concept_dependency dependency",
                    "INNER JOIN derived_concept_definition derivedconcept",
                    "ON dependency.derived_concept_id = derivedconcept.id WHERE parent_concept_path in (:"
                            + PARENT_CONCEPT_PATH + ") OR concept_path in (:" + DERIVED_CONCEPT_PATH + ")");

            parameterSource.addValue(PARENT_CONCEPT_PATH, conceptPath);
            parameterSource.addValue(DERIVED_CONCEPT_PATH, conceptPath);
            derivedConceptDependencies = this.namedParameterJdbcTemplate.query(query, parameterSource,
                    new DerivedConceptDependencyMapper());
        }
        return derivedConceptDependencies;
    }
}