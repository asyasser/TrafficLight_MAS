package com.trafficlight.models;

public class VehicleState {
    private String vehicleId;
    private int currentX;
    private int currentY;
    private String direction; // NORTH, SOUTH, EAST, WEST
    private boolean waitingAtLight;
    private String targetIntersection;

    public VehicleState(String vehicleId, int x, int y, String direction) {
        this.vehicleId = vehicleId;
        this.currentX = x;
        this.currentY = y;
        this.direction = direction;
        this.waitingAtLight = false;
    }

    // Getters and setters
    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public int getCurrentX() { return currentX; }
    public void setCurrentX(int currentX) { this.currentX = currentX; }

    public int getCurrentY() { return currentY; }
    public void setCurrentY(int currentY) { this.currentY = currentY; }

    public String getDirection() { return direction; }
    public void setDirection(String direction) { this.direction = direction; }

    public boolean isWaitingAtLight() { return waitingAtLight; }
    public void setWaitingAtLight(boolean waitingAtLight) { this.waitingAtLight = waitingAtLight; }

    public String getTargetIntersection() { return targetIntersection; }
    public void setTargetIntersection(String targetIntersection) { this.targetIntersection = targetIntersection; }
}
