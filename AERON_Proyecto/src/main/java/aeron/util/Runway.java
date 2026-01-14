package aeron.util;

/**
 * Representa una Pista de Aterrizaje/Despegue física en el aeropuerto.
 * Es un recurso compartido limitado (SCARCE RESOURCE).
 * <p>
 * NOTA DE DISEÑO: Esta clase es un POJO (Plain Old Java Object) simple.
 * No tiene lógica de sincronización propia porque la exclusión mutua
 * para acceder a ella la gestiona la clase ControlTowerConcurrent.
 */
public class Runway {

    // Identificador de la pista (ej: "PIS1")
    private String id;

    // Estado del recurso: true = libre (verde), false = ocupada (rojo)
    private boolean libre = true;

    /**
     * Constructor para inicializar la pista.
     * @param id Nombre identificativo de la pista.
     */
    public Runway(String id) {
        this.id = id;
    }

    public String getId() { return id; }

    /**
     * Consulta el estado de la pista.
     * @return true si la pista está libre, false si hay un avión usándola.
     */
    // Necesario para AirportState
    public boolean isAvailable() {
        return libre;
    }

    /**
     * Cambia el estado de ocupación de la pista.
     * @param libre true para liberar, false para ocupar.
     */
    // Necesario para que la Torre la ocupe/libere.
    // Este metodo es llamado EXCLUSIVAMENTE por el Operario dentro de la Torre.
    // Como el Operario ya está dentro de una sección crítica (monitor o semáforo),
    // es seguro cambiar este booleano sin riesgo de condición de carrera aquí.
    public void setLibre(boolean libre) {
        this.libre = libre;
    }

    // Opcional: para imprimir bonito
    @Override
    public String toString() {
        return "Pista " + id + (libre ? " (Libre)" : " (Ocupada)");
    }
}