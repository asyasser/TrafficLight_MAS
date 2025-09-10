package com.trafficlight.behaviours;

import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import com.trafficlight.models.IntersectionData;
import com.trafficlight.models.TrafficLightState;
import com.trafficlight.messages.MessageConstants;

public class TrafficLightBehaviour extends TickerBehaviour {
    private IntersectionData intersectionData;
    private long stateStartTime;

    public TrafficLightBehaviour(Agent agent, IntersectionData intersectionData) {
        super(agent, 1000); 
        this.intersectionData = intersectionData;
        this.stateStartTime = System.currentTimeMillis();
    }

    @Override
    protected void onTick() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - stateStartTime;

        TrafficLightState currentState = intersectionData.getCurrentState();

        
        if (elapsedTime >= currentState.getDuration()) {
            changeToNextState();
            stateStartTime = currentTime;
            sendStatusUpdate();
        }
    }

    private void changeToNextState() {
        TrafficLightState currentState = intersectionData.getCurrentState();
        TrafficLightState nextState = currentState.getNext();

        intersectionData.setCurrentState(nextState);

        System.out.println("Traffic Light " + intersectionData.getIntersectionId() +
                " changed from " + currentState + " to " + nextState);
    }

    private void sendStatusUpdate() {
        System.out.println("TrafficLight " + intersectionData.getIntersectionId() + " trying to send status update...");

        
        try {
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType(MessageConstants.CONTROLLER_SERVICE);
            template.addServices(sd);

            DFAgentDescription[] result = DFService.search(myAgent, template);
            System.out.println("Found " + result.length + " controller agents");

            if (result.length > 0) {
                System.out.println("Sending status to controller: " + result[0].getName().getLocalName());

                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                msg.addReceiver(result[0].getName());
                msg.setOntology(MessageConstants.TRAFFIC_ONTOLOGY);
                msg.setContent(MessageConstants.STATUS_UPDATE + ":" +
                        intersectionData.getIntersectionId() + ":" +
                        intersectionData.getCurrentState() + ":" +
                        intersectionData.getVehicleCount());

                myAgent.send(msg);
                System.out.println("Message sent successfully!");
            } else {
                System.out.println("ERROR: No controller agents found!");
            }
        } catch (FIPAException fe) {
            System.out.println("ERROR in sendStatusUpdate: " + fe.getMessage());
            fe.printStackTrace();
        }
    }
}
