package com.dion.SmartCampusNavigation.services;

import com.dion.SmartCampusNavigation.algorithms.Dijkstra;
import com.dion.SmartCampusNavigation.data.CampusDataLoader;
import com.dion.SmartCampusNavigation.graph.Graph;
import com.dion.SmartCampusNavigation.model.Building;
import com.dion.SmartCampusNavigation.model.Edge;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class RouteServices {
    private final CampusDataLoader dataLoader;

    public RouteServices(CampusDataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    public boolean testConnection() throws SQLException {
        return dataLoader.testConnection();
    }

    public List<Building> listBuildings() throws SQLException {
        List<Building> buildings = new ArrayList<>();
        try (Connection connection = dataLoader.openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT building_id, building_name FROM buildings ORDER BY building_name")) {
            while (resultSet.next()) {
                buildings.add(new Building(
                        resultSet.getString("building_id"),
                        resultSet.getString("building_name")));
            }
        }
        return List.copyOf(buildings);
    }

    public Dijkstra.Path findBuildingRoute(String startBuildingId, String destinationBuildingId)
            throws SQLException {
        Graph graph = new Graph();
        try (Connection connection = dataLoader.openConnection()) {
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("SELECT building_id FROM buildings")) {
                while (resultSet.next()) {
                    graph.addNode(resultSet.getString("building_id"));
                }
            }
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery("""
                         SELECT from_node_id, to_node_id, distance
                         FROM pathways
                         ORDER BY from_node_id, to_node_id
                         """)) {
                while (resultSet.next()) {
                    double storedDistance = resultSet.getDouble("distance");
                    Double distance = resultSet.wasNull() ? null : storedDistance;
                    graph.addDirectedEdge(new Edge(
                            resultSet.getString("from_node_id"),
                            resultSet.getString("to_node_id"),
                            distance));
                }
            }
        }
        return new Dijkstra().findShortestPath(graph, startBuildingId, destinationBuildingId);
    }

    public Dijkstra.Path findRouteBetweenLocations(
            String startLocationId,
            String destinationLocationId)
            throws SQLException {
        String startBuildingId = findBuildingId(startLocationId);
        String destinationBuildingId = findBuildingId(destinationLocationId);
        return findBuildingRoute(startBuildingId, destinationBuildingId);
    }

    public Dijkstra.Path findRoute(String startId, String destinationId) throws SQLException {
        return findBuildingRoute(resolveBuildingId(startId), resolveBuildingId(destinationId));
    }

    private String resolveBuildingId(String buildingOrLocationId) throws SQLException {
        try (Connection connection = dataLoader.openConnection();
             PreparedStatement statement = connection.prepareStatement("""
                     SELECT building_id FROM buildings WHERE building_id = ?
                     UNION ALL
                     SELECT building_id FROM locations WHERE location_id = ?
                     LIMIT 1
                     """)) {
            statement.setString(1, buildingOrLocationId);
            statement.setString(2, buildingOrLocationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException(
                            "Unknown building or location ID: " + buildingOrLocationId);
                }
                return resultSet.getString("building_id");
            }
        }
    }

    private String findBuildingId(String locationId) throws SQLException {
        try (Connection connection = dataLoader.openConnection();
             PreparedStatement statement = connection.prepareStatement(
                     "SELECT building_id FROM locations WHERE location_id = ?")) {
            statement.setString(1, locationId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("Unknown location ID: " + locationId);
                }
                return resultSet.getString("building_id");
            }
        }
    }
}
