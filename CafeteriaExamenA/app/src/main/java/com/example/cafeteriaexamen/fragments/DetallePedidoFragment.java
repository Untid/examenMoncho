package com.example.cafeteriaexamen.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.cafeteriaexamen.R;
import com.example.cafeteriaexamen.socket.SocketManager;
import com.example.cafeteriaexamen.viewmodels.EstadoPedidoViewModel;

import org.json.JSONArray;
import org.json.JSONObject;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class DetallePedidoFragment extends Fragment {

    private TextView tvPedidoIdDetalle, tvClienteDetalle, tvEstadoDetalle, tvTiempoDetalle, tvTotalDetalle;
    private LinearLayout containerProductosDetalle;
    private Button btnVolverDetalle;
    private Socket socket;
    private String pedidoId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        System.out.println("📱 DETALLE_PEDIDO_FRAGMENT - onCreateView");

        View view = inflater.inflate(R.layout.fragment_detalle_pedido, container, false);

        // Inicializar vistas
        tvPedidoIdDetalle = view.findViewById(R.id.tvPedidoIdDetalle);
        tvClienteDetalle = view.findViewById(R.id.tvClienteDetalle);
        tvEstadoDetalle = view.findViewById(R.id.tvEstadoDetalle);
        tvTiempoDetalle = view.findViewById(R.id.tvTiempoDetalle);
        tvTotalDetalle = view.findViewById(R.id.tvTotalDetalle);
        containerProductosDetalle = view.findViewById(R.id.containerProductosDetalle);
        btnVolverDetalle = view.findViewById(R.id.btnVolverDetalle);

        // Manejo de argumentos
        if (getArguments() != null) {
            String pedidoJson = getArguments().getString("pedido_data");
            if (pedidoJson != null) {
                try {
                    JSONObject pedido = new JSONObject(pedidoJson);
                    pedidoId = pedido.getString("_id");
                    System.out.println("📱 Pedido cargado en detalle: " + pedidoId);
                    mostrarDetallePedido(pedido);

                    // ✅ CONFIGURAR SOCKET PARA ESTE PEDIDO ESPECÍFICO
                    configurarSocket();

                } catch (Exception e) {
                    System.out.println("❌ Error cargando pedido: " + e.getMessage());
                }
            }
        }

        // Botón volver - CON REFRESCO
        btnVolverDetalle.setOnClickListener(v -> {
            System.out.println("🔙 Botón volver - Solicitando refresh via ViewModel");

            // ✅ SOLICITAR REFRESCO AL VIEWMODEL COMPARTIDO
            try {
                // Obtener el mismo ViewModel que usa EstadoPedidoFragment
                EstadoPedidoViewModel viewModel = new ViewModelProvider(requireActivity()).get(EstadoPedidoViewModel.class);
                viewModel.solicitarRefresh();
                System.out.println("✅ Refresh solicitado al ViewModel");
            } catch (Exception e) {
                System.out.println("❌ Error solicitando refresh: " + e.getMessage());
            }

            requireActivity().onBackPressed();
        });

        return view;
    }

    // ✅ CONFIGURACIÓN SIMPLE DE SOCKET
    private void configurarSocket() {
        socket = SocketManager.getInstance().getSocket();

        socket.on("estado-actualizado", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        try {
                            JSONObject pedidoActualizado = (JSONObject) args[0];
                            String pedidoActualizadoId = pedidoActualizado.getString("_id");

                            // ✅ ACTUALIZAR SOLO SI ES EL MISMO PEDIDO
                            if (pedidoActualizadoId.equals(pedidoId)) {
                                System.out.println("🔄 Actualización en detalle: " + pedidoActualizado.getString("estado"));
                                mostrarDetallePedido(pedidoActualizado);
                            }
                        } catch (Exception e) {
                            System.out.println("❌ Error en actualización: " + e.getMessage());
                        }
                    });
                }
            }
        });

        System.out.println("📡 Socket configurado para detalle del pedido: " + pedidoId);
    }

    private void mostrarDetallePedido(JSONObject pedido) {
        try {
            String estado = pedido.getString("estado");
            int tiempo = pedido.getInt("tiempo_estimado_min");
            double total = pedido.getDouble("total");

            // Obtener nombre del cliente
            String clienteNombre = "Cliente";
            if (pedido.has("cliente")) {
                JSONObject cliente = pedido.getJSONObject("cliente");
                if (cliente.has("nombre")) {
                    clienteNombre = cliente.getString("nombre");
                }
            }

            // Actualizar información básica
            tvPedidoIdDetalle.setText("Pedido #" + pedidoId.substring(0, 8));
            tvClienteDetalle.setText("Cliente: " + clienteNombre);
            tvEstadoDetalle.setText("Estado: " + estado);
            tvTiempoDetalle.setText("Tiempo estimado: " + tiempo + " min");
            tvTotalDetalle.setText(String.format("Total: €%.2f", total));

            // Mostrar productos
            mostrarProductosDetalle(pedido.getJSONArray("productos"));

            // Color del estado
            aplicarColorEstado(estado);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void aplicarColorEstado(String estado) {
        int color;
        switch (estado) {
            case "Pedido": color = getResources().getColor(android.R.color.holo_orange_dark); break;
            case "En preparación": color = getResources().getColor(android.R.color.holo_blue_dark); break;
            case "Listo para recoger": color = getResources().getColor(android.R.color.holo_green_dark); break;
            case "Recogido": color = getResources().getColor(android.R.color.darker_gray); break;
            default: color = getResources().getColor(android.R.color.black); break;
        }
        tvEstadoDetalle.setTextColor(color);
    }

    private void mostrarProductosDetalle(JSONArray productos) {
        containerProductosDetalle.removeAllViews();

        try {
            for (int i = 0; i < productos.length(); i++) {
                JSONObject producto = productos.getJSONObject(i);
                String nombre = producto.getString("nombre");
                int cantidad = producto.getInt("cantidad");
                double precio = producto.getDouble("precio");
                double subtotal = precio * cantidad;

                View itemView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_producto_detalle, containerProductosDetalle, false);

                TextView tvProducto = itemView.findViewById(R.id.tvProductoDetalle);
                TextView tvCantidad = itemView.findViewById(R.id.tvCantidadDetalle);
                TextView tvPrecioUnitario = itemView.findViewById(R.id.tvPrecioUnitarioDetalle);
                TextView tvSubtotal = itemView.findViewById(R.id.tvSubtotalDetalle);

                // ✅ OBTENER EMOJI SI EXISTE
                String emoji = "";
                if (producto.has("emoji")) {
                    emoji = producto.getString("emoji") + " ";
                }

                tvProducto.setText(emoji + nombre); // ✅ MOSTRAR EMOJI + NOMBRE
                tvCantidad.setText("x" + cantidad);
                tvPrecioUnitario.setText(String.format("€%.2f", precio));
                tvSubtotal.setText(String.format("€%.2f", subtotal));

                containerProductosDetalle.addView(itemView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // ✅ LIMPIAR SOLO EL LISTENER DE ESTE FRAGMENT
        if (socket != null) {
            socket.off("estado-actualizado");
            System.out.println("🔇 Socket limpiado en detalle");
        }
    }
}