package com.c5durango.alertalsm.Servicios;


import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.c5durango.alertalsm.Clases.ModeloUbicacion;
import com.c5durango.alertalsm.Constantes;
import com.c5durango.alertalsm.Envios.EnviarCoordenadas;
import com.c5durango.alertalsm.R;
import com.c5durango.alertalsm.Utilidades.Notificaciones;
import com.c5durango.alertalsm.Utilidades.PreferencesReporte;
import com.c5durango.alertalsm.Utilidades.Utilidades;

import static com.c5durango.alertalsm.Constantes.CHANNEL_ID;
import static com.c5durango.alertalsm.Constantes.ID_SERVICIO_WIDGET_CREAR_REPORTE;
import static com.c5durango.alertalsm.Constantes.ID_SERVICIO_WIDGET_GENERAR_ALERTA;

public class WidgetService extends Service {

    static String TAG = "ServicioWidget";

    // VARIABLES PARA USO DE MULTIMEDIA
    //ImageTraseraResultReceiver resultTReceiver;
    //ImageFrontalResultReceiver resultFReceiver;
    //int ciclo = 1;

    public static final int SUCCESS = 1;
    public static final int ERROR = 0;

    Boolean procesoImagenFrontal = false;
    Boolean procesoImageTrasera = false;
    Boolean ESCUCHADOR_GPS = false;

    String IMAGEN_FRONTAL = "Ninguna";
    String IMAGEN_TRASERA = "Ninguna";

    String FECHA_FRONTAL = "";
    String FECHA_TRASERA = "";

    int reporteCreado = 0;
    int idComercio = 0;
    int idUsuario = 0;
    int idMunicipio = 0;
    int claveMunicipio = 0;
    // static int contador = 0;

    Intent intentGPS;
    ModeloUbicacion modeloUbicacion = new ModeloUbicacion("","",0.0,0.0);
    Notificaciones notificaciones = new Notificaciones();
    Boolean reportegenerado = false;


    public WidgetService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        comenzarProceso();
        crearNotificacionPersistente(getApplicationContext(), WidgetService.class, CHANNEL_ID, "¡Botón de pánico activado!", "Botón activado desde el widget. Enviando multimedia..", R.drawable.ic_siren_chica, "Descripción", ID_SERVICIO_WIDGET_CREAR_REPORTE);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void comenzarProceso(){
        //contador++;

        Boolean puedeEnviar = PreferencesReporte.puedeEnviarReporte(getApplicationContext(), System.currentTimeMillis());
        if(puedeEnviar){
            PreferencesReporte.guardarReporteInicializado(getApplicationContext());
            LocalBroadcastManager.getInstance(getApplication()).registerReceiver(broadcastReceiverGenerarAlerta, new IntentFilter("generarAlertaService"));
            // Terminar escuchadores
            terminarServicioGPS();
            // Registrar escuchadores
            registrarEscuchadorGPS();
            registrarReceiverGnerarAlerta();
            // Iniciar el servicio GenerarAlertaService
            comenzarGPS();
        } else {
            Notificaciones notificaciones = new Notificaciones();
            notificaciones.crearNotificacionNormal(getApplicationContext(), CHANNEL_ID,  R.drawable.ic_color_error,
                    "¡Ocurrió un erro!", "Error al emitir alerta de pánico, reintente.", ID_SERVICIO_WIDGET_GENERAR_ALERTA);
            if(puedoFinalizar(getApplicationContext()))
                stopSelf();
            // Debo terminar
        }
    }

    public void crearNotificacionPersistente(Context context, Class clase, String canal, String titulo, String mensaje, int icono, String descripcion, int idServicio){

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, clase), 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Crear notificación de servicio activo
            Notification notification =
                    new Notification.Builder(context, canal)
                            .setColor(Color.WHITE)
                            .setContentTitle(titulo)
                            .setContentText(mensaje)
                            .setColor(Color.RED)
                            .setSmallIcon(icono)
                            .setContentIntent(pendingIntent)
                            .build();

            // Crear al canal de notificación
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, Constantes.NOMBRE_APP, NotificationManager.IMPORTANCE_LOW);
            notificationChannel.setDescription(descripcion);
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
            startForeground(idServicio, notification);

        } else {
            // Crear notificación de servicio activo
            Notification notification =
                    new Notification.Builder(context)
                            .setColor(Color.WHITE)
                            .setContentTitle(titulo)
                            .setContentText(mensaje)
                            .setSmallIcon(icono)
                            .setColor(Color.GRAY)
                            .setContentIntent(pendingIntent)
                            .build();

            startForeground(idServicio, notification);
        }
    }

    /**********************************************************************************************
     *                                 GENERAR  ALERTA                                             *
     **********************************************************************************************/

    public void registrarReceiverGnerarAlerta(){
        LocalBroadcastManager.getInstance(getApplication()).registerReceiver(broadcastReceiverGenerarAlerta, new IntentFilter("generarAlertaService"));
    }

    private void iniciarServicioGenerarAlerta(){
        SharedPreferences preferences = getSharedPreferences("Login", Context.MODE_PRIVATE);
        if (preferences.contains("comercio") && preferences.contains("usuario") && preferences.contains("id_municipio")){
            idComercio = preferences.getInt("comercio" ,0);
            idUsuario = preferences.getInt("usuario", 0);
            idMunicipio = preferences.getInt("id_municipio", 0);
            claveMunicipio = preferences.getInt("clave_municipio",0);
        } else {
            // No se puede agregar la notificación
        }

        Intent intent = new Intent(getApplicationContext(), GenerarAlertaService.class);
        intent.putExtra("comercio", idComercio);
        intent.putExtra("usuario", idUsuario);
        intent.putExtra("sala", "Incluyente");
        intent.putExtra("fecha", Utilidades.obtenerFecha());
        intent.putExtra("id_municipio", idMunicipio);
        intent.putExtra("clave_municipio", claveMunicipio);

        intent.putExtra("latitud", modeloUbicacion.getLatitud());
        intent.putExtra("longitud", modeloUbicacion.getLongitud());
        intent.putExtra("fecha", modeloUbicacion.getFechaHora());
        intent.putExtra("lugar", modeloUbicacion.getLugar());

        intent.setPackage("com.c5durango.appalertagenero");
        getApplicationContext().startService(intent);
    }

    public void detenerServicioAlerta(){
        Intent intent = new Intent(getApplicationContext(), GenerarAlertaService.class);
        getApplicationContext().stopService(intent);
    }


    /**********************************************************************************************
     *                                      FOTOGRAFIAS                                            *
     **********************************************************************************************/

    /*public void iniciarProcesoFotografias(){
        if( getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT) ){
            // INICIAR PROCESO CAMARA FRONTAL
            resultFReceiver = new ImageFrontalResultReceiver(new Handler());
            Intent intentFrontal = new Intent(getApplicationContext(), FotografiaService.class);
            intentFrontal.putExtra("reporteCreado", reporteCreado);
            intentFrontal.putExtra("tipoCamara", "frontal");
            intentFrontal.putExtra("receiver", resultFReceiver);
            getApplicationContext().startService(intentFrontal);

        } else if ( getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA) ){
            // INICIAR PROCESO CAMARA TRAERA
            resultTReceiver = new ImageTraseraResultReceiver(new Handler());
            Intent intentTrasera = new Intent(getApplicationContext(), FotografiaService.class);
            intentTrasera.putExtra("reporteCreado", reporteCreado);
            intentTrasera.putExtra("tipoCamara", "trasera");
            intentTrasera.putExtra("receiver", resultTReceiver);
            getApplicationContext().startService(intentTrasera);

        } else {
            if( procesoImagenFrontal ){
                // ENVIA LA IMAGEN FRONTAL (2)
                terminarServicioFotografias();
                Boolean resultadoenvio = EnviarImagenes.enviarImagenFrontal(getApplicationContext(), IMAGEN_FRONTAL, FECHA_FRONTAL, reporteCreado);
                //calcularNuevoEnvioFotografias();
            } else{
                // TERMINA PROCESO Y NO ENVIA NADA (3)
                finProcesoFotografias();
            }
        }
    }*/

    /*public void terminarServicioFotografias(){
        Intent intent = new Intent(getApplicationContext(), FotografiaService.class);
        getApplicationContext().stopService(intent);
    }

    public void finProcesoFotografias(){
        Log.d(TAG, "Termina proceso vacio de fotografias");
        // Al menos guardar las fotografía en el dispositivo
    }*/

    /*********************************************************************************************
     *                                        GPS                                                 *
     *********************************************************************************************/

    public void comenzarGPS(){
        intentGPS = new Intent(getApplicationContext(), GPSService.class);
        intentGPS.putExtra("reporteGenerado", reporteCreado);
        intentGPS.putExtra("padre", "ServicioWidget");
        intentGPS.putExtra("escuchador", "GPSServiceWidget");
        startService(intentGPS);
    }

    public void terminarServicioGPS(){
        Intent intent = new Intent(getApplicationContext(), GPSService.class);
        getApplicationContext().stopService(intent);
        Log.d(TAG, "TERMINAR SERVICIO GPS");
    }

    /*********************************************************************************************
     *                                         AUDIO                                              *
     *********************************************************************************************/

    public void comenzarGrabacionAudio(){
        registrarEscuchadorAudio();
        Intent intent = new Intent(getApplicationContext(), AudioService.class);
        intent.putExtra("nombreAudio", "GrabacionBotonDePanico");
        intent.putExtra("reporteCreado", reporteCreado);
        intent.putExtra("soloUno", true);
        getApplicationContext().startService(intent);
    }

    public void terminarGrabacionAudio(){
        eliminarEscuchadorAudio();
        Intent intent = new Intent(getApplicationContext(), AudioService.class);
        getApplicationContext().stopService(intent);
    }

    /*********************************************************************************************
     *                     INICIAN RESULT RECEIVER DE FOTOGRAFIAS                                 *
     *********************************************************************************************/

    /*private class ImageTraseraResultReceiver extends ResultReceiver {
        public ImageTraseraResultReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            procesoImageTrasera = true;
            FECHA_TRASERA = resultData.getString("fecha");
            // GUARDAR LA IMAGEN DE RETORNO
            String imagenTrasera = resultData.getString("imagen");
            if(!imagenTrasera.equals("Ninguna")){
                IMAGEN_TRASERA = imagenTrasera;
            }

            switch (resultCode) {
                case ERROR:
                    Log.d(TAG, "Ocurrió un error al devolver camara trasera");
                    break;

                case SUCCESS:
                    // ENVIAR MULTIMEDIA (1)
                    terminarServicioFotografias();
                    if(procesoImagenFrontal){
                        Boolean res = EnviarImagenes.enviarImagenFrontal(getApplicationContext(), IMAGEN_FRONTAL, FECHA_FRONTAL, reporteCreado);
                        Log.d(TAG, "El resultado del envio de frontal es: "+ res);
                    }
                    if(procesoImageTrasera){
                        Boolean res = EnviarImagenes.enviarImagenTrasera(getApplicationContext(), IMAGEN_TRASERA, FECHA_TRASERA, reporteCreado);
                        Log.d(TAG, "El resultado del envio de trasera es: "+ res);
                    }

                    //calcularNuevoEnvioFotografias();
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }
    }

    private class ImageFrontalResultReceiver extends ResultReceiver {
        public ImageFrontalResultReceiver(Handler handler) {
            super(handler);
        }
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            procesoImagenFrontal = true;
            FECHA_FRONTAL = resultData.getString("fecha");

            // GUARDAR LA IMAGEN DE RETORNO
            String imagenFrontal = resultData.getString("imagen");
            if(!imagenFrontal.equals("Ninguna")){
                IMAGEN_FRONTAL = imagenFrontal;
            }

            switch (resultCode) {
                case ERROR:
                    Log.d(TAG, "Ocurrió un error al devolver cámara frontal");
                    // SE SIGUE CON LA TRASERA
                    break;
                case SUCCESS:

                    if(getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
                        // INICIAR PROCESO CAMARA TRAERA
                        resultTReceiver = new ImageTraseraResultReceiver(new Handler());
                        Intent intentTrasera = new Intent(getApplicationContext(), FotografiaService.class);
                        intentTrasera.putExtra("reporteCreado", reporteCreado);
                        intentTrasera.putExtra("tipoCamara", "trasera");
                        intentTrasera.putExtra("receiver", resultTReceiver);
                        startService(intentTrasera);
                    }else {
                        if(procesoImagenFrontal){
                            // ENVIAR LA IMAGEN FRONTAL (5)
                            terminarServicioFotografias();
                            Boolean res = EnviarImagenes.enviarImagenFrontal(getApplicationContext(), IMAGEN_FRONTAL, FECHA_FRONTAL, reporteCreado);

                            //calcularNuevoEnvioFotografias();
                            Log.d(TAG, "El resultado del envio de frontal es: "+ res);
                        } else{
                            // FIN DEL PROCESO (6)
                            finProcesoFotografias();
                        }
                    }
                    break;
            }
            super.onReceiveResult(resultCode, resultData);
        }
    }*/

    /*public void calcularNuevoEnvioFotografias(){
        ciclo--;
        Log.d(TAG, "CICLO: "+ ciclo);

        if(ciclo != 0){
            // Iniciar nuevamente el proceso de fotografias
            iniciarProcesoFotografias();
        } else if (ciclo == 0){
            terminarServicioFotografias();
            deboTerminar();
        }
    } */

    /*********************************************************************************************
     *                       BROADCAST RECEIVERS - ESCUCHADORES                                   *
     *********************************************************************************************/
    public void registrarEscuchadorGPS(){
        if(!ESCUCHADOR_GPS) {
            LocalBroadcastManager.getInstance(getApplication()).registerReceiver(broadcastReceiverGPSService, new IntentFilter("GPSServiceWidget"));
            ESCUCHADOR_GPS = true;
        }
    }

    public void eliminarEscuchadorGPS(){
        Log.d(TAG, "Eliminando el escuchador GPS");
        ESCUCHADOR_GPS = false;
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(broadcastReceiverGPSService);
    }

    public void registrarEscuchadorAudio(){
        LocalBroadcastManager.getInstance(getApplication()).registerReceiver(broadcastReceiverAudio, new IntentFilter("AudioBroadcast"));
    }

    public void eliminarEscuchadorAudio(){
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(broadcastReceiverAudio);
    }

    private BroadcastReceiver broadcastReceiverGenerarAlerta = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intentGenerarAlerta) {

            try{
                Bundle parametros = intentGenerarAlerta.getExtras();
                reporteCreado = parametros.getInt("reporteCreado", 0);

                detenerServicioAlerta();

                if (reporteCreado != 0) {
                    reportegenerado = true;
                    // Guardar la información del ultimo reporte generado
                    PreferencesReporte.actualizarUltimoReporte(getApplicationContext(), reporteCreado);
                    notificaciones.crearNotificacionNormal(context, CHANNEL_ID,  R.drawable.ic_color_success, "", "Se generó alerta con folio #" + reporteCreado, ID_SERVICIO_WIDGET_GENERAR_ALERTA);

                    if( Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED){
                            if(!Utilidades.isMyServiceRunning(getApplication(), AudioService.class)){
                                comenzarGrabacionAudio();
                            }
                        }
                    }

                    // COMENZAR A GENERAR LA MULTIMEDIA
                    if(ESCUCHADOR_GPS){
                        eliminarEscuchadorGPS();
                        terminarServicioGPS();
                    }

                    if (puedoFinalizar(context))
                        stopSelf();
                } else {
                    notificaciones.crearNotificacionNormal(getApplicationContext(), CHANNEL_ID, R.drawable.ic_color_error, "¡No se pudo generar la alerta de pánico!", parametros.getString("message", "Sin mensaje"), ID_SERVICIO_WIDGET_CREAR_REPORTE);
                    if (puedoFinalizar(context))
                        stopSelf();
                }
            } catch (Exception e){
                Log.e(TAG, "Catch broadcast receiver generar alerta: " + e.getMessage());
            }
        }
    };

    private BroadcastReceiver broadcastReceiverGPSService = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle parametros = intent.getExtras();

            modeloUbicacion.setLatitud(parametros.getDouble("latitud", 0.0));
            modeloUbicacion.setLongitud(parametros.getDouble("longitud", 0.0));
            modeloUbicacion.setFechaHora(parametros.getString("fecha", ""));
            modeloUbicacion.setLugar("Actual");

            iniciarServicioGenerarAlerta();

            terminarServicioGPS();
            eliminarEscuchadorGPS();
            if (puedoFinalizar(context))
                stopSelf();
        }
    };

    private BroadcastReceiver broadcastReceiverAudio = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            terminarGrabacionAudio();
            if (puedoFinalizar(context))
                stopSelf();
        }
    };

    private static Boolean puedoFinalizar(Context context){
        if((Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1 && !Utilidades.isMyServiceRunning(context, AudioService.class) && !Utilidades.isMyServiceRunning(context, GPSService.class))
                || (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1 && !Utilidades.isMyServiceRunning(context, GPSService.class))){
            return true;
        } else {
            if(!Utilidades.isMyServiceRunning(context, AudioService.class) && Utilidades.isMyServiceRunning(context, GPSService.class)){
                // Nuevamente enviar GPS o detener el servicio
                Log.d(TAG, "Se detiene servicio de GPS por mal funcionamiento");
                return true;
            }
            return false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        eliminarEscuchadorGPS();
    }
}
