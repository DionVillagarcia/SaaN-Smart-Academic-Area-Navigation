package com.dion.SmartCampusNavigation.algorithms;

import java.util.*;

public class Dijkstra {

    private Map<String, String> locations = new LinkedHashMap<>();
    private Map<String, List<Edge>> graph = new LinkedHashMap<>();

    static class Edge {
        String destination;
        int weight;

        Edge(String destination, int weight) {
            this.destination = destination;
            this.weight = weight;
        }
    }

    // Add location
    public void addLocation(String id, String name) {
        locations.put(id, name);
        graph.putIfAbsent(id, new ArrayList<>());
    }

    // Add path
    public void addPath(String from, String to, int weight) {
        graph.get(from).add(new Edge(to, weight));
        graph.get(to).add(new Edge(from, weight));
    }

    // Dijkstra Algorithm
    public void findShortestPath(String start, String destination) {

        if (!locations.containsKey(start)) {
            System.out.println("Invalid current location.");
            return;
        }

        if (!locations.containsKey(destination)) {
            System.out.println("Invalid destination.");
            return;
        }

        Map<String, Integer> distance = new HashMap<>();
        Map<String, String> previous = new HashMap<>();

        for (String node : locations.keySet()) {
            distance.put(node, Integer.MAX_VALUE);
        }

        distance.put(start, 0);

        PriorityQueue<String> queue =
                new PriorityQueue<>(Comparator.comparingInt(distance::get));

        queue.add(start);

        while (!queue.isEmpty()) {

            String current = queue.poll();

            if (current.equals(destination)) {
                break;
            }

            for (Edge edge : graph.get(current)) {

                int newDistance =
                        distance.get(current) + edge.weight;

                if (newDistance < distance.get(edge.destination)) {

                    distance.put(edge.destination, newDistance);
                    previous.put(edge.destination, current);

                    queue.remove(edge.destination);
                    queue.add(edge.destination);
                }
            }
        }

        if (distance.get(destination) == Integer.MAX_VALUE) {
            System.out.println("No route found.");
            return;
        }

        List<String> path = new ArrayList<>();
        String current = destination;

        while (current != null) {
            path.add(current);
            current = previous.get(current);
        }

        Collections.reverse(path);

        System.out.println("\n===== SHORTEST ROUTE =====");

        for (int i = 0; i < path.size(); i++) {
            String node = path.get(i);

            System.out.print(node + " (" + locations.get(node) + ")");

            if (i < path.size() - 1) {
                System.out.print(" -> ");
            }
        }

        System.out.println("\nTotal Distance: " +
                distance.get(destination));
    }

    public static void main(String[] args) {

        Dijkstra campus = new Dijkstra();

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

        // Connections (all weights = 1)
        campus.addPath("MAIN-ENT", "GH", 1);
        campus.addPath("MAIN-ENT", "CHAP", 1);

        campus.addPath("GH", "FAAL", 1);
        campus.addPath("GH", "BLDG-A", 1);
        campus.addPath("GH", "CHAP", 1);

        campus.addPath("FAAL", "CAN", 1);

        campus.addPath("BLDG-A", "KIOSK", 1);
        campus.addPath("BLDG-A", "ZONTA", 1);

        campus.addPath("KIOSK", "M-KIOSK", 1);
        campus.addPath("KIOSK", "KIOSK-CR", 1);

        campus.addPath("CHAP", "DORM", 1);

        campus.addPath("DORM", "ENGR-G", 1);

        campus.addPath("ZONTA", "AQUA", 1);

        campus.addPath("AQUA", "MNT", 1);
        campus.addPath("AQUA", "BLDG-B", 1);
        campus.addPath("AQUA", "GYM", 1);

        campus.addPath("BLDG-B", "BLDG-EDUC", 1);

        campus.addPath("BLDG-EDUC", "BLDG-BUS", 1);

        campus.addPath("ENGR-G", "BLDG-ENGR", 1);

        Scanner scanner = new Scanner(System.in);

        System.out.println("===== SMART CAMPUS NAVIGATION (DIJKSTRA) =====");

        System.out.print("Enter Current Location ID: ");
        String start = scanner.nextLine().trim().toUpperCase();

        System.out.print("Enter Destination Building ID: ");
        String destination = scanner.nextLine().trim().toUpperCase();

        campus.findShortestPath(start, destination);

        scanner.close();
    }
}