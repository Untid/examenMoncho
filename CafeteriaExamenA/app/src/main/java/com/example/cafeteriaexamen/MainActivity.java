package com.example.cafeteriaexamen;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.cafeteriaexamen.db.DatabaseHelper;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private NavigationView navigationView;
    private TextView tvUsuarioHeader;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ✅ INICIALIZAR DATABASE HELPER
        databaseHelper = new DatabaseHelper(this);

        // Configurar toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // ✅ OBTENER REFERENCIA AL HEADER
        View headerView = navigationView.getHeaderView(0);
        tvUsuarioHeader = headerView.findViewById(R.id.tvUsuarioHeader);

        // Configurar Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();

            // Configurar AppBarConfiguration CON el drawer
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_inicio, R.id.nav_registro, R.id.nav_estado_pedido, R.id.nav_satisfaccion)
                    .setOpenableLayout(drawerLayout)  // ← Esto es importante!
                    .build();

            // Configurar NavigationUI - Esto mostrará el icono de hamburguesa
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);
        }

        // ✅ ACTUALIZAR HEADER AL INICIAR LA APP
        actualizarHeaderUsuario();
    }

    // ✅ MÉTODO PARA ACTUALIZAR EL HEADER DEL USUARIO
    private void actualizarHeaderUsuario() {
        // Verificar si hay usuario logueado en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String usuarioEmail = prefs.getString("usuario_logueado", null);

        if (usuarioEmail != null && !usuarioEmail.isEmpty()) {
            // ✅ HAY USUARIO LOGUEADO - Mostrar su nombre
            Cursor cursor = databaseHelper.obtenerUsuario(usuarioEmail);
            if (cursor != null && cursor.moveToFirst()) {
                try {
                    int nombreIndex = cursor.getColumnIndex("nombre");
                    if (nombreIndex != -1) {
                        String nombreUsuario = cursor.getString(nombreIndex);
                        tvUsuarioHeader.setText("Hola, " + nombreUsuario);
                        System.out.println("👤 Header actualizado: " + nombreUsuario);
                    } else {
                        tvUsuarioHeader.setText("Usuario registrado");
                    }
                } catch (Exception e) {
                    tvUsuarioHeader.setText("Usuario registrado");
                    System.out.println("❌ Error obteniendo nombre: " + e.getMessage());
                } finally {
                    cursor.close();
                }
            } else {
                tvUsuarioHeader.setText("Usuario registrado");
                System.out.println("ℹ️ Usuario en prefs pero no en BD");
            }
        } else {
            // ✅ NO HAY USUARIO LOGUEADO
            tvUsuarioHeader.setText("No registrado");
            System.out.println("👤 Estado: No registrado");
        }
    }

    // ✅ MÉTODO PÚBLICO PARA CUANDO SE HAGA LOGIN
    public void onUsuarioLogueado(String email) {
        // Guardar en SharedPreferences
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("usuario_logueado", email);
        editor.apply();

        System.out.println("🔐 Usuario logueado: " + email);
        actualizarHeaderUsuario();
    }

    // ✅ MÉTODO PÚBLICO PARA CUANDO SE HAGA LOGOUT
    public void onUsuarioDeslogueado() {
        // Limpiar SharedPreferences
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove("usuario_logueado");
        editor.apply();

        System.out.println("🔓 Usuario deslogueado");
        actualizarHeaderUsuario();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ✅ ACTUALIZAR HEADER CADA VEZ QUE SE VUELVE A LA ACTIVITY
        actualizarHeaderUsuario();
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Esto maneja el icono de hamburguesa
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            return NavigationUI.navigateUp(navHostFragment.getNavController(), appBarConfiguration)
                    || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}