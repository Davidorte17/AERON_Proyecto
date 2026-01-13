package aeron;

public class Gate {
    private String id; // Ejemplo: Gate 1
    private boolean libre = true;

    public Gate(String id) {
        this.id = id;
    }

    public String getId() { return id; }
}