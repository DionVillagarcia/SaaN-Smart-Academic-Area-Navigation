package com.dion.SmartCampusNavigation.algorithms;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;


//cd C:\Users\taesang\Documents\Projects\SaaN-Smart-Academic-Area-Navigation\src\main\java
//javac com\dion\SmartCampusNavigation\algorithms\BFS.java
//java com.dion.SmartCampusNavigation.algorithms.BFS
//this is for running the program


public class BFS {

    private static final Map<String, String> buildingNameToId = new HashMap<>();
    private static final Map<String, List<String>> graph = new HashMap<>();
    private static final Map<String, String> roomToBuilding = new HashMap<>();
    private static final Set<String> buildings = new HashSet<>();

    public static void main(String[] args) {

        try {
            System.out.println(System.getProperty("user.dir"));

            loadBuildings();
            loadLocations();
            loadPathways();

            Scanner scanner = new Scanner(System.in);


            // first output to input values
            System.out.println("===== CAMPUS NAVIGATION =====");
            System.out.print("Enter Current Building/Location: ");
            String currentInput = scanner.nextLine().trim().toUpperCase();

            System.out.print("Enter Destination Building/Location: ");
            String destinationInput = scanner.nextLine().trim().toUpperCase();

            String start = getDestinationBuilding(currentInput);
            String destination = getDestinationBuilding(destinationInput);

            if (start == null) {
                System.out.println("Current location not found.");
                return;
            }

            if (destination == null) {
                System.out.println("Destination location not found.");
                return;
            }

            //BFS starts
            List<String> path = findShortestPath(start, destination);


            if (path.isEmpty()) {
                System.out.println("No route found.");
                return;
            }

            //this part is for the output to show the route
            System.out.println("\nCurrent Location: " + start);
            System.out.println("Destination: " + destination);

            System.out.println("\nShortest Route:");
            System.out.println(String.join(" -> ", path));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static BufferedReader getReader(String fileName)
        throws FileNotFoundException {

    String path = "../resources/data/" + fileName;

    return new BufferedReader(new FileReader(path));
    }

    private static void loadPathways() throws IOException {

        BufferedReader br = getReader("pathways.csv");

        br.readLine(); // skip header

        String line;

        while ((line = br.readLine()) != null) {

            String[] parts = line.split(",");

            if (parts.length < 2) {
                continue;
            }

            String from = parts[0].trim();
            String to = parts[1].trim();

            graph.computeIfAbsent(
                    from,
                    k -> new ArrayList<>()
            ).add(to);
        }

        br.close();
    }

    private static void loadLocations() throws IOException {

        BufferedReader br = getReader("locations.csv");

        br.readLine(); // skip header

        String line;

        while ((line = br.readLine()) != null) {

            String[] parts = line.split(",");

            if (parts.length < 2) {
                continue;
            }

            String locationId = parts[0].trim().toUpperCase();
            String buildingId = parts[1].trim().toUpperCase();

            roomToBuilding.put(locationId, buildingId);
        }

        br.close();
    }

    private static void loadBuildings() throws IOException {

    BufferedReader br = getReader("building.csv");

    br.readLine(); // skip header

    String line;

    while ((line = br.readLine()) != null) {

        String[] parts = line.split(",");

        if (parts.length < 2) {
            continue;
        }

        String buildingId = parts[0].trim().toUpperCase();
        String buildingName = parts[1].trim().toUpperCase();

        buildings.add(buildingId);

        buildingNameToId.put(buildingName, buildingId);
    }

    br.close();
}

    // Converts room into building
    private static String getDestinationBuilding(String input) {

    input = input.trim().toUpperCase();

    // Check building ID
    if (buildings.contains(input)) {
        return input;
    }

    // Check building name
    if (buildingNameToId.containsKey(input)) {
        return buildingNameToId.get(input);
    }

    // Check room ID
    if (roomToBuilding.containsKey(input)) {
        return roomToBuilding.get(input);
    }

    return null;
}

    public static List<String> findShortestPath(
            String start,
            String destination) {

        // Initialize Queue
        Queue<String> queue = new LinkedList<>();
        // Initialize Visited
        Set<String> visited = new HashSet<>();
        // Parent Map
        Map<String, String> parent = new HashMap<>();

        queue.offer(start);
        visited.add(start);


        // This is the Main BFS Loop
        while (!queue.isEmpty()) {

            String current = queue.poll();
            // Check destination, Stops when destination is found
            if (current.equals(destination)) {
                break;
            }
            
            //Explores neighbor
            for (String neighbor :
                    graph.getOrDefault(
                            current,
                            Collections.emptyList())) {

                if (!visited.contains(neighbor)) {

                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.offer(neighbor);
                }
            }
        }
        // Visit new nodes
        if (!visited.contains(destination)) {
            return Collections.emptyList();
        }

        List<String> path = new ArrayList<>();

        String current = destination;

        while (current != null) {
            path.add(current);
            current = parent.get(current);
        }

        Collections.reverse(path);

        return path;
    }
}