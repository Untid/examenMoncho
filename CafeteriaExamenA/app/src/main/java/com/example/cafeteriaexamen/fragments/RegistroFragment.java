package com.example.cafeteriaexamen.fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.example.cafeteriaexamen.R;
import com.example.cafeteriaexamen.db.DatabaseHelper;

public class RegistroFragment extends Fragment {

    private EditText etNombre, etEmail, etPassword, etTelefono;
    private Button btnRegistrar, btnLogin, btnLogout; // ✅ AGREGADO btnLogout
    private DatabaseHelper databaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_registro, container, false);

        databaseHelper = new DatabaseHelper(getActivity());

        // Inicializar vistas
        etNombre = view.findViewById(R.id.etNombre);
        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        etTelefono = view.findViewById(R.id.etTelefono);
        btnRegistrar = view.findViewById(R.id.btnRegistrar);
        btnLogin = view.findViewById(R.id.btnLogin);
        btnLogout = view.findViewById(R.id.btnLogout); // ✅ INICIALIZAR LOGOUT

        btnRegistrar.setOnClickListener(v -> registrarUsuario());
        btnLogin.setOnClickListener(v -> loginUsuario());
        btnLogout.setOnClickListener(v -> cerrarSesion()); // ✅ LISTENER LOGOUT

        // ✅ CARGAR DATOS SI EL USUARIO ESTÁ LOGUEADO
        cargarDatosUsuarioActual();

        return view;
    }

    // ✅ NUEVO MÉTODO: CARGAR DATOS DEL USUARIO ACTUAL
    private void cargarDatosUsuarioActual() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE);
        String usuarioEmail = prefs.getString("usuario_logueado", null);

        if (usuarioEmail != null && !usuarioEmail.isEmpty()) {
            // ✅ USUARIO LOGUEADO - CARGAR SUS DATOS
            Cursor cursor = databaseHelper.obtenerUsuario(usuarioEmail);
            if (cursor != null && cursor.moveToFirst()) {
                try {
                    int nombreIndex = cursor.getColumnIndex("nombre");
                    int emailIndex = cursor.getColumnIndex("email");
                    int telefonoIndex = cursor.getColumnIndex("telefono");

                    if (nombreIndex != -1) {
                        String nombre = cursor.getString(nombreIndex);
                        etNombre.setText(nombre);
                    }
                    if (emailIndex != -1) {
                        String email = cursor.getString(emailIndex);
                        etEmail.setText(email);
                    }
                    if (telefonoIndex != -1) {
                        String telefono = cursor.getString(telefonoIndex);
                        etTelefono.setText(telefono != null ? telefono : "");
                    }

                    // ✅ CAMBIAR TEXTO DEL BOTÓN A "ACTUALIZAR PERFIL" Y MOSTRAR LOGOUT
                    btnRegistrar.setText("Actualizar Perfil");
                    btnLogout.setVisibility(View.VISIBLE); // ✅ MOSTRAR BOTÓN LOGOUT
                    System.out.println("👤 Datos de usuario cargados para edición");

                } catch (Exception e) {
                    System.out.println("❌ Error cargando datos usuario: " + e.getMessage());
                } finally {
                    cursor.close();
                }
            }
        } else {
            // ✅ NO HAY USUARIO LOGUEADO - MODO REGISTRO NORMAL
            btnRegistrar.setText("Registrarse");
            btnLogout.setVisibility(View.GONE); // ✅ OCULTAR BOTÓN LOGOUT
            System.out.println("👤 Modo registro - usuario no logueado");
        }
    }

    // ✅ NUEVO MÉTODO: CERRAR SESIÓN
    private void cerrarSesion() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("usuario_logueado");
        editor.apply();

        // Notificar MainActivity para actualizar el header
        if (getActivity() instanceof com.example.cafeteriaexamen.MainActivity) {
            ((com.example.cafeteriaexamen.MainActivity) getActivity()).onUsuarioDeslogueado();
        }

        // Limpiar campos y volver a modo registro
        limpiarCampos();
        btnRegistrar.setText("Registrarse");
        btnLogout.setVisibility(View.GONE); // ✅ OCULTAR LOGOUT

        Toast.makeText(getActivity(), "✅ Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();
        System.out.println("👤 Usuario cerró sesión");
    }

    private void registrarUsuario() {
        String nombre = etNombre.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // ✅ VERIFICAR SI ES REGISTRO NUEVO O ACTUALIZACIÓN
        SharedPreferences prefs = requireActivity().getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE);
        String usuarioLogueado = prefs.getString("usuario_logueado", null);

        boolean success;

        if (usuarioLogueado != null && usuarioLogueado.equals(email)) {
            // ✅ MODO ACTUALIZACIÓN - USUARIO YA LOGUEADO
            success = databaseHelper.actualizarUsuario(nombre, email, password, telefono);
            if (success) {
                Toast.makeText(getActivity(), "✅ Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show();
                System.out.println("👤 Perfil actualizado: " + email);
            } else {
                Toast.makeText(getActivity(), "❌ Error actualizando perfil", Toast.LENGTH_SHORT).show();
            }
        } else {
            // ✅ MODO REGISTRO - USUARIO NUEVO
            success = databaseHelper.registrarUsuario(nombre, email, password, telefono);
            if (success) {
                Toast.makeText(getActivity(), "✅ Registro exitoso", Toast.LENGTH_SHORT).show();
                System.out.println("👤 Nuevo usuario registrado: " + email);

                // AUTOMÁTICAMENTE LOGUEAR AL USUARIO DESPUÉS DEL REGISTRO
                if (getActivity() instanceof com.example.cafeteriaexamen.MainActivity) {
                    ((com.example.cafeteriaexamen.MainActivity) getActivity()).onUsuarioLogueado(email);
                }

                // ✅ ACTUALIZAR INTERFAZ PARA MODO EDICIÓN
                btnRegistrar.setText("Actualizar Perfil");
                btnLogout.setVisibility(View.VISIBLE); // ✅ MOSTRAR LOGOUT

            } else {
                Toast.makeText(getActivity(), "❌ Error en el registro. Email ya existe", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loginUsuario() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getActivity(), "Por favor, ingrese email y contraseña", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean success = databaseHelper.verificarLogin(email, password);
        if (success) {
            Toast.makeText(getActivity(), "✅ Login exitoso", Toast.LENGTH_SHORT).show();

            // NOTIFICAR A MAIN ACTIVITY DEL LOGIN EXITOSO
            if (getActivity() instanceof com.example.cafeteriaexamen.MainActivity) {
                ((com.example.cafeteriaexamen.MainActivity) getActivity()).onUsuarioLogueado(email);
            }

            // ✅ RECARGAR DATOS PARA MODO EDICIÓN
            cargarDatosUsuarioActual();

        } else {
            Toast.makeText(getActivity(), "❌ Credenciales incorrectas", Toast.LENGTH_SHORT).show();
        }
    }

    private void limpiarCampos() {
        etNombre.setText("");
        etEmail.setText("");
        etPassword.setText("");
        etTelefono.setText("");
    }

    @Override
    public void onResume() {
        super.onResume();
        // ✅ ACTUALIZAR INTERFAZ CADA VEZ QUE SE VUELVE AL FRAGMENT
        cargarDatosUsuarioActual();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}