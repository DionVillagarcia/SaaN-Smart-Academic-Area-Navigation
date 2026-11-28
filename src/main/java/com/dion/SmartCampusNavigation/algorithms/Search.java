
package com.dion.SmartCampusNavigation.algorithms;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

//cd C:\Users\taesang\Documents\Projects\SaaN-Smart-Academic-Area-Navigation\src\main\java
//javac com\dion\SmartCampusNavigation\algorithms\Search.java
//java com.dion.SmartCampusNavigation.algorithms.Search
//this is for running the program

public class Search {

    private static final Map<String, String> buildings = new HashMap<>();
    private static final List<Location> locations = new ArrayList<>();

    private static BufferedReader getReader(String fileName)
        throws IOException {

    String path = "../resources/data/" + fileName;

    return new BufferedReader(new FileReader(path));
}

    public static void main(String[] args) {

        try {
            loadBuildings();
            loadLocations();

            Scanner scanner = new Scanner(System.in);

            System.out.println("===== CAMPUS LOCATION SEARCH :] =====");
            System.out.print("Enter Building or Room: ");

            String input = scanner.nextLine().trim();

            if (!nodeExists(input)) {
                System.out.println("\nNode does not exist.");
                return;
            }

            searchLocation(input);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Loads building.csv
    private static void loadBuildings() throws IOException {

        BufferedReader br = getReader("building.csv");

        br.readLine(); // Skip header

        String line;

        while ((line = br.readLine()) != null) {

            String[] parts = line.split(",");

            if (parts.length < 2) {
                continue;
            }

            String buildingId = parts[0].trim().toUpperCase();
            String buildingName = parts[1].trim();

            buildings.put(buildingId, buildingName);
        }

        br.close();
    }

    // Loads location.csv
    private static void loadLocations() throws IOException {

        BufferedReader br = getReader("locations.csv");

        br.readLine(); // Skip header

        String line;

        while ((line = br.readLine()) != null) {

            String[] parts = line.split(",", -1);

            if (parts.length < 5) {
                continue;
            }

            String locationId = parts[0].trim().toUpperCase();
            String buildingId = parts[1].trim().toUpperCase();
            String floor = parts[2].trim();
            String room = parts[3].trim();
            String locationName = parts[4].trim();

            locations.add(new Location(
                locationId,
                buildingId,
                floor,
                room,
                locationName
            ));
        }

        br.close();
    }

    // Checks if the entered node exists
    public static boolean nodeExists(String input) {

        String search = input.trim().toUpperCase();

        // Check building ID
        if (buildings.containsKey(search)) {
            return true;
        }

        // Check building name
        for (String name : buildings.values()) {
            if (name.toUpperCase().equals(search)) {
                return true;
            }
        }

        // Check room ID, room number, or location name
        for (Location loc : locations) {

            if (loc.locationId.equals(search)
                    || loc.room.equals(search)
                    || loc.locationName.toUpperCase().equals(search)) {

                return true;
            }
        }

        return false;
    }

    // Searches and displays location information
    public static void searchLocation(String input) {

        String search = input.trim().toUpperCase();

        // Check if input is a building
        if (buildings.containsKey(search)) {

            System.out.println("\nNode Found!");
            System.out.println("Building ID: " + search);
            System.out.println("Building Name: " + buildings.get(search));

            return;
        }

        // Check building name
        for (Map.Entry<String, String> entry : buildings.entrySet()) {

            if (entry.getValue().toUpperCase().equals(search)) {

                System.out.println("\nNode Found!");
                System.out.println("Building ID: " + entry.getKey());
                System.out.println("Building Name: " + entry.getValue());

                return;
            }
        }

        // Search rooms and locations
        boolean found = false;

        for (Location loc : locations) {

            if (loc.locationId.equals(search)
                    || loc.room.equals(search)
                    || loc.locationName.toUpperCase().equals(search)) {

                found = true;

                System.out.println("\nNode Found!");
                System.out.println("-------------------------");

                System.out.println("Location ID: " + loc.locationId);
                System.out.println("Room: " + loc.room);
                System.out.println("Location Name: " + loc.locationName);

                System.out.println("Building ID: " + loc.buildingId);

                System.out.println("Building Name: "
                        + buildings.getOrDefault(
                            loc.buildingId,
                            "Unknown Building"
                        ));

                System.out.println("Floor: " + loc.floor);
            }
        }

        if (!found) {
            System.out.println("Node not found.");
        }
    }

    // Location data structure
    static class Location {

        String locationId;
        String buildingId;
        String floor;
        String room;
        String locationName;

        Location(String locationId, String buildingId,
                 String floor, String room, String locationName) {

            this.locationId = locationId;
            this.buildingId = buildingId;
            this.floor = floor;
            this.room = room;
            this.locationName = locationName;
        }
    }
}
