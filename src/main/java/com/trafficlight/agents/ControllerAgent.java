package com.trafficlight.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import com.trafficlight.messages.MessageConstants;
import com.trafficlight.behaviours.MonitoringBehaviour;
import com.trafficlight.gui.TrafficLightGUI;

import java.util.HashMap;
import java.util.Map;

public class ControllerAgent extends Agent {
    private Map<String, String> systemStatus;
    private TrafficLightGUI gui;

    @Override
    protected void setup() {
        systemStatus = new HashMap<>();

        System.out.println("Controller Agent ready - monitoring traffic system");

        registerService();
        javax.swing.SwingUtilities.invokeLater(() -> {
            gui = new TrafficLightGUI(this);
            gui.setVisible(true);
        });

        addBehaviour(new MonitoringBehaviour(this, 2000));
        addBehaviour(new ReceiveMessageBehaviour());
    }

    private void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(MessageConstants.CONTROLLER_SERVICE);
        sd.setName("traffic-controller");
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

        if (gui != null) {
            gui.dispose();
        }

        System.out.println("Controller Agent terminating.");
    }

    public Map<String, String> getSystemStatus() {
        return systemStatus;
    }

    public void updateSystemStatus(String component, String status) {
        systemStatus.put(component, status);
        if (gui != null) {
            gui.updateStatus(component, status);
        }
    }


    private class ReceiveMessageBehaviour extends CyclicBehaviour {
        @Override
        public void action() {
            System.out.println("ControllerAgent: Checking for messages...");

            MessageTemplate mt = MessageTemplate.MatchOntology(MessageConstants.TRAFFIC_ONTOLOGY);
            ACLMessage msg = receive(mt);

            if (msg != null) {
                System.out.println("ControllerAgent: Received message!");
                System.out.println("  From: " + msg.getSender().getLocalName());
                System.out.println("  Performative: " + msg.getPerformative());
                System.out.println("  Content: " + msg.getContent());
                handleMessage(msg);
            } else {
                System.out.println("ControllerAgent: No messages found, blocking...");
                block();
            }
        }

        private void handleMessage(ACLMessage msg) {
            System.out.println("ControllerAgent: Handling message with performative: " + msg.getPerformative());

            switch (msg.getPerformative()) {
                case ACLMessage.INFORM:
                    System.out.println("ControllerAgent: Processing INFORM message");
                    if (msg.getContent().startsWith(MessageConstants.STATUS_UPDATE)) {
                        System.out.println("ControllerAgent: This is a STATUS_UPDATE message");
                        handleStatusUpdate(msg);
                    } else {
                        System.out.println("ControllerAgent: INFORM message but not STATUS_UPDATE: " + msg.getContent());
                    }
                    break;
                case ACLMessage.REQUEST:
                    System.out.println("ControllerAgent: Processing REQUEST message");
                    
                    break;
                default:
                    System.out.println("ControllerAgent: Unknown message type: " + msg.getPerformative());
            }
        }

        private void handleStatusUpdate(ACLMessage msg) {
            String sender = msg.getSender().getLocalName();
            String content = msg.getContent();

            
            System.out.println("ControllerAgent received status update from: " + sender);
            System.out.println("Message content: " + content);

            updateSystemStatus(sender, content);
            System.out.println("Called updateSystemStatus for " + sender + ": " + content);
        }
    }
}
