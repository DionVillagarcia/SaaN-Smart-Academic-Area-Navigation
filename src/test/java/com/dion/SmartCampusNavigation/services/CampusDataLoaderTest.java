package com.dion.SmartCampusNavigation.services;

import com.dion.SmartCampusNavigation.algorithms.Dijkstra;
import com.dion.SmartCampusNavigation.data.CampusDataLoader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CampusDataLoaderTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void loadsCsvAndCalculatesRouteThroughSqlite() throws IOException, SQLException {
        Path databaseFile = temporaryFolder.newFile("campus.db").toPath();
        CampusDataLoader loader = new CampusDataLoader("jdbc:sqlite:" + databaseFile);

        loader.initializeAndLoad();

        assertTrue(loader.testConnection());
        RouteServices routes = new RouteServices(loader);
        List<String> buildingNames = routes.listBuildings().stream()
                .map(building -> building.buildingName())
                .toList();
        assertTrue(buildingNames.contains("Gate"));
        assertTrue(buildingNames.contains("Building A"));
        Dijkstra.Path route = routes.findRouteBetweenLocations(
                "BLDG-ENGR-E101", "BLDG-EDUC-F101");

        assertTrue(route.isReachable());
        assertEquals("BLDG-ENGR", route.nodeIds().get(0));
        assertEquals("BLDG-EDUC", route.nodeIds().get(route.nodeIds().size() - 1));
        assertEquals(5.0, route.totalCost(), 0.0);
        assertTrue(route.usesTemporaryWeights());
        assertEquals(route, routes.findRoute("BLDG-ENGR-E101", "BLDG-EDUC"));
        assertEquals(List.of("BLDG-ENGR"),
                routes.findRouteBetweenLocations("BLDG-ENGR-E101", "BLDG-ENGR-E102").nodeIds());
    }

    @Test
    public void currentPathwaysContainTheDiagramWaypointNodes() throws IOException, SQLException {
        Path databaseFile = temporaryFolder.newFile("map-waypoints.db").toPath();
        CampusDataLoader loader = new CampusDataLoader("jdbc:sqlite:" + databaseFile);
        loader.initializeAndLoad();
        RouteServices routes = new RouteServices(loader);

        assertEquals(List.of("MAIN-ENT", "P1"),
                routes.findBuildingRoute("MAIN-ENT", "P1").nodeIds());
        assertEquals(List.of("MAIN-ENT", "P1", "P2", "P3", "P4", "ENGR-G",
                        "J2", "J3", "BLDG-BUS"),
                routes.findBuildingRoute("MAIN-ENT", "BLDG-BUS").nodeIds());
    }

    @Test
    public void currentCsvProducesTheFourDiagramRoutes() throws IOException, SQLException {
        Path databaseFile = temporaryFolder.newFile("declared-routes.db").toPath();
        CampusDataLoader loader = new CampusDataLoader("jdbc:sqlite:" + databaseFile);
        loader.initializeAndLoad();
        RouteServices routes = new RouteServices(loader);

        Dijkstra.Path businessRoute = routes.findBuildingRoute("MAIN-ENT", "BLDG-BUS");
        assertEquals(
                List.of("MAIN-ENT", "P1", "P2", "P3", "P4", "ENGR-G",
                        "J2", "J3", "BLDG-BUS"),
                businessRoute.nodeIds());
        assertEquals(8.0, businessRoute.totalCost(), 0.0);
        assertTrue(businessRoute.usesTemporaryWeights());

        Dijkstra.Path aquariumRoute = routes.findBuildingRoute("MAIN-ENT", "AQUA");
        assertEquals(
                List.of("MAIN-ENT", "P1", "P2", "P3", "P4", "P5", "P6", "P7", "AQUA"),
                aquariumRoute.nodeIds());
        assertEquals(8.0, aquariumRoute.totalCost(), 0.0);

        Dijkstra.Path canteenRoute = routes.findBuildingRoute("MAIN-ENT", "CAN");
        assertEquals(
                List.of("MAIN-ENT", "P1", "GH", "CAN"),
                canteenRoute.nodeIds());
        assertEquals(3.0, canteenRoute.totalCost(), 0.0);

        Dijkstra.Path canteenToBusinessRoute = routes.findBuildingRoute("CAN", "BLDG-BUS");
        assertEquals(
                List.of("CAN", "GH", "P1", "P2", "P3", "P4", "ENGR-G",
                        "J2", "J3", "BLDG-BUS"),
                canteenToBusinessRoute.nodeIds());
        assertEquals(9.0, canteenToBusinessRoute.totalCost(), 0.0);

        assertTrue(routes.findBuildingRoute("BLDG-BUS", "MAIN-ENT").isReachable());
    }

    @Test
    public void migratesExistingSqlitePathwaysTableToRouteNodes() throws IOException, SQLException {
        Path databaseFile = temporaryFolder.newFile("legacy-schema.db").toPath();
        try (Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databaseFile);
             Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("""
                    CREATE TABLE buildings (
                        building_id TEXT PRIMARY KEY,
                        building_name TEXT NOT NULL
                    )
                    """);
            statement.execute("""
                    INSERT INTO buildings VALUES
                        ('MAIN-ENT', 'Main Entrance'), ('GH', 'Guard House')
                    """);
            statement.execute("""
                    CREATE TABLE pathways (
                        from_building_id TEXT NOT NULL,
                        to_building_id TEXT NOT NULL,
                        PRIMARY KEY (from_building_id, to_building_id),
                        FOREIGN KEY (from_building_id) REFERENCES buildings(building_id),
                        FOREIGN KEY (to_building_id) REFERENCES buildings(building_id)
                    )
                    """);
            statement.execute("INSERT INTO pathways VALUES ('MAIN-ENT', 'GH')");
        }

        CampusDataLoader loader = new CampusDataLoader("jdbc:sqlite:" + databaseFile);
        loader.initializeAndLoad();
        RouteServices routes = new RouteServices(loader);

        assertTrue(routes.findBuildingRoute("MAIN-ENT", "BLDG-BUS").usesTemporaryWeights());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsUnknownLocationIds() throws IOException, SQLException {
        Path databaseFile = temporaryFolder.newFile("unknown-location.db").toPath();
        CampusDataLoader loader = new CampusDataLoader("jdbc:sqlite:" + databaseFile);
        loader.initializeAndLoad();

        new RouteServices(loader).findRouteBetweenLocations("UNKNOWN", "BLDG-EDUC-F101");
    }
}
