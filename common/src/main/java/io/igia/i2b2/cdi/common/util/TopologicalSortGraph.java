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
/**
 * ACKNOWLEDGEMENT:
 * A Java program to print topological sorting of a graph using indegrees. Code snippet in this file has been 
 * taken  from below link. 
 * https://www.geeksforgeeks.org/topological-sorting-indegree-based-solution/
 */
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

//Class to represent a graph 
public class TopologicalSortGraph {
    int v;// No. of vertices

    // An Array of List which contains
    // references to the Adjacency List of
    // each vertex
    List<Integer>[] adj;
    List<Integer> order = new ArrayList<>();
    private String message;

    public TopologicalSortGraph(int v)// Constructor
    {
    this.v = v;
    adj = new ArrayList[v];
    for (int i = 0; i < v; i++)
        adj[i] = new ArrayList<>();
    }

    public List<Integer> getOrder() {
    return order;
    }

    public void setOrder(List<Integer> order) {
    this.order = order;
    }

    // function to add an edge to graph
    public void addEdge(int u, int v) {
    adj[u].add(v);
    }

    // prints a Topological Sort of the complete graph
    public TopologicalSortGraph topologicalSort() {
    // Create a array to store indegrees of all
    // vertices. Initialize all indegrees as 0.
    int[] indegree = new int[v];

    // Traverse adjacency lists to fill indegrees of
    // vertices. This step takes O(V+E) time
    for (int i = 0; i < v; i++) {
        List<Integer> temp = (ArrayList<Integer>) adj[i];
        for (int node : temp) {
        indegree[node]++;
        }
    }

    // Create a queue and enqueue all vertices with
    // indegree 0
    Queue<Integer> q = new LinkedList<>();
    for (int i = 0; i < v; i++) {
        if (indegree[i] == 0)
        q.add(i);
    }

    // Initialize count of visited vertices
    int cnt = 0;

    // Create a list to store result (A topological
    // ordering of the vertices)
    List<Integer> topOrder = new ArrayList<>();
    while (!q.isEmpty()) {
        // Extract front of queue (or perform dequeue)
        // and add it to topological order
        int u = q.poll();
        topOrder.add(u);

        // Iterate through all its neighbouring nodes
        // of dequeued node u and decrease their in-degree
        // by 1
        for (int node : adj[u]) {
        // If in-degree becomes zero, add it to queue
        if (--indegree[node] == 0)
            q.add(node);
        }
        cnt++;
    }

    // Check if there was a cycle
    if (cnt != v) {
        setMessage("There exists a cycle in the graph !!");
        setExecutionOrder(topOrder);
        return this;
    }

    // Store topological order
    setExecutionOrder(topOrder);
    return this;
    }

    private void setExecutionOrder(List<Integer> topOrder) {
    if (order == null)
        order = new ArrayList<>();
    for (int i : topOrder) {
        order.add(i);
    }
    }

    public String getMessage() {
    return message;
    }

    public void setMessage(String message) {
    this.message = message;
    }
}
