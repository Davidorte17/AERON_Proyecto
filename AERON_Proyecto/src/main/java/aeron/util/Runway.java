package aeron.util;

public class Runway {
    private String id;
    private boolean libre = true;

    public Runway(String id) {
        this.id = id;
    }

    public String getId() { return id; }

    // Necesario para AirportState
    public boolean isAvailable() {
        return libre;
    }

    // Necesario para que la Torre la ocupe/libere
    public void setLibre(boolean libre) {
        this.libre = libre;
    }

    // Opcional: para imprimir bonito
    @Override
    public String toString() {
        return "Pista " + id + (libre ? " (Libre)" : " (Ocupada)");
    }
}