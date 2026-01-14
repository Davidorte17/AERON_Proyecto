package aeron.util;

/**
 * Representa una Puerta de Embarque (Gate) en el aeropuerto.
 * Es un recurso compartido limitado (tenemos 5 puertas para 20 aviones).
 * <p>
 * NOTA DE DEFENSA: Al igual que Runway, esta clase no es Thread-Safe por sí misma
 * porque no lo necesita. La concurrencia se controla en la Torre antes de llamar
 * a estos métodos.
 */
public class Gate {

    // Identificador de la puerta (ej: "GATE 1")
    private String id;

    // Estado de la puerta: true = libre, false = ocupada.
    private boolean libre = true;

    /**
     * Constructor de la puerta.
     * @param id Nombre identificativo.
     */
    public Gate(String id) {
        this.id = id;
    }

    public String getId() { return id; }

    /**
     * Comprueba si la puerta está ocupada.
     * @return true si hay un avión en la puerta, false si está libre.
     */
    // Necesario para AirportState (para pintar rojo/verde en el log)
    // Aquí la lógica es inversa a Runway (allí es isAvailable).
    // Simplemente devuelve el inverso del flag 'libre'.
    public boolean isOccupied() {
        return !libre;
    }

    /**
     * Cambia el estado de ocupación de la puerta.
     * @param libre true para liberar, false para ocupar.
     */
    // Necesario para que la Torre (el Operario) la ocupe/libere.
    public void setLibre(boolean libre) {
        this.libre = libre;
    }

    @Override
    public String toString() {
        return "Puerta " + id;
    }
}