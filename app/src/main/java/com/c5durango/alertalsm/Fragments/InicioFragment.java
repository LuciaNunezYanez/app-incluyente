package com.c5durango.alertalsm.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.c5durango.alertalsm.R;

public class InicioFragment extends Fragment  implements View.OnClickListener {

    public DataListener callback;

    private ImageButton btnReporteNotificacion;
    private ImageButton btnReporteRapido;
    public View root;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_inicio, container, false);
        btnReporteNotificacion = root.findViewById(R.id.btnGenerarReporte);
        btnReporteNotificacion.setOnClickListener(this);
        btnReporteRapido = root.findViewById(R.id.btnReporteRapido);
        btnReporteRapido.setOnClickListener(this);
        return root;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            callback = (DataListener) context;
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnGenerarReporte:
                callback.generarReporteNotificacion();
                break;
            case R.id.btnReporteRapido:
                callback.generarReporteRapido();
                break;

        }
    }

    public interface DataListener{
        void generarReporteNotificacion();
        void generarReporteRapido();
    }
}