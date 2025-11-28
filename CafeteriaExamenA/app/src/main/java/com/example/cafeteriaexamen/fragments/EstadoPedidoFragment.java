package com.example.cafeteriaexamen.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.cafeteriaexamen.R;
import com.example.cafeteriaexamen.adapters.PedidosAdapter;
import com.example.cafeteriaexamen.socket.SocketManager;
import com.example.cafeteriaexamen.viewmodels.EstadoPedidoViewModel;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import io.socket.client.Socket;

public class EstadoPedidoFragment extends Fragment {

    private Socket socket;
    private RecyclerView recyclerPedidos;
    private TextView tvMensajeVacio;
    private Button btnRefresh;
    private PedidosAdapter adapter;
    private EstadoPedidoViewModel viewModel; // ✅ USAR VIEWMODEL
    private boolean socketsConfigurados = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_estado_pedido, container, false);

        recyclerPedidos = view.findViewById(R.id.recyclerPedidos);
        tvMensajeVacio = view.findViewById(R.id.tvMensajeVacio);
        btnRefresh = view.findViewById(R.id.btnRefresh);

        System.out.println("🔄 EstadoPedidoFragment - onCreateView");

        // ✅ INICIALIZAR VIEWMODEL
        viewModel = new ViewModelProvider(this).get(EstadoPedidoViewModel.class);

        // Configurar RecyclerView
        adapter = new PedidosAdapter();
        recyclerPedidos.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerPedidos.setAdapter(adapter);

        // ✅ OBSERVAR LOS DATOS DEL VIEWMODEL
        viewModel.getPedidos().observe(getViewLifecycleOwner(), pedidos -> {
            System.out.println("🔄 ViewModel - Pedidos actualizados: " + pedidos.size());
            adapter.setPedidos(pedidos);
            actualizarVisibilidadLista(pedidos);
        });

        viewModel.getMensajeEstado().observe(getViewLifecycleOwner(), mensaje -> {
            System.out.println("🔄 ViewModel - Mensaje: " + mensaje);
            tvMensajeVacio.setText(mensaje);
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            btnRefresh.setEnabled(!isLoading);
            btnRefresh.setText(isLoading ? "⏳" : "🔄");
        });

        // Configurar click para ir al detalle
        adapter.setOnPedidoClickListener(pedido -> {
            try {
                System.out.println("🎯 Navegando al detalle: " + pedido.getString("_id"));

                Bundle args = new Bundle();
                args.putString("pedido_data", pedido.toString());

                NavController navController = Navigation.findNavController(requireView());
                navController.navigate(R.id.action_estado_to_detalle, args);

            } catch (Exception e) {
                System.out.println("❌ Error navegación: " + e.getMessage());
            }
        });

        // Botón refresh
        btnRefresh.setOnClickListener(v -> {
            System.out.println("🔄 Botón refresh presionado");
            viewModel.setMensajeEstado("🔄 Recargando pedidos...");
            cargarPedidosExistentes();
        });

        // ✅ CARGAR PEDIDOS SI ES NECESARIO (según ViewModel)
        if (viewModel.debeRefrescar()) {
            System.out.println("📥 Cargando pedidos iniciales...");
            cargarPedidosExistentes();
        } else {
            System.out.println("✅ Usando datos existentes del ViewModel");
            // Los observers ya se encargan de actualizar la UI
        }

        // CONFIGURAR SOCKETS
        if (!socketsConfigurados) {
            configurarSocket();
            socketsConfigurados = true;
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        System.out.println("🔄 EstadoPedidoFragment - onResume");

        // ✅ VERIFICAR SI NECESITA REFRESCO (INCLUYendo CUANDO VUELVE DEL DETALLE)
        if (viewModel.debeRefrescar()) {
            System.out.println("🔄 REFRESCO AUTOMÁTICO AL VOLVER");
            viewModel.setMensajeEstado("🔄 Actualizando...");
            cargarPedidosExistentes();
            viewModel.resetRefresh(); // Resetear después del refresh
        } else {
            System.out.println("✅ No necesita refresh - Mostrando datos actuales");
        }
    }

    private void cargarPedidosExistentes() {
        System.out.println("🌐 Iniciando carga de pedidos...");
        viewModel.setIsLoading(true);

        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://192.168.1.48:3000/api/pedidos");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty("Accept", "application/json");

                System.out.println("🔗 Conectando a: " + url);

                int responseCode = connection.getResponseCode();
                System.out.println("📡 Código de respuesta: " + responseCode);

                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();

                    System.out.println("📦 Datos recibidos: " + content.length() + " caracteres");

                    JSONArray pedidosArray = new JSONArray(content.toString());
                    final List<JSONObject> nuevaLista = new ArrayList<>();
                    for (int i = 0; i < pedidosArray.length(); i++) {
                        nuevaLista.add(pedidosArray.getJSONObject(i));
                    }

                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            // ✅ ACTUALIZAR VIEWMODEL
                            viewModel.setPedidos(nuevaLista);
                            viewModel.setIsLoading(false);

                            if (nuevaLista.isEmpty()) {
                                viewModel.setMensajeEstado("✅ No hay pedidos activos");
                            } else {
                                viewModel.setMensajeEstado("✅ " + nuevaLista.size() + " pedidos cargados");
                            }
                        });
                    }
                } else {
                    System.out.println("❌ Error HTTP: " + responseCode);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            viewModel.setMensajeEstado("❌ Error del servidor: " + responseCode);
                            viewModel.setIsLoading(false);
                        });
                    }
                }

            } catch (Exception e) {
                System.out.println("❌ Error de conexión: " + e.getMessage());
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        viewModel.setMensajeEstado("❌ Error de conexión");
                        viewModel.setIsLoading(false);
                    });
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }

    private void configurarSocket() {
        socket = SocketManager.getInstance().getSocket();

        socket.off("nuevo-pedido");
        socket.off("estado-actualizado");

        socket.on("nuevo-pedido", args -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    try {
                        JSONObject pedido = (JSONObject) args[0];
                        System.out.println("📨 Socket - Nuevo pedido: " + pedido.getString("_id"));
                        // ✅ USAR VIEWMODEL
                        viewModel.agregarOActualizarPedido(pedido);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });

        socket.on("estado-actualizado", args -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    try {
                        JSONObject pedido = (JSONObject) args[0];
                        String estado = pedido.getString("estado");
                        System.out.println("📨 Socket - Estado actualizado: " + pedido.getString("_id"));

                        if ("Recogido".equals(estado)) {
                            // Eliminar pedido recogido
                            eliminarPedido(pedido.getString("_id"));
                        } else {
                            viewModel.agregarOActualizarPedido(pedido);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });

        if (!socket.connected()) {
            SocketManager.getInstance().connect();
        }
    }

    private void eliminarPedido(String pedidoId) {
        try {
            List<JSONObject> current = viewModel.getPedidos().getValue();
            if (current != null) {
                for (int i = 0; i < current.size(); i++) {
                    if (current.get(i).getString("_id").equals(pedidoId)) {
                        current.remove(i);
                        viewModel.setPedidos(current); // ✅ ACTUALIZAR VIEWMODEL
                        System.out.println("🗑️ Pedido eliminado: " + pedidoId);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("❌ Error eliminando pedido: " + e.getMessage());
        }
    }

    private void actualizarVisibilidadLista(List<JSONObject> pedidos) {
        if (pedidos == null || pedidos.isEmpty()) {
            recyclerPedidos.setVisibility(View.GONE);
            tvMensajeVacio.setVisibility(View.VISIBLE);
        } else {
            recyclerPedidos.setVisibility(View.VISIBLE);
            tvMensajeVacio.setVisibility(View.GONE);
        }
    }

    // ✅ MÉTODO PÚBLICO PARA QUE DETALLESPEDIDOFRAGMENT SOLICITE REFRESCO
    public void solicitarRefresh() {
        System.out.println("📨 EstadoPedidoFragment - Solicitud de refresh recibida");
        viewModel.solicitarRefresh();
    }
}