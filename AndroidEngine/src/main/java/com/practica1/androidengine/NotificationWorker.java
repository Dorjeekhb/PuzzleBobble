package com.practica1.androidengine;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Clase que define un trabajador personalizado (`NotificationWorker`) que utiliza WorkManager
 * para enviar notificaciones cuando se ejecuta.
 */
public class NotificationWorker extends Worker {

    // ID del canal de notificaciones (necesario para versiones de Android 8.0 y superiores)
    private static final String CHANNEL_ID = "work_manager_notification_channel";
    private static final int NOTIFICATION_ID = 1; // ID único para la notificación

    /**
     * Constructor de la clase `NotificationWorker`.
     * @param context Contexto de la aplicación.
     * @param workerParams Parámetros de entrada para el trabajador.
     */
    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    /**
     * Método principal que ejecuta la tarea cuando WorkManager programa este trabajador.
     * En este caso, se genera y muestra una notificación.
     * @return Devuelve `Result.success()` si la tarea se ejecuta correctamente.
     */
    @NonNull
    @Override
    public Result doWork() {
        // Recupera el intent para lanzar la actividad principal de la aplicación
        Intent mainActivityIntent = getApplicationContext().getPackageManager()
                .getLaunchIntentForPackage(getApplicationContext().getPackageName());

        if (mainActivityIntent != null) {
            // Configura flags para reiniciar la actividad principal y limpia la pila de actividades
            mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // Añade un indicador extra al intent para notificar que se accedió desde una recompensa
            mainActivityIntent.putExtra("NOTIFICATION_REWARD", true);
        }

        // Crea un PendingIntent que se ejecutará al tocar la notificación
        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0, // Request code para este PendingIntent
                mainActivityIntent, // Intent configurado para abrir la actividad principal
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // Flags de actualización y seguridad
        );

        // Verifica si es necesario crear un canal de notificaciones (Android 8.0 y superiores)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Configura un canal de notificaciones con un ID único, nombre y nivel de importancia
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, // ID del canal
                    "WorkManager Notifications", // Nombre del canal visible para el usuario
                    NotificationManager.IMPORTANCE_DEFAULT // Nivel de importancia (normal)
            );
            channel.setDescription("Notifications sent by WorkManager"); // Descripción del canal
            // Obtiene el servicio de notificaciones del sistema y crea el canal
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel); // Crea el canal si no existe
            }
        }

        // Construye la notificación utilizando NotificationCompat.Builder
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_bubble_chart_24) // Icono de la notificación
                .setContentTitle("Puzzle Bobble") // Título de la notificación
                .setContentText("¡Entra y ganarás 3 monedas!") // Texto del cuerpo de la notificación
                .setAutoCancel(true) // La notificación se elimina automáticamente al tocarla
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Prioridad de la notificación
                .setContentIntent(pendingIntent); // Acción que se ejecutará al tocar la notificación

        // Muestra la notificación
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            // Solo envía la notificación si el permiso `POST_NOTIFICATIONS` está otorgado
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }

        // Devuelve un resultado exitoso para indicar que el trabajo terminó correctamente
        return Result.success();
    }
}
