package com.c5durango.alertalsm.Fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.c5durango.alertalsm.Clases.ModeloUbicacion;
import com.c5durango.alertalsm.Dialogs.Dialogs;
import com.c5durango.alertalsm.R;
import com.c5durango.alertalsm.Servicios.GPSService;
import com.c5durango.alertalsm.Utilidades.Utilidades;

import java.util.ArrayList;


public class UbicacionFragment extends Fragment implements View.OnClickListener{

    private DataListener callback;

    private CardView cardUbicacionActual;
    private CardView cardCasa;
    private CardView cardGoogleMaps;

    private String LUGAR_ACTUAL = "Actual", LUGAR_CASA = "Casa", LUGAR_GOOGLE = "Otra";
    ModeloUbicacion modeloUbicacion = new ModeloUbicacion("Actual", Utilidades.obtenerFecha(), 0.0, 0.0);

    public UbicacionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            callback = (DataListener) context;
        } catch(Exception e){
            Toast.makeText(context, "ERROR ON ATTACH " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!= null){
            try {
                // Recibir los datos de ubicación
                if (getArguments().containsKey("lugar"))
                    modeloUbicacion.setLugar(getArguments().getString("lugar"));
                if (getArguments().containsKey("fecha"))
                    modeloUbicacion.setFechaHora(getArguments().getString("fecha"));
                if (getArguments().containsKey("latitud"))
                    modeloUbicacion.setLatitud(getArguments().getDouble("latitud"));
                if (getArguments().containsKey("longitud"))
                    modeloUbicacion.setLongitud(getArguments().getDouble("longitud"));
            } catch (Exception e){
                Toast.makeText(getContext(), "#1: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ubicacion, container, false);
        cardUbicacionActual = (CardView) view.findViewById(R.id.cardUbicacionActual);
        cardUbicacionActual.setOnClickListener(this);
        cardCasa = (CardView) view.findViewById(R.id.cardEnCasa);
        cardCasa.setOnClickListener(this);
        cardGoogleMaps = (CardView) view.findViewById(R.id.cardGoogleMaps);
        cardGoogleMaps.setOnClickListener(this);

        try {
            dibujarCardUbicacion(modeloUbicacion.getLugar());
        } catch (Exception e){
            Toast.makeText(getContext(), "#2: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return view;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case (R.id.cardUbicacionActual):
                modeloUbicacion.setLugar(LUGAR_ACTUAL);
                modeloUbicacion.setFechaHora(Utilidades.obtenerFecha());
                modeloUbicacion.setLongitud(0.0);
                modeloUbicacion.setLatitud(0.0);
                callback.sendUbicacion(modeloUbicacion.getLugar(), modeloUbicacion.getFechaHora(), modeloUbicacion.getLatitud(), modeloUbicacion.getLongitud());
                dibujarCardUbicacion(LUGAR_ACTUAL);
                break;
            case (R.id.cardEnCasa):
                modeloUbicacion.setLugar(LUGAR_CASA);
                modeloUbicacion.setFechaHora(Utilidades.obtenerFecha());
                modeloUbicacion.setLongitud(0.0);
                modeloUbicacion.setLatitud(0.0);
                callback.sendUbicacion(modeloUbicacion.getLugar(), modeloUbicacion.getFechaHora(), modeloUbicacion.getLatitud(), modeloUbicacion.getLongitud());
                dibujarCardUbicacion(LUGAR_CASA);
                break;
            case (R.id.cardGoogleMaps):
                callback.abrirMaps();
                /*modeloUbicacion.setLugar(LUGAR_GOOGLE);
                modeloUbicacion.setFechaHora(Utilidades.obtenerFecha());
                modeloUbicacion.setLongitud(0.0);
                modeloUbicacion.setLatitud(0.0);
                callback.sendUbicacion(modeloUbicacion.getLugar(), modeloUbicacion.getFechaHora(), modeloUbicacion.getLatitud(), modeloUbicacion.getLongitud());*/
                dibujarCardUbicacion(LUGAR_GOOGLE);
                break;
        }
    }

    private void dibujarCardUbicacion(String lugar){
        if(lugar.equals(LUGAR_ACTUAL)){
            cardUbicacionActual.setBackgroundColor(getResources().getColor(R.color.grisClaro));
            cardCasa.setBackgroundColor(Color.WHITE);
            cardGoogleMaps.setBackgroundColor(Color.WHITE);
        }
        if(lugar.equals(LUGAR_CASA)){
            cardUbicacionActual.setBackgroundColor(Color.WHITE);
            cardCasa.setBackgroundColor(getResources().getColor(R.color.grisClaro));
            cardGoogleMaps.setBackgroundColor(Color.WHITE);
        }
        if(lugar.equals(LUGAR_GOOGLE)){
            cardUbicacionActual.setBackgroundColor(Color.WHITE);
            cardCasa.setBackgroundColor(Color.WHITE);
            cardGoogleMaps.setBackgroundColor(getResources().getColor(R.color.grisClaro));
        }
    }


    public interface DataListener{
        void sendUbicacion(String lugar, String fechaHora, double latitud, double longitug);
        void abrirMaps();
    }
}