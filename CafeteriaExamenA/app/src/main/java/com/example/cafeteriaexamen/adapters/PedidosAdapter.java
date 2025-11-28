package com.example.cafeteriaexamen.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cafeteriaexamen.R;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class PedidosAdapter extends RecyclerView.Adapter<PedidosAdapter.PedidoViewHolder> {

    private List<JSONObject> pedidos = new ArrayList<>();
    private OnPedidoClickListener listener;

    public interface OnPedidoClickListener {
        void onPedidoClick(JSONObject pedido);
    }

    public void setPedidos(List<JSONObject> pedidos) {
        this.pedidos = pedidos != null ? pedidos : new ArrayList<>();
        System.out.println("🔄 Adapter: " + this.pedidos.size() + " pedidos");
        notifyDataSetChanged();
    }

    public void setOnPedidoClickListener(OnPedidoClickListener listener) {
        this.listener = listener;
        System.out.println("🎯 Listener configurado en adapter: " + (listener != null));
    }

    @NonNull
    @Override
    public PedidoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_pedido, parent, false);
        return new PedidoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PedidoViewHolder holder, int position) {
        JSONObject pedido = pedidos.get(position);
        holder.bind(pedido);

        // ✅ CLICK LISTENER MEJORADO
        holder.itemView.setOnClickListener(v -> {
            System.out.println("🔥 CLICK en posición " + position);
            if (listener != null) {
                listener.onPedidoClick(pedido);
            } else {
                System.out.println("❌ Listener es null - verificar configuración");
            }
        });
    }

    @Override
    public int getItemCount() {
        return pedidos.size();
    }

    static class PedidoViewHolder extends RecyclerView.ViewHolder {
        private TextView tvPedidoId, tvEstado, tvTiempoEstimado, tvProductosResumen, tvCliente;

        public PedidoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPedidoId = itemView.findViewById(R.id.tvPedidoId);
            tvEstado = itemView.findViewById(R.id.tvEstado);
            tvTiempoEstimado = itemView.findViewById(R.id.tvTiempoEstimado);
            tvProductosResumen = itemView.findViewById(R.id.tvProductosResumen);
            tvCliente = itemView.findViewById(R.id.tvCliente);

            // ✅ DEBUG DE VISTAS
            if (tvCliente == null) {
                System.out.println("⚠️ tvCliente no encontrado - verificar layout");
            }
        }

        public void bind(JSONObject pedido) {
            try {
                String pedidoId = pedido.getString("_id");
                String estado = pedido.getString("estado");
                int tiempo = pedido.getInt("tiempo_estimado_min");
                JSONArray productos = pedido.getJSONArray("productos");

                // Obtener nombre del cliente
                String clienteNombre = "Cliente";
                if (pedido.has("cliente")) {
                    JSONObject cliente = pedido.getJSONObject("cliente");
                    if (cliente.has("nombre")) {
                        clienteNombre = cliente.getString("nombre");
                    }
                }

                // Actualizar vistas
                tvPedidoId.setText("Pedido #" + pedidoId.substring(0, 8));
                if (tvCliente != null) {
                    tvCliente.setText("Cliente: " + clienteNombre);
                }
                tvEstado.setText("Estado: " + estado);
                tvTiempoEstimado.setText("Tiempo: " + tiempo + " min");

                // ✅ RESUMEN DE PRODUCTOS CON EMOJIS
                StringBuilder resumen = new StringBuilder("Productos: ");
                for (int i = 0; i < Math.min(productos.length(), 2); i++) {
                    JSONObject prod = productos.getJSONObject(i);
                    if (i > 0) resumen.append(", ");

                    // ✅ OBTENER EMOJI SI EXISTE
                    String emoji = "";
                    if (prod.has("emoji")) {
                        emoji = prod.getString("emoji") + " ";
                    }

                    resumen.append(emoji).append(prod.getString("nombre"));
                }
                if (productos.length() > 2) {
                    resumen.append(" y ").append(productos.length() - 2).append(" más");
                }
                tvProductosResumen.setText(resumen.toString());

                // Color según estado
                switch (estado) {
                    case "Pedido":
                        tvEstado.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_orange_dark));
                        break;
                    case "En preparación":
                        tvEstado.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_blue_dark));
                        break;
                    case "Listo para recoger":
                        tvEstado.setTextColor(itemView.getContext().getResources().getColor(android.R.color.holo_green_dark));
                        break;
                    case "Recogido":
                        tvEstado.setTextColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                        break;
                }

            } catch (Exception e) {
                tvPedidoId.setText("Error cargando pedido");
                System.out.println("❌ Error en bind: " + e.getMessage());
            }
        }
    }
}