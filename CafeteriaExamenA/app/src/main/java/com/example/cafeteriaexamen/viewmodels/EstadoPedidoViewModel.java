package com.example.cafeteriaexamen.viewmodels;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class EstadoPedidoViewModel extends ViewModel {
    private MutableLiveData<List<JSONObject>> pedidos = new MutableLiveData<>(new ArrayList<>());
    private MutableLiveData<String> mensajeEstado = new MutableLiveData<>("Cargando...");
    private MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private MutableLiveData<Boolean> necesitaRefresh = new MutableLiveData<>(false); // ✅ NUEVO
    private long ultimaActualizacion = 0; // ✅ NUEVO

    public MutableLiveData<List<JSONObject>> getPedidos() {
        return pedidos;
    }

    public MutableLiveData<String> getMensajeEstado() {
        return mensajeEstado;
    }

    public MutableLiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public MutableLiveData<Boolean> getNecesitaRefresh() { // ✅ NUEVO
        return necesitaRefresh;
    }

    public long getUltimaActualizacion() { // ✅ NUEVO
        return ultimaActualizacion;
    }

    public void setPedidos(List<JSONObject> nuevosPedidos) {
        pedidos.setValue(nuevosPedidos);
        this.ultimaActualizacion = System.currentTimeMillis(); // ✅ ACTUALIZAR TIMESTAMP
    }

    public void setMensajeEstado(String mensaje) {
        mensajeEstado.setValue(mensaje);
    }

    public void setIsLoading(boolean loading) {
        isLoading.setValue(loading);
    }

    public void solicitarRefresh() { // ✅ NUEVO
        necesitaRefresh.setValue(true);
        System.out.println("🔄 ViewModel - Refresh solicitado");
    }

    public void resetRefresh() { // ✅ NUEVO
        necesitaRefresh.setValue(false);
    }

    public boolean debeRefrescar() { // ✅ NUEVO
        long tiempoActual = System.currentTimeMillis();
        long tiempoDesdeUltimaActualizacion = tiempoActual - ultimaActualizacion;
        boolean necesita = necesitaRefresh.getValue() != null && necesitaRefresh.getValue();
        boolean tiempoExcedido = tiempoDesdeUltimaActualizacion > 10000; // 10 segundos

        return necesita || tiempoExcedido ||
                (pedidos.getValue() != null && pedidos.getValue().isEmpty());
    }

    public void agregarOActualizarPedido(JSONObject nuevoPedido) {
        List<JSONObject> current = pedidos.getValue();
        if (current != null) {
            try {
                String nuevoId = nuevoPedido.getString("_id");
                boolean existe = false;

                for (int i = 0; i < current.size(); i++) {
                    if (current.get(i).getString("_id").equals(nuevoId)) {
                        current.set(i, nuevoPedido);
                        existe = true;
                        break;
                    }
                }

                if (!existe) {
                    current.add(0, nuevoPedido);
                }

                pedidos.setValue(current);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}