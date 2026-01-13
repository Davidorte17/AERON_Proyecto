package aeron;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static PrintWriter writer;

    public static void setup(String mode, int nAviones, int nPistas, int nPuertas, int nOperarios) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName;

        // Definimos la carpeta según el modo
        String folder = mode.equalsIgnoreCase("SEQUENTIAL") ? "logs/secuencial/" : "logs/concurrent/";

        // Formato obligatorio: aeron-MODE-N1AV-N2PIS-N3PUE[-N4OPE-]-TIMESTAMP
        if (mode.equalsIgnoreCase("CONCURRENT")) {
            fileName = String.format("aeron-%s-%dAV-%dPIS-%dPUE-%dOPE-%s.log",
                    mode, nAviones, nPistas, nPuertas, nOperarios, timestamp);
        } else {
            fileName = String.format("aeron-%s-%dAV-%dPIS-%dPUE-%s.log",
                    mode, nAviones, nPistas, nPuertas, timestamp);
        }

        try {
            // El 'true' permite añadir texto si el archivo ya existe
            writer = new PrintWriter(new FileWriter(folder + fileName, true), true);
            System.out.println("Log iniciado en: " + folder + fileName);
        } catch (IOException e) {
            System.err.println("ERROR CRÍTICO: No se puede crear el fichero de log.");
            e.printStackTrace();
        }
    }

    // Metodo para escribir mensajes (sincronizado para el futuro modo concurrente)
    public static synchronized void log(String message) {
        if (writer != null) {
            writer.println(message);
            // También mostramos por consola para depurar
            System.out.println(message);
        }
    }

    public static void close() {
        if (writer != null) writer.close();
    }
}