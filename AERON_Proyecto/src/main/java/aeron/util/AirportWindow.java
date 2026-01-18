package aeron.util;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Ventana gráfica (GUI) mejorada con estilo "Terminal de Torre de Control".
 * <p>
 * MEJORAS ESTÉTICAS:
 * 1. Tema Oscuro (Dark Mode) para simular un monitor de radar antiguo.
 * 2. Fuente Consolas/Monospaced más grande y legible.
 * 3. Cabecera decorativa.
 * 4. Scroll automático suavizado.
 */
public class AirportWindow extends JFrame {

    // Componente de texto donde escribiremos los logs
    private JTextArea logArea;

    public AirportWindow() {
        // 1. Intentamos poner el estilo nativo del sistema operativo (Windows/Mac/Linux)
        // para que los bordes de la ventana se vean modernos.
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Configuración básica
        setTitle("📡 AERON SYSTEM - LIVE MONITOR");
        setSize(900, 700); // Un poco más grande
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // --- CABECERA (HEADER) ---
        JPanel headerPanel = new JPanel();
        headerPanel.setBackground(new Color(45, 45, 50)); // Gris oscuro
        headerPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titleLabel = new JLabel("🛫 TORRE DE CONTROL - REGISTRO DE EVENTOS EN VIVO");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        headerPanel.add(titleLabel);

        add(headerPanel, BorderLayout.NORTH);

        // --- ÁREA DE LOGS (ESTILO TERMINAL) ---
        logArea = new JTextArea();
        logArea.setEditable(false);

        // Color de fondo: Negro casi puro
        logArea.setBackground(new Color(20, 20, 20));
        // Color de texto: Verde terminal (o blanco suave si prefieres new Color(220, 220, 220))
        logArea.setForeground(new Color(50, 205, 50)); // Lime Green

        // Fuente: Importante que sea MONOSPACED para que las tablas ASCII no se rompan
        // "Consolas" es genial en Windows, si no existe usa la por defecto.
        logArea.setFont(new Font("Consolas", Font.PLAIN, 14));

        // Margen interno para que el texto no se pegue al borde
        logArea.setMargin(new Insets(10, 15, 10, 15));

        // Scroll
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setBorder(null); // Quitar borde feo por defecto

        // Personalizar la barra de scroll (opcional, solo para oscurecerla un poco si el OS deja)
        scrollPane.getViewport().setBackground(new Color(20, 20, 20));

        add(scrollPane, BorderLayout.CENTER);

        // Colocar en la esquina superior izquierda (x=0, y=0)
        setLocation(0, 0);

        // Hacer visible
        setVisible(true);
    }

    // Metodo para añadir texto a la ventana desde fuera
    public void addLog(String text) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(text + "\n");
            // Auto-scroll hacia abajo
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
}