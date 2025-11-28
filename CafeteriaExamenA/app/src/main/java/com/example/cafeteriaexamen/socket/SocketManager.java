package com.example.cafeteriaexamen.socket;

import android.util.Log;
import io.socket.client.IO;
import io.socket.client.Socket;
import org.json.JSONObject;
import java.net.URISyntaxException;
import java.util.Arrays;

public class SocketManager {
    private static SocketManager instance;
    private Socket socket;
    private static final String SERVER_URL = "http://192.168.1.48:3000";

    private SocketManager() {
        try {
            IO.Options options = new IO.Options();
            options.forceNew = true;
            options.timeout = 10000;

            socket = IO.socket(SERVER_URL, options);

        } catch (URISyntaxException e) {
            Log.e("SOCKET", "Error en URL: " + e.getMessage());
        }
    }

    public static SocketManager getInstance() {
        if (instance == null) {
            instance = new SocketManager();
        }
        return instance;
    }

    public void connect() {
        if (socket != null && !socket.connected()) {
            Log.d("SOCKET_DEBUG", "🔗 Conectando a: " + SERVER_URL);

            // ✅ TODOS LOS EVENTOS DE DEBUG
            socket.on(Socket.EVENT_CONNECT, args -> {
                Log.d("SOCKET_DEBUG", "✅ CONECTADO al servidor!");

                // Identificarse como cliente móvil
                identificarComoCliente();
            });

            socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
                Log.e("SOCKET_DEBUG", "❌ Error conexión: " + Arrays.toString(args));
            });

            socket.on(Socket.EVENT_DISCONNECT, args -> {
                Log.d("SOCKET_DEBUG", "🔌 Desconectado: " + Arrays.toString(args));
            });

            // ✅ ESCUCHAR CUALQUIER EVENTO DEL SERVIDOR (DEBUG)
            socket.on("test-conexion", args -> {
                Log.d("SOCKET_DEBUG", "🎉 EVENTO TEST: " + Arrays.toString(args));
            });

            socket.on("nuevo-pedido", args -> {
                Log.d("SOCKET_DEBUG", "📦 EVENTO NUEVO PEDIDO: " + Arrays.toString(args));
            });

            socket.on("estado-actualizado", args -> {
                Log.d("SOCKET_DEBUG", "🔄 EVENTO ESTADO ACTUALIZADO: " + Arrays.toString(args));
            });

            socket.on("pedido-creado", args -> {
                Log.d("SOCKET_DEBUG", "📝 EVENTO PEDIDO CREADO: " + Arrays.toString(args));
            });

            socket.connect();
        }
    }

    // ✅ MÉTODO PARA IDENTIFICARSE
    private void identificarComoCliente() {
        try {
            JSONObject data = new JSONObject();
            data.put("id_local", "cliente_movil_" + System.currentTimeMillis());
            data.put("tipo", "android");

            socket.emit("cliente-conectado", data);
            Log.d("SOCKET_DEBUG", "📱 Identificándose como cliente móvil...");

        } catch (Exception e) {
            Log.e("SOCKET_DEBUG", "❌ Error al identificarse: " + e.getMessage());
        }
    }

    public Socket getSocket() { return socket; }
    public void disconnect() { if (socket != null) socket.disconnect(); }
}