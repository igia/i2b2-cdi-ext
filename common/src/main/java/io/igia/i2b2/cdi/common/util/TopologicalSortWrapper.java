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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.igia.i2b2.cdi.common.domain.DerivedConceptDefinition;
import io.igia.i2b2.cdi.common.domain.DerivedConceptDependency;

public class TopologicalSortWrapper {

    private static final Logger log = LoggerFactory.getLogger(TopologicalSortWrapper.class);

    public List<DerivedConceptDefinition> generateDependencyList(List<DerivedConceptDefinition> derivedConceptsList,
            List<DerivedConceptDependency> dependencies) throws CyclicDependencyException {
        Map<String, Integer> pathMap = new HashMap<>();
        int cnt = 0;
        
        for (int i = 0; i < dependencies.size(); i++) {
            final String derivedConceptPath = dependencies.get(i).getDerivedConceptPath();
            final Integer value1 = pathMap.get(derivedConceptPath);
            if (value1 == null) {
                pathMap.put(derivedConceptPath, cnt++);
            }
            dependencies.get(i).setDerivedConceptPathIndex(pathMap.get(derivedConceptPath));

            final String parentConceptPath = dependencies.get(i).getParentConceptPath();
            final Integer value2 = pathMap.get(parentConceptPath);
            if (value2 == null) {
                pathMap.put(parentConceptPath, cnt++);
            }
            dependencies.get(i).setParentConceptPathIndex(pathMap.get(parentConceptPath));
        }
                
        // Add edges and do topological sort
        Graph g = new Graph(pathMap.size());
        for (DerivedConceptDependency dependency : dependencies) {
            g.addEdge(dependency.getParentConceptPathIndex(), dependency.getDerivedConceptPathIndex());
        }
        
        g.topologicalSort();

        // Reverse the Key values in Map
        Map<Integer, String> reversedPathMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : pathMap.entrySet()) {
            reversedPathMap.put(entry.getValue(), entry.getKey());
        }

        // Print the dependency
        List<DerivedConceptDefinition> derivedConcepts = new ArrayList<>();
        log.info("Topological sort for derived concepts is : ");
        for (int i : g.getOrder()) {
            log.info("{} ", reversedPathMap.get(i));
            for (DerivedConceptDefinition derivedConcept : derivedConceptsList) {
                if (derivedConcept.getConceptPath().equals(reversedPathMap.get(i))) {
                    derivedConcepts.add(derivedConcept);
                }
            }
        }
        return derivedConcepts;
    }
    
    /**
     * Get derived concepts topological sequence.
     * @param derivedConcepts - List of derived concepts for sorting
     * @param dependencies - List of dependencies for cycle detection.
     * @return
     */
    public List<DerivedConceptDefinition> getDerivedConceptTopologicalSequence(
            List<DerivedConceptDefinition> derivedConcepts, List<DerivedConceptDependency> dependencies) {
        DerivedConceptTopologicalSortDto topologicalSortDto = detectDerivedConceptCyclicDependency(dependencies);
        // Reverse the Key values in Map
        Map<Integer, String> reversedPathMap = new HashMap<>();
        for (Map.Entry<String, Integer> entry : topologicalSortDto.getPathMap().entrySet()) {
            reversedPathMap.put(entry.getValue(), entry.getKey());
        }

        // Sort the dependency hierarchy
        List<DerivedConceptDefinition> sortedDerivedConcepts = new ArrayList<>();
        for (int i : topologicalSortDto.getOrder()) {
            for (DerivedConceptDefinition derivedConcept : derivedConcepts) {
                if (derivedConcept.getConceptPath().equals(reversedPathMap.get(i))) {
                    sortedDerivedConcepts.add(derivedConcept);
                }
            }
        }
        return sortedDerivedConcepts;
    }
    
    private DerivedConceptTopologicalSortDto detectDerivedConceptCyclicDependency(
            List<DerivedConceptDependency> dependencies) {
        Map<String, Integer> pathMap = new HashMap<>();
        int[] incrementer = { 0 };

        for (int i = 0; i < dependencies.size(); i++) {
            final String derivedConceptPath = dependencies.get(i).getDerivedConceptPath();
            pathMap.computeIfAbsent(derivedConceptPath, conceptPath -> pathMap.put(conceptPath, incrementer[0]++));

            dependencies.get(i).setDerivedConceptPathIndex(pathMap.get(derivedConceptPath));

            final String parentConceptPath = dependencies.get(i).getParentConceptPath();
            pathMap.computeIfAbsent(parentConceptPath, conceptPath -> incrementer[0]++);

            dependencies.get(i).setParentConceptPathIndex(pathMap.get(parentConceptPath));
        }

        // Add edges and do topological sort
        TopologicalSortGraph graph = new TopologicalSortGraph(pathMap.size());
        for (DerivedConceptDependency dependency : dependencies) {
            graph.addEdge(dependency.getParentConceptPathIndex(), dependency.getDerivedConceptPathIndex());
        }
        graph.topologicalSort();
        DerivedConceptTopologicalSortDto derivedConceptTopologicalSortDto = new DerivedConceptTopologicalSortDto();
        derivedConceptTopologicalSortDto.setOrder(graph.getOrder());
        derivedConceptTopologicalSortDto.setMessage(graph.getMessage());
        derivedConceptTopologicalSortDto.setPathMap(pathMap);
        return derivedConceptTopologicalSortDto;
    }
}
