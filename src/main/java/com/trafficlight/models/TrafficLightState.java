package com.trafficlight.models;

public  enum TrafficLightState {
    RED(5000),      // 5 seconds
    YELLOW(2000),   // 2 seconds
    GREEN(8000);    // 8 seconds

    private final int duration;

    TrafficLightState(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public TrafficLightState getNext() {
        switch (this) {
            case RED:
                return GREEN;
            case GREEN:
                return YELLOW;
            case YELLOW:
                return RED;
            default:
                return RED;
        }
    }
}
