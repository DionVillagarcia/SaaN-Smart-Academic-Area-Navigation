package com.dion.SmartCampusNavigation.model;

public record Location(
        String locationId,
        String buildingId,
        String floor,
        String room,
        String locationName) {
}
