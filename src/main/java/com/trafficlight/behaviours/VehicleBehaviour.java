package com.trafficlight.behaviours;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import com.trafficlight.models.VehicleState;
import com.trafficlight.messages.MessageConstants;

import java.util.Random;

public class VehicleBehaviour extends TickerBehaviour {
    private VehicleState vehicleState;
    private Random random;
    private int moveSpeed = 1; // squares per tick

    public VehicleBehaviour(Agent agent, VehicleState vehicleState) {
        super(agent, 2000); // Move every 2 seconds
        this.vehicleState = vehicleState;
        this.random = new Random();
    }

    @Override
    protected void onTick() {
        if (!vehicleState.isWaitingAtLight()) {
            moveVehicle();
            checkForTrafficLight();
        }

        sendStatusUpdate();
    }

    private void moveVehicle() {
        String direction = vehicleState.getDirection();

        switch (direction) {
            case "NORTH":
                vehicleState.setCurrentY(vehicleState.getCurrentY() - moveSpeed);
                break;
            case "SOUTH":
                vehicleState.setCurrentY(vehicleState.getCurrentY() + moveSpeed);
                break;
            case "EAST":
                vehicleState.setCurrentX(vehicleState.getCurrentX() + moveSpeed);
                break;
            case "WEST":
                vehicleState.setCurrentX(vehicleState.getCurrentX() - moveSpeed);
                break;
        }

        // Simple boundary check - turn around if out of bounds
        if (vehicleState.getCurrentX() < 0 || vehicleState.getCurrentX() > 10 ||
                vehicleState.getCurrentY() < 0 || vehicleState.getCurrentY() > 10) {
            turnAround();
        }

        System.out.println("Vehicle " + vehicleState.getVehicleId() +
                " moved to (" + vehicleState.getCurrentX() + "," + vehicleState.getCurrentY() + ")");
    }

    private void turnAround() {
        String currentDirection = vehicleState.getDirection();
        String newDirection;

        switch (currentDirection) {
            case "NORTH":
                newDirection = "SOUTH";
                vehicleState.setCurrentY(vehicleState.getCurrentY() + 1);
                break;
            case "SOUTH":
                newDirection = "NORTH";
                vehicleState.setCurrentY(vehicleState.getCurrentY() - 1);
                break;
            case "EAST":
                newDirection = "WEST";
                vehicleState.setCurrentX(vehicleState.getCurrentX() - 1);
                break;
            case "WEST":
                newDirection = "EAST";
                vehicleState.setCurrentX(vehicleState.getCurrentX() + 1);
                break;
            default:
                newDirection = "NORTH";
        }

        vehicleState.setDirection(newDirection);
        System.out.println("Vehicle " + vehicleState.getVehicleId() + " turned around, now heading " + newDirection);
    }

    private void checkForTrafficLight() {
        // Check if vehicle is approaching an intersection (simplified logic)
        if (isAtIntersection()) {
            requestPassage();
        }
    }

    private boolean isAtIntersection() {
        // Simple intersection detection - assume intersections at grid points
        int x = vehicleState.getCurrentX();
        int y = vehicleState.getCurrentY();

        // Intersections at every 3rd grid point
        return (x % 3 == 0 && y % 3 == 0);
    }

    private void requestPassage() {
        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(MessageConstants.TRAFFIC_LIGHT_SERVICE);
            template.addServices(sd);

            DFAgentDescription[] result = DFService.search(myAgent, template);

            // Find nearest traffic light (simplified - just pick first one for now)
            if (result.length > 0) {
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(result[0].getName());
                msg.setOntology(MessageConstants.TRAFFIC_ONTOLOGY);
                msg.setContent(MessageConstants.REQUEST_PASSAGE);

                myAgent.send(msg);

                System.out.println("Vehicle " + vehicleState.getVehicleId() +
                        " requested passage from " + result[0].getName().getLocalName());
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    private void sendStatusUpdate() {
        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(MessageConstants.CONTROLLER_SERVICE);
            template.addServices(sd);

            DFAgentDescription[] result = DFService.search(myAgent, template);

            if (result.length > 0) {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(result[0].getName());
                msg.setOntology(MessageConstants.TRAFFIC_ONTOLOGY);
                msg.setContent(MessageConstants.STATUS_UPDATE + ":" +
                        vehicleState.getVehicleId() + ":" +
                        vehicleState.getCurrentX() + ":" +
                        vehicleState.getCurrentY() + ":" +
                        vehicleState.getDirection() + ":" +
                        vehicleState.isWaitingAtLight());

                myAgent.send(msg);
            }
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
}