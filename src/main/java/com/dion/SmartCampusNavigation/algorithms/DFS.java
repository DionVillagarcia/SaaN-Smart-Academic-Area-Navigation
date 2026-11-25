package com.dion.SmartCampusNavigation.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class DFS {

    private Map<String, String> locations = new LinkedHashMap<>();
    private Map<String, List<String>> graph = new LinkedHashMap<>();

    // Add location
    public void addLocation(String id, String name) {
        locations.put(id, name);
        graph.putIfAbsent(id, new ArrayList<>());
    }

    // Add path
    public void addPath(String from, String to) {
        graph.get(from).add(to);
        graph.get(to).add(from);
    }

    // Find route using DFS
    public boolean searchLocation(String start, String target) {

    if (!locations.containsKey(start)) {
        System.out.println("Invalid current location.");
        return false;
    }

    if (!locations.containsKey(target)) {
        System.out.println("Invalid destination.");
        return false;
    }

    Set<String> visited = new HashSet<>();
    List<String> path = new ArrayList<>();

    boolean found = dfs(start, target, visited, path);

    if (found) {
        System.out.println("\n===== ROUTE FOUND =====");

        for (int i = 0; i < path.size(); i++) {
            String node = path.get(i);

            System.out.print(node + " (" + locations.get(node) + ")");

            if (i < path.size() - 1) {
                System.out.print(" -> ");
            }
        }

        System.out.println();
    }

    return found;
}

    private boolean dfs(String current,
                    String target,
                    Set<String> visited,
                    List<String> path) {

    visited.add(current);
    path.add(current);

    if (current.equalsIgnoreCase(target)) {
        return true;
    }

    for (String neighbor : graph.get(current)) {
        if (!visited.contains(neighbor)) {

            if (dfs(neighbor, target, visited, path)) {
                return true;
            }
        }
    }

    path.remove(path.size() - 1);
    return false;
}

    public static void main(String[] args) {

        DFS campus = new DFS();

        // Locations
        campus.addLocation("MAIN-ENT", "Main Entrance");
        campus.addLocation("GH", "Guard House");
        campus.addLocation("FAAL", "Facility Admin and Alumni Lounge");
        campus.addLocation("CAN", "Canteen");
        campus.addLocation("BLDG-A", "Building A");
        campus.addLocation("CHAP", "Chapel");
        campus.addLocation("DORM", "Dormitory");
        campus.addLocation("KIOSK", "Kiosk");
        campus.addLocation("M-KIOSK", "Mother Kiosk");
        campus.addLocation("ZONTA", "Zonta Park");
        campus.addLocation("AQUA", "Aquarium");
        campus.addLocation("MNT", "Maintenance");
        campus.addLocation("BLDG-B", "Building B");
        campus.addLocation("BLDG-EDUC", "EDUC Building");
        campus.addLocation("ENGR-G", "Engineering Ground");
        campus.addLocation("BLDG-ENGR", "Engineering Building");
        campus.addLocation("GYM", "Gymnasium");
        campus.addLocation("BLDG-BUS", "Business Building");
        campus.addLocation("KIOSK-CR", "CR Kiosk Area");

        // Main Entrance connections
        campus.addPath("MAIN-ENT", "GH");
        campus.addPath("MAIN-ENT", "CHAP");

        // Guard House connections
        campus.addPath("GH", "FAAL");
        campus.addPath("GH", "BLDG-A");
        campus.addPath("GH", "CHAP");

        // Facility Admin connections
        campus.addPath("FAAL", "CAN");

        // Building A connections
        campus.addPath("BLDG-A", "KIOSK");
        campus.addPath("BLDG-A", "ZONTA");

        // Kiosk connections
        campus.addPath("KIOSK", "M-KIOSK");
        campus.addPath("KIOSK", "KIOSK-CR");

        // Chapel connections
        campus.addPath("CHAP", "DORM");

        // Dormitory connections
        campus.addPath("DORM", "ENGR-G");

        // Zonta Park connections
        campus.addPath("ZONTA", "AQUA");

        // Aquarium connections
        campus.addPath("AQUA", "MNT");
        campus.addPath("AQUA", "BLDG-B");
        campus.addPath("AQUA", "GYM");

        // Building B connections
        campus.addPath("BLDG-B", "BLDG-EDUC");

        // EDUC Building connections
        campus.addPath("BLDG-EDUC", "BLDG-BUS");

        // Engineering Ground connections
        campus.addPath("ENGR-G", "BLDG-ENGR");

        Scanner scanner = new Scanner(System.in);

        System.out.println("===== SMART CAMPUS NAVIGATION (DFS) =====");

        System.out.print("Enter Current Location ID: ");
        String currentLocation = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter Destination Building ID: ");
        String destination = scanner.nextLine().trim().toUpperCase();

        boolean found = campus.searchLocation(currentLocation, destination);
        if (!found) {
            System.out.println("\nLocation not found.");
        }

        scanner.close();
    }
}