package aeron.sequential;

import aeron.model.Airplane;
import aeron.model.FlightStatus;
import aeron.util.Logger;
import aeron.util.TowerInterface;

import java.util.LinkedList;
import java.util.Queue;

public class ControlTower implements TowerInterface{
    // Definimos la cola de
    private Queue<Airplane> peticiones;

    public ControlTower() {
        this.peticiones = new LinkedList<>();
        Logger.log("Torre de Control operativa y esperando peticiones.");
    }

    // Metodo que usan los aviones para pedir permiso (Aterrizar o Despegar)
    @Override
    public void registrarPeticion(Airplane avion) {
        // Añadimos el avión a la cola
        peticiones.add(avion);
        Logger.log("Torre: Registrada petición de " + avion.getId() + " [" + avion.getStatus() + "]");

        procesarPeticionesSecuencial();
    }

    // Metodo simple para procesar la cola (Solo para modo Secuencial)
    private void procesarPeticionesSecuencial() {
        while (!peticiones.isEmpty()) {
            Airplane avion = peticiones.poll(); // Sacamos el primero de la cola
            Logger.log("Torre: Procesando petición de " + avion.getId());

            // Lógica simple de asignación (Simulada por ahora)
            if (avion.getStatus() == FlightStatus.LANDING_REQUEST) {
                // Le damos permiso para aterrizar
                avion.setStatus(FlightStatus.LANDING_ASSIGNED);
                Logger.log("Torre: Autorizado aterrizaje para " + avion.getId());
                // NOTA: En Práctica 4 aquí buscaremos Pista y Puerta libres de verdad.

            } else if (avion.getStatus() == FlightStatus.TAKEOFF_REQUESTED) {
                // Le damos permiso para despegar
                avion.setStatus(FlightStatus.TAKEOFF_ASSIGNED);
                Logger.log("Torre: Autorizado despegue para " + avion.getId());
            }
        }
    }

    @Override
    public void liberarPista(Airplane avion) {
        // En secuencial no hacemos nada, pero hay que ponerlo para cumplir el contrato de la interfaz
    }
}