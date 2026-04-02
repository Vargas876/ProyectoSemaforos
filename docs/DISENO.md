# Diseño del Sistema: Aeropuerto Inteligente

## 1. Arquitectura General
El proyecto sigue el paradigma de la Programación Orientada a Objetos en un entorno de **multi-hilos**.
- **Airport**: Representa el gestor controlador de los recursos críticos y concurrentes.
- **Airplane**: Extiende de `Thread`. Cada avión que se genera cobra vida propia e intenta seguir su ciclo de vuelos: **Aterrizaje -> Embarque -> Despegue**.
- **SimulatorGUI**: Interfaz gráfica en Java Swing. No maneja la lógica de validación pero refleja en tiempo real el estado de concurrencia.
- **Logger**: Administrador de salidas por consola interactiva; garantiza que el cruce de hilos no sobreponga el texto usando cerrojos (Locks).

## 2. Aplicación de Semáforos y Exclusión Mutua

### Semáforos Binarios (Pistas)
- Código: `runwaySemaphore = new Semaphore(1, true)`
- **Justificación:** La pista es un recurso crítico que acarrea exclusión mutua; solo una aeronave puede estar usándola en un momento específico de tiempo natural. Inicializarlo a 1 significa que actúa como *Mutex*. 

### Semáforos de Conteo (Puertas de Embarque)
- Código: `gatesSemaphore = new Semaphore(3, true)`
- **Justificación:** Representa un recurso limitado. El aeropuerto soporta hasta 3 aviones estacionados simultáneamente. Si llegan más, se bloquearán (`acquire()`) hasta que haya puertas libres.

### Lock de Reentrada (Protección Compartida)
- Código: `private static final ReentrantLock lock = new ReentrantLock()`
- **Justificación:** En `Logger.java` los mensajes no deben cortarse entre sí cuando varios aviones completan una acción al mismo milisegundo. Esto blinda la operación de adición y registro en la UI.

## 3. Problemas Clásicos y Solución

### Condición de Carrera simulada
En el `SimulationMode.RACE_CONDITION`, el avión omite llamar `runwaySemaphore.acquire()`.
- **Qué ocurre:** Dos aviones en proceso de aproximación final o en despegue imprimirán *"despegando"* o *"aterrizando"* simultáneamente sin esperar en la fila de la pista. 
- **Solución aplicable (Modo Seguro):** Activar un `Semáforo` binario con el método `acquire()` antes de intentar entrar o modificar las variables del recurso pista y `release()` de inmediato al salir, tal como se codifica en `landingSafeMode()`.

### Deadlock (Interbloqueo circular) simulado
En el `SimulationMode.DEADLOCK`, la lógica invierte el algoritmo estándar. El avión atrapa la "Pista" primero, y, sin liberarla, espera a que le asignen una "Puerta".
- **Qué ocurre:** Si 3 aviones aterrizados tienen ocupadas las 3 puertas, van a solicitar la pista para salir. Paralelamente, si un 4to avión entra y toma la pista para aterrizar, se quedará estancado esperando que se libere una puerta de embarque. Nadie avanza: El Avión 4 tiene la pista y necesita puerta. Los Aviones 1,2,3 tienen puerta y necesitan pista.
- **Solución aplicable (Prevención por orden global):** En el modo seguro obtenemos los recursos en base a prioridades lineales: Solicitar puerta asignada ANTES de acercarse a tomar la exclusiva pista para aterrizaje. Para despegue, tomar pista exclusiva ANTES de soltar la puerta. Así rompemos las posibles dependencias circulares.
