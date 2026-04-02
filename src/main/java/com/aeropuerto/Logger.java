package com.aeropuerto;

import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.ReentrantLock;

public class Logger {
    private static JTextArea textArea;
    private static final ReentrantLock lock = new ReentrantLock();
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");

    public static void setTextArea(JTextArea newTextArea) {
        textArea = newTextArea;
    }

    public static void log(String message) {
        lock.lock();
        try {
            String time = LocalTime.now().format(dtf);
            String finalMessage = "[" + time + "] " + message;
            System.out.println(finalMessage);
            
            if (textArea != null) {
                SwingUtilities.invokeLater(() -> {
                    textArea.append(finalMessage + "\n");
                    textArea.setCaretPosition(textArea.getDocument().getLength());
                });
            }
        } finally {
            lock.unlock();
        }
    }
}
