package com.trafficlight.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import com.trafficlight.models.TrafficLightState;

public class IntersectionPanel extends JPanel {
    private Map<String, TrafficLightState> intersectionStates;
    private Map<String, Point> intersectionPositions;
    private Map<String, Point> vehiclePositions;
    private final int GRID_SIZE = 40;
    private final int INTERSECTION_SIZE = 20;
    private final int VEHICLE_SIZE = 8;

    public IntersectionPanel() {
        intersectionStates = new HashMap<>();
        intersectionPositions = new HashMap<>();
        vehiclePositions = new HashMap<>();

        setPreferredSize(new Dimension(600, 400));
        setBackground(Color.DARK_GRAY);

        // Initialize some default intersections
        addIntersection("TL1", 3, 3);
        addIntersection("TL2", 6, 3);
        addIntersection("TL3", 3, 6);
        addIntersection("TL4", 6, 6);

        // Start a timer to repaint the panel
        Timer timer = new Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });
        timer.start();
    }

    public void addIntersection(String id, int gridX, int gridY) {
        intersectionStates.put(id, TrafficLightState.RED);
        intersectionPositions.put(id, new Point(gridX, gridY));
    }

    public void updateIntersectionState(String id, TrafficLightState state) {
        intersectionStates.put(id, state);
        repaint();
    }

    public void updateVehiclePosition(String vehicleId, int gridX, int gridY) {
        vehiclePositions.put(vehicleId, new Point(gridX, gridY));
        repaint();
    }

    public void removeVehicle(String vehicleId) {
        vehiclePositions.remove(vehicleId);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawGrid(g2d);
        drawRoads(g2d);
        drawIntersections(g2d);
        drawVehicles(g2d);
        drawLegend(g2d);
    }

    private void drawGrid(Graphics2D g2d) {
        g2d.setColor(Color.GRAY);
        g2d.setStroke(new BasicStroke(1));

        // Draw grid lines
        for (int i = 0; i <= getWidth(); i += GRID_SIZE) {
            g2d.drawLine(i, 0, i, getHeight());
        }
        for (int i = 0; i <= getHeight(); i += GRID_SIZE) {
            g2d.drawLine(0, i, getWidth(), i);
        }
    }

    private void drawRoads(Graphics2D g2d) {
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.setStroke(new BasicStroke(3));

        // Draw horizontal roads
        for (Point pos : intersectionPositions.values()) {
            int y = pos.y * GRID_SIZE + GRID_SIZE/2;
            g2d.drawLine(0, y, getWidth(), y);
        }

        // Draw vertical roads
        for (Point pos : intersectionPositions.values()) {
            int x = pos.x * GRID_SIZE + GRID_SIZE/2;
            g2d.drawLine(x, 0, x, getHeight());
        }
    }

    private void drawIntersections(Graphics2D g2d) {
        for (Map.Entry<String, Point> entry : intersectionPositions.entrySet()) {
            String id = entry.getKey();
            Point pos = entry.getValue();
            TrafficLightState state = intersectionStates.getOrDefault(id, TrafficLightState.RED);

            int x = pos.x * GRID_SIZE + GRID_SIZE/2 - INTERSECTION_SIZE/2;
            int y = pos.y * GRID_SIZE + GRID_SIZE/2 - INTERSECTION_SIZE/2;

            // Draw intersection background
            g2d.setColor(Color.BLACK);
            g2d.fillRect(x, y, INTERSECTION_SIZE, INTERSECTION_SIZE);

            // Draw traffic light color
            Color lightColor = getTrafficLightColor(state);
            g2d.setColor(lightColor);
            g2d.fillOval(x + 2, y + 2, INTERSECTION_SIZE - 4, INTERSECTION_SIZE - 4);

            // Draw intersection ID
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x + (INTERSECTION_SIZE - fm.stringWidth(id)) / 2;
            int textY = y + INTERSECTION_SIZE + 15;
            g2d.drawString(id, textX, textY);
        }
    }

    private void drawVehicles(Graphics2D g2d) {
        g2d.setColor(Color.BLUE);

        for (Map.Entry<String, Point> entry : vehiclePositions.entrySet()) {
            String vehicleId = entry.getKey();
            Point pos = entry.getValue();

            int x = pos.x * GRID_SIZE + GRID_SIZE/2 - VEHICLE_SIZE/2;
            int y = pos.y * GRID_SIZE + GRID_SIZE/2 - VEHICLE_SIZE/2;

            // Draw vehicle as a small circle
            g2d.fillOval(x, y, VEHICLE_SIZE, VEHICLE_SIZE);

            // Draw vehicle ID
            g2d.setColor(Color.CYAN);
            g2d.setFont(new Font("Arial", Font.PLAIN, 8));
            FontMetrics fm = g2d.getFontMetrics();
            int textX = x + (VEHICLE_SIZE - fm.stringWidth(vehicleId)) / 2;
            int textY = y - 2;
            g2d.drawString(vehicleId, textX, textY);
            g2d.setColor(Color.BLUE);
        }
    }

    private void drawLegend(Graphics2D g2d) {
        int legendX = 10;
        int legendY = getHeight() - 80;

        g2d.setColor(Color.BLACK);
        g2d.fillRect(legendX - 5, legendY - 20, 150, 70);
        g2d.setColor(Color.WHITE);
        g2d.drawRect(legendX - 5, legendY - 20, 150, 70);

        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString("Legend:", legendX, legendY);

        // Red light
        g2d.setColor(Color.RED);
        g2d.fillOval(legendX, legendY + 5, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Red Light", legendX + 15, legendY + 15);

        // Yellow light
        g2d.setColor(Color.YELLOW);
        g2d.fillOval(legendX, legendY + 20, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Yellow Light", legendX + 15, legendY + 30);

        // Green light
        g2d.setColor(Color.GREEN);
        g2d.fillOval(legendX, legendY + 35, 10, 10);
        g2d.setColor(Color.WHITE);
        g2d.drawString("Green Light", legendX + 15, legendY + 45);
    }

    private Color getTrafficLightColor(TrafficLightState state) {
        switch (state) {
            case RED:
                return Color.RED;
            case YELLOW:
                return Color.YELLOW;
            case GREEN:
                return Color.GREEN;
            default:
                return Color.RED;
        }
    }
}
