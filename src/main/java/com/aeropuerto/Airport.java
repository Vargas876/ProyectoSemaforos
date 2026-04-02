package com.aeropuerto;

import java.util.concurrent.Semaphore;

public class Airport {
    public static final int NUM_RUNWAYS = 1;
    public static final int NUM_GATES = 3;

    private Semaphore runwaySemaphore;
    private Semaphore gatesSemaphore;

    public enum SimulationMode {
        SAFE_MODE, RACE_CONDITION, DEADLOCK
    }

    private SimulationMode currentMode = SimulationMode.SAFE_MODE;

    public Airport() {
        resetSemaphores();
    }

    public void setMode(SimulationMode mode) {
        this.currentMode = mode;
        resetSemaphores();
    }

    public void resetSemaphores() {
        // En Java los semáforos pueden inicializarse con 'fairness' (true) para garantizar FIFO
        runwaySemaphore = new Semaphore(NUM_RUNWAYS, true);
        gatesSemaphore = new Semaphore(NUM_GATES, true);
    }

    // Punto de entrada para el ciclo de vida del avión
    public void airplaneLogic(Airplane airplane) {
        switch (currentMode) {
            case SAFE_MODE:
                logicSafeMode(airplane);
                break;
            case RACE_CONDITION:
                logicRaceCondition(airplane);
                break;
            case DEADLOCK:
                logicDeadlock(airplane);
                break;
        }
    }

    /*
     * MODO SEGURO
     * Sincronización compuesta: Adquirimos el recurso limitando (puerta) ANTES 
     * del recurso crítico y exclusivo (pista) para aterrizar. Así garantizamos:
     * - Exclusión mutua en la pista.
     * - Límite de capacidad en puertas.
     * - Ausencia de deadlocks por orden global de adquisición.
     */
    private void logicSafeMode(Airplane airplane) {
        try {
            // FASE 1: Solicitud de Puerta (evitar sobrepoblar la pista sin destino)
            Logger.log("🛩️ " + airplane.getPlaneName() + " en el aire, solicitando puerta para poder aterrizar.");
            airplane.setWaiting(true);
            gatesSemaphore.acquire(); 
            // Ya tenemos puerta asignada, ahora solitamos usar la pista
            
            // FASE 2: Aterrizaje
            Logger.log("🛩️ " + airplane.getPlaneName() + " (Con puerta asegurada) solicita pista para aterrizar.");
            runwaySemaphore.acquire();
            
            airplane.setWaiting(false);
            airplane.setLanding(true);
            Logger.log("🛬 " + airplane.getPlaneName() + " EXCLUSIÓN MUTUA: Aterrizando en la pista.");
            Thread.sleep(1500); // Simulando tiempo de aterrizaje
            
            airplane.setLanding(false);
            airplane.setAtGate(true);
            Logger.log("🏢 " + airplane.getPlaneName() + " se estaciona en su puerta. Liberando pista.");
            runwaySemaphore.release();

            // FASE 3: En Puerta de Embarque
            Thread.sleep(3000); // Simulando embarque/desembarque

            // FASE 4: Despegue (ahora solicitamos pista para salir, y liberaremos la puerta)
            Logger.log("🛩️ " + airplane.getPlaneName() + " listo para despegar. Solicita pista.");
            runwaySemaphore.acquire();

            airplane.setAtGate(false);
            airplane.setTakingOff(true);
            
            // Ya en pista, liberamos la puerta para que otro pueda ir solicitándola
            Logger.log("🏢 " + airplane.getPlaneName() + " libera la puerta de embarque.");
            gatesSemaphore.release();

            Logger.log("🛫 " + airplane.getPlaneName() + " EXCLUSIÓN MUTUA: Despegando de la pista.");
            Thread.sleep(1500); // Simulando tiempo de despegue

            airplane.setTakingOff(false);
            airplane.setFinished(true);
            Logger.log("🛫 " + airplane.getPlaneName() + " ha despegado. Liberando pista.");
            runwaySemaphore.release();

        } catch (InterruptedException e) {
            Logger.log("❌ Error en avión " + airplane.getPlaneName() + ": " + e.getMessage());
        }
    }

    /*
     * CONDICIÓN DE CARRERA
     * Omitimos la exclusión mutua de la pista (runwaySemaphore).
     * Los aviones colisionarán en la pista al no haber semáforo de por medio.
     */
    private void logicRaceCondition(Airplane airplane) {
        try {
            Logger.log("🛩️ " + airplane.getPlaneName() + " en el aire, solicitando puerta.");
            airplane.setWaiting(true);
            gatesSemaphore.acquire(); 
            
            airplane.setWaiting(false);
            airplane.setLanding(true);
            // 🚨 NO ADQUIRIMOS runwaySemaphore 🚨
            Logger.log("🔴 PELIGRO: " + airplane.getPlaneName() + " aterrizando SIN CHEQUEAR PISTA (Race Condition).");
            Thread.sleep(1500); 
            
            airplane.setLanding(false);
            airplane.setAtGate(true);
            Logger.log("🏢 " + airplane.getPlaneName() + " se estaciona en su puerta.");

            Thread.sleep(3000); 

            Logger.log("🛩️ " + airplane.getPlaneName() + " listo para despegar.");
            
            airplane.setAtGate(false);
            airplane.setTakingOff(true);
            
            gatesSemaphore.release();

            // 🚨 NO ADQUIRIMOS runwaySemaphore 🚨
            Logger.log("🔴 PELIGRO: " + airplane.getPlaneName() + " despegando SIN CHEQUEAR PISTA (Race Condition).");
            Thread.sleep(1500); 

            airplane.setTakingOff(false);
            airplane.setFinished(true);
            Logger.log("🛫 " + airplane.getPlaneName() + " ha despegado.");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
     * DEADLOCK
     * Modificamos artificialmente el orden en que los aviones intentan tener la pista y la puerta.
     * En lugar de pedir puerta primero, pedimos pista primero y lugego puerta.
     * Si varios aviones se aglomeran en las puertas queriendo salir (piden pista),
     * y un avión aterriza pidiendo pista primero, tomará la pista y esperará eternamente una puerta.
     */
    private void logicDeadlock(Airplane airplane) {
        try {
            Logger.log("🛩️ " + airplane.getPlaneName() + " en el aire solicitando PISTA.");
            airplane.setWaiting(true);
            
            // Tomamos la pista PRIMERO
            runwaySemaphore.acquire();
            Logger.log("🟡 " + airplane.getPlaneName() + " obtuvo la PISTA. Ahora solicita PUERTA.");
            
            // Ahora pide puerta (Si las puertas están llenas de aviones que quieren salir, se crea el Deadlock)
            gatesSemaphore.acquire(); 
            
            airplane.setWaiting(false);
            airplane.setLanding(true);
            Logger.log("🛬 " + airplane.getPlaneName() + " aterrizando en la pista.");
            Thread.sleep(1000); 
            
            airplane.setLanding(false);
            airplane.setAtGate(true);
            Logger.log("🏢 " + airplane.getPlaneName() + " se estaciona y libera pista.");
            runwaySemaphore.release();

            Thread.sleep(3000); 

            Logger.log("🛩️ " + airplane.getPlaneName() + " listo para despegar. Solicita PISTA.");
            runwaySemaphore.acquire();
            
            airplane.setAtGate(false);
            airplane.setTakingOff(true);
            Logger.log("🏢 " + airplane.getPlaneName() + " libera puerta.");
            gatesSemaphore.release();

            Logger.log("🛫 " + airplane.getPlaneName() + " despegando.");
            Thread.sleep(1000); 

            airplane.setTakingOff(false);
            airplane.setFinished(true);
            Logger.log("🛫 " + airplane.getPlaneName() + " ha despegado y libera pista.");
            runwaySemaphore.release();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Para la UI, getters de permisos disponibles:
    public int getAvailableGates() {
        return gatesSemaphore.availablePermits();
    }
    public int getAvailableRunways() {
        return runwaySemaphore.availablePermits();
    }
}
