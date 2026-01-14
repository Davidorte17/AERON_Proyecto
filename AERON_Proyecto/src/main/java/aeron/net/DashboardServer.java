package aeron.net;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class DashboardServer extends Thread {
    private int port;
    private ServerSocket serverSocket;
    // Lista de clientes conectados (sus canales de escritura)
    private List<PrintWriter> connectedClients;

    public DashboardServer(int port) {
        this.port = port;
        this.connectedClients = new ArrayList<>();
    }

    // Para saber si hay alguien conectado
    public int getNumClients() {
        synchronized (connectedClients) {
            return connectedClients.size();
        }
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("[SERVIDOR] Escuchando conexiones en puerto " + port);

            while (true) {
                // 1. Esperar a que llegue un cliente (Panel Remoto)
                Socket clientSocket = serverSocket.accept();
                System.out.println("[SERVIDOR] Nuevo Panel conectado desde: " + clientSocket.getInetAddress());

                // 2. Guardar su canal de salida para enviarle datos luego
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                synchronized (connectedClients) {
                    connectedClients.add(out);
                }
            }
        } catch (IOException e) {
            System.err.println("[SERVIDOR] Error o cierre del servidor: " + e.getMessage());
        }
    }

    // Metodo que llamará la Torre/Logger para enviar novedades
    public void broadcastUpdate(String flightId, String status) {
        String message = flightId + ":" + status; // Protocolo simple: "IBE-001:LANDED"

        synchronized (connectedClients) {
            for (PrintWriter out : connectedClients) {
                out.println(message);
            }
        }
    }

    public void close() {
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException e) {}
    }
}