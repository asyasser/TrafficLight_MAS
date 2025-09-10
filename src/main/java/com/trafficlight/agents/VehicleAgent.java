package com.trafficlight.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

import com.trafficlight.models.VehicleState;
import com.trafficlight.messages.MessageConstants;
import com.trafficlight.behaviours.VehicleBehaviour;

public class VehicleAgent extends Agent {
    private VehicleState vehicleState;

    @Override
    protected void setup() {
        
        Object[] args = getArguments();
        if (args != null && args.length >= 4) {
            String vehicleId = (String) args[0];
            int x = (Integer) args[1];
            int y = (Integer) args[2];
            String direction = (String) args[3];

            vehicleState = new VehicleState(vehicleId, x, y, direction);

            System.out.println("Vehicle Agent " + vehicleId + " ready at position (" + x + "," + y + ") heading " + direction);
        } else {
            System.err.println("Vehicle Agent needs ID, coordinates and direction!");
            doDelete();
            return;
        }

        
        registerService();

       
        addBehaviour(new VehicleBehaviour(this, vehicleState));
        addBehaviour(new ReceiveMessageBehaviour());
    }

    private void registerService() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType(MessageConstants.VEHICLE_SERVICE);
        sd.setName(vehicleState.getVehicleId());
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
        System.out.println("Vehicle Agent " + vehicleState.getVehicleId() + " terminating.");
    }

    public VehicleState getVehicleState() {
        return vehicleState;
    }

  
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
                case ACLMessage.AGREE:
                    if (MessageConstants.PASSAGE_GRANTED.equals(msg.getContent())) {
                        handlePassageGranted();
                    }
                    break;
                case ACLMessage.REFUSE:
                    if (MessageConstants.PASSAGE_DENIED.equals(msg.getContent())) {
                        handlePassageDenied();
                    }
                    break;
                case ACLMessage.INFORM:
                   
                    break;
            }
        }

        private void handlePassageGranted() {
            System.out.println("Vehicle " + vehicleState.getVehicleId() + " received passage permission");
            vehicleState.setWaitingAtLight(false);
            
        }

        private void handlePassageDenied() {
            System.out.println("Vehicle " + vehicleState.getVehicleId() + " must wait at traffic light");
            vehicleState.setWaitingAtLight(true);
        }
    }
}
