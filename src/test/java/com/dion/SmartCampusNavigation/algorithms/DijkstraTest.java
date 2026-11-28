package com.dion.SmartCampusNavigation.algorithms;

import com.dion.SmartCampusNavigation.graph.Graph;
import com.dion.SmartCampusNavigation.model.Edge;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DijkstraTest {
    private final Dijkstra dijkstra = new Dijkstra();

    @Test
    public void findsGateToBusinessExampleInRouteFixture() {
        assertExamplePath(
                List.of("Gate", "P1", "P2", "P3", "P4",
                        "Engineering Ground", "J2", "J3", "Business Building"));
    }

    @Test
    public void findsGateToAquariumExampleInRouteFixture() {
        assertExamplePath(
                List.of("Gate", "P1", "P2", "P3", "P4", "P5", "P6", "P7", "Aquarium"));
    }

    @Test
    public void findsGateToCanteenExampleInRouteFixture() {
        assertExamplePath(List.of("Gate", "P1", "Guard House", "Canteen"));
    }

    @Test
    public void findsCanteenToBusinessExampleInRouteFixture() {
        assertExamplePath(
                List.of("Canteen", "Guard House", "P1", "P2", "P3", "P4",
                        "Engineering Ground", "J2", "J3", "Business Building"));
    }

    @Test
    public void doesNotInventReverseForOneWayPathway() {
        Graph graph = new Graph();
        graph.addDirectedEdge(new Edge("A", "B"));

        assertTrue(dijkstra.findShortestPath(graph, "A", "B").isReachable());
        assertFalse(dijkstra.findShortestPath(graph, "B", "A").isReachable());
    }

    @Test
    public void reciprocalPathwayRowsAllowBothDirections() {
        Graph graph = new Graph();
        graph.addDirectedEdge(new Edge("A", "B"));
        graph.addDirectedEdge(new Edge("B", "A"));

        assertEquals(List.of("A", "B"), dijkstra.findShortestPath(graph, "A", "B").nodeIds());
        assertEquals(List.of("B", "A"), dijkstra.findShortestPath(graph, "B", "A").nodeIds());
    }

    @Test
    public void returnsNoPathForUnreachableDestination() {
        Graph graph = new Graph();
        graph.addNode("A");
        graph.addNode("B");

        Dijkstra.Path path = dijkstra.findShortestPath(graph, "A", "B");

        assertFalse(path.isReachable());
        assertTrue(Double.isInfinite(path.totalCost()));
    }

    @Test
    public void sameSourceAndDestinationHasZeroCost() {
        Graph graph = new Graph();
        graph.addNode("A");

        Dijkstra.Path path = dijkstra.findShortestPath(graph, "A", "A");

        assertEquals(List.of("A"), path.nodeIds());
        assertEquals(0.0, path.totalCost(), 0.0);
    }

    @Test
    public void usesActualDistanceAndTemporaryWeightForMissingDistance() {
        Graph graph = new Graph();
        graph.addDirectedEdge(new Edge("A", "B", 8.0));
        graph.addDirectedEdge(new Edge("A", "C"));
        graph.addDirectedEdge(new Edge("C", "B"));

        Dijkstra.Path path = dijkstra.findShortestPath(graph, "A", "B");

        assertEquals(List.of("A", "C", "B"), path.nodeIds());
        assertEquals(2.0, path.totalCost(), 0.0);
        assertTrue(path.usesTemporaryWeights());
        assertTrue(path.usesOnlyTemporaryWeights());
        assertEquals(Edge.TEMPORARY_WEIGHT, new Edge("X", "Y").weight(), 0.0);
    }

    @Test
    public void prefersAnActualDistanceWhenItHasLowerCost() {
        Graph graph = new Graph();
        graph.addDirectedEdge(new Edge("A", "B", 0.5));
        graph.addDirectedEdge(new Edge("A", "C"));
        graph.addDirectedEdge(new Edge("C", "B"));

        Dijkstra.Path path = dijkstra.findShortestPath(graph, "A", "B");

        assertEquals(List.of("A", "B"), path.nodeIds());
        assertEquals(0.5, path.totalCost(), 0.0);
        assertFalse(path.usesTemporaryWeights());
    }

    @Test
    public void identifiesMixedActualAndTemporaryPathCosts() {
        Graph graph = new Graph();
        graph.addDirectedEdge(new Edge("A", "B", 0.25));
        graph.addDirectedEdge(new Edge("B", "D"));
        graph.addDirectedEdge(new Edge("A", "C"));
        graph.addDirectedEdge(new Edge("C", "D"));

        Dijkstra.Path path = dijkstra.findShortestPath(graph, "A", "D");

        assertEquals(List.of("A", "B", "D"), path.nodeIds());
        assertEquals(1.25, path.totalCost(), 0.0);
        assertTrue(path.usesTemporaryWeights());
        assertFalse(path.usesOnlyTemporaryWeights());
    }

    private void assertExamplePath(List<String> expectedPath) {
        Graph graph = routeExampleFixture();
        Dijkstra.Path path = dijkstra.findShortestPath(
                graph, expectedPath.get(0), expectedPath.get(expectedPath.size() - 1));

        assertEquals(expectedPath, path.nodeIds());
        assertEquals(expectedPath.size() - 1, path.totalCost(), 0.0);
        assertTrue(path.usesTemporaryWeights());
        assertTrue(path.usesOnlyTemporaryWeights());
    }

    private Graph routeExampleFixture() {
        Graph graph = new Graph();
        addPath(graph, "Gate", "P1", "P2", "P3", "P4",
                "Engineering Ground", "J2", "J3", "Business Building");
        addPath(graph, "Gate", "P1", "P2", "P3", "P4", "P5", "P6", "P7", "Aquarium");
        addPath(graph, "Gate", "P1", "Guard House", "Canteen");
        addPath(graph, "Canteen", "Guard House", "P1", "P2", "P3", "P4",
                "Engineering Ground", "J2", "J3", "Business Building");
        return graph;
    }

    private void addPath(Graph graph, String... nodeIds) {
        for (int index = 0; index + 1 < nodeIds.length; index++) {
            graph.addDirectedEdge(new Edge(nodeIds[index], nodeIds[index + 1]));
        }
    }
}
