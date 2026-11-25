package com.dion.SmartCampusNavigation.graph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Graph {

    // Store building IDs and their names
    private Map<String, String> locations = new LinkedHashMap<>();

    // Store connections between buildings
    private Map<String, List<String>> graph = new LinkedHashMap<>();

    // Add a location
    public void addLocation(String id, String name) {
        locations.put(id, name);
        graph.putIfAbsent(id, new ArrayList<>());
    }

    // Connect two locations
    public void addPath(String from, String to) {
        graph.get(from).add(to);
        graph.get(to).add(from);
    }

    // Display all locations and connections
    public void displayGraph() {
        System.out.println("===== CAMPUS GRAPH =====");

        for (String id : graph.keySet()) {
            System.out.println(locations.get(id) + ": " + id);
            System.out.println(id + " -> " + graph.get(id));
            System.out.println();
        }
    }

    public static void main(String[] args) {

        Graph campus = new Graph();

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

        // Engineering Court connections
        campus.addPath("ENGR-G", "BLDG-ENGR");

        campus.displayGraph();
    }
}