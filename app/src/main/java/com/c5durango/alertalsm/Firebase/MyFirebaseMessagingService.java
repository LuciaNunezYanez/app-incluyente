package com.c5durango.alertalsm.Firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.c5durango.alertalsm.R;
import com.c5durango.alertalsm.Utilidades.Notificaciones;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import static com.c5durango.alertalsm.Constantes.CHANNEL_ID;
import static com.c5durango.alertalsm.Constantes.ID_SERVICIO_NOTIFICACION_REPORTE_RECIBIDO;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "Firebase";

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, remoteMessage.getNotification().toString());

            Notificaciones notificaciones = new Notificaciones();
            notificaciones.crearNotificacionNormal(
                    getApplicationContext(),
                    CHANNEL_ID,
                    R.drawable.ic_alarm_chic,
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody(),
                    ID_SERVICIO_NOTIFICACION_REPORTE_RECIBIDO);

        }

    }
}
