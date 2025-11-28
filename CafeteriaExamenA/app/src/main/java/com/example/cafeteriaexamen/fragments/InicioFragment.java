package com.example.cafeteriaexamen.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.cafeteriaexamen.R;
import com.example.cafeteriaexamen.db.DatabaseHelper; // ✅ Importar DatabaseHelper

public class InicioFragment extends Fragment {

    private TextView tvSatisfaccionGlobal;
    private DatabaseHelper databaseHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inicio, container, false);

        databaseHelper = new DatabaseHelper(getContext());
        tvSatisfaccionGlobal = view.findViewById(R.id.tvSatisfaccionGlobal);

        cargarSatisfaccionGlobal();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarSatisfaccionGlobal();
    }

    private void cargarSatisfaccionGlobal() {
        double satisfaccionGlobal = databaseHelper.obtenerSatisfaccionGlobal();

        String texto;
        if (satisfaccionGlobal > 0) {
            texto = String.format("%.1f/10 ⭐", satisfaccionGlobal);
        } else {
            texto = "Sé el primero en valorarnos!";
        }

        tvSatisfaccionGlobal.setText(texto);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (databaseHelper != null) {
            databaseHelper.close();
        }
    }
}