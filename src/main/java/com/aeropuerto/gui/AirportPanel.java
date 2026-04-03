package com.aeropuerto.gui;

import com.aeropuerto.Airplane;
import com.aeropuerto.Airport;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AirportPanel extends JPanel {
    private List<Airplane> airplanes;
    private Airport airport;
    private Timer animationTimer;
    
    // Almacena la posición y ángulo actual de cada avión para la animación fluida
    private Map<Airplane, PlaneState> planeVisuals = new HashMap<>();

    class PlaneState {
        double x, y;
        double angle;
        boolean initialized = false;
        
        // Memoria para que el avión no cambie de puerta ni brinque
        int assignedGate = -1;
        double waitStartX = -1;
        double waitStartY = -1;
    }

    public AirportPanel(List<Airplane> airplanes, Airport airport) {
        this.airplanes = airplanes;
        this.airport = airport;
        
        setBackground(new Color(20, 25, 30)); 

        // Bucle principal de animación fluida (~60 FPS)
        animationTimer = new Timer(16, e -> {
            updateAnimations();
            repaint();
        });
        animationTimer.start();
    }

    private void updateAnimations() {
        int width = getWidth();
        int height = getHeight();
        if (width == 0) return;

        int runwayY = height / 2 - 20;
        int gateY = height - 120;
        int gateWidth = 140;
        int gap = (width - (Airport.NUM_GATES * gateWidth)) / (Airport.NUM_GATES + 1);

        List<Airplane> planesCopy;
        synchronized (airplanes) {
            planesCopy = List.copyOf(airplanes);
        }

        for (Airplane plane : planesCopy) {
            if (plane.isFinished()) {
                // Limpiar aviones que ya despegaron
                planeVisuals.remove(plane);
                continue;
            }

            PlaneState state = planeVisuals.computeIfAbsent(plane, k -> new PlaneState());

            double targetX = width / 2.0;
            double targetY = 0;

            if (plane.isWaiting()) {
                // Zona de Cielo
                if (state.waitStartX == -1) {
                    // Asignar un sector en el cielo pseudo-aleatorio para hacer fila aérea (stacking)
                    state.waitStartX = 150 + (Math.abs(plane.getPlaneName().hashCode()) % (width - 300));
                    state.waitStartY = 40 + (Math.abs(plane.hashCode()) % 80);
                }
                targetX = state.waitStartX;
                targetY = state.waitStartY;
            } else if (plane.isLanding()) {
                // Aterrizar: Bajar centro de la pista para uso
                targetX = width / 2.0 - 50;
                targetY = runwayY + 15;
            } else if (plane.isTakingOff()) {
                // Despegar: Avanzar rápido por la pista a la derecha para salir de pantalla
                targetX = width + 200; 
                targetY = runwayY + 15;
            } else if (plane.isAtGate()) {
                // En puerta: Usar una puerta única estable ligada al nombre del avión
                if (state.assignedGate == -1) {
                    state.assignedGate = Math.abs(plane.getPlaneName().hashCode()) % Airport.NUM_GATES;
                }
                targetX = gap + state.assignedGate * (gateWidth + gap) + gateWidth / 2.0 - 15;
                targetY = gateY + 50;
            }

            if (!state.initialized) {
                // Aparece por la izquierda suavemente
                state.x = -150; 
                state.y = targetY;
                state.angle = 0; // Mirando a la derecha
                state.initialized = true;
            }

            // --- FÍSICAS DE VUELO Y ROTACIÓN ---
            double dx = targetX - state.x;
            double dy = targetY - state.y;
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (dist > 3.0) {
                double speed = 5.0; // Velocidad avión
                state.x += (dx / dist) * speed;
                state.y += (dy / dist) * speed;
                
                // Rotación orgánica hacia donde viaja
                double targetAngle = Math.atan2(dy, dx);
                double diff = targetAngle - state.angle;
                
                while (diff < -Math.PI) diff += 2 * Math.PI; // Normalizar a semicírculo corto
                while (diff > Math.PI) diff -= 2 * Math.PI;
                state.angle += diff * 0.15; // Interpolación angular para curva de vuelo
            } else {
                // Posiciones de reposo estacionario o alineamiento
                double finalAngle = 0;
                if (plane.isLanding() || plane.isTakingOff()) {
                    finalAngle = 0; // Pista: apuntar a la derecha
                } else if (plane.isAtGate()) {
                    finalAngle = Math.PI / 2; // Puerta: apuntar Abajo
                } else if (plane.isWaiting()) {
                    finalAngle = 0; // Aire: apuntar al horizonte
                }
                
                double diff = finalAngle - state.angle;
                while (diff < -Math.PI) diff += 2 * Math.PI;
                while (diff > Math.PI) diff -= 2 * Math.PI;
                state.angle += diff * 0.1;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        drawEnvironment(g2, width, height);
        drawAircrafts(g2);
    }

    private void drawEnvironment(Graphics2D g2, int width, int height) {
        int runwayY = height / 2 - 20;
        int runwayHeight = 80;
        int gateY = height - 120;
        int gateWidth = 140;

        // 1. Pista realista
        g2.setColor(new Color(45, 50, 55));
        g2.fillRect(30, runwayY, width - 60, runwayHeight);
        
        g2.setColor(new Color(200, 200, 200));
        g2.fillRect(30, runwayY, width - 60, 4);
        g2.fillRect(30, runwayY + runwayHeight - 4, width - 60, 4);
        
        g2.setColor(new Color(255, 204, 0));
        g2.setStroke(new BasicStroke(4, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10, new float[]{30, 20}, 0));
        g2.drawLine(40, runwayY + runwayHeight / 2, width - 40, runwayY + runwayHeight / 2);
        
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 24));
        g2.drawString("09R", 60, runwayY + runwayHeight/2 + 8);
        g2.drawString("27L", width - 110, runwayY + runwayHeight/2 + 8);

        drawTrafficLight(g2, width - 150, runwayY - 30, airport.getAvailableRunways() > 0 ? Color.GREEN : Color.RED, "PISTA");

        // 2. Terminal
        int gap = (width - (Airport.NUM_GATES * gateWidth)) / (Airport.NUM_GATES + 1);

        g2.setColor(new Color(30, 35, 40)); 
        g2.fillRect(0, gateY, width, height - gateY);
        
        g2.setColor(new Color(15, 20, 25));
        g2.fillRect(0, gateY + 50, width, height - gateY - 50);

        for (int i = 0; i < Airport.NUM_GATES; i++) {
            int x = gap + i * (gateWidth + gap);
            
            // Dibujar estructura de la Terminal (Puerta)
            g2.setColor(new Color(60, 65, 70));
            g2.fillRoundRect(x - 5, gateY + 40, gateWidth + 10, 80, 15, 15);
            
            // Túnel de embarque (Manga)
            g2.setColor(new Color(100, 105, 110));
            g2.fillRect(x + gateWidth/2 - 12, gateY, 24, 45);
            g2.setColor(new Color(40, 40, 40));
            for(int j = 0; j < 5; j++) { // Efecto acordeón
                g2.drawLine(x + gateWidth/2 - 12, gateY + (j*8), x + gateWidth/2 + 12, gateY + (j*8));
            }
            
            // Área de parqueo demarcada
            g2.setColor(new Color(255, 204, 0, 150));
            g2.setStroke(new BasicStroke(2));
            g2.drawRoundRect(x + 5, gateY + 5, gateWidth - 10, 95, 15, 15);
            
            // Nombre en español
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.drawString("PUERTA " + (i + 1), x + gateWidth/2 - 40, gateY + 85);

            Color gateLight = (airport.getAvailableGates() > 0) ? Color.GREEN : Color.RED;
            drawTrafficLight(g2, x + 10, gateY + 10, gateLight, "");
        }
        
        drawTrafficLight(g2, 50, gateY - 30, airport.getAvailableGates() > 0 ? Color.GREEN : Color.RED, "PUERTAS");

        // 3. Cielo
        g2.setColor(new Color(255, 255, 255, 150));
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.drawString("✈ ZONA DE ESPERA AÉREA", 30, 40);
    }

    private void drawTrafficLight(Graphics2D g2, int x, int y, Color color, String label) {
        g2.setColor(Color.BLACK);
        g2.fillRoundRect(x, y, 50, 20, 10, 10);
        g2.setColor(Color.DARK_GRAY);
        g2.drawRoundRect(x, y, 50, 20, 10, 10);

        if (color == Color.GREEN) {
            g2.setColor(new Color(0, 255, 0));
            g2.fillOval(x + 5, y + 3, 14, 14);
            g2.setColor(new Color(0, 255, 0, 80));
            g2.fillOval(x + 1, y - 1, 22, 22);
        } else {
            g2.setColor(new Color(0, 80, 0));
            g2.fillOval(x + 5, y + 3, 14, 14);
        }

        if (color == Color.RED) {
            g2.setColor(new Color(255, 0, 0));
            g2.fillOval(x + 25, y + 3, 14, 14);
            g2.setColor(new Color(255, 0, 0, 80));
            g2.fillOval(x + 21, y - 1, 22, 22);
        } else {
            g2.setColor(new Color(80, 0, 0));
            g2.fillOval(x + 25, y + 3, 14, 14);
        }

        if (!label.isEmpty()) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 12));
            g2.drawString(label, x + 55, y + 15);
        }
    }

    private void drawAircrafts(Graphics2D g2) {
        List<Airplane> planesCopy;
        synchronized (airplanes) {
            planesCopy = List.copyOf(airplanes);
        }

        for (Airplane plane : planesCopy) {
            PlaneState state = planeVisuals.get(plane);
            if (state == null) continue;

            AffineTransform oldTransform = g2.getTransform();
            
            // Translación y rotación alineada al vuelo
            g2.translate(state.x + 20, state.y + 20); 
            g2.rotate(state.angle);

            int cx = -25;
            int cy = -20;

            Path2D.Double wings = new Path2D.Double();
            wings.moveTo(cx + 25, cy + 10);
            wings.lineTo(cx + 5, cy - 10);
            wings.lineTo(cx + 15, cy - 10);
            wings.lineTo(cx + 35, cy + 10);
            wings.lineTo(cx + 15, cy + 30);
            wings.lineTo(cx + 5, cy + 30);
            wings.closePath();
            g2.setColor(new Color(170, 180, 190));
            g2.fill(wings);
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(1));
            g2.draw(wings);

            Path2D.Double tailWings = new Path2D.Double();
            tailWings.moveTo(cx + 5, cy + 10);
            tailWings.lineTo(cx - 5, cy + 2);
            tailWings.lineTo(cx, cy + 2);
            tailWings.lineTo(cx + 10, cy + 10);
            tailWings.lineTo(cx, cy + 18);
            tailWings.lineTo(cx - 5, cy + 18);
            tailWings.closePath();
            g2.setColor(new Color(170, 180, 190));
            g2.fill(tailWings);

            g2.setColor(Color.WHITE);
            g2.fillRoundRect(cx, cy + 5, 50, 12, 12, 12);
            g2.setColor(Color.DARK_GRAY);
            g2.drawRoundRect(cx, cy + 5, 50, 12, 12, 12);

            g2.setColor(new Color(100, 200, 255));
            g2.fillArc(cx + 38, cy + 6, 10, 10, -90, 180);

            // Sombra
            g2.setColor(new Color(0, 0, 0, 50));
            g2.fillRoundRect(cx - 5, cy + 15, 45, 8, 10, 10);

            g2.setTransform(oldTransform);
            
            // Texto Nombre del avión fijo debajo de él
            String plate = plane.getPlaneName().replace("Avión-", "A");
            g2.setColor(Color.BLACK);
            g2.drawString(plate, (int)state.x + 8, (int)state.y - 6);
            g2.setColor(Color.CYAN);
            g2.drawString(plate, (int)state.x + 7, (int)state.y - 7);
        }
    }
}
