package aeron.util;

public class Gate {
    private String id;
    private boolean libre = true;

    public Gate(String id) {
        this.id = id;
    }

    public String getId() { return id; }

    // Necesario para AirportState
    public boolean isOccupied() {
        return !libre;
    }

    // Necesario para que la Torre la ocupe/libere
    public void setLibre(boolean libre) {
        this.libre = libre;
    }

    @Override
    public String toString() {
        return "Puerta " + id;
    }
}