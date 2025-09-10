package com.trafficlight.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import com.trafficlight.agents.ControllerAgent;
import com.trafficlight.models.TrafficLightState;

public class TrafficLightGUI extends JFrame {
    private ControllerAgent controllerAgent;
    private IntersectionPanel intersectionPanel;
    private JTextArea statusArea;
    private JLabel systemStatsLabel;

   
    private JButton addVehicleButton;
    private JButton removeVehicleButton;
    private JButton pauseButton;
    private JButton resetButton;

    private boolean isPaused = false;

    public TrafficLightGUI(ControllerAgent controllerAgent) {
        this.controllerAgent = controllerAgent;
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("Traffic Light Multi-Agent System");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLayout(new BorderLayout());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int option = JOptionPane.showConfirmDialog(
                        TrafficLightGUI.this,
                        "Are you sure you want to exit the Traffic Light System?",
                        "Exit Confirmation",
                        JOptionPane.YES_NO_OPTION
                );

                if (option == JOptionPane.YES_OPTION) {
                    controllerAgent.doDelete();
                    System.exit(0);
                }
            }
        });

        createMainPanel();
        createControlPanel();
        createStatusPanel();

        pack();
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private void createMainPanel() {
        intersectionPanel = new IntersectionPanel();
        JScrollPane scrollPane = new JScrollPane(intersectionPanel);
        scrollPane.setPreferredSize(new Dimension(600, 400));
        add(scrollPane, BorderLayout.CENTER);
    }

    private void createControlPanel() {
        JPanel controlPanel = new JPanel(new GridLayout(2, 3, 5, 5));
        controlPanel.setBorder(BorderFactory.createTitledBorder("System Controls"));

        addVehicleButton = new JButton("Add Vehicle");
        addVehicleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showAddVehicleDialog();
            }
        });

        removeVehicleButton = new JButton("Remove Vehicle");
        removeVehicleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showRemoveVehicleDialog();
            }
        });

        pauseButton = new JButton("Pause System");
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                togglePause();
            }
        });

        resetButton = new JButton("Reset System");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetSystem();
            }
        });

        JButton emergencyButton = new JButton("Emergency Mode");
        emergencyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                activateEmergencyMode();
            }
        });

        systemStatsLabel = new JLabel("System Status: Running");
        systemStatsLabel.setHorizontalAlignment(SwingConstants.CENTER);

        controlPanel.add(addVehicleButton);
        controlPanel.add(removeVehicleButton);
        controlPanel.add(pauseButton);
        controlPanel.add(resetButton);
        controlPanel.add(emergencyButton);
        controlPanel.add(systemStatsLabel);

        add(controlPanel, BorderLayout.NORTH);
    }

    private void createStatusPanel() {
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createTitledBorder("System Status"));

        statusArea = new JTextArea(8, 50);
        statusArea.setEditable(false);
        statusArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        statusArea.setBackground(Color.BLACK);
        statusArea.setForeground(Color.GREEN);

        JScrollPane statusScrollPane = new JScrollPane(statusArea);
        statusScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        statusPanel.add(statusScrollPane, BorderLayout.CENTER);
        add(statusPanel, BorderLayout.SOUTH);

        // Add initial status message
        updateStatusArea("Traffic Light System initialized...\n");
        updateStatusArea("Controller Agent ready\n");
    }

    public void updateStatus(String component, String status) {
        SwingUtilities.invokeLater(() -> {
            String message = String.format("[%s] %s: %s\n",
                    new java.util.Date().toString(),
                    component, status);
            updateStatusArea(message);

            parseStatusUpdate(component, status);
        });
    }

    private void parseStatusUpdate(String component, String status) {
     
        System.out.println("GUI received - Component: " + component + ", Status: " + status);

        String[] parts = status.split(":");

        if (parts.length >= 3 && parts[0].equals("STATUS_UPDATE")) {
            if (component.startsWith("TL")) {
                
                if (parts.length >= 3) {
                    String lightId = parts[1];
                    String state = parts[2];
                    try {
                        TrafficLightState lightState = TrafficLightState.valueOf(state);
                        intersectionPanel.updateIntersectionState(lightId, lightState);
                        System.out.println("GUI: Updated " + lightId + " to " + state);
                    } catch (IllegalArgumentException e) {
                        System.out.println("GUI: Invalid traffic light state: " + state);
                    }
                }
            } else if (component.startsWith("V")) {
              
                if (parts.length >= 6) {
                    String vehicleId = parts[1];
                    try {
                        int x = Integer.parseInt(parts[2]);
                        int y = Integer.parseInt(parts[3]);
                        intersectionPanel.updateVehiclePosition(vehicleId, x, y);
                        System.out.println("GUI: Updated vehicle " + vehicleId + " to (" + x + "," + y + ")");
                    } catch (NumberFormatException e) {
                        System.out.println("GUI: Invalid vehicle coordinates: " + parts[2] + "," + parts[3]);
                    }
                }
            }
        } else {
            System.out.println("GUI: Unrecognized message format: " + status);
        }
    }

    private void updateStatusArea(String message) {
        statusArea.append(message);
        statusArea.setCaretPosition(statusArea.getDocument().getLength());

        
        String text = statusArea.getText();
        String[] lines = text.split("\n");
        if (lines.length > 100) {
            StringBuilder sb = new StringBuilder();
            for (int i = lines.length - 100; i < lines.length; i++) {
                sb.append(lines[i]).append("\n");
            }
            statusArea.setText(sb.toString());
        }
    }

    private void showAddVehicleDialog() {
        JPanel panel = new JPanel(new GridLayout(4, 2));
        JTextField idField = new JTextField("V" + System.currentTimeMillis() % 1000);
        JSpinner xSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 15, 1));
        JSpinner ySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 15, 1));
        JComboBox<String> directionBox = new JComboBox<>(new String[]{"NORTH", "SOUTH", "EAST", "WEST"});

        panel.add(new JLabel("Vehicle ID:"));
        panel.add(idField);
        panel.add(new JLabel("X Position:"));
        panel.add(xSpinner);
        panel.add(new JLabel("Y Position:"));
        panel.add(ySpinner);
        panel.add(new JLabel("Direction:"));
        panel.add(directionBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Vehicle", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String id = idField.getText();
            int x = (Integer) xSpinner.getValue();
            int y = (Integer) ySpinner.getValue();
            String direction = (String) directionBox.getSelectedItem();

            updateStatusArea("Creating new vehicle: " + id + " at (" + x + "," + y + ") heading " + direction + "\n");
          
        }
    }

    private void showRemoveVehicleDialog() {
        String vehicleId = JOptionPane.showInputDialog(this, "Enter Vehicle ID to remove:");
        if (vehicleId != null && !vehicleId.trim().isEmpty()) {
            intersectionPanel.removeVehicle(vehicleId);
            updateStatusArea("Removed vehicle: " + vehicleId + "\n");
        }
    }

    private void togglePause() {
        isPaused = !isPaused;
        if (isPaused) {
            pauseButton.setText("Resume System");
            systemStatsLabel.setText("System Status: PAUSED");
            updateStatusArea("System paused by user\n");
        } else {
            pauseButton.setText("Pause System");
            systemStatsLabel.setText("System Status: Running");
            updateStatusArea("System resumed by user\n");
        }
    }

    private void resetSystem() {
        int option = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to reset the entire system?",
                "Reset Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (option == JOptionPane.YES_OPTION) {
            updateStatusArea("System reset initiated...\n");
            
            intersectionPanel.repaint();
        }
    }

    private void activateEmergencyMode() {
        updateStatusArea("EMERGENCY MODE ACTIVATED - All lights set to RED\n");
        JOptionPane.showMessageDialog(this,
                "Emergency mode activated!\nAll traffic lights set to RED.",
                "Emergency Mode",
                JOptionPane.WARNING_MESSAGE);
    }
}
