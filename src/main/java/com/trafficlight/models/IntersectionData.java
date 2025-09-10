package com.trafficlight.models;

public class IntersectionData {
    private String intersectionId;
    private int x;
    private int y;
    private TrafficLightState currentState;
    private int vehicleCount;
    private long lastChangeTime;

    public IntersectionData(String intersectionId, int x, int y) {
        this.intersectionId = intersectionId;
        this.x = x;
        this.y = y;
        this.currentState = TrafficLightState.RED;
        this.vehicleCount = 0;
        this.lastChangeTime = System.currentTimeMillis();
    }

    // Getters and setters
    public String getIntersectionId() { return intersectionId; }
    public void setIntersectionId(String intersectionId) { this.intersectionId = intersectionId; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public TrafficLightState getCurrentState() { return currentState; }
    public void setCurrentState(TrafficLightState currentState) {
        this.currentState = currentState;
        this.lastChangeTime = System.currentTimeMillis();
    }

    public int getVehicleCount() { return vehicleCount; }
    public void setVehicleCount(int vehicleCount) { this.vehicleCount = vehicleCount; }

    public long getLastChangeTime() { return lastChangeTime; }
    public void setLastChangeTime(long lastChangeTime) { this.lastChangeTime = lastChangeTime; }

    public void incrementVehicleCount() { this.vehicleCount++; }
    public void decrementVehicleCount() {
        if (this.vehicleCount > 0) this.vehicleCount--;
    }
}
