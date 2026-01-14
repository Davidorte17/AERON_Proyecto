package aeron.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import aeron.main.Simulation;

/**
 * Clase de utilidad estática encargada de centralizar todas las salidas del sistema.
 * Gestiona la escritura en consola, en el fichero de log, en la ventana gráfica
 * y coordina la actualización del Panel de Vuelos (JSON y Sockets).
 */
public class Logger {
    // Objeto para escribir en el fichero de texto
    private static PrintWriter writer;

    // Referencia a la ventana gráfica para mostrar los mensajes en la GUI
    private static AirportWindow window;

    /**
     * Configura el sistema de logs al inicio de la simulación.
     * Crea las carpetas necesarias y establece el nombre del fichero según el formato del PDF.
     * * @param mode Modo de ejecución ("SEQUENTIAL" o "CONCURRENT").
     * @param nAviones Número total de aviones.
     * @param nPistas Número de pistas.
     * @param nPuertas Número de puertas.
     * @param nOperarios Número de operarios (0 si es secuencial).
     */
    public static void setup(String mode, int nAviones, int nPistas, int nPuertas, int nOperarios) {
        // Generamos una marca de tiempo única para el nombre del fichero
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName;

        // Determinamos la carpeta de destino según el modo
        String folder = mode.equalsIgnoreCase("SEQUENTIAL") ? "logs/secuencial/" : "logs/concurrent/";

        // Verificamos si la carpeta existe, y si no, la creamos
        File directory = new File(folder);
        if (!directory.exists()) {
            boolean creadas = directory.mkdirs();
            if (creadas) System.out.println("Carpetas de log creadas: " + folder);
        }

        // Construimos el nombre del fichero siguiendo la nomenclatura estricta del enunciado
        if (mode.equalsIgnoreCase("CONCURRENT")) {
            fileName = String.format("aeron-%s-%dAV-%dPIS-%dPUE-%dOPE-%s.log",
                    mode, nAviones, nPistas, nPuertas, nOperarios, timestamp);
        } else {
            fileName = String.format("aeron-%s-%dAV-%dPIS-%dPUE-%s.log",
                    mode, nAviones, nPistas, nPuertas, timestamp);
        }

        try {
            // Inicializamos el escritor en modo "append" (añadir al final) y con auto-flush
            writer = new PrintWriter(new FileWriter(folder + fileName, true), true);
            System.out.println("Log iniciado en: " + folder + fileName);

        } catch (IOException e) {
            // Capturamos el error nativo de Java y lanzamos nuestra excepción personalizada
            try {
                // Lanzamos la excepción específica que pide la práctica
                throw new aeron.exceptions.LogException(fileName);
            } catch (aeron.exceptions.LogException ex) {
                // Imprimimos el mensaje oficial requerido: "No se ha encontrado el archivo de log..."
                System.err.println(ex.getMessage());
            }
        }
    }

    /**
     * Vincula la ventana gráfica al logger para replicar los mensajes en la GUI.
     */
    public static void setWindow(AirportWindow w) {
        window = w;
    }

    // --- MÉTODOS DE APOYO PARA FORMATO ---

    /**
     * Metodo auxiliar para registrar eventos propios del ciclo de vida del Avión.
     * Añade el prefijo [AVION] automáticamente.
     */
    public static void logEventos(String message) {
        log("[AVION] " + message);
    }

    /**
     * Metodo auxiliar para registrar acciones de la Torre de Control y los Operarios.
     * Añade el prefijo [TORRE] automáticamente.
     */
    public static void logTorre(String message) {
        log("[TORRE] " + message);
    }

    /**
     * Metodo centralizado para actualizar el estado del Panel de Vuelos.
     * Coordina tres acciones: Log visual, actualización del JSON y envío por Sockets.
     * * @param id Identificador del vuelo.
     * @param estado Nuevo estado del vuelo.
     * @param pista Pista asignada (o "-" si no aplica).
     * @param puerta Puerta asignada (o "-" si no aplica).
     */
    public static void updatePanel(String id, String estado, String pista, String puerta) {
        // 1. Generamos el mensaje formateado para el log visual
        String msg = String.format("[PANEL] Avión: %s | Estado: %s | Pista: %s | Puerta: %s",
                id, estado, pista != null ? pista : "-", puerta != null ? puerta : "-");
        log(msg);

        // 2. PRÁCTICA 5: Actualizamos el fichero JSON mediante Monitor (synchronized)
        AirportJson.actualizarEstado(id, estado);

        // 3. PRÁCTICA 7: Enviamos el cambio de estado al servidor de Sockets (si está activo)
        // Esto notifica a los Paneles Remotos conectados.
        if (Simulation.server != null) {
            Simulation.server.broadcastUpdate(id, estado);
        }
    }

    /**
     * Metodo núcleo de escritura. Es SYNCHRONIZED para garantizar la exclusión mutua.
     * Evita que los mensajes se mezclen cuando múltiples hilos (Aviones/Operarios) intentan escribir a la vez.
     * Escribe simultáneamente en Fichero, Consola y Ventana.
     */
    public static synchronized void log(String message) {
        // Escribir en fichero
        if (writer != null) {
            writer.println(message);
        }
        // Escribir en consola del IDE
        System.out.println(message);
        // Escribir en la ventana gráfica Swing
        if (window != null) {
            window.addLog(message);
        }
    }

    /**
     * Cierra el flujo de escritura del fichero al finalizar la simulación.
     */
    public static void close() {
        if (writer != null) writer.close();
    }
}