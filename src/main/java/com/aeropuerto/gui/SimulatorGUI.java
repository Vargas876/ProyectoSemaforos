package com.aeropuerto.gui;

import com.aeropuerto.Airplane;
import com.aeropuerto.Airport;
import com.aeropuerto.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimulatorGUI extends JFrame {
    private Airport airport;
    private List<Airplane> airplanes;
    private int airplaneCounter = 1;
    private AirportPanel airportPanel;

    private JTextArea logArea;
    private JLabel lblRunwayStatus;
    private JLabel lblGatesStatus;
    private JLabel lblWaitingPlanes;

    public SimulatorGUI() {
        airport = new Airport();
        airplanes = Collections.synchronizedList(new ArrayList<>());

        setTitle("Simulación de Aeropuerto Inteligente");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        initUI();

        // Timer para actualizar indicadores frecuentemente
        Timer timer = new Timer(500, e -> updateStatus());
        timer.start();
    }

    private void initUI() {
        // --- Panel Superior: Controles ---
        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(BorderFactory.createTitledBorder("Controles de Simulación"));

        JButton btnSafeMode = new JButton("Modo Seguro");
        JButton btnRaceMode = new JButton("Condición de Carrera");
        JButton btnDeadlockMode = new JButton("Deadlock");
        JButton btnSpawnPlane = new JButton("✈️ Generar Avión");
        JButton btnReset = new JButton("🔄 Reiniciar");

        controlPanel.add(btnSafeMode);
        controlPanel.add(btnRaceMode);
        controlPanel.add(btnDeadlockMode);
        controlPanel.add(btnSpawnPlane);
        controlPanel.add(btnReset);

        add(controlPanel, BorderLayout.NORTH);

        // --- Panel Central: Estado y Animación ---
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Simulación Gráfica en Tiempo Real", 
            javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, 
            new Font("SansSerif", Font.BOLD, 12), Color.WHITE));
        statusPanel.setBackground(new Color(30, 35, 40));

        JPanel textPanel = new JPanel(new GridLayout(1, 3));
        textPanel.setBackground(new Color(30, 35, 40));

        lblRunwayStatus = new JLabel("Pistas Disponibles: " + Airport.NUM_RUNWAYS);
        lblRunwayStatus.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblRunwayStatus.setForeground(Color.WHITE);
        
        lblGatesStatus = new JLabel("Puertas Disponibles: " + Airport.NUM_GATES);
        lblGatesStatus.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblGatesStatus.setForeground(Color.WHITE);
        
        lblWaitingPlanes = new JLabel("Aviones en Espera / Volando: 0");
        lblWaitingPlanes.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblWaitingPlanes.setForeground(Color.WHITE);

        textPanel.add(lblRunwayStatus);
        textPanel.add(lblGatesStatus);
        textPanel.add(lblWaitingPlanes);

        statusPanel.add(textPanel, BorderLayout.NORTH);

        // Nuestro nuevo panel de animación (requiere objeto airport para los semáforos verdes/rojos)
        airportPanel = new AirportPanel(airplanes, airport);
        statusPanel.add(airportPanel, BorderLayout.CENTER);

        add(statusPanel, BorderLayout.CENTER);

        // --- Panel Inferior: Logs ---
        logArea = new JTextArea(10, 30);
        logArea.setEditable(false);
        logArea.setBackground(new Color(15, 20, 25));
        logArea.setForeground(new Color(180, 200, 255));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), "Log de Eventos Concurrencia (Exclusión Mutua)", 
            javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, 
            new Font("SansSerif", Font.BOLD, 12), Color.WHITE));
        
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(30, 35, 40));
        bottomPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(bottomPanel, BorderLayout.SOUTH);

        // Enlazar el Logger
        Logger.setTextArea(logArea);
        Logger.log("SISTEMA INICIADO. Listos para operar.");

        // --- Acciones de Botones ---
        btnSafeMode.addActionListener(e -> setMode(Airport.SimulationMode.SAFE_MODE, "MODO SEGURO ACTIVO"));
        btnRaceMode.addActionListener(e -> setMode(Airport.SimulationMode.RACE_CONDITION, "CONDICIÓN DE CARRERA ACTIVA"));
        btnDeadlockMode.addActionListener(e -> setMode(Airport.SimulationMode.DEADLOCK, "DEADLOCK ACTIVO (Cambiando el orden de Semáforos)"));

        btnSpawnPlane.addActionListener(e -> spawnPlane());

        btnReset.addActionListener(e -> {
            // Detenemos de forma drástica para la demostración
            for (Airplane p : airplanes) {
                p.interrupt();
            }
            airplanes.clear();
            airport.resetSemaphores();
            airplaneCounter = 1;
            logArea.setText("");
            Logger.log("--- SIMULACIÓN REINICIADA ---");
        });
    }

    private void setMode(Airport.SimulationMode mode, String msg) {
        airport.setMode(mode);
        Logger.log("===============================");
        Logger.log(msg);
        Logger.log("===============================");
    }

    private void spawnPlane() {
        Airplane plane = new Airplane("Avión-" + airplaneCounter++, airport);
        airplanes.add(plane);
        plane.start();
    }

    private void updateStatus() {
        int r = airport.getAvailableRunways();
        int g = airport.getAvailableGates();

        lblRunwayStatus.setText("Pistas Disponibles: " + r + " / " + Airport.NUM_RUNWAYS);
        lblGatesStatus.setText("Puertas Disponibles: " + g + " / " + Airport.NUM_GATES);

        long waitingCount = airplanes.stream().filter(Airplane::isWaiting).count();
        lblWaitingPlanes.setText("Aviones en Espera / Volando: " + waitingCount);

        // Si es que está ocupada
        if(r == 0) lblRunwayStatus.setForeground(Color.RED);
        else lblRunwayStatus.setForeground(new Color(0, 150, 0));

        if(g == 0) lblGatesStatus.setForeground(Color.RED);
        else lblGatesStatus.setForeground(new Color(0, 150, 0));

        // Refrescar animación
        if (airportPanel != null) {
            airportPanel.repaint();
        }
    }
}
