package aeron.sequential;

import aeron.model.Airplane;
import aeron.model.FlightStatus;
import aeron.util.Logger;
import aeron.util.TowerInterface;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Implementación de la Torre de Control en modo SECUENCIAL (Práctica 2).
 * <p>
 * Esta clase gestiona las peticiones de los aviones de forma directa e inmediata,
 * sin utilizar hilos concurrentes para los operarios ni semáforos para la gestión
 * de recursos limitados. Sirve como línea base para comparar con la versión concurrente.
 */
public class ControlTower implements TowerInterface {

    // Cola FIFO (First In, First Out) simple para almacenar las peticiones de los aviones.
    // En este modo secuencial, no necesitamos protegerla con exclusión mutua porque
    // el flujo de ejecución es lineal.
    private Queue<Airplane> peticiones;

    /**
     * Constructor de la torre secuencial.
     * Inicializamos las estructuras de datos necesarias.
     */
    public ControlTower() {
        this.peticiones = new LinkedList<>();
        Logger.log("Torre de Control operativa y esperando peticiones.");
    }

    /**
     * Metodo de entrada que utilizan los aviones para solicitar una operación.
     * Al ser la versión secuencial, en cuanto llega una petición, la encolamos
     * y forzamos su procesamiento inmediato.
     * * @param avion El objeto avión que realiza la solicitud.
     */
    @Override
    public void registrarPeticion(Airplane avion) {
        // Añadimos el avión a la cola de espera
        peticiones.add(avion);
        Logger.log("Torre: Registrada petición de " + avion.getId() + " [" + avion.getStatus() + "]");

        // Invocamos directamente el procesamiento. En la versión concurrente,
        // esto lo harían los hilos de los Operarios de forma asíncrona.
        procesarPeticionesSecuencial();
    }

    /**
     * Lógica interna para vaciar la cola de peticiones.
     * Revisa qué quiere hacer el avión y le concede permiso inmediatamente
     * cambiando su estado, sin verificar la disponibilidad real de pistas o puertas.
     */
    // Metodo simple para procesar la cola (Solo para modo Secuencial)
    private void procesarPeticionesSecuencial() {
        // Procesamos todos los elementos que haya en la cola uno a uno
        while (!peticiones.isEmpty()) {
            Airplane avion = peticiones.poll(); // Sacamos el primero de la cola (FIFO)
            Logger.log("Torre: Procesando petición de " + avion.getId());

            // Lógica simple de asignación (Simulada por ahora)
            if (avion.getStatus() == FlightStatus.LANDING_REQUEST) {
                // CASO ATERRIZAJE:
                // Le damos permiso para aterrizar cambiando su estado.
                // Esto desbloqueará el bucle 'while' que tiene el avión en su método run().
                avion.setStatus(FlightStatus.LANDING_ASSIGNED);
                Logger.log("Torre: Autorizado aterrizaje para " + avion.getId());
                // NOTA: En Práctica 4 aquí buscaremos Pista y Puerta libres de verdad.

            } else if (avion.getStatus() == FlightStatus.TAKEOFF_REQUESTED) {
                // CASO DESPEGUE:
                // Le damos permiso para despegar inmediatamente.
                avion.setStatus(FlightStatus.TAKEOFF_ASSIGNED);
                Logger.log("Torre: Autorizado despegue para " + avion.getId());
            }
        }
    }

    /**
     * Metodo para liberar recursos.
     * En la implementación secuencial no gestionamos la ocupación de pistas (siempre están "libres"),
     * pero mantenemos el metodo vacío para cumplir con el contrato de la interfaz TowerInterface.
     * * @param avion El avión que libera la pista.
     */
    @Override
    public void liberarPista(Airplane avion) {
        // En secuencial no hacemos nada, pero hay que ponerlo para cumplir el contrato de la interfaz
    }
}