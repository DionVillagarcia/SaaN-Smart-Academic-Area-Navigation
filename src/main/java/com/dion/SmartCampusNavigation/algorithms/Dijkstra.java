package com.dion.SmartCampusNavigation.algorithms;

import com.dion.SmartCampusNavigation.graph.Graph;
import com.dion.SmartCampusNavigation.model.Edge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

public class Dijkstra {
    public record Path(List<String> nodeIds, double totalCost, int temporaryEdgeCount) {
        public Path {
            nodeIds = List.copyOf(nodeIds);
            if (temporaryEdgeCount < 0 || temporaryEdgeCount > Math.max(0, nodeIds.size() - 1)) {
                throw new IllegalArgumentException("Invalid temporary edge count");
            }
        }

        public boolean usesTemporaryWeights() {
            return temporaryEdgeCount > 0;
        }

        public boolean usesOnlyTemporaryWeights() {
            return nodeIds.size() > 1 && temporaryEdgeCount == nodeIds.size() - 1;
        }

        public boolean isReachable() {
            return !nodeIds.isEmpty();
        }

        public static Path unreachable() {
            return new Path(List.of(), Double.POSITIVE_INFINITY, 0);
        }
    }

    private record Candidate(String nodeId, double cost) {
    }

    public Path findShortestPath(Graph graph, String source, String destination) {
        Objects.requireNonNull(graph, "graph");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(destination, "destination");

        if (!graph.containsNode(source) || !graph.containsNode(destination)) {
            return Path.unreachable();
        }
        if (source.equals(destination)) {
            return new Path(List.of(source), 0, 0);
        }

        Map<String, Double> distances = new HashMap<>();
        Map<String, Edge> previousEdges = new HashMap<>();
        for (String nodeId : graph.nodeIds()) {
            distances.put(nodeId, Double.POSITIVE_INFINITY);
        }
        distances.put(source, 0.0);

        PriorityQueue<Candidate> pending = new PriorityQueue<>(
                Comparator.comparingDouble(Candidate::cost).thenComparing(Candidate::nodeId));
        pending.add(new Candidate(source, 0));

        while (!pending.isEmpty()) {
            Candidate current = pending.remove();
            if (Double.compare(current.cost(), distances.get(current.nodeId())) != 0) {
                continue;
            }
            if (current.nodeId().equals(destination)) {
                break;
            }

            for (Edge edge : graph.outgoingEdges(current.nodeId())) {
                double candidateCost = current.cost() + edge.weight();
                if (candidateCost < distances.get(edge.toNodeId())) {
                    distances.put(edge.toNodeId(), candidateCost);
                    previousEdges.put(edge.toNodeId(), edge);
                    pending.add(new Candidate(edge.toNodeId(), candidateCost));
                }
            }
        }

        double totalCost = distances.get(destination);
        if (!Double.isFinite(totalCost)) {
            return Path.unreachable();
        }

        List<String> reversedPath = new ArrayList<>();
        int temporaryEdgeCount = 0;
        String current = destination;
        reversedPath.add(current);
        while (!current.equals(source)) {
            Edge previous = previousEdges.get(current);
            if (previous == null) {
                return Path.unreachable();
            }
            if (previous.usesTemporaryWeight()) {
                temporaryEdgeCount++;
            }
            current = previous.fromNodeId();
            reversedPath.add(current);
        }
        Collections.reverse(reversedPath);
        return new Path(reversedPath, totalCost, temporaryEdgeCount);
    }
}
