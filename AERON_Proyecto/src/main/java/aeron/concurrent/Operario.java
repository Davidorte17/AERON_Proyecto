package aeron.concurrent;

import aeron.util.Logger;

/**
 * Representa a un controlador aéreo (Operario) que trabaja en la Torre.
 * <p>
 * ROL EN EL SISTEMA: CONSUMIDOR.
 * Este hilo se encarga de extraer (consumir) las peticiones que los aviones (Productores)
 * han dejado en la cola de la Torre y procesarlas una a una.
 */
public class Operario implements Runnable {

    // Referencia a la torre compartida donde están la cola y los semáforos
    private ControlTowerConcurrent tower;

    // Identificador único del operario (ej: "OP-001")
    private String id;

    /**
     * Constructor del operario.
     * @param tower La torre donde va a trabajar.
     * @param numero El número asignado para generar su ID.
     */
    public Operario(ControlTowerConcurrent tower, int numero) {
        this.tower = tower;
        // Formateamos el ID para cumplir con los requisitos de log (OP-001, OP-002...)
        this.id = String.format("OP-%03d", numero);
    }

    /**
     * Ciclo de vida del hilo del Operario.
     * Ejecuta un bucle infinito para procesar peticiones continuamente.
     */
    @Override
    public void run() {
        Logger.logTorre("Operario [" + id + "] esperando nueva petición...");
        try {
            // Bucle infinito: El operario nunca deja de trabajar mientras la simulación esté activa.
            while (true) {

                // PASO 1: CONSUMIR (Bloqueante)
                // Intentamos sacar una petición de la cola.
                // IMPORTANTE PARA DEFENSA: Si la cola está vacía, el hilo se queda BLOQUEADO
                // en esta línea (esperando en el semáforo 'semaforoPeticiones' de la torre).
                // No consume CPU mientras espera.
                Request peticion = tower.obtenerSiguientePeticion();

                // PASO 2: PROCESAR (Sección Crítica)
                // Una vez tenemos la petición, delegamos la lógica compleja a la torre.
                // Pasamos nuestro ID para que salga reflejado en los logs.
                tower.procesarPeticion(peticion, this.id);

                // Simulamos un pequeño tiempo de descanso o gestión administrativa entre tareas
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            // Si el hilo es interrumpido (al cerrar la app), salimos del bucle limpiamente.
        }
    }
}