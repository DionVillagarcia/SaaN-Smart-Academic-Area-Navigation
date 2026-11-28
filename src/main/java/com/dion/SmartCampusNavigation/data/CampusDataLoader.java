package com.dion.SmartCampusNavigation.data;

import com.dion.SmartCampusNavigation.model.Building;
import com.dion.SmartCampusNavigation.model.Edge;
import com.dion.SmartCampusNavigation.model.Location;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CampusDataLoader {
    private final String jdbcUrl;

    public CampusDataLoader() {
        this(System.getProperty("campus.db.url", "jdbc:sqlite:campus.db"));
    }

    public CampusDataLoader(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    public Connection openConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException exception) {
            connection.close();
            throw exception;
        }
        return connection;
    }

    public void initializeAndLoad() throws SQLException, IOException {
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS buildings (
                        building_id TEXT PRIMARY KEY,
                        building_name TEXT NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS locations (
                        location_id TEXT PRIMARY KEY,
                        building_id TEXT NOT NULL,
                        floor TEXT NOT NULL,
                        room TEXT NOT NULL,
                        location_name TEXT NOT NULL,
                        FOREIGN KEY (building_id) REFERENCES buildings(building_id)
                    )
                    """);
        }
        ensurePathwaySchema();

        try (Connection connection = openConnection()) {
            connection.setAutoCommit(false);
            try {
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate("DELETE FROM pathways");
                    statement.executeUpdate("DELETE FROM locations");
                    statement.executeUpdate("DELETE FROM buildings");
                }
                loadBuildings(connection);
                loadLocations(connection);
                loadPathways(connection);
                connection.commit();
            } catch (SQLException | IOException | RuntimeException exception) {
                connection.rollback();
                throw exception;
            }
        }
    }
    public boolean testConnection() throws SQLException {
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT 1")) {
            return resultSet.next() && resultSet.getInt(1) == 1;
        }
    }

    private static void loadBuildings(Connection connection) throws SQLException, IOException {
        try (CsvReader csv = new CsvReader("/data/building.csv");
             PreparedStatement insert = connection.prepareStatement(
                     "INSERT INTO buildings (building_id, building_name) VALUES (?, ?)")) {
            Map<String, Integer> columns = csv.readHeader("building_id", "building_name");
            List<String> row;
            while ((row = csv.readRow()) != null) {
                Building building = new Building(
                        csv.value(row, columns, "building_id"),
                        csv.value(row, columns, "building_name"));
                insert.setString(1, building.buildingId());
                insert.setString(2, building.buildingName());
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    private static void loadLocations(Connection connection) throws SQLException, IOException {
        try (CsvReader csv = new CsvReader("/data/locations.csv");
             PreparedStatement insert = connection.prepareStatement("""
                     INSERT INTO locations
                         (location_id, building_id, floor, room, location_name)
                     VALUES (?, ?, ?, ?, ?)
                     """)) {
            Map<String, Integer> columns = csv.readHeader(
                    "location_id", "building_id", "floor", "room", "location_name");
            List<String> row;
            while ((row = csv.readRow()) != null) {
                Location location = new Location(
                        csv.value(row, columns, "location_id"),
                        csv.value(row, columns, "building_id"),
                        csv.value(row, columns, "floor"),
                        csv.value(row, columns, "room"),
                        csv.value(row, columns, "location_name"));
                insert.setString(1, location.locationId());
                insert.setString(2, location.buildingId());
                insert.setString(3, location.floor());
                insert.setString(4, location.room());
                insert.setString(5, location.locationName());
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    private static void loadPathways(Connection connection) throws SQLException, IOException {
        try (CsvReader csv = new CsvReader("/data/pathways.csv");
             PreparedStatement insert = connection.prepareStatement("""
                     INSERT INTO pathways (from_node_id, to_node_id, distance)
                     VALUES (?, ?, ?)
                     """)) {
            Map<String, Integer> columns = csv.readHeader("from_node_id", "to_node_id");
            List<String> row;
            while ((row = csv.readRow()) != null) {
                Edge edge;
                try {
                    edge = new Edge(
                            csv.value(row, columns, "from_node_id"),
                            csv.value(row, columns, "to_node_id"),
                            csv.optionalDouble(row, columns, "distance"));
                } catch (IllegalArgumentException exception) {
                    throw csv.formatError(exception.getMessage());
                }
                insert.setString(1, edge.fromNodeId());
                insert.setString(2, edge.toNodeId());
                if (edge.distance() == null) {
                    insert.setNull(3, Types.DOUBLE);
                } else {
                    insert.setDouble(3, edge.distance());
                }
                insert.addBatch();
            }
            insert.executeBatch();
        }
    }

    private void ensurePathwaySchema() throws SQLException {
        Set<String> columns = new HashSet<>();
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("PRAGMA table_info(pathways)")) {
            while (resultSet.next()) {
                columns.add(resultSet.getString("name"));
            }
        }

        if (columns.isEmpty()) {
            createPathwayTable("pathways");
        } else if (columns.contains("from_building_id") && columns.contains("to_building_id")) {
            migrateLegacyPathwayTable(columns.contains("distance"));
        } else {
            if (!columns.contains("from_node_id") || !columns.contains("to_node_id")) {
                throw new SQLException("Unsupported pathways table schema");
            }
            if (!columns.contains("distance")) {
                try (Connection connection = openConnection();
                     Statement statement = connection.createStatement()) {
                    statement.executeUpdate("ALTER TABLE pathways ADD COLUMN distance REAL");
                }
            }
        }
    }

    private void createPathwayTable(String tableName) throws SQLException {
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE %s (
                        from_node_id TEXT NOT NULL,
                        to_node_id TEXT NOT NULL,
                        distance REAL,
                        PRIMARY KEY (from_node_id, to_node_id)
                    )
                    """.formatted(tableName));
        }
    }

    private void migrateLegacyPathwayTable(boolean hasDistanceColumn) throws SQLException {
        try (Connection connection = openConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("DROP TABLE IF EXISTS pathways_node_migration");
            statement.executeUpdate("""
                    CREATE TABLE pathways_node_migration (
                        from_node_id TEXT NOT NULL,
                        to_node_id TEXT NOT NULL,
                        distance REAL,
                        PRIMARY KEY (from_node_id, to_node_id)
                    )
                    """);
            String distanceValue = hasDistanceColumn ? "distance" : "NULL";
            statement.executeUpdate("""
                    INSERT INTO pathways_node_migration (from_node_id, to_node_id, distance)
                    SELECT from_building_id, to_building_id, %s FROM pathways
                    """.formatted(distanceValue));
            statement.executeUpdate("DROP TABLE pathways");
            statement.executeUpdate(
                    "ALTER TABLE pathways_node_migration RENAME TO pathways");
        }
    }

    private static final class CsvReader implements AutoCloseable {
        private final String resourceName;
        private final BufferedReader reader;
        private int lineNumber;

        private CsvReader(String resourceName) throws IOException {
            this.resourceName = resourceName;
            InputStream stream = CampusDataLoader.class.getResourceAsStream(resourceName);
            if (stream == null) {
                throw new IOException("CSV resource not found: " + resourceName);
            }
            reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }

        private Map<String, Integer> readHeader(String... requiredColumns) throws IOException {
            String header = reader.readLine();
            lineNumber++;
            if (header == null) {
                throw formatError("missing header");
            }
            List<String> fields = parseRecord(header);
            Map<String, Integer> columns = new HashMap<>();
            for (int index = 0; index < fields.size(); index++) {
                columns.put(fields.get(index).trim(), index);
            }
            for (String requiredColumn : requiredColumns) {
                if (!columns.containsKey(requiredColumn)) {
                    throw formatError("missing required column: " + requiredColumn);
                }
            }
            return columns;
        }

        private List<String> readRow() throws IOException {
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (!line.isBlank()) {
                    return parseRecord(line);
                }
            }
            return null;
        }

        private String value(List<String> row, Map<String, Integer> columns, String column)
                throws IOException {
            int index = columns.get(column);
            if (index >= row.size()) {
                throw formatError("missing value for column: " + column);
            }
            return row.get(index).trim();
        }

        private Double optionalDouble(
                List<String> row,
                Map<String, Integer> columns,
                String column) throws IOException {
            Integer index = columns.get(column);
            if (index == null || index >= row.size() || row.get(index).isBlank()) {
                return null;
            }
            try {
                return Double.valueOf(row.get(index).trim());
            } catch (NumberFormatException exception) {
                throw formatError("invalid number in column: " + column);
            }
        }

        private IOException formatError(String message) {
            return new IOException(resourceName + ":" + lineNumber + ": " + message);
        }

        private List<String> parseRecord(String line) throws IOException {
            List<String> fields = new ArrayList<>();
            StringBuilder field = new StringBuilder();
            boolean quoted = false;
            for (int index = 0; index < line.length(); index++) {
                char character = line.charAt(index);
                if (character == '"') {
                    if (quoted && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                        field.append('"');
                        index++;
                    } else {
                        quoted = !quoted;
                    }
                } else if (character == ',' && !quoted) {
                    fields.add(field.toString());
                    field.setLength(0);
                } else {
                    field.append(character);
                }
            }
            if (quoted) {
                throw formatError("unterminated quoted field");
            }
            fields.add(field.toString());
            return fields;
        }

        @Override
        public void close() throws IOException {
            reader.close();
        }
    }
}
