package org.example.view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.json.JSONArray;
import org.json.JSONObject;
import org.example.ApiClient;

public class CamareroView extends JPanel {
    private JTextField txtNombreCliente;
    private JTextField txtIdLocal;
    private JTextArea txtProductos;
    private JTextArea txtResultado;
    private JButton btnCargarMenu;
    private JButton btnCrearPedido;
    private JPanel panelMenuContainer; // ✅ CAMBIADO: Panel en lugar de TextArea
    private JScrollPane scrollMenu; // ✅ Para el scroll

    public CamareroView() {
        setLayout(new BorderLayout(10, 10));

        // Panel superior - Datos del cliente
        JPanel panelCliente = new JPanel(new GridLayout(2, 2, 5, 5));
        panelCliente.setBorder(BorderFactory.createTitledBorder("Datos del Cliente"));
        panelCliente.setPreferredSize(new Dimension(600, 80));

        panelCliente.add(new JLabel("Nombre:"));
        txtNombreCliente = new JTextField();
        panelCliente.add(txtNombreCliente);

        panelCliente.add(new JLabel("ID Local (opcional):"));
        txtIdLocal = new JTextField();
        panelCliente.add(txtIdLocal);

        // Panel central dividido en dos - Menú y Productos
        JPanel panelCentral = new JPanel(new GridLayout(1, 2, 10, 10));

        // Panel izquierdo - Menú CON IMÁGENES
        JPanel panelMenu = new JPanel(new BorderLayout());
        panelMenu.setBorder(BorderFactory.createTitledBorder("🍽️ Menú Disponible con Imágenes"));

        // ✅ PANEL CONTAINER PARA LOS PRODUCTOS CON IMÁGENES
        panelMenuContainer = new JPanel();
        panelMenuContainer.setLayout(new BoxLayout(panelMenuContainer, BoxLayout.Y_AXIS));
        panelMenuContainer.setBackground(Color.WHITE);

        scrollMenu = new JScrollPane(panelMenuContainer);
        scrollMenu.setPreferredSize(new Dimension(400, 300));
        scrollMenu.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Mensaje inicial
        JLabel lblMensajeInicial = new JLabel("Haz clic en 'Cargar Menú' para ver los productos con imágenes");
        lblMensajeInicial.setHorizontalAlignment(SwingConstants.CENTER);
        panelMenuContainer.add(lblMensajeInicial);

        panelMenu.add(scrollMenu, BorderLayout.CENTER);

        JPanel panelBotonesMenu = new JPanel(new FlowLayout());
        btnCargarMenu = new JButton("🔄 Cargar Menú con Imágenes");
        panelBotonesMenu.add(btnCargarMenu);
        panelMenu.add(panelBotonesMenu, BorderLayout.SOUTH);

        // Panel derecho - Productos del pedido
        JPanel panelProductos = new JPanel(new BorderLayout());
        panelProductos.setBorder(BorderFactory.createTitledBorder("Productos del Pedido"));

        txtProductos = new JTextArea(15, 25);
        txtProductos.setText("Formato: id,cantidad (uno por línea)\nEjemplo:\n1,2\n2,1\n3,1");
        JScrollPane scrollProductos = new JScrollPane(txtProductos);
        panelProductos.add(scrollProductos, BorderLayout.CENTER);

        panelCentral.add(panelMenu);
        panelCentral.add(panelProductos);

        // Panel inferior - Botones y resultado
        JPanel panelBotones = new JPanel(new FlowLayout());
        btnCrearPedido = new JButton("Crear Pedido");
        panelBotones.add(btnCrearPedido);

        txtResultado = new JTextArea(6, 60);
        txtResultado.setEditable(false);
        txtResultado.setFont(new Font("Consolas", Font.PLAIN, 12));
        JScrollPane scrollResultado = new JScrollPane(txtResultado);

        // Layout principal
        add(panelCliente, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);

        JPanel panelSur = new JPanel(new BorderLayout());
        panelSur.add(panelBotones, BorderLayout.NORTH);
        panelSur.add(scrollResultado, BorderLayout.CENTER);
        add(panelSur, BorderLayout.SOUTH);

        // Event listeners
        btnCargarMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cargarMenuConImagenes();
            }
        });

        btnCrearPedido.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                crearPedido();
            }
        });
    }

    // ✅ MÉTODO PARA CARGAR IMAGEN DESDE URL
    private ImageIcon cargarImagenDesdeURL(String urlString, int ancho, int alto) {
        try {
            java.net.URL url = new java.net.URL(urlString);
            java.awt.Image imagenOriginal = javax.imageio.ImageIO.read(url);

            // Redimensionar la imagen
            java.awt.Image imagenRedimensionada = imagenOriginal.getScaledInstance(ancho, alto, java.awt.Image.SCALE_SMOOTH);

            return new ImageIcon(imagenRedimensionada);
        } catch (Exception e) {
            System.out.println("❌ Error cargando imagen: " + urlString);
            // Devolver un icono de placeholder si falla
            return crearIconoPlaceholder(ancho, alto);
        }
    }

    // ✅ MÉTODO PARA CREAR ICONO PLACEHOLDER
    private ImageIcon crearIconoPlaceholder(int ancho, int alto) {
        java.awt.Image imagen = new java.awt.image.BufferedImage(ancho, alto, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = (Graphics2D) imagen.getGraphics();
        g2d.setColor(Color.LIGHT_GRAY);
        g2d.fillRect(0, 0, ancho, alto);
        g2d.setColor(Color.DARK_GRAY);
        g2d.drawRect(0, 0, ancho-1, alto-1);
        g2d.drawString("❌ Imagen", 5, alto/2);
        g2d.dispose();
        return new ImageIcon(imagen);
    }

    // ✅ NUEVO MÉTODO: CARGAR MENÚ CON IMÁGENES REALES
    private void cargarMenuConImagenes() {
        new Thread(() -> {
            String resultado = ApiClient.obtenerMenu();
            SwingUtilities.invokeLater(() -> {
                try {
                    JSONArray menu = new JSONArray(resultado);

                    // ✅ LIMPIAR EL PANEL ANTERIOR
                    panelMenuContainer.removeAll();
                    panelMenuContainer.setBackground(Color.WHITE);

                    // ✅ TÍTULO
                    JLabel lblTitulo = new JLabel("🎯 MENÚ DISPONIBLE - " + menu.length() + " productos");
                    lblTitulo.setFont(new Font("Arial", Font.BOLD, 14));
                    lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
                    panelMenuContainer.add(lblTitulo);
                    panelMenuContainer.add(Box.createRigidArea(new Dimension(0, 10)));

                    for (int i = 0; i < menu.length(); i++) {
                        JSONObject producto = menu.getJSONObject(i);

                        // ✅ CREAR PANEL PARA CADA PRODUCTO
                        JPanel panelProducto = new JPanel(new BorderLayout(10, 5));
                        panelProducto.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                                BorderFactory.createEmptyBorder(8, 8, 8, 8)
                        ));
                        panelProducto.setBackground(Color.WHITE);
                        panelProducto.setMaximumSize(new Dimension(380, 80));

                        // ✅ PANEL IZQUIERDO: IMAGEN
                        JPanel panelImagen = new JPanel(new FlowLayout(FlowLayout.LEFT));
                        panelImagen.setBackground(Color.WHITE);

                        ImageIcon icono = null;
                        boolean tieneImagen = false;

                        // ✅ INTENTAR CARGAR LA PRIMERA IMAGEN SI EXISTE
                        if (producto.has("imagenes_urls")) {
                            try {
                                JSONArray imagenesUrls = producto.getJSONArray("imagenes_urls");
                                if (imagenesUrls.length() > 0) {
                                    String primeraImagenUrl = imagenesUrls.getString(0);
                                    icono = cargarImagenDesdeURL(primeraImagenUrl, 60, 60);
                                    tieneImagen = true;
                                }
                            } catch (Exception e) {
                                System.out.println("Error cargando imagen para producto " + producto.getInt("id"));
                            }
                        }

                        if (!tieneImagen) {
                            icono = crearIconoPlaceholder(60, 60);
                        }

                        JLabel lblImagen = new JLabel(icono);
                        panelImagen.add(lblImagen);

                        // ✅ PANEL DERECHO: INFORMACIÓN
                        JPanel panelInfo = new JPanel(new GridLayout(3, 1, 2, 2));
                        panelInfo.setBackground(Color.WHITE);

                        String emoji = producto.has("emoji") ? producto.getString("emoji") : "❓";
                        String nombre = producto.getString("nombre");
                        String tipo = producto.getString("tipo");
                        String precio = producto.getString("precio") + "€";
                        int id = producto.getInt("id");

                        JLabel lblNombre = new JLabel(emoji + " " + nombre);
                        lblNombre.setFont(new Font("Arial", Font.BOLD, 12));

                        JLabel lblDetalles = new JLabel(tipo + " • " + precio);
                        lblDetalles.setFont(new Font("Arial", Font.PLAIN, 11));
                        lblDetalles.setForeground(Color.DARK_GRAY);

                        JLabel lblId = new JLabel("ID: " + id);
                        lblId.setFont(new Font("Arial", Font.PLAIN, 10));
                        lblId.setForeground(Color.GRAY);

                        panelInfo.add(lblNombre);
                        panelInfo.add(lblDetalles);
                        panelInfo.add(lblId);

                        // ✅ ENSAMBLAR EL PANEL DEL PRODUCTO
                        panelProducto.add(panelImagen, BorderLayout.WEST);
                        panelProducto.add(panelInfo, BorderLayout.CENTER);

                        panelMenuContainer.add(panelProducto);
                        panelMenuContainer.add(Box.createRigidArea(new Dimension(0, 8)));
                    }

                    // ✅ ACTUALIZAR LA INTERFAZ
                    panelMenuContainer.revalidate();
                    panelMenuContainer.repaint();

                    // ✅ MOSTRAR MENSAJE DE ÉXITO EN LA CONSOLA
                    System.out.println("✅ Menú cargado con " + menu.length() + " productos e imágenes");

                } catch (Exception e) {
                    // ✅ MOSTRAR ERROR EN EL PANEL
                    panelMenuContainer.removeAll();
                    JLabel lblError = new JLabel("❌ Error cargando menú: " + e.getMessage());
                    lblError.setForeground(Color.RED);
                    panelMenuContainer.add(lblError);
                    panelMenuContainer.revalidate();
                    panelMenuContainer.repaint();
                }
            });
        }).start();
    }

    // ✅ MÉTODO ORIGINAL cargarMenu (lo mantenemos por si acaso)
    private void cargarMenu() {
        cargarMenuConImagenes(); // Redirigir al nuevo método
    }

    // ✅ MÉTODO crearPedido (SE MANTIENE IGUAL)
    private void crearPedido() {
        String nombre = txtNombreCliente.getText().trim();
        String idLocal = txtIdLocal.getText().trim();
        String productosText = txtProductos.getText().trim();

        if (nombre.isEmpty() || productosText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "⚠️ Datos incompletos\n\n" +
                            "• Nombre del cliente: Obligatorio\n" +
                            "• Productos: Al menos un producto requerido",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        new Thread(() -> {
            try {
                // Parsear productos
                JSONArray productosArray = new JSONArray();
                String[] lineas = productosText.split("\n");

                boolean tieneErrores = false;
                StringBuilder errores = new StringBuilder();

                for (String linea : lineas) {
                    linea = linea.trim();
                    if (linea.isEmpty() || linea.startsWith("Formato") || linea.startsWith("Ejemplo")) {
                        continue;
                    }

                    String[] partes = linea.split(",");
                    if (partes.length == 2) {
                        try {
                            int id = Integer.parseInt(partes[0].trim());
                            int cantidad = Integer.parseInt(partes[1].trim());

                            if (cantidad < 1) {
                                tieneErrores = true;
                                errores.append("❌ Línea '").append(linea).append("': Cantidad debe ser 1 o más\n");
                                continue;
                            }

                            if (id < 1 || id > 6) {
                                tieneErrores = true;
                                errores.append("❌ Línea '").append(linea).append("': ID debe estar entre 1-6\n");
                                continue;
                            }

                            JSONObject producto = new JSONObject();
                            producto.put("id", id);
                            producto.put("cantidad", cantidad);
                            productosArray.put(producto);

                        } catch (NumberFormatException e) {
                            tieneErrores = true;
                            errores.append("❌ Línea '").append(linea).append("': Formato incorrecto. Usa: número,número\n");
                        }
                    } else {
                        tieneErrores = true;
                        errores.append("❌ Línea '").append(linea).append("': Formato incorrecto. Usa: id,cantidad\n");
                    }
                }

                if (tieneErrores) {
                    SwingUtilities.invokeLater(() -> {
                        txtResultado.setText("⚠️ ERRORES EN LOS PRODUCTOS:\n\n" + errores.toString() +
                                "\n📝 Formato correcto: id,cantidad\n" +
                                "   Ejemplo: 1,2 (2 Café Latte)\n" +
                                "   IDs válidos: 1 al 6");
                    });
                    return;
                }

                if (productosArray.length() == 0) {
                    SwingUtilities.invokeLater(() -> {
                        txtResultado.setText("❌ Error: No se encontraron productos válidos\n" +
                                "Formato correcto: id,cantidad (ej: 1,2)\n" +
                                "IDs válidos: 1 al 6");
                    });
                    return;
                }

                // Crear JSON del pedido
                JSONObject pedido = new JSONObject();
                JSONObject cliente = new JSONObject();
                cliente.put("nombre", nombre);
                cliente.put("id_local", idLocal.isEmpty() ? "anonimo" : idLocal);

                pedido.put("cliente", cliente);
                pedido.put("productos", productosArray);

                // Enviar al servidor
                String resultado = ApiClient.crearPedido(pedido.toString());

                SwingUtilities.invokeLater(() -> {
                    try {
                        JSONObject pedidoCreado = new JSONObject(resultado);
                        StringBuilder sb = new StringBuilder();

                        sb.append("╔══════════════════════════════════════╗\n");
                        sb.append("║         PEDIDO CREADO ✅             ║\n");
                        sb.append("╚══════════════════════════════════════╝\n\n");

                        sb.append("┌────────────────────────────────────┐\n");
                        sb.append("│ 👤 CLIENTE: ").append(padRight(pedidoCreado.getJSONObject("cliente").getString("nombre"), 25)).append("│\n");
                        String idCorto = pedidoCreado.getString("_id");
                        if (idCorto.length() > 20) {
                            idCorto = idCorto.substring(0, 17) + "...";
                        }
                        sb.append("│ 🆔 ID: ").append(padRight(idCorto, 31)).append("│\n");
                        sb.append("│ 📊 ESTADO: ").append(padRight(pedidoCreado.getString("estado"), 26)).append("│\n");
                        sb.append("│ ⏱️  TIEMPO: ").append(padRight(pedidoCreado.getInt("tiempo_estimado_min") + " min", 25)).append("│\n");
                        sb.append("├────────────────────────────────────┤\n");

                        JSONArray productos = pedidoCreado.getJSONArray("productos");
                        double total = 0;
                        for (int i = 0; i < productos.length(); i++) {
                            JSONObject producto = productos.getJSONObject(i);
                            int cantidad = producto.getInt("cantidad");
                            double precio = producto.getDouble("precio");
                            double subtotal = cantidad * precio;
                            total += subtotal;

                            String nombreProducto = producto.getString("nombre");

                            // ✅ OBTENER EMOJI SI EXISTE
                            String emoji = "";
                            if (producto.has("emoji")) {
                                emoji = producto.getString("emoji") + " ";
                            }

                            if (nombreProducto.length() > 18) {
                                nombreProducto = nombreProducto.substring(0, 15) + "...";
                            }

                            sb.append("│ • ").append(emoji)
                                    .append(padRight(nombreProducto, 18))
                                    .append(" x").append(cantidad)
                                    .append(" ").append(String.format("%5.2f", subtotal)).append("€ │\n");
                        }

                        sb.append("├────────────────────────────────────┤\n");
                        sb.append("│ 💰 TOTAL: ").append(padRight(String.format("%.2f€", total), 27)).append("│\n");
                        sb.append("└────────────────────────────────────┘\n\n");

                        sb.append("📢 Pedido enviado a cocina ✓\n");
                        sb.append("📱 Cliente puede ver estado en app móvil\n");

                        txtResultado.setText(sb.toString());

                        // ✅ LIMPIAR CAMPOS
                        txtNombreCliente.setText("");
                        txtIdLocal.setText("");
                        txtProductos.setText("Formato: id,cantidad (uno por línea)\nEjemplo:\n1,2\n2,1\n3,1");

                    } catch (Exception ex) {
                        txtResultado.setText("=== RESPUESTA DEL SERVIDOR ===\n" + resultado);

                        if (resultado.contains("_id") && resultado.contains("productos")) {
                            txtNombreCliente.setText("");
                            txtIdLocal.setText("");
                            txtProductos.setText("Formato: id,cantidad (uno por línea)\nEjemplo:\n1,2\n2,1\n3,1");
                        }
                    }
                });

            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    txtResultado.setText("❌ Error inesperado:\n" + ex.getMessage());
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