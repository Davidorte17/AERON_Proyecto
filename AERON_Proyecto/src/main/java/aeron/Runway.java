package aeron;

public class Runway {
    private String id; // Ejemplo: Pista 1
    private boolean libre = true; // Para saber si está ocupada

    public Runway(String id) {
        this.id = id;
    }

    public String getId() { return id; }
}