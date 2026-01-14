package aeron.model;

import aeron.util.Logger;
import aeron.util.TowerInterface;
import java.util.Random;

/**
 * Representa un avión individual dentro de la simulación.
 * Implementa la interfaz Runnable para que cada avión pueda ejecutarse
 * en su propio hilo (Thread), permitiendo la concurrencia.
 * * Simula el ciclo de vida completo: Vuelo -> Aterrizaje -> Embarque -> Despegue.
 */
public class Airplane implements Runnable {

    // Identificador único del avión (ej: IBE-001)
    private String id;

    // Estado actual del avión (crucial para la máquina de estados de la Torre)
    private FlightStatus status;

    // Referencia a la torre (interfaz) para poder enviarle peticiones sin conocer su implementación interna
    private TowerInterface tower;

    // Generador de aleatorios para simular tiempos variables de vuelo y embarque
    private Random random = new Random();

    // Variables para almacenar los recursos que me asigne el Operario
    // Necesario para mostrar en el log: "Me ha tocado la pista X"
    private String assignedRunwayId;
    private String assignedGateId;

    /**
     * Constructor del avión.
     * @param id Identificador del vuelo.
     * @param tower Referencia a la torre de control con la que nos comunicaremos.
     */
    public Airplane(String id, TowerInterface tower) {
        this.id = id;
        this.tower = tower;
        // Inicializamos el avión directamente en vuelo antes de llegar al aeropuerto
        this.status = FlightStatus.IN_FLIGHT;
    }

    // --- SETTERS Y GETTERS ---

    /**
     * Método utilizado por la Torre (Operario) para comunicarme qué pista me ha asignado.
     * @param id ID de la pista (ej: PIS1).
     */
    public void setAssignedRunwayId(String id) { this.assignedRunwayId = id; }

    /**
     * Método utilizado por la Torre (Operario) para comunicarme qué puerta me ha asignado.
     * @param id ID de la puerta (ej: GATE 1).
     */
    public void setAssignedGateId(String id) { this.assignedGateId = id; }

    public String getId() { return id; }
    public FlightStatus getStatus() { return status; }

    // Permite cambiar mi estado (usado por mí mismo o por la Torre para autorizarme)
    public void setStatus(FlightStatus status) { this.status = status; }

    /**
     * Lógica principal del hilo del avión.
     * Ejecuta secuencialmente las fases de Aterrizaje, Embarque y Despegue.
     */
    @Override
    public void run() {
        // Log inicial para trazar que el hilo ha arrancado
        Logger.logEventos("Avión [" + id + " - " + status + "] Inicia ciclo");
        Logger.logEventos("Avión [" + id + " - " + status + "] El avión está en vuelo");

        try {
            // Simulamos el tiempo que tarda el avión en llegar al espacio aéreo del aeropuerto
            Thread.sleep(random.nextInt(1000) + 500);

            // =============================================================
            // FASE 1: SOLICITUD DE ATERRIZAJE
            // =============================================================

            // 1. Cambio mi estado a "Solicitando Aterrizaje"
            this.status = FlightStatus.LANDING_REQUEST;
            Logger.logEventos("Avión [" + id + " - IN_FLIGHT] Solicita aterrizaje a torre de control");

            // 2. Productor: Añado mi petición a la cola de la torre
            // (La torre gestionará la concurrencia y los semáforos internamente)
            tower.registrarPeticion(this);

            Logger.logEventos("Avión [" + id + " - LANDING_REQUEST] Solicitud de aterrizaje en cola");
            Logger.logEventos("Avión [" + id + " - LANDING_REQUEST] Espera autorización de aterrizaje");

            // 3. ESPERA ACTIVA (Punto clave de la defensa):
            // Me quedo en este bucle "durmiendo" a trocitos hasta que un Operario
            // procese mi petición y cambie mi estado a LANDING_ASSIGNED.
            // Usamos sleep(10) para no saturar la CPU mientras esperamos.
            while (this.status != FlightStatus.LANDING_ASSIGNED) {
                Thread.sleep(10);
            }

            // --- AQUÍ YA TENEMOS RECURSOS ASIGNADOS ---
            // Si el código llega aquí, es que el Operario me ha dado Pista y Puerta
            Logger.logEventos("Avión [" + id + " - LANDING_ASSIGNED] Aterrizaje autorizado");
            Logger.logEventos("Avión [" + id + " - LANDING_ASSIGNED] Me ha tocado aterrizar en la Pista [" + assignedRunwayId + "]");
            Logger.logEventos("Avión [" + id + " - LANDING_ASSIGNED] Me ha tocado embarcar en la Puerta [" + assignedGateId + "]");

            // 4. Realizamos la maniobra de aterrizaje
            this.status = FlightStatus.LANDING;
            Logger.logEventos("Avión [" + id + " - LANDING] Aterrizando");

            // Notificamos a la torre que estamos aterrizando (para actualizar el Panel/JSON)
            tower.registrarPeticion(this);

            // Simulamos el tiempo que tardo en usar la pista
            Thread.sleep(100);

            // 5. Fin del aterrizaje
            this.status = FlightStatus.LANDED;
            Logger.logEventos("Avión [" + id + " - LANDED] Aterrizado");

            // Aviso a la torre de que he aterrizado.
            // IMPORTANTE: Esto hará que el Operario libere mi Pista (pero mantengo la Puerta).
            tower.registrarPeticion(this);
            Thread.sleep(50); // Pequeña pausa técnica

            // =============================================================
            // FASE 2: EMBARQUE (Puerta asignada)
            // =============================================================

            this.status = FlightStatus.BOARDING;
            Logger.logEventos("Avión [" + id + " - BOARDING] Embarcando");
            // Simulamos el tiempo de carga/descarga de pasajeros
            Thread.sleep(random.nextInt(500));

            this.status = FlightStatus.BOARDED;
            Logger.logEventos("Avión [" + id + " - BOARDED] Embarcado");

            // Aviso a la torre. El Operario liberará mi Puerta.
            tower.registrarPeticion(this);
            Thread.sleep(50);

            // =============================================================
            // FASE 3: SOLICITUD DE DESPEGUE
            // =============================================================

            this.status = FlightStatus.TAKEOFF_REQUESTED;
            Logger.logEventos("Avión [" + id + " - TAKEOFF_REQUESTED] Solicitud de despegue en cola");

            // Vuelvo a ponerme en la cola, esta vez pidiendo pista de salida
            tower.registrarPeticion(this);

            // ESPERA ACTIVA 2: Espero a que me asignen una pista libre para irme
            while (this.status != FlightStatus.TAKEOFF_ASSIGNED) {
                Thread.sleep(10);
            }

            Logger.logEventos("Avión [" + id + " - TAKEOFF_ASSIGNED] Despegue autorizado");
            Logger.logEventos("Avión [" + id + " - TAKEOFF_ASSIGNED] Me ha tocado despegar en la Pista [" + assignedRunwayId + "]");

            // Maniobra de despegue
            this.status = FlightStatus.DEPARTING;
            Logger.logEventos("Avión [" + id + " - DEPARTING] Despegando");
            // Actualizo panel
            tower.registrarPeticion(this);

            // Tiempo ocupando la pista de despegue
            Thread.sleep(100);

            // Fin del ciclo
            this.status = FlightStatus.DEPARTED;
            Logger.logEventos("Avión [" + id + " - DEPARTED] El avión ha despegado");

            // Último aviso: Libera la pista y salgo de la simulación
            tower.registrarPeticion(this);

        } catch (InterruptedException e) {
            // Manejo de interrupciones del hilo
            e.printStackTrace();
        }
    }

    @Override
    public String toString() { return this.id; }
}