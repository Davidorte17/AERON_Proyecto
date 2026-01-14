package aeron.model;

import aeron.util.Logger;
import aeron.util.TowerInterface;
import java.util.Random;

public class Airplane implements Runnable {
    private String id;
    private FlightStatus status;
    private TowerInterface tower;
    private Random random = new Random();

    private String assignedRunwayId;
    private String assignedGateId;

    public Airplane(String id, TowerInterface tower) {
        this.id = id;
        this.tower = tower;
        this.status = FlightStatus.IN_FLIGHT;
    }

    // Setters para que la Torre nos diga qué nos ha tocado
    public void setAssignedRunwayId(String id) { this.assignedRunwayId = id; }
    public void setAssignedGateId(String id) { this.assignedGateId = id; }

    public String getId() { return id; }
    public FlightStatus getStatus() { return status; }
    public void setStatus(FlightStatus status) { this.status = status; }

    @Override
    public void run() {
        Logger.logEventos("Avión [" + id + " - " + status + "] Inicia ciclo");
        Logger.logEventos("Avión [" + id + " - " + status + "] El avión está en vuelo");

        try {
            Thread.sleep(random.nextInt(1000) + 500);

            // --- ATERRIZAJE ---
            this.status = FlightStatus.LANDING_REQUEST;
            Logger.logEventos("Avión [" + id + " - IN_FLIGHT] Solicita aterrizaje a torre de control");
            tower.registrarPeticion(this);
            Logger.logEventos("Avión [" + id + " - LANDING_REQUEST] Solicitud de aterrizaje en cola");
            Logger.logEventos("Avión [" + id + " - LANDING_REQUEST] Espera autorización de aterrizaje");

            while (this.status != FlightStatus.LANDING_ASSIGNED) { Thread.sleep(10); }

            // Aquí ya tenemos pista asignada por la torre
            Logger.logEventos("Avión [" + id + " - LANDING_ASSIGNED] Aterrizaje autorizado");
            Logger.logEventos("Avión [" + id + " - LANDING_ASSIGNED] Me ha tocado aterrizar en la Pista [" + assignedRunwayId + "]");
            Logger.logEventos("Avión [" + id + " - LANDING_ASSIGNED] Me ha tocado embarcar en la Puerta [" + assignedGateId + "]");

            this.status = FlightStatus.LANDING;
            Logger.logEventos("Avión [" + id + " - LANDING] Aterrizando");
            // Notificar que empieza a aterrizar (para panel)
            tower.registrarPeticion(this);
            Thread.sleep(100);

            this.status = FlightStatus.LANDED;
            Logger.logEventos("Avión [" + id + " - LANDED] Aterrizado");
            tower.registrarPeticion(this); // Notificar LANDED (Libera pista)
            Thread.sleep(50);

            // --- EMBARQUE ---
            this.status = FlightStatus.BOARDING;
            Logger.logEventos("Avión [" + id + " - BOARDING] Embarcando");
            Thread.sleep(random.nextInt(500));

            this.status = FlightStatus.BOARDED;
            Logger.logEventos("Avión [" + id + " - BOARDED] Embarcado");
            tower.registrarPeticion(this); // Notificar BOARDED (Libera puerta)
            Thread.sleep(50);

            // --- DESPEGUE ---
            this.status = FlightStatus.TAKEOFF_REQUESTED;
            Logger.logEventos("Avión [" + id + " - TAKEOFF_REQUESTED] Solicitud de despegue en cola");
            tower.registrarPeticion(this);

            while (this.status != FlightStatus.TAKEOFF_ASSIGNED) { Thread.sleep(10); }

            Logger.logEventos("Avión [" + id + " - TAKEOFF_ASSIGNED] Despegue autorizado");
            Logger.logEventos("Avión [" + id + " - TAKEOFF_ASSIGNED] Me ha tocado despegar en la Pista [" + assignedRunwayId + "]");

            this.status = FlightStatus.DEPARTING;
            Logger.logEventos("Avión [" + id + " - DEPARTING] Despegando");
            // Notificar que empieza a despegar
            tower.registrarPeticion(this);
            Thread.sleep(100);

            this.status = FlightStatus.DEPARTED;
            Logger.logEventos("Avión [" + id + " - DEPARTED] El avión ha despegado");
            tower.registrarPeticion(this); // Fin

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() { return this.id; }
}