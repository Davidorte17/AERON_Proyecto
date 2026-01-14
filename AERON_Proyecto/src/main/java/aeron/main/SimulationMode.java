package aeron.main;

/**
 * Enumerado de configuración para determinar el comportamiento del simulador.
 * <p>
 * UTILIDAD:
 * Nos permite alternar limpiamente entre la implementación de la Práctica 2 (Secuencial)
 * y la implementación final del proyecto (Concurrente/Distribuida) desde la clase Simulation.
 * Evita el uso de "magic strings" o números enteros confusos en el código.
 */
public enum SimulationMode {

    /**
     * Ejecución en un único hilo (Main Thread).
     * Los aviones se procesan uno detrás de otro sin solapamiento temporal real.
     * (Corresponde a la entrega de la Práctica 2).
     */
    SEQUENTIAL,

    /**
     * Ejecución multihilo completa.
     * Se activan los hilos de Aviones, Operarios y el Servidor de Sockets.
     * Se utilizan Semáforos y Monitores para la sincronización.
     * (Corresponde al escenario final del proyecto).
     */
    CONCURRENT
}