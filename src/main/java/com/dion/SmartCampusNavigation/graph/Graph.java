package com.dion.SmartCampusNavigation.graph;

import com.dion.SmartCampusNavigation.model.Edge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Graph {
    private final Map<String, List<Edge>> outgoingEdges = new LinkedHashMap<>();

    public void addNode(String nodeId) {
        if (nodeId == null || nodeId.isBlank()) {
            throw new IllegalArgumentException("Node ID cannot be blank");
        }
        outgoingEdges.computeIfAbsent(nodeId, ignored -> new ArrayList<>());
    }

    public void addDirectedEdge(Edge edge) {
        Objects.requireNonNull(edge, "edge");
        addNode(edge.fromNodeId());
        addNode(edge.toNodeId());
        outgoingEdges.get(edge.fromNodeId()).add(edge);
    }

    public List<Edge> outgoingEdges(String nodeId) {
        return List.copyOf(outgoingEdges.getOrDefault(nodeId, List.of()));
    }

    public Set<String> nodeIds() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(outgoingEdges.keySet()));
    }

    public boolean containsNode(String nodeId) {
        return outgoingEdges.containsKey(nodeId);
    }

}
