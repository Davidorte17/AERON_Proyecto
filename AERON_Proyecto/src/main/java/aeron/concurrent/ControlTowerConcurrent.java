package aeron.concurrent;

import aeron.model.Airplane;
import aeron.model.FlightStatus;
import aeron.util.TowerInterface;
import aeron.util.Gate;
import aeron.util.Runway;
import aeron.util.AirportState;
import aeron.util.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;

public class ControlTowerConcurrent implements TowerInterface {

    // --- CLASE INTERNA REQUEST ---
    public static class Request {
        public Airplane plane;
        public RequestType type;

        public Request(Airplane plane, RequestType type) {
            this.plane = plane;
            this.type = type;
        }
        @Override
        public String toString() { return plane.getId(); }
    }

    // RECURSOS
    private List<Runway> runways;
    private List<Gate> gates;

    // COLA DE PETICIONES Y PENDIENTES
    private Queue<Request> requestQueue;
    private List<Request> pendingLandings;
    private List<Request> pendingTakeoffs;

    // SEMÁFOROS
    private Semaphore semaforoPeticiones;
    private Semaphore mutexCola;

    public ControlTowerConcurrent(int numPistas, int numPuertas) {
        this.runways = new ArrayList<>();
        this.gates = new ArrayList<>();
        this.requestQueue = new LinkedList<>();
        this.pendingLandings = new LinkedList<>();
        this.pendingTakeoffs = new LinkedList<>();

        this.semaforoPeticiones = new Semaphore(0);
        this.mutexCola = new Semaphore(1);

        // Nombres estrictos según PDF (PIS1, PIS2... / GATE 1...)
        for (int i = 1; i <= numPistas; i++) runways.add(new Runway("PIS" + i));
        for (int i = 1; i <= numPuertas; i++) gates.add(new Gate("GATE " + i));
    }

    // --- MÉTODOS DE LA INTERFAZ (Llamados por Avión) ---

    @Override
    public void registrarPeticion(Airplane avion) {
        RequestType tipo = null;
        switch (avion.getStatus()) {
            case LANDING_REQUEST: tipo = RequestType.LANDING; break;
            case LANDED:          tipo = RequestType.LANDED; break;
            case BOARDED:         tipo = RequestType.BOARDED; break;
            case TAKEOFF_REQUESTED: tipo = RequestType.TAKEOFF; break;
            case DEPARTED:        tipo = RequestType.DEPARTED; break;
            default: return;
        }

        try {
            mutexCola.acquire();
            Request req = new Request(avion, tipo);
            requestQueue.add(req);

            // Log específico del avión poniendo la petición
            if (tipo == RequestType.LANDING) {
                Logger.logEventos("Avión [" + avion.getId() + " - LANDING_REQUESTED] Solicitud de aterrizaje en cola");
            } else if (tipo == RequestType.TAKEOFF) {
                Logger.logEventos("Avión [" + avion.getId() + " - TAKEOFF_REQUESTED] Solicitud de despegue en cola");
            }

            mutexCola.release();
            imprimirEstado();
            semaforoPeticiones.release();

        } catch (InterruptedException e) { e.printStackTrace(); }
    }

    // --- MÉTODOS DEL OPERARIO (Consumidor) ---

    public Request obtenerSiguientePeticion() throws InterruptedException {
        semaforoPeticiones.acquire();
        mutexCola.acquire();
        Request req = requestQueue.poll();
        mutexCola.release();
        return req;
    }

    // CAMBIO CLAVE: Aceptamos el ID del operario para los logs
    public synchronized void procesarPeticion(Request req, String operarioId) throws InterruptedException {
        Airplane avion = req.plane;
        String avionEstado = "Avión [" + avion.getId() + " - " + req.type + "]"; // Formato log

        Logger.logTorre("Operario [" + operarioId + "] ha cogido una petición de tipo " + req.type + " para " + avionEstado);
        Logger.logTorre("Procesando petición de " + req.type + " de " + avionEstado);

        switch (req.type) {
            case LANDING:
                if (getFreeRunway() != null && getFreeGate() != null) {
                    asignarAterrizaje(req, operarioId);
                } else {
                    pendingLandings.add(req);
                    Logger.logTorre("Petición POSPUESTA por falta de recursos.");
                }
                break;

            case LANDED:
                liberarPistaDeAvion(avion);
                Logger.logTorre("Pista [" + avion.getId() + "] (simulado) se libera");
                Logger.logTorre("Operario [" + operarioId + "] ha completado la petición de tipo LANDED para Avión [" + avion.getId() + " - LANDED]");
                Logger.updatePanel(avion.getId(), "LANDED", "LIBRE", "OCUPADA");
                revisarPendientes(operarioId);
                break;

            case BOARDED:
                liberarPuertaDeAvion(avion);
                Logger.logTorre("Puerta liberada por " + avion.getId());
                Logger.logTorre("Operario [" + operarioId + "] ha completado la petición de tipo BOARDED para Avión [" + avion.getId() + " - BOARDED]");
                Logger.updatePanel(avion.getId(), "BOARDED", "-", "LIBRE");
                revisarPendientes(operarioId);
                break;

            case TAKEOFF:
                if (getFreeRunway() != null) {
                    asignarDespegue(req, operarioId);
                } else {
                    pendingTakeoffs.add(req);
                    Logger.logTorre("Despegue POSPUESTO (Pistas llenas).");
                }
                break;

            case DEPARTED:
                liberarPistaDeAvion(avion);
                Logger.logTorre("Pista liberada. Avión [" + avion.getId() + " - DEPARTED] fuera del sistema.");
                Logger.logTorre("Operario [" + operarioId + "] ha completado la petición de tipo DEPARTED para Avión [" + avion.getId() + " - DEPARTED]");
                Logger.updatePanel(avion.getId(), "DEPARTED", "LIBRE", "-");
                revisarPendientes(operarioId);
                break;
        }
        imprimirEstado();
    }

    // --- MÉTODOS AUXILIARES ---

    private void revisarPendientes(String operarioId) {
        if (!pendingLandings.isEmpty()) {
            if (getFreeRunway() != null && getFreeGate() != null) {
                Request req = pendingLandings.remove(0);
                Logger.logTorre("Recuperando petición pendiente de " + req.plane.getId());
                asignarAterrizaje(req, operarioId);
            }
        }
        if (!pendingTakeoffs.isEmpty() && getFreeRunway() != null) {
            Request req = pendingTakeoffs.remove(0);
            Logger.logTorre("Recuperando despegue pendiente de " + req.plane.getId());
            asignarDespegue(req, operarioId);
        }
    }

    private void asignarAterrizaje(Request req, String operarioId) {
        Runway r = getFreeRunway();
        Gate g = getFreeGate();
        r.setLibre(false);
        g.setLibre(false);

        // Asignamos recursos al avión para que él sepa qué decir en sus logs
        req.plane.setAssignedRunwayId(r.getId());
        req.plane.setAssignedGateId(g.getId());

        // Logs estrictos del PDF
        Logger.logTorre("Pista [" + r.getId() + "] pasa a estar ocupada por el avión Avión [" + req.plane.getId() + " - IN_FLIGHT]");
        Logger.logTorre("Puerta [" + g.getId() + "] pasa a estar ocupada por el avión Avión [" + req.plane.getId() + " - IN_FLIGHT]");
        Logger.logTorre("Avión [" + req.plane.getId() + " - LANDING_REQUEST] autorizado para aterrizar en Pista [" + r.getId() + "]");
        Logger.logTorre("Avión [" + req.plane.getId() + " - LANDING_REQUEST] autorizado para embarcar en Puerta [" + g.getId() + "]");

        req.plane.setStatus(FlightStatus.LANDING_ASSIGNED);

        Logger.logTorre("Operario [" + operarioId + "] ha completado la petición de tipo LANDING para Avión [" + req.plane.getId() + " - LANDING_ASSIGNED]");
        Logger.updatePanel(req.plane.getId(), "LANDING_ASSIGNED", r.getId(), g.getId());
    }

    private void asignarDespegue(Request req, String operarioId) {
        Runway r = getFreeRunway();
        r.setLibre(false);

        req.plane.setAssignedRunwayId(r.getId());

        Logger.logTorre("Pista [" + r.getId() + "] pasa a estar ocupada por el avión Avión [" + req.plane.getId() + " - TAKEOFF_REQUESTED]");
        Logger.logTorre("Avión [" + req.plane.getId() + " - TAKEOFF_REQUESTED] autorizado para despegar en Pista [" + r.getId() + "]");

        req.plane.setStatus(FlightStatus.TAKEOFF_ASSIGNED);

        Logger.logTorre("Operario [" + operarioId + "] ha completado la petición de tipo TAKEOFF para Avión [" + req.plane.getId() + " - TAKEOFF_ASSIGNED]");
        Logger.updatePanel(req.plane.getId(), "TAKEOFF_ASSIGNED", r.getId(), "-");
    }

    private Runway getFreeRunway() {
        for (Runway r : runways) if (r.isAvailable()) return r;
        return null;
    }

    private Gate getFreeGate() {
        for (Gate g : gates) if (!g.isOccupied()) return g;
        return null;
    }

    private void liberarPistaDeAvion(Airplane a) {
        for(Runway r : runways) { if (!r.isAvailable()) { r.setLibre(true); break; } }
    }

    private void liberarPuertaDeAvion(Airplane a) {
        for(Gate g : gates) { if (g.isOccupied()) { g.setLibre(true); break; } }
    }

    private void imprimirEstado() {
        Logger.log(AirportState.showResourcesStatus(runways, gates));
        try {
            mutexCola.acquire();
            List<Request> listaParaDibujar = new ArrayList<>(requestQueue);
            Logger.log(AirportState.showRequestQueue(listaParaDibujar));
            mutexCola.release();
        } catch(Exception e){}
    }

    // Método antiguo de interfaz (para compatibilidad o secuencial), redirige a registrar
    @Override
    public void liberarPista(Airplane avion) {
        registrarPeticion(avion);
    }
}