package aeron.concurrent;

import aeron.model.Airplane;
import aeron.model.FlightStatus;
import aeron.util.TowerInterface;
import aeron.util.Gate;
import aeron.util.Runway;
import aeron.util.AirportState;

import java.util.ArrayList;
import java.util.List;

public class ControlTowerConcurrent implements TowerInterface {

    // --- CLASE INTERNA REQUEST (Para el visualizador) ---
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

    // --- RECURSOS ---
    private List<Runway> runways;
    private List<Gate> gates;
    private List<Request> requestQueue; // Cola para el dibujo

    public ControlTowerConcurrent() {
        // Inicializamos recursos: Por ejemplo 1 Pista y 3 Puertas
        this.runways = new ArrayList<>();
        this.gates = new ArrayList<>();
        this.requestQueue = new ArrayList<>();

        runways.add(new Runway("R1")); // Solo 1 pista para probar conflictos

        gates.add(new Gate("G1"));
        gates.add(new Gate("G2"));
        gates.add(new Gate("G3"));
    }

    // --- METODO 1: PEDIR PERMISO (Entrada al Monitor) ---
    @Override
    public synchronized void registrarPeticion(Airplane avion) {
        // 1. Identificar qué quiere el avión según su estado
        RequestType tipo = (avion.getStatus() == FlightStatus.LANDING_REQUEST) ?
                RequestType.LANDING : RequestType.TAKEOFF;

        // 2. Añadir a la cola y pintar
        Request req = new Request(avion, tipo);
        requestQueue.add(req);
        imprimirEstado();

        // 3. BUCLE DE ESPERA (Wait Loop)
        // Mientras NO haya recursos libres... esperamos.
        while (getFreeRunway() == null || (tipo == RequestType.LANDING && getFreeGate() == null)) {
            try {
                System.out.println("Torre: Avión " + avion.getId() + " esperando pista/puerta...");
                wait(); // <--- EL HILO SE DUERME AQUÍ
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 4. ASIGNACIÓN DE RECURSOS (Si sale del while, es que hay sitio)
        Runway pista = getFreeRunway();
        pista.setLibre(false); // Ocupamos la pista

        if (tipo == RequestType.LANDING) {
            Gate puerta = getFreeGate();
            puerta.setLibre(false); // Reservamos la puerta también (para luego)
            avion.setStatus(FlightStatus.LANDING_ASSIGNED); // Damos permiso
        } else {
            // Si es despegue, liberamos su puerta actual (ya sale del gate)
            // Nota: Aquí podrías buscar qué puerta tenía, por simplicidad asumimos lógica en liberar
            avion.setStatus(FlightStatus.TAKEOFF_ASSIGNED);
        }

        // 5. Quitamos de la cola de espera y pintamos
        requestQueue.remove(req);
        imprimirEstado();
    }

    // --- METODO 2: LIBERAR PISTA (Salida del Monitor) ---
    @Override
    public synchronized void liberarPista(Airplane avion) {
        // Buscamos la pista ocupada y la liberamos
        // (En una implementación real buscaríamos QUÉ pista tiene este avión,
        // aquí liberamos la primera ocupada por simplicidad si solo hay 1)
        for(Runway r : runways) {
            if(!r.isAvailable()) {
                r.setLibre(true);
                break;
            }
        }

        // Si ha despegado, también liberamos la puerta si no se hizo antes
        if (avion.getStatus() == FlightStatus.DEPARTED) {
            for(Gate g : gates) {
                if(g.isOccupied()) { // Simplificación: liberamos una puerta ocupada
                    g.setLibre(true);
                    break;
                }
            }
        }

        imprimirEstado();
        notifyAll(); // <--- DESPERTAMOS A LOS QUE ESTÁN EN WAIT()
    }

    // --- MÉTODOS AUXILIARES ---
    private Runway getFreeRunway() {
        for (Runway r : runways) if (r.isAvailable()) return r;
        return null;
    }

    private Gate getFreeGate() {
        for (Gate g : gates) if (!g.isOccupied()) return g;
        return null;
    }

    private void imprimirEstado() {
        // Truco para limpiar consola (puede no funcionar en todos los IDEs)
        // System.out.print("\033[H\033[2J");
        // System.out.flush();
        aeron.util.Logger.log(AirportState.showResourcesStatus(runways, gates));
        aeron.util.Logger.log(AirportState.showRequestQueue(requestQueue));
        aeron.util.Logger.log("--------------------------------------------------");
    }
}