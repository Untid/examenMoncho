package com.example.cafeteriaexamen.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.example.cafeteriaexamen.R;
import com.example.cafeteriaexamen.db.DatabaseHelper; // ✅ Asegúrate de importar tu DatabaseHelper
import com.google.android.material.snackbar.Snackbar;

public class SatisfaccionFragment extends Fragment {

    private SeekBar sliderSatisfaccion;
    private TextView tvValoracion;
    private TextView tvCarita;
    private TextView tvDescripcion;
    private Button btnEnviar;
    private DatabaseHelper databaseHelper; // ✅ Referencia al DatabaseHelper

    // Array de emojis según la valoración
    private final String[] emojis = {
            "😢",  // 0-2: Muy malo
            "🙁",  // 3-4: Malo
            "😐",  // 5: Normal
            "🙂",  // 6-7: Bueno
            "😊",  // 8-9: Muy bueno
            "😍"   // 10: Excelente
    };

    // Array de descripciones según la valoración
    private final String[] descripciones = {
            "Muy malo",
            "Malo",
            "Normal",
            "Bueno",
            "Muy bueno",
            "Excelente"
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_satisfaccion, container, false);

        System.out.println("🎯 SatisfaccionFragment - Creado");

        // ✅ INICIALIZAR DATABASE HELPER
        databaseHelper = new DatabaseHelper(getContext());

        // Inicializar vistas
        sliderSatisfaccion = view.findViewById(R.id.sliderSatisfaccion);
        tvValoracion = view.findViewById(R.id.tvValoracion);
        tvCarita = view.findViewById(R.id.tvCarita);
        tvDescripcion = view.findViewById(R.id.tvDescripcion);
        btnEnviar = view.findViewById(R.id.btnEnviarValoracion);

        // Configurar slider
        sliderSatisfaccion.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                actualizarInterfaz(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Botón enviar - ✅ AHORA GUARDA EN LA BD
        btnEnviar.setOnClickListener(v -> {
            int valoracion = sliderSatisfaccion.getProgress();
            guardarValoracion(valoracion);
        });

        // Configuración inicial
        actualizarInterfaz(5); // Valor por defecto: 5

        return view;
    }

    private void actualizarInterfaz(int progreso) {
        // Animación de fade out/in
        tvCarita.animate()
                .alpha(0f)
                .setDuration(150)
                .withEndAction(() -> {
                    // Actualizar contenido
                    tvValoracion.setText(progreso + "/10");

                    int indice;
                    if (progreso <= 2) indice = 0;
                    else if (progreso <= 4) indice = 1;
                    else if (progreso == 5) indice = 2;
                    else if (progreso <= 7) indice = 3;
                    else if (progreso <= 9) indice = 4;
                    else indice = 5;

                    tvCarita.setText(emojis[indice]);
                    tvDescripcion.setText(descripciones[indice]);

                    // Fade in
                    tvCarita.animate()
                            .alpha(1f)
                            .setDuration(150)
                            .start();
                })
                .start();
    }

    private void guardarValoracion(int valoracion) {
        databaseHelper.guardarValoracion(valoracion);

        String mensaje = "";
        if (valoracion <= 3) {
            mensaje = "😢 Lamentamos que tu experiencia no fuera buena. ¡Trabajaremos para mejorar!";
        } else if (valoracion <= 6) {
            mensaje = "🙂 Gracias por tu feedback. ¡Seguimos mejorando!";
        } else {
            mensaje = "😍 ¡Gracias! Nos alegra que hayas disfrutado tu experiencia.";
        }

        // ✅ USAR SNACKBAR EN LUGAR DE TOAST
        View view = requireView();
        Snackbar snackbar = Snackbar.make(view, mensaje, Snackbar.LENGTH_LONG);

        // Personalizar el Snackbar
        View snackbarView = snackbar.getView();
        TextView textView = snackbarView.findViewById(com.google.android.material.R.id.snackbar_text);
        textView.setMaxLines(3); // Permitir múltiples líneas
        textView.setTextSize(14);

        // Cambiar color según valoración
        if (valoracion <= 3) {
            snackbarView.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
        } else if (valoracion <= 6) {
            snackbarView.setBackgroundColor(getResources().getColor(android.R.color.holo_orange_light));
        } else {
            snackbarView.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
        }

        snackbar.show();

        System.out.println("💾 Valoración guardada en BD: " + valoracion + "/10");

        // Navegar de vuelta después de un delay
        new android.os.Handler().postDelayed(() -> {
            try {
                requireActivity().onBackPressed();
            } catch (Exception e) {
                System.out.println("⚠️ Error al navegar back: " + e.getMessage());
            }
        }, 2500); // 2.5 segundos de delay
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // ✅ CERRAR LA CONEXIÓN SI ES NECESARIO
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}