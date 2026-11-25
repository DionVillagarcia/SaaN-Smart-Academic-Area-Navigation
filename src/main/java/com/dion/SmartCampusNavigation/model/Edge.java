package com.dion.SmartCampusNavigation.model;

public record Edge(String fromNodeId, String toNodeId, Double distance) {
    public static final double TEMPORARY_WEIGHT = 1.0;

    public Edge(String fromNodeId, String toNodeId) {
        this(fromNodeId, toNodeId, null);
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
    }

    public double weight() {
        return distance == null ? TEMPORARY_WEIGHT : distance;
    }

    public boolean usesTemporaryWeight() {
        return distance == null;
    }
}
