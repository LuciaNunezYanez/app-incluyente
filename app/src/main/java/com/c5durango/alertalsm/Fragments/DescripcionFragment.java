package com.c5durango.alertalsm.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.c5durango.alertalsm.Clases.ModelDescripcion;
import com.c5durango.alertalsm.R;

public class DescripcionFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    ModelDescripcion modelDescripcion = new ModelDescripcion("");
    private EditText txtDescripcion;
    private String TAG = "DescripFragm";
    private TextView txtLongitud;
    private int MAX_LONGITUD = 1500;
    private int LONGITUD_ACTUAL = 0;
    DataListener callback;

    public DescripcionFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static DescripcionFragment newInstance(String param1, String param2) {
        DescripcionFragment fragment = new DescripcionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_descripcion, container, false);
        txtLongitud = view.findViewById(R.id.txtLongitudDescripcion);
        txtDescripcion = (EditText) view.findViewById(R.id.txtDescripcion);
        try{
            if(getArguments()!= null){
                if(getArguments().containsKey("descripcion")){
                    modelDescripcion.setDescripcion(getArguments().getString("descripcion"));
                    txtDescripcion.setText(modelDescripcion.getDescripcion());
                    LONGITUD_ACTUAL = modelDescripcion.getDescripcion().length();
                    txtLongitud.setText(LONGITUD_ACTUAL +"/" + MAX_LONGITUD);
                }
            }
        } catch (Exception e){
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        txtDescripcion.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                modelDescripcion.setDescripcion(txtDescripcion.getText().toString());
                // SETEAR LONGITUD ACTUAL
                LONGITUD_ACTUAL = modelDescripcion.getDescripcion().length();
                txtLongitud.setText(LONGITUD_ACTUAL +"/" + MAX_LONGITUD);
                // ENVIAR TEXTO REPORTE ACTIVITY
                callback.sendDataDescripcion(modelDescripcion.getDescripcion());
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "AFTER");
            }
        });
        return view;
    }

    public interface DataListener{
        void sendDataDescripcion(String descripcion);
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
    public void onDetach() {
        super.onDetach();
    }

}
