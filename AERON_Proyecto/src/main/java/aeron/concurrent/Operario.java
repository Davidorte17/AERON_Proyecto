package aeron.concurrent;

import aeron.util.Logger;

public class Operario implements Runnable {
    private ControlTowerConcurrent tower;
    private String id; // Ahora es String para "OP-001"

    public Operario(ControlTowerConcurrent tower, int numero) {
        this.tower = tower;
        // Formatear ID: OP-001, OP-002...
        this.id = String.format("OP-%03d", numero);
    }

    @Override
    public void run() {
        Logger.logTorre("Operario [" + id + "] esperando nueva petición...");
        try {
            while (true) {
                ControlTowerConcurrent.Request peticion = tower.obtenerSiguientePeticion();
                // Delegamos el log detallado a la torre para centralizar la lógica
                tower.procesarPeticion(peticion, this.id);
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            // Fin
        }
    }
}