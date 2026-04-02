# Conclusiones y Reflexión

## Conclusiones Técnicas
1. **La concurrencia sin regulación desencadena operaciones inconsistentes**: Durante el escenario de "Condición de Carrera", observamos que la manipulación de recursos por encima de la capacidad permitida produce estados visuales desorganizados y fallas de acceso lógico a un recurso único (la pista).
2. **Los Semáforos de conteo simplifican la administración de límites**: Utilizar `Semaphore(n)` permite a Java manejar eficientemente la cola interna de espera usando colas FIFO (`fairness = true`), deteniendo automáticamente los Hilos adicionales una vez consumida su cuota de tres permisos y despertándolos cuando corresponda.
3. **El orden de solicitud de recursos define la salud estructural anti-deadlocks**: La simple alteración del orden de petición en `acquire()` entre pista y puerta es la diferencia exacta entre un aeropuerto funcional e impecable y un Deadlock total del sistema informático.

## Reflexión: Sistemas Operativos vs Vida Real

Los problemas detectados en esta simulación de un aeropuerto inteligente son análogos a conceptos clave explicados en la asignatura de Sistemas Operativos.

- En la realidad, **el Controlador de Tráfico Aéreo funciona como nuestro "Semáforo"**: asigna acceso bajo protocolos de control estricto de Exclusión Mutua para no mezclar aviones en pistas.
- Cuando manejamos bases de datos complejas o transacciones en **sistemas bancarios**, un interbloqueo (*deadlock*) que deja colgado el sistema corresponde a nuestro caso en que dos clientes se esperan entre sí para un retiro mutuo de cuentas. Garantizar un esquema ordenado previene la caída infinita de la aplicación.
- Servidores que atienden grandes descargas limitan su número de peticiones a procesar concurrentemente (similares a nuestras **3 Puertas de Embarque**) porque el hardware se saturaría sin gestión concurrencial. 

**Cierre:**
La simulación evidencia cómo la correcta aplicación de semáforos, exclusión mutua y sincronización permite gestionar múltiples procesos concurrentes de forma segura, evitando errores críticos como condiciones de carrera y deadlocks.
