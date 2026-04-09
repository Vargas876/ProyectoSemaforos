# Simulación de Aeropuerto Inteligente: Concurrencia y Semáforos en Java

Este proyecto es una demostración visual y práctica de los fundamentos de concurrencia en Arquitecturas y Sistemas Operativos. Demuestra de manera entretenida cómo se comportan cientos de hilos (aviones) manejando memoria compartida y recursos limitados.

El simulador demuestra tanto soluciones estables (**Exclusión Mutua** y **Sincronización**) como los peligros fatales del multiprocesamiento paralelo (**Condiciones de Carrera** y **Abrazos Mortales/Deadlocks**).

---

## Características Principales

* **Multihilo Real**: Cada avión instanciado arranca en un hilo nativo completamente apartado (`Thread`).
* **Gráficos en Tiempo Real**: Desarrollado con **Java Swing** y renderizado pasivo de paneles. Visualiza los aterrizajes, esperas en pista y aforo de la terminal en tiempo real.
* **Control UI Interactivo**: Lanza docenas de aviones dando un clic al botón.
* **Consola de Estado Integral**: Un log text-based incorporado en la UI para monitorear cada solicitud (`acquire`) y cada liberación (`release`) del sistema.

## 🛠️ Modos de Simulación 🚨

El programa ofrece tres botones principales en la interfaz que cambian el algoritmo de vuelo al vuelo:

1. 🟢 **Modo Seguro (Sincronización Total)**
   Implementación perfecta usando una Pista (Recurso Exclusivo / `Mutex`) y Tres Puertas (Recurso Múltiple Sincronizado). Utiliza semáforos preprogramados (`java.util.concurrent.Semaphore`) para garantizar el tráfico fluido mediante la arquitectura "Hold and Wait" estricta, previniendo el caos.
2. 🔴 **Modo Condición de Carrera (Caos Paralelo)**
   ¿Qué pasa si quitas la cerradura y dejas la puerta abierta? En este modo rompemos adrede la *Exclusión Mutua*. Al saltarse el chequeo de la Pista, varios hilos aterrizan unos encima del otro modificando las mismas variables al mismo tiempo, resultando en colisiones evidenciables en la bitácora gráfica.
3. 🟡 **Modo Deadlock (El Abrazo Mortal)**
   El sistema no colisionará, simplemente **se paralizará de por vida**. Rompemos la jerarquía exigente de la concurrencia alterando el orden de bloqueo: forzamos al avión a pedir la Pista y enredar a todo el tráfico saliente por carencia de puertas. Es una muestra perfecta matemática de "Espera Circular".

---

## Requisitos y Ejecución

**Tecnologías:**

- Java Development Kit (JDK) 8+
- Maven (Para compilación y arranque rápido)
- Visualización de interfaz gráfica orientada a objetos usando Java Swing / AWT.

**Ejecutar el proyecto (Desde la terminal de la carpeta raíz):**
Si tienes Maven configurado, utiliza:

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.aeropuerto.Main"
```

O si utilizas tu IDE favorito (Visual Studio Code, IntelliJ IDEA, Eclipse), simplemente busca el archivo `com.aeropuerto.Main` y pulsa **Run**.

---

## 📂 Estructura del Proyecto

📁 `src/main/java/com/aeropuerto`
├── 📄 `Main.java`                -> **Arrancador del proyecto y gestor de UI en el Event Dispatch Thread.**
├── 📄 `Airport.java`             -> **El 'Monitor' y el Cerebro. Instancia y resguarda los Semáforos Concurrentes.**
├── 📄 `Airplane.java`            -> **Subclase de Thread. Los actores individuales de alto rendimiento.**
├── 📄 `Logger.java`              -> **Gestor estático que alimenta el rastreo atómico concurrente.**
└── 📁 `gui`
    ├── 📄 `SimulatorGUI.java`    -> **Controles frontales y gestión de los JFrame Swing.**
    └── 📄 `AirportPanel.java`    -> **El lienzo donde ocurre toda la animación de bits multihilo.**

---

## 📖 Documentación Extra

Para una inmersión completa y análisis analógicos sobre cómo el aeropuerto demuestra las metodologías del núcleo de sistemas como Linux o Windows (Planificadores de Procesos, Exclusión Mutua, Bus, y colas FCFS), consulta los documentos complementarios en la carpeta `docs/`.

> **Desarrollado y diagramado para dominar los problemas puros de la Concurrencia de Software.**
