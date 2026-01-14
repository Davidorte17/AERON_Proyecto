package aeron.model;

import aeron.sequential.ControlTower;
import aeron.util.Logger;
import aeron.util.TowerInterface;

import java.util.Random;

// Implementamos Runnable para que en el futuro sea un Hilo (Thread) fácilmente
public class Airplane implements Runnable {
    private String id;
    private FlightStatus status;
    private TowerInterface tower; // El avión necesita conocer la torre para pedir cosas
    private Random random = new Random();

    public Airplane(String id, TowerInterface tower) {
        this.id = id;
        this.tower = tower;
        this.status = FlightStatus.IN_FLIGHT;
    }

    public String getId() {
        return id;
    }

    public FlightStatus getStatus() {
        return status;
    }

    public void setStatus(FlightStatus status) {
        this.status = status;
    }

    // Este es el ciclo de vida del avión
    @Override
    public void run() {
        Logger.log("Avión [" + id + " - " + status + "] Inicia ciclo");

        try {
            // 1. Vuelo inicial
            Thread.sleep(random.nextInt(1000) + 500);

            // 2. SOLICITA ATERRIZAJE
            this.status = FlightStatus.LANDING_REQUEST;
            Logger.log("Avión [" + id + " - " + status + "] Solicita aterrizaje...");

            // --- CAMBIO: LLAMAMOS A LA TORRE ---
            tower.registrarPeticion(this);

            // Esperamos a que la torre nos cambie el estado a ASSIGNED
            // (En secuencial es instantáneo gracias al metodo procesarPeticionesSecuencial)
            if (this.status == FlightStatus.LANDING_ASSIGNED) {
                // 3. Aterrizando
                this.status = FlightStatus.LANDING;
                Logger.log("Avión [" + id + " - " + status + "] Aterrizando...");
                Thread.sleep(100);

                // 4. Aterrizado
                this.status = FlightStatus.LANDED;
                Logger.log("Avión [" + id + " - " + status + "] Aterrizado.");

                tower.liberarPista(this);

                // 5. Embarcando
                this.status = FlightStatus.BOARDING;
                Logger.log("Avión [" + id + " - " + status + "] Embarcando...");
                Thread.sleep(random.nextInt(500));

                // 6. Embarcado
                this.status = FlightStatus.BOARDED;
                Logger.log("Avión [" + id + " - " + status + "] Embarcado.");

                // 7. SOLICITA DESPEGUE
                this.status = FlightStatus.TAKEOFF_REQUESTED;
                Logger.log("Avión [" + id + " - " + status + "] Solicita despegue...");

                // --- CAMBIO: LLAMAMOS A LA TORRE OTRA VEZ ---
                tower.registrarPeticion(this);

                if (this.status == FlightStatus.TAKEOFF_ASSIGNED) {
                    // 8. Despegando
                    this.status = FlightStatus.DEPARTING;
                    Logger.log("Avión [" + id + " - " + status + "] Despegando...");
                    Thread.sleep(100);

                    // 9. Fin
                    this.status = FlightStatus.DEPARTED;
                    Logger.log("Avión [" + id + " - " + status + "] Despegado. Fin.");
                    tower.liberarPista(this);
                }
            }

        } catch (InterruptedException e) {
            Logger.log("Error en avión " + id);
        }
    }
    @Override
    public String toString() {
        return this.id;
    }
}