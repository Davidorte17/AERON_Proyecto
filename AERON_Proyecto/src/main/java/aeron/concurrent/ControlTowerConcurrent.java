package aeron.concurrent;

import aeron.exceptions.ResourceException;
import aeron.exceptions.SaturationException;
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

/**
 * Implementación CONCURRENTE de la Torre de Control.
 * <p>
 * Esta clase actúa como el "cerebro" del sistema y resuelve varios problemas clásicos de concurrencia:
 * 1. Productor-Consumidor: Los Aviones (hilos) producen peticiones y los Operarios (hilos) las consumen.
 * 2. Gestión de Recursos Limitados: Administramos las Pistas y Puertas para evitar conflictos.
 * 3. Prevención de Interbloqueos (Deadlocks): Usamos listas de espera y asignación atómica.
 */
public class ControlTowerConcurrent implements TowerInterface {

    // --- RECURSOS COMPARTIDOS ---
    // Listas de recursos físicos limitados del aeropuerto
    private List<Runway> runways;
    private List<Gate> gates;

    // Límite artificial de la cola para probar la excepción de saturación (Práctica 6)
    private static final int MAX_COLA = 5;

    // --- ESTRUCTURAS DE DATOS DE COORDINACIÓN ---
    // Cola principal donde los aviones dejan sus solicitudes (Buffer del Productor-Consumidor)
    private Queue<Request> requestQueue;

    // Listas de espera secundarias para evitar esperas activas o bloqueos
    // Si un avión no tiene recursos, lo movemos aquí en lugar de bloquear al operario.
    private List<Request> pendingLandings;
    private List<Request> pendingTakeoffs;

    // --- MECANISMOS DE SINCRONIZACIÓN (SEMAFOROS - TEMA 4) ---

    // Semáforo contador: Indica cuántos elementos hay en la cola listos para consumir.
    // Los Operarios se bloquearán aquí si es 0.
    private Semaphore semaforoPeticiones;

    // Semáforo binario (Mutex): Garantiza la exclusión mutua para acceder a la cola 'requestQueue'.
    // Solo un hilo (Avión u Operario) puede tocar la cola a la vez.
    private Semaphore mutexCola;

    /**
     * Constructor de la Torre.
     * Inicializamos las listas, colas y los semáforos necesarios.
     */
    public ControlTowerConcurrent(int numPistas, int numPuertas) {
        this.runways = new ArrayList<>();
        this.gates = new ArrayList<>();
        this.requestQueue = new LinkedList<>();
        this.pendingLandings = new LinkedList<>();
        this.pendingTakeoffs = new LinkedList<>();

        // Inicializamos el semáforo de peticiones a 0 (la cola empieza vacía)
        this.semaforoPeticiones = new Semaphore(0);
        // Inicializamos el mutex a 1 (el primero que llegue entra)
        this.mutexCola = new Semaphore(1);

        // Configuramos los recursos con los nombres estrictos según PDF (PIS1, GATE 1...)
        for (int i = 1; i <= numPistas; i++) runways.add(new Runway("PIS" + i));
        for (int i = 1; i <= numPuertas; i++) gates.add(new Gate("GATE " + i));
    }

    // --- PARTE DEL PRODUCTOR (AVIÓN) ---

    /**
     * Metodo llamado por los hilos 'Airplane'.
     * Actúa como el PRODUCTOR en el patrón Productor-Consumidor.
     * Introduce una nueva solicitud en la cola protegida.
     */
    @Override
    public void registrarPeticion(Airplane avion) {
        // Transformamos el estado del avión en un tipo de petición manejable
        RequestType tipo = null;
        switch (avion.getStatus()) {
            case LANDING_REQUEST: tipo = RequestType.LANDING; break;
            case LANDED:          tipo = RequestType.LANDED; break;
            case BOARDED:         tipo = RequestType.BOARDED; break;
            case TAKEOFF_REQUESTED: tipo = RequestType.TAKEOFF; break;
            case DEPARTED:        tipo = RequestType.DEPARTED; break;
            default: return; // Si no es un estado relevante, ignoramos
        }

        try {
            // 1. Protocolo de entrada: Adquirimos el Mutex para acceso exclusivo a la cola
            mutexCola.acquire();

            // 2. PRÁCTICA 6: Verificamos si hay Saturación antes de añadir
            if (requestQueue.size() >= MAX_COLA) {
                mutexCola.release(); // Importante: soltar el mutex antes de lanzar la excepción para no bloquear el sistema

                // Lanzamos la excepción personalizada de saturación
                throw new SaturationException(tipo.toString(), avion.getId());
            }

            // 3. Sección Crítica: Añadimos la petición a la cola
            Request req = new Request(avion, tipo);
            requestQueue.add(req);

            // Log específico del avión poniendo la petición (Traza del Productor)
            if (tipo == RequestType.LANDING) {
                Logger.logEventos("Avión [" + avion.getId() + " - LANDING_REQUESTED] Solicitud de aterrizaje en cola");
            } else if (tipo == RequestType.TAKEOFF) {
                Logger.logEventos("Avión [" + avion.getId() + " - TAKEOFF_REQUESTED] Solicitud de despegue en cola");
            }

            // 4. Protocolo de salida: Liberamos el Mutex
            mutexCola.release();

            // Actualizamos la visualización del estado del aeropuerto en el log
            imprimirEstado();

            // 5. Señalizamos al Consumidor (Operario) que hay una nueva petición disponible
            semaforoPeticiones.release();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (SaturationException e) {
            // Capturamos la excepción de saturación para registrarla en el log de la Torre
            Logger.logTorre(e.getMessage());
        }
    }

    // --- PARTE DEL CONSUMIDOR (OPERARIO) ---

    /**
     * Metodo llamado por los hilos 'Operario'.
     * Actúa como el CONSUMIDOR. Extrae una petición de la cola.
     * Si la cola está vacía, el hilo se bloquea aquí hasta que llegue algo.
     */
    public Request obtenerSiguientePeticion() throws InterruptedException {
        // 1. Esperamos (acquire) a que el semáforo contador > 0. Si es 0, nos dormimos.
        semaforoPeticiones.acquire();

        // 2. Adquirimos exclusión mutua para sacar el elemento de la cola sin conflictos
        mutexCola.acquire();
        Request req = requestQueue.poll();
        mutexCola.release();

        return req;
    }

    // --- MONITOR (TEMA 5) ---

    /**
     * Metodo central de lógica de negocio.
     * Al ser SYNCHRONIZED, convertimos la Torre en un MONITOR para esta operación.
     * Garantizamos que solo un Operario esté modificando el estado de las pistas/puertas a la vez.
     * Esto previene condiciones de carrera críticas en la asignación de recursos.
     */
    public synchronized void procesarPeticion(Request req, String operarioId) throws InterruptedException {
        Airplane avion = req.plane;
        String avionEstado = "Avión [" + avion.getId() + " - " + req.type + "]"; // Formato log

        Logger.logTorre("Operario [" + operarioId + "] ha cogido una petición de tipo " + req.type + " para " + avionEstado);
        Logger.logTorre("Procesando petición de " + req.type + " de " + avionEstado);

        switch (req.type) {
            case LANDING:
                // CASO CRÍTICO: Problema de los Filósofos / Asignación Múltiple
                // Necesitamos DOS recursos (Pista Y Puerta) a la vez.
                // Comprobamos ambos atómicamente para evitar Deadlocks (Abrazo mortal).
                if (getFreeRunway() != null && getFreeGate() != null) {
                    asignarAterrizaje(req, operarioId);
                } else {
                    // Si falta alguno, NO cogemos ninguno y posponemos la petición.
                    try {
                        // PRÁCTICA 6: Lanzamos excepción informativa de falta de recursos
                        String recursoFaltante = (getFreeRunway() == null) ? "Pista" : "Puerta";
                        throw new ResourceException(recursoFaltante, avion.getId());
                    } catch (ResourceException e) {
                        // Registramos el error en el log
                        Logger.logTorre(e.getMessage());
                    }
                    // Guardamos la petición en una lista de espera interna
                    pendingLandings.add(req);
                    Logger.logTorre("Petición POSPUESTA por falta de recursos.");
                }
                break;

            case LANDED:
                // El avión ha aterrizado, liberamos la Pista (pero mantiene la Puerta)
                liberarPistaDeAvion(avion);
                Logger.logTorre("Pista [" + avion.getId() + "] (simulado) se libera");
                Logger.logTorre("Operario [" + operarioId + "] ha completado la petición de tipo LANDED para Avión [" + avion.getId() + " - LANDED]");

                // Actualizamos Panel, JSON y Sockets
                Logger.updatePanel(avion.getId(), "LANDED", "LIBRE", "OCUPADA");

                // Al liberar un recurso, comprobamos si alguien en la lista de espera lo necesita
                revisarPendientes(operarioId);
                break;

            case BOARDED:
                // El embarque terminó, liberamos la Puerta
                liberarPuertaDeAvion(avion);
                Logger.logTorre("Puerta liberada por " + avion.getId());
                Logger.logTorre("Operario [" + operarioId + "] ha completado la petición de tipo BOARDED para Avión [" + avion.getId() + " - BOARDED]");
                Logger.updatePanel(avion.getId(), "BOARDED", "-", "LIBRE");

                // Comprobamos si algún aterrizaje pendiente puede entrar ahora
                revisarPendientes(operarioId);
                break;

            case TAKEOFF:
                // Solo necesitamos Pista libre
                if (getFreeRunway() != null) {
                    asignarDespegue(req, operarioId);
                } else {
                    // Si no hay pista, a la lista de espera de despegues
                    pendingTakeoffs.add(req);
                    Logger.logTorre("Despegue POSPUESTO (Pistas llenas).");
                }
                break;

            case DEPARTED:
                // El avión se ha ido, liberamos la Pista de despegue
                liberarPistaDeAvion(avion);
                Logger.logTorre("Pista liberada. Avión [" + avion.getId() + " - DEPARTED] fuera del sistema.");
                Logger.logTorre("Operario [" + operarioId + "] ha completado la petición de tipo DEPARTED para Avión [" + avion.getId() + " - DEPARTED]");
                Logger.updatePanel(avion.getId(), "DEPARTED", "LIBRE", "-");

                // Al liberar pista, revisamos si alguien quiere aterrizar o despegar
                revisarPendientes(operarioId);
                break;
        }
        // Mostramos el estado actualizado de colas y recursos
        imprimirEstado();
    }

    // --- MÉTODOS AUXILIARES Y GESTIÓN DE COLAS DE ESPERA ---

    /**
     * Revisa las listas de espera (pendingLandings/pendingTakeoffs).
     * Se llama siempre que se libera un recurso (Pista o Puerta).
     * Esto evita la inanición (Starvation) de los procesos en espera.
     */
    private void revisarPendientes(String operarioId) {
        // Prioridad 1: Aterrizajes (Si hay Pista Y Puerta)
        if (!pendingLandings.isEmpty()) {
            if (getFreeRunway() != null && getFreeGate() != null) {
                Request req = pendingLandings.remove(0);
                Logger.logTorre("Recuperando petición pendiente de " + req.plane.getId());
                asignarAterrizaje(req, operarioId);
            }
        }
        // Prioridad 2: Despegues (Si hay Pista)
        if (!pendingTakeoffs.isEmpty() && getFreeRunway() != null) {
            Request req = pendingTakeoffs.remove(0);
            Logger.logTorre("Recuperando despegue pendiente de " + req.plane.getId());
            asignarDespegue(req, operarioId);
        }
    }

    /**
     * Lógica para hacer efectiva la asignación de aterrizaje.
     * Marca recursos como ocupados y actualiza el estado del avión para desbloquearlo.
     */
    private void asignarAterrizaje(Request req, String operarioId) {
        Runway r = getFreeRunway();
        Gate g = getFreeGate();
        r.setLibre(false); // Ocupamos recursos
        g.setLibre(false);

        // Comunicamos al avión qué recursos le han tocado (para sus logs)
        req.plane.setAssignedRunwayId(r.getId());
        req.plane.setAssignedGateId(g.getId());

        // Logs requeridos por el enunciado
        Logger.logTorre("Pista [" + r.getId() + "] pasa a estar ocupada por el avión Avión [" + req.plane.getId() + " - IN_FLIGHT]");
        Logger.logTorre("Puerta [" + g.getId() + "] pasa a estar ocupada por el avión Avión [" + req.plane.getId() + " - IN_FLIGHT]");
        Logger.logTorre("Avión [" + req.plane.getId() + " - LANDING_REQUEST] autorizado para aterrizar en Pista [" + r.getId() + "]");
        Logger.logTorre("Avión [" + req.plane.getId() + " - LANDING_REQUEST] autorizado para embarcar en Puerta [" + g.getId() + "]");

        // CAMBIO DE ESTADO CRÍTICO:
        // Al poner LANDING_ASSIGNED, el bucle 'while' del hilo del Avión se rompe y el avión continúa.
        req.plane.setStatus(FlightStatus.LANDING_ASSIGNED);

        Logger.logTorre("Operario [" + operarioId + "] ha completado la petición de tipo LANDING para Avión [" + req.plane.getId() + " - LANDING_ASSIGNED]");
        Logger.updatePanel(req.plane.getId(), "LANDING_ASSIGNED", r.getId(), g.getId());
    }

    /**
     * Lógica para hacer efectiva la asignación de despegue.
     */
    private void asignarDespegue(Request req, String operarioId) {
        Runway r = getFreeRunway();
        r.setLibre(false);

        req.plane.setAssignedRunwayId(r.getId());

        Logger.logTorre("Pista [" + r.getId() + "] pasa a estar ocupada por el avión Avión [" + req.plane.getId() + " - TAKEOFF_REQUESTED]");
        Logger.logTorre("Avión [" + req.plane.getId() + " - TAKEOFF_REQUESTED] autorizado para despegar en Pista [" + r.getId() + "]");

        // Desbloqueamos al avión
        req.plane.setStatus(FlightStatus.TAKEOFF_ASSIGNED);

        Logger.logTorre("Operario [" + operarioId + "] ha completado la petición de tipo TAKEOFF para Avión [" + req.plane.getId() + " - TAKEOFF_ASSIGNED]");
        Logger.updatePanel(req.plane.getId(), "TAKEOFF_ASSIGNED", r.getId(), "-");
    }

    // Buscadores simples de recursos libres
    private Runway getFreeRunway() {
        for (Runway r : runways) if (r.isAvailable()) return r;
        return null;
    }

    private Gate getFreeGate() {
        for (Gate g : gates) if (!g.isOccupied()) return g;
        return null;
    }

    // Métodos para liberar recursos cuando el avión termina una fase
    private void liberarPistaDeAvion(Airplane a) {
        for(Runway r : runways) { if (!r.isAvailable()) { r.setLibre(true); break; } }
    }

    private void liberarPuertaDeAvion(Airplane a) {
        for(Gate g : gates) { if (g.isOccupied()) { g.setLibre(true); break; } }
    }

    /**
     * Genera la representación visual del estado actual (Tablas ASCII).
     * Protegemos la lectura de la cola con el Mutex para no leer mientras alguien escribe.
     */
    private void imprimirEstado() {
        Logger.log(AirportState.showResourcesStatus(runways, gates));
        try {
            mutexCola.acquire();
            // Hacemos una copia de la cola para dibujarla sin bloquear demasiado tiempo
            List<Request> listaParaDibujar = new ArrayList<>(requestQueue);
            Logger.log(AirportState.showRequestQueue(listaParaDibujar));
            mutexCola.release();
        } catch(Exception e){}
    }

    // Metodo antiguo de interfaz (para compatibilidad o secuencial), redirige a registrar
    @Override
    public void liberarPista(Airplane avion) {
        registrarPeticion(avion);
    }
}