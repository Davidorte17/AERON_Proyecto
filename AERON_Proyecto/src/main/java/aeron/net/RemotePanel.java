package aeron.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class RemotePanel {

    // Mapa local para pintar la tabla bonita en el cliente
    private static Map<String, String> vuelos = new HashMap<>();

    public static void main(String[] args) {
        String host = "localhost";
        int port = 9999;

        System.out.println("--- PANEL DE VUELOS REMOTO ---");
        System.out.println("Conectando a " + host + ":" + port + "...");

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("¡Conectado! Esperando actualizaciones de la Torre...");

            String inputLine;
            // Bucle infinito leyendo mensajes del servidor
            while ((inputLine = in.readLine()) != null) {
                // Protocolo: "IBE-001:LANDED"
                String[] partes = inputLine.split(":");
                if (partes.length == 2) {
                    String id = partes[0];
                    String estado = partes[1];

                    // Actualizamos memoria local
                    vuelos.put(id, estado);

                    // Repintamos la consola
                    limpiarConsola();
                    imprimirTabla();
                }
            }

        } catch (IOException e) {
            System.err.println("Error de conexión (¿Está la simulación encendida?): " + e.getMessage());
        }
    }

    private static void imprimirTabla() {
        System.out.println("\n╔════════════════════════════════╗");
        System.out.println("║       PANEL DE VUELOS REMOTO   ║");
        System.out.println("╠════════════════╤═══════════════╣");
        System.out.println("║ VUELO          │ ESTADO        ║");
        System.out.println("╠════════════════╪═══════════════╣");

        // Ordenamos por ID para que salga bonito
        vuelos.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    System.out.printf("║ %-14s │ %-13s ║%n", entry.getKey(), entry.getValue());
                });

        System.out.println("╚════════════════╧═══════════════╝");
    }

    // Truco simple para "limpiar" consola imprimiendo saltos de línea
    private static void limpiarConsola() {
        for(int i = 0; i < 5; i++) System.out.println();
    }
}