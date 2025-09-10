package com.trafficlight.main;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class TrafficLightMain {

    public static void main(String[] args) {
        try {
            // Get JADE runtime
            Runtime rt = Runtime.instance();

            // Create a profile for the main container
            Profile profile = new ProfileImpl();
            profile.setParameter(Profile.MAIN_HOST, "localhost");
            profile.setParameter(Profile.MAIN_PORT, "1099");
            profile.setParameter(Profile.GUI, "true"); // Show JADE GUI

            // Create the main container
            AgentContainer mainContainer = rt.createMainContainer(profile);

            System.out.println("Starting Traffic Light Multi-Agent System...");

            // Create and start the Controller Agent
            AgentController controllerAgent = mainContainer.createNewAgent(
                    "ControllerAgent",
                    "com.trafficlight.agents.ControllerAgent",
                    null
            );
            controllerAgent.start();

            // Create Traffic Light Agents at different intersections
            createTrafficLightAgent(mainContainer, "TL1", 3, 3);
            createTrafficLightAgent(mainContainer, "TL2", 6, 3);
            createTrafficLightAgent(mainContainer, "TL3", 3, 6);
            createTrafficLightAgent(mainContainer, "TL4", 6, 6);

            // Wait a bit for traffic lights to initialize
            Thread.sleep(2000);

            // Create some initial vehicles
            createVehicleAgent(mainContainer, "V1", 0, 3, "EAST");
            createVehicleAgent(mainContainer, "V2", 3, 0, "SOUTH");
            createVehicleAgent(mainContainer, "V3", 9, 6, "WEST");

            System.out.println("All agents started successfully!");
            System.out.println("Traffic Light System is now running...");
            System.out.println("Check the GUI window for visual representation.");
            System.out.println("You can also open JADE RMA GUI to monitor agents.");

        } catch (Exception e) {
            System.err.println("Error starting Traffic Light System: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void createTrafficLightAgent(AgentContainer container, String name, int x, int y) {
        try {
            Object[] args = {name, x, y};
            AgentController agent = container.createNewAgent(
                    name,
                    "com.trafficlight.agents.TrafficLightAgent",
                    args
            );
            agent.start();
            System.out.println("Created Traffic Light Agent: " + name + " at position (" + x + "," + y + ")");
        } catch (StaleProxyException e) {
            System.err.println("Error creating Traffic Light Agent " + name + ": " + e.getMessage());
        }
    }

    private static void createVehicleAgent(AgentContainer container, String name, int x, int y, String direction) {
        try {
            Object[] args = {name, x, y, direction};
            AgentController agent = container.createNewAgent(
                    name,
                    "com.trafficlight.agents.VehicleAgent",
                    args
            );
            agent.start();
            System.out.println("Created Vehicle Agent: " + name + " at position (" + x + "," + y + ") heading " + direction);
        } catch (StaleProxyException e) {
            System.err.println("Error creating Vehicle Agent " + name + ": " + e.getMessage());
        }
    }
}