package com.dion.SmartCampusNavigation;

import com.dion.SmartCampusNavigation.data.CampusDataLoader;
import com.dion.SmartCampusNavigation.algorithms.Dijkstra;
import com.dion.SmartCampusNavigation.model.Building;
import com.dion.SmartCampusNavigation.services.RouteServices;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        CampusDataLoader dataLoader = new CampusDataLoader();
        RouteServices routeServices = new RouteServices(dataLoader);
        Label connectionStatus = new Label();
        List<Building> buildings = List.of();
        boolean connected;
        try {
            dataLoader.initializeAndLoad();
            connected = routeServices.testConnection();
            if (connected) {
                buildings = routeServices.listBuildings();
            }
            connectionStatus.setText(connected
                    ? "Connected to SQLite; campus CSV data loaded."
                    : "SQLite connection check failed.");
        } catch (SQLException | IOException exception) {
            connected = false;
            connectionStatus.setText("SQLite connection failed: " + exception.getMessage());
        }

        ComboBox<Building> currentLocation = createBuildingDropdown("Select current building");
        ComboBox<Building> destination = createBuildingDropdown("Select destination building");
        currentLocation.getItems().setAll(buildings);
        destination.getItems().setAll(buildings);
        Map<String, String> buildingNames = buildings.stream().collect(
                Collectors.toMap(Building::buildingId, Building::buildingName));
        Label routeResult = new Label("Choose buildings to find a shortest route.");
        Button findRoute = new Button("Shortest Route");
        findRoute.setDisable(!connected);
        findRoute.setOnAction(event -> {
            Building start = currentLocation.getValue();
            Building end = destination.getValue();
            if (start == null || end == null) {
                routeResult.setText("Select both a current building and a destination.");
                return;
            }
            try {
                if (!routeServices.testConnection()) {
                    routeResult.setText("SQLite connection check failed.");
                    return;
                }
                Dijkstra.Path route = routeServices.findBuildingRoute(
                        start.buildingId(), end.buildingId());
                if (!route.isReachable()) {
                    routeResult.setText("No route found between those buildings.");
                    return;
                }
                String pathText = route.nodeIds().stream()
                        .map(nodeId -> buildingNames.getOrDefault(nodeId, nodeId))
                        .collect(Collectors.joining(" -> "));
                String costText;
                if (route.nodeIds().size() == 1) {
                    costText = "Path cost: 0 (same building)";
                } else if (route.usesOnlyTemporaryWeights()) {
                    costText = "Temporary edge cost: " + route.totalCost()
                            + " pathway edges (not physical distance)";
                } else if (route.usesTemporaryWeights()) {
                    costText = "Weighted path cost: " + route.totalCost()
                            + " (missing distances use 1; not physical distance)";
                } else {
                    costText = "Path cost: " + route.totalCost() + " CSV distance units";
                }
                routeResult.setText("Route: " + pathText + "\n" + costText);
            } catch (SQLException exception) {
                routeResult.setText("Unable to find route: " + exception.getMessage());
            }
        });

        VBox content = new VBox(
                10,
                new Label("SQLite connection"),
                connectionStatus,
                new Label("Current location"),
                currentLocation,
                new Label("Destination"),
                destination,
                findRoute,
                routeResult);
        content.setPadding(new Insets(20));
        stage.setTitle("Smart Campus Navigation");
        stage.setScene(new Scene(content, 480, 360));
        stage.show();
    }

    private static ComboBox<Building> createBuildingDropdown(String prompt) {
        ComboBox<Building> dropdown = new ComboBox<>();
        dropdown.setPromptText(prompt);
        dropdown.setMaxWidth(Double.MAX_VALUE);
        dropdown.setCellFactory(list -> createBuildingCell());
        dropdown.setButtonCell(createBuildingCell());
        return dropdown;
    }

    private static ListCell<Building> createBuildingCell() {
        return new ListCell<>() {
            @Override
            protected void updateItem(Building building, boolean empty) {
                super.updateItem(building, empty);
                setText(empty || building == null ? null : building.buildingName());
            }
        };
    }

    public static void main(String[] args) {
        launch(args);
    }
}