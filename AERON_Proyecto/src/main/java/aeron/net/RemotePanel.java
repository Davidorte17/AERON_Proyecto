package aeron.net;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 * Cliente Gráfico (Swing).
 * Se conecta al servidor y muestra la tabla de vuelos en una ventana real.
 */
public class RemotePanel extends JFrame {

    private DefaultTableModel tableModel;
    private Map<String, String> vuelos = new HashMap<>();
    private JLabel statusLabel;

    public RemotePanel() {
        // 1. Configuración de la Ventana
        setTitle("✈️ PANEL DE VUELOS REMOTO (CLIENTE) ✈️");
        setSize(400, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocation(910, 0);
        setLayout(new BorderLayout());

        // 2. Crear la Tabla
        String[] columnNames = {"VUELO", "ESTADO"};
        tableModel = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(tableModel);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setRowHeight(25);

        // Colorines para la cabecera
        table.getTableHeader().setBackground(new Color(50, 50, 150));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));

        add(new JScrollPane(table), BorderLayout.CENTER);

        // 3. Barra de estado inferior
        statusLabel = new JLabel("Conectando...");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        add(statusLabel, BorderLayout.SOUTH);

        setVisible(true);

        // 4. Iniciar la conexión en un hilo aparte para no congelar la ventana
        new Thread(this::conectarYEscuchar).start();
    }

    private void conectarYEscuchar() {
        String host = "localhost";
        int port = 9999;

        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            SwingUtilities.invokeLater(() -> statusLabel.setText("✅ CONECTADO AL SERVIDOR"));
            statusLabel.setForeground(new Color(0, 150, 0));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                // Protocolo: "IBE-001:LANDED"
                String[] partes = inputLine.split(":");
                if (partes.length == 2) {
                    String id = partes[0];
                    String estado = partes[1];

                    // Actualizar datos
                    vuelos.put(id, estado);

                    // Refrescar la tabla en el hilo de Swing
                    SwingUtilities.invokeLater(this::actualizarTabla);
                }
            }

        } catch (IOException e) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("❌ DESCONECTADO");
                statusLabel.setForeground(Color.RED);
            });
        }
    }

    private void actualizarTabla() {
        // Limpiamos la tabla actual
        tableModel.setRowCount(0);

        // Volvemos a llenarla con los datos ordenados
        vuelos.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    tableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
                });
    }

    public static void main(String[] args) {
        // Arrancar la interfaz gráfica
        SwingUtilities.invokeLater(RemotePanel::new);
    }
}