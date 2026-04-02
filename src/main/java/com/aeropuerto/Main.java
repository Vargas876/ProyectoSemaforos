package com.aeropuerto;

import com.aeropuerto.gui.SimulatorGUI;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SimulatorGUI gui = new SimulatorGUI();
            gui.setVisible(true);
        });
    }
}
