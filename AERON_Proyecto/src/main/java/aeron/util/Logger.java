package aeron.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import aeron.main.Simulation;

public class Logger {
    private static PrintWriter writer;
    private static AirportWindow window;

    public static void setup(String mode, int nAviones, int nPistas, int nPuertas, int nOperarios) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName;

        String folder = mode.equalsIgnoreCase("SEQUENTIAL") ? "logs/secuencial/" : "logs/concurrent/";

        File directory = new File(folder);
        if (!directory.exists()) {
            boolean creadas = directory.mkdirs();
            if (creadas) System.out.println("Carpetas de log creadas: " + folder);
        }

        if (mode.equalsIgnoreCase("CONCURRENT")) {
            fileName = String.format("aeron-%s-%dAV-%dPIS-%dPUE-%dOPE-%s.log",
                    mode, nAviones, nPistas, nPuertas, nOperarios, timestamp);
        } else {
            fileName = String.format("aeron-%s-%dAV-%dPIS-%dPUE-%s.log",
                    mode, nAviones, nPistas, nPuertas, timestamp);
        }

        try {
            writer = new PrintWriter(new FileWriter(folder + fileName, true), true);
            System.out.println("Log iniciado en: " + folder + fileName);
        } catch (IOException e) {
            // --- NUEVO PRÁCTICA 6: Capturar y mostrar excepción personalizada ---
            try {
                // Lanzamos la excepción específica que pide la práctica
                throw new aeron.exceptions.LogException(fileName);
            } catch (aeron.exceptions.LogException ex) {
                // Imprimimos el mensaje oficial: "No se ha encontrado el archivo de log..."
                System.err.println(ex.getMessage());
            }
        }
    }

    public static void setWindow(AirportWindow w) {
        window = w;
    }

    // --- MÉTODOS NUEVOS PARA CORREGIR ERRORES DE COMPILACIÓN ---

    // 1. Log específico para eventos del Avión
    public static void logEventos(String message) {
        log("[AVION] " + message);
    }

    // 2. Log específico para eventos de la Torre/Operarios
    public static void logTorre(String message) {
        log("[TORRE] " + message);
    }

    // 3. Metodo para el Panel (Lo dejamos preparado para la Práctica 5)
    // De momento, solo lo escribe en el log normal para que veas que funciona.
    public static void updatePanel(String id, String estado, String pista, String puerta) {
        String msg = String.format("[PANEL] Avión: %s | Estado: %s | Pista: %s | Puerta: %s",
                id, estado, pista != null ? pista : "-", puerta != null ? puerta : "-");
        log(msg);
        //Actualizamos el JSON
        AirportJson.actualizarEstado(id, estado);

        //Enviamos al servidor si está activo
        if (Simulation.server != null) {
            Simulation.server.broadcastUpdate(id, estado);
        }
    }

    public static synchronized void log(String message) {
        if (writer != null) {
            writer.println(message);
        }
        System.out.println(message);
        if (window != null) {
            window.addLog(message);
        }
    }

    public static void close() {
        if (writer != null) writer.close();
    }
}