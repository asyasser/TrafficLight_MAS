package com.trafficlight.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import com.trafficlight.models.TrafficLightState;
import com.trafficlight.models.IntersectionData;
import com.trafficlight.messages.MessageConstants;
import com.trafficlight.behaviours.TrafficLightBehaviour;

public class TrafficLightAgent extends Agent {
    private IntersectionData intersectionData;

    @Override
    protected void setup() {
        // Get arguments (intersection ID, x, y coordinates)
        Object[] args = getArguments();
        if (args != null && args.length >= 3) {
            String intersectionId = (String) args[0];
            int x = (Integer) args[1];
            int y = (Integer) args[2];

            intersectionData = new IntersectionData(intersectionId, x, y);

            System.out.println("Traffic Light Agent " + intersectionId + " ready at position (" + x + "," + y + ")");
        } else {
            System.err.println("Traffic Light Agent needs intersection ID and coordinates!");
            doDelete();
            return;
        }

        // Register service
        registerService();

        // Add behaviours
        addBehaviour(new TrafficLightBehaviour(this, intersectionData));
        addBehaviour(new ReceiveMessageBehaviour());
    }

    private void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(MessageConstants.TRAFFIC_LIGHT_SERVICE);
        sd.setName(intersectionData.getIntersectionId());
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("Traffic Light Agent " + intersectionData.getIntersectionId() + " terminating.");
    }

    public IntersectionData getIntersectionData() {
        return intersectionData;
    }

    // Inner class for handling messages
    private class ReceiveMessageBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchOntology(MessageConstants.TRAFFIC_ONTOLOGY);
            ACLMessage msg = receive(mt);

            if (msg != null) {
                handleMessage(msg);
            } else {
                block();
            }
        }

        private void handleMessage(ACLMessage msg) {
            switch (msg.getPerformative()) {
                case ACLMessage.REQUEST:
                    if (MessageConstants.REQUEST_PASSAGE.equals(msg.getContent())) {
                        handlePassageRequest(msg);
                    }
                    break;
                case ACLMessage.INFORM:
                    // Handle status updates from other agents
                    break;
            }
        }

        private void handlePassageRequest(ACLMessage msg) {
            ACLMessage reply = msg.createReply();
            reply.setOntology(MessageConstants.TRAFFIC_ONTOLOGY);

            if (intersectionData.getCurrentState() == TrafficLightState.GREEN) {
                reply.setPerformative(ACLMessage.AGREE);
                reply.setContent(MessageConstants.PASSAGE_GRANTED);
                intersectionData.incrementVehicleCount();
            } else {
                reply.setPerformative(ACLMessage.REFUSE);
                reply.setContent(MessageConstants.PASSAGE_DENIED);
            }

            send(reply);
        }
    }
}
