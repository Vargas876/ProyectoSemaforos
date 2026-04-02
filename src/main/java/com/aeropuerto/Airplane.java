package com.aeropuerto;

public class Airplane extends Thread {
    private final String planeName;
    private final Airport airport;
    
    // Estados internos para la UI (reflejando dónde está el avión)
    private boolean isWaiting = true;
    private boolean isLanding = false;
    private boolean isAtGate = false;
    private boolean isTakingOff = false;
    private boolean isFinished = false;

    public Airplane(String planeName, Airport airport) {
        this.planeName = planeName;
        this.airport = airport;
    }

    public String getPlaneName() {
        return planeName;
    }

    @Override
    public void run() {
        // Al iniciar el hilo, el avión se asume que intenta aterrizar
        airport.airplaneLogic(this);
    }
    
    // Métodos para cambiar de estado (pueden ser útiles si la UI los sondea)
    public void setWaiting(boolean waiting) { this.isWaiting = waiting; }
    public void setLanding(boolean landing) { this.isLanding = landing; }
    public void setAtGate(boolean atGate) { this.isAtGate = atGate; }
    public void setTakingOff(boolean takingOff) { this.isTakingOff = takingOff; }
    public void setFinished(boolean finished) { this.isFinished = finished; }

    public boolean isWaiting() { return isWaiting; }
    public boolean isLanding() { return isLanding; }
    public boolean isAtGate() { return isAtGate; }
    public boolean isTakingOff() { return isTakingOff; }
    public boolean isFinished() { return isFinished; }
}
