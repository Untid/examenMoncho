package org.example.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.json.JSONArray;
import org.json.JSONObject;
import org.example.ApiClient;

public class BaristaView extends JPanel {
    private JTextArea txtPedidos;
    private JTextArea txtResultado;
    private JButton btnActualizar;
    private JComboBox<String> cmbEstados;

    public BaristaView() {
        setLayout(new BorderLayout(5, 5));

        // Panel superior - Lista de pedidos (MÁS GRANDE)
        JPanel panelPedidos = new JPanel(new BorderLayout());
        panelPedidos.setBorder(BorderFactory.createTitledBorder("📦 Pedidos Activos"));

        txtPedidos = new JTextArea(20, 60);
        txtPedidos.setEditable(false);
        txtPedidos.setFont(new Font("Consolas", Font.PLAIN, 11));
        JScrollPane scrollPedidos = new JScrollPane(txtPedidos);
        panelPedidos.add(scrollPedidos, BorderLayout.CENTER);

        // Panel central - Controles (FIJO, siempre visible)
        JPanel panelControles = new JPanel(new GridLayout(1, 5, 10, 5)); // ✅ GridLayout fijo
        panelControles.setBorder(BorderFactory.createTitledBorder("⚙️ Gestión de Estados"));
        panelControles.setPreferredSize(new Dimension(800, 60)); // ✅ Altura fija

        btnActualizar = new JButton("🔄 Actualizar Lista");
        cmbEstados = new JComboBox<>(new String[]{
                "En preparación", "Listo para recoger", "Recogido"
        });

        JButton btnCambiarEstado = new JButton("📝 Cambiar Estado");

        // ✅ Añadir componentes en orden fijo
        panelControles.add(btnActualizar);
        panelControles.add(new JLabel("Nuevo Estado:"));
        panelControles.add(cmbEstados);
        panelControles.add(btnCambiarEstado);
        panelControles.add(new JLabel("")); // Espacio vacío para equilibrio

        // Panel inferior - Resultado
        JPanel panelResultado = new JPanel(new BorderLayout());
        panelResultado.setBorder(BorderFactory.createTitledBorder("📋 Resultado"));
        panelResultado.setPreferredSize(new Dimension(800, 100));

        txtResultado = new JTextArea(4, 60);
        txtResultado.setEditable(false);
        txtResultado.setFont(new Font("Consolas", Font.PLAIN, 11));
        JScrollPane scrollResultado = new JScrollPane(txtResultado);
        panelResultado.add(scrollResultado, BorderLayout.CENTER);

        // Layout principal
        add(panelPedidos, BorderLayout.CENTER); // ✅ Pedidos ocupan el centro
        add(panelControles, BorderLayout.NORTH); // ✅ Controles arriba, siempre visibles
        add(panelResultado, BorderLayout.SOUTH); // ✅ Resultado abajo

        // Event listeners
        btnActualizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                actualizarPedidos();
            }
        });

        btnCambiarEstado.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cambiarEstadoPedido();
            }
        });

        // Cargar pedidos al iniciar
        actualizarPedidos();
    }

    private void actualizarPedidos() {
        new Thread(() -> {
            String resultado = ApiClient.obtenerPedidosActivos();
            SwingUtilities.invokeLater(() -> {
                try {
                    JSONArray pedidos = new JSONArray(resultado);
                    StringBuilder sb = new StringBuilder();
                    sb.append("╔══════════════════════════════════════════════════════════════╗\n");
                    sb.append("║                       PEDIDOS ACTIVOS                        ║\n");
                    sb.append("╚══════════════════════════════════════════════════════════════╝\n\n");

                    if (pedidos.length() == 0) {
                        sb.append("    No hay pedidos activos en este momento\n");
                        sb.append("    ──────────────────────────────────────\n");
                    }

                    for (int i = 0; i < pedidos.length(); i++) {
                        JSONObject pedido = pedidos.getJSONObject(i);

                        // Encabezado del pedido más compacto
                        sb.append("┌────────────────────────────────────────────────────────────┐\n");
                        sb.append("│ 🆔 PEDIDO #").append(i + 1).append(" - ").append(padRight(pedido.getJSONObject("cliente").getString("nombre"), 30)).append("│\n");
                        sb.append("├────────────────────────────────────────────────────────────┤\n");
                        sb.append("│ 📍 ID: ").append(padRight(pedido.getString("_id"), 47)).append("│\n");
                        sb.append("│ 📊 Estado: ").append(padRight(pedido.getString("estado"), 40)).append("│\n");
                        sb.append("│ ⏱️  Tiempo: ").append(padRight(pedido.getInt("tiempo_estimado_min") + " min", 39)).append("│\n");

                        // Productos más compactos
                        JSONArray productos = pedido.getJSONArray("productos");
                        sb.append("├────────────────────────────────────────────────────────────┤\n");
                        sb.append("│ 🍽️  PRODUCTOS:                                             │\n");

                        double total = 0;
                        for (int j = 0; j < productos.length(); j++) {
                            JSONObject producto = productos.getJSONObject(j);
                            int cantidad = producto.getInt("cantidad");
                            double precio = producto.getDouble("precio");
                            double subtotal = cantidad * precio;
                            total += subtotal;

                            String nombreProducto = producto.getString("nombre");
                            if (nombreProducto.length() > 25) {
                                nombreProducto = nombreProducto.substring(0, 22) + "...";
                            }

                            sb.append("│   • ").append(padRight(nombreProducto, 25))
                                    .append(" x").append(padRight(String.valueOf(cantidad), 2))
                                    .append(" ").append(String.format("%6.2f", subtotal)).append("€ │\n");
                        }

                        // Total
                        sb.append("├────────────────────────────────────────────────────────────┤\n");
                        sb.append("│ 💰 TOTAL: ").append(padRight(String.format("%.2f€", total), 42)).append("│\n");
                        sb.append("└────────────────────────────────────────────────────────────┘\n\n");
                    }

                    txtPedidos.setText(sb.toString());

                } catch (Exception e) {
                    txtPedidos.setText("❌ Error parseando respuesta: " + resultado);
                }
            });
        }).start();
    }

    private void cambiarEstadoPedido() {
        String pedidoId = JOptionPane.showInputDialog(this,
                "Ingresa el ID del pedido:\n\n(Puedes copiarlo de la lista de abajo)",
                "Cambiar Estado", JOptionPane.QUESTION_MESSAGE);

        if (pedidoId == null || pedidoId.trim().isEmpty()) {
            return;
        }

        String nuevoEstado = (String) cmbEstados.getSelectedItem();

        new Thread(() -> {
            try {
                JSONObject estadoData = new JSONObject();
                estadoData.put("estado", nuevoEstado);
                estadoData.put("barista_id", 1);

                String resultado = ApiClient.actualizarEstadoPedido(pedidoId.trim(), estadoData.toString());

                SwingUtilities.invokeLater(() -> {
                    try {
                        JSONObject pedidoActualizado = new JSONObject(resultado);
                        StringBuilder sb = new StringBuilder();

                        sb.append("╔══════════════════════════════════════╗\n");
                        sb.append("║      ESTADO ACTUALIZADO ✅           ║\n");
                        sb.append("╚══════════════════════════════════════╝\n\n");

                        sb.append("┌────────────────────────────────────┐\n");
                        sb.append("│ 📦 PEDIDO ACTUALIZADO              │\n");
                        sb.append("├────────────────────────────────────┤\n");
                        sb.append("│ 👤 Cliente: ").append(padRight(pedidoActualizado.getJSONObject("cliente").getString("nombre"), 25)).append("│\n");
                        sb.append("│ 🆔 ID: ").append(padRight(pedidoActualizado.getString("_id"), 27)).append("│\n");
                        sb.append("│ 📊 NUEVO ESTADO: ").append(padRight(pedidoActualizado.getString("estado"), 20)).append("│\n");
                        sb.append("└────────────────────────────────────┘\n\n");

                        sb.append("✅ El estado ha sido actualizado correctamente\n");

                        txtResultado.setText(sb.toString());
                        actualizarPedidos(); // Refrescar lista

                    } catch (Exception ex) {
                        txtResultado.setText("=== RESPUESTA ===\n" + resultado);
                    }
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    txtResultado.setText("❌ Error: " + ex.getMessage());
                });
            }
        }).start();
    }

    // Método auxiliar para alinear texto
    private String padRight(String s, int n) {
        if (s == null) s = "";
        return String.format("%-" + n + "s", s);
    }
}