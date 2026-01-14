package aeron.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static PrintWriter writer;
    private static AirportWindow window;

    public static void setup(String mode, int nAviones, int nPistas, int nPuertas, int nOperarios) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName;

        // Definimos la carpeta según el modo
        String folder = mode.equalsIgnoreCase("SEQUENTIAL") ? "logs/secuencial/" : "logs/concurrent/";

        // --- CORRECCIÓN: CREAR CARPETAS SI NO EXISTEN ---
        File directory = new File(folder);
        if (!directory.exists()) {
            boolean creadas = directory.mkdirs(); // Crea logs y logs/concurrent a la vez
            if (creadas) System.out.println("Carpetas de log creadas: " + folder);
        }
        // ------------------------------------------------

        // Formato obligatorio: aeron-MODE-N1AV-N2PIS-N3PUE[-N4OPE-]-TIMESTAMP
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
            System.err.println("ERROR CRÍTICO: No se puede crear el fichero de log.");
            e.printStackTrace();
        }
    }

    public static void setWindow(AirportWindow w) {
        window = w;
    }

    // Metodo para escribir mensajes (sincronizado para el futuro modo concurrente)
    public static synchronized void log(String message) {
        // Escribir en fichero
        if (writer != null) {
            writer.println(message);
        }

        // Escribir en consola
        System.out.println(message);

        // <--- 3. ESCRIBIR EN LA VENTANA (Si existe)
        if (window != null) {
            window.addLog(message);
        }
    }

    public static void close() {
        if (writer != null) writer.close();
    }
}