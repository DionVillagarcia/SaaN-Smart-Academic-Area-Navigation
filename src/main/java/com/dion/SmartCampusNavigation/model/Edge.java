package com.dion.SmartCampusNavigation.model;

import java.util.Locale;

public record Edge(String fromNodeId, String toNodeId, Double distance, RoadType roadType) {
    public enum RoadType {
        ROAD,
        PATHWAY,
        BOTH;

        public static RoadType fromCsv(String value) {
            return switch (value.trim().toLowerCase(Locale.ROOT)) {
                case "road" -> ROAD;
                case "pathway" -> PATHWAY;
                case "both" -> BOTH;
                default -> throw new IllegalArgumentException("Unknown road type: " + value);
            };
        }
    }

    public Edge(String fromNodeId, String toNodeId, Double distance) {
        this(fromNodeId, toNodeId, distance, RoadType.BOTH);
    }

    public Edge(String fromNodeId, String toNodeId) {
        this(fromNodeId, toNodeId, null, RoadType.BOTH);
    }

    public Edge {
        if (fromNodeId == null || fromNodeId.isBlank()) {
            throw new IllegalArgumentException("Pathway source ID cannot be blank");
        }
        if (toNodeId == null || toNodeId.isBlank()) {
            throw new IllegalArgumentException("Pathway destination ID cannot be blank");
        }
        if (distance != null && (!Double.isFinite(distance) || distance < 0)) {
            throw new IllegalArgumentException("Pathway distance must be a nonnegative number");
        }
        if (roadType == null) {
            throw new IllegalArgumentException("Road type cannot be null");
        }
    }
}
