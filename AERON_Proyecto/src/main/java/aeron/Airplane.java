package aeron;

public class Airplane {
    private String id; // Ejemplo: IBE-001

    public Airplane(String id) {
        this.id = id;
    }

    public String getId() { return id; }

    // Aquí pondremos el ciclo de vida (run) más adelante
}