package com.trafficlight.behaviours;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import com.trafficlight.messages.MessageConstants;

public class MonitoringBehaviour extends TickerBehaviour {
    private int trafficLightCount = 0;
    private int vehicleCount = 0;

    public MonitoringBehaviour(Agent agent, long period) {
        super(agent, period);
    }

    @Override
    protected void onTick() {
        countActiveAgents();
        printSystemStatistics();
    }

    private void countActiveAgents() {
        trafficLightCount = countAgentsByService(MessageConstants.TRAFFIC_LIGHT_SERVICE);
        vehicleCount = countAgentsByService(MessageConstants.VEHICLE_SERVICE);
    }

    private int countAgentsByService(String serviceType) {
        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(serviceType);
            template.addServices(sd);

            DFAgentDescription[] result = DFService.search(myAgent, template);
            return result.length;
        } catch (FIPAException fe) {
            fe.printStackTrace();
            return 0;
        }
    }

    private void printSystemStatistics() {
        System.out.println("\n=== TRAFFIC SYSTEM STATISTICS ===");
        System.out.println("Active Traffic Lights: " + trafficLightCount);
        System.out.println("Active Vehicles: " + vehicleCount);
        System.out.println("System Time: " + new java.util.Date());
        System.out.println("================================\n");
    }
}
