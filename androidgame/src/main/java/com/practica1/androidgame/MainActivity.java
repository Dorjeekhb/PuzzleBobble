package com.practica1.androidgame;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdView;
import com.practica1.androidengine.AndroidEngine;
import com.practica1.androidengine.AndroidMobile;
import com.practica1.engine.File;
import com.practica1.engine.Mobile;
import com.practica1.gamelogic.SceneManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import android.content.Intent;
import android.net.Uri;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.provider.Settings;
import androidx.core.app.NotificationManagerCompat;

public class MainActivity extends AppCompatActivity  implements SensorEventListener {
    // Declaración de los elementos principales de la actividad
    private SurfaceView renderView; // Vista donde se renderizarán los gráficos del juego
    private AndroidEngine engine; // Motor del juego
    private Mobile mobile; // Gestión de lógica específica del dispositivo
    private AdView adView; // Vista para mostrar anuncios publicitarios
    private File file; // Archivo para guardar datos
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private boolean isUserClose = false; // Para controlar si el usuario sigue cerca
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable openYouTubeTask;

    static {
        System.loadLibrary("androidgame"); // Cargar la biblioteca nativa al inicio
    }

    private native String computeSha256(String input); // Método nativo para calcular SHA-256
    private native String generateHmac(String message, String key); // Método nativo para generar HMAC

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        // Inicializar vistas principales
        renderView = findViewById(R.id.renderView);
        adView = findViewById(R.id.adView);

        // Inicializar motor
        engine = new AndroidEngine(renderView, this);
        mobile = new AndroidMobile(this, renderView, adView);
   
        // Leer JSON guardado
        file = engine.getInternalFile("data.json");
        String savedJson = file.getContent();
        
        if (savedJson == null) {
            // Generar un nuevo JSON si no existe
            String newJson = generateJson();
            file.setContent(newJson);

            // Calcular HMAC y SHA-256 y guardar
            String jsonHash = computeSha256(newJson);
            saveHashToFile(jsonHash); // Guardar el hash
            Log.d("JsonStatus", "Generated and saved new JSON with its hash.");
        } else {
            // Verificar integridad del JSON
            String savedHash = readHashFromFile(); // Leer hash guardado
            String currentHash = computeSha256(savedJson); // Calcular hash actual

            if (!currentHash.equals(savedHash)) {
                // Si el hash no coincide, es porque el archivo fue modificado manualmente
                Toast.makeText(this, "JSON modificado, reseteando datos...", Toast.LENGTH_LONG).show();
                resetAppData(); // Resetear datos si no coinciden
                finish();
                return;
            }
        }
        // Inicializar el sensor de proximidad
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
            if (proximitySensor == null) {
                Toast.makeText(this, "El dispositivo no tiene un sensor de proximidad", Toast.LENGTH_LONG).show();
            }
        }
        // Inicializar lógica del juego
        SceneManager.getInstance().Init(engine, mobile, 500, 1000, "data.json");
        if (getIntent().getBooleanExtra("NOTIFICATION_REWARD", false)) {
            rewardUserFromNotification();
        }
    }

    private String generateJson() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", "Dorjee");
            jsonObject.put("id", 12345);
            jsonObject.put("timestamp", System.currentTimeMillis());
            return jsonObject.toString();
        } catch (Exception e) {
            Log.e("JsonGenerate", "Error generating JSON", e);
            return null;
        }
    }

    private void saveHashToFile(String hash) {
        try {
            FileOutputStream fos = openFileOutput("hash_file.txt", Context.MODE_PRIVATE);
            fos.write(hash.getBytes());
            fos.close();
            Log.d("HashCheck", "Hash saved to file.");
        } catch (IOException e) {
            Log.e("HashCheck", "Error saving hash to file", e);
        }
    }

    private String readHashFromFile() {
        try {
            FileInputStream fis = openFileInput("hash_file.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            fis.close();
            return sb.toString();
        } catch (FileNotFoundException e) {
            Log.d("HashCheck", "Hash file not found.");
            return null;
        } catch (IOException e) {
            Log.e("HashCheck", "Error reading hash from file", e);
            return null;
        }
    }

    private void resetAppData() {
        deleteFile("data.json");
        deleteFile("hash_file.txt");
        Log.d("AppReset", "Application data reset successfully.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        engine.pause();

        this.mobile.scheduleNotificationWithWorkManager();
        //Toast.makeText(this, "¡PROGRESO GUARDADO!", Toast.LENGTH_LONG).show();
        // Guardar estado actual del JSON
        SceneManager.getInstance().saveFile("data.json");

        // Recalcular el hash después de guardar
        String currentContent = file.getContent();
        if (currentContent != null) {
            String newHash = computeSha256(currentContent); // Calcular SHA-256 del nuevo contenido
            saveHashToFile(newHash); // Guardar el nuevo hash
            Log.d("HashUpdate", "Updated hash saved after game progress.");
        } else {
            Log.e("HashUpdate", "Failed to update hash because file content is null.");
        }
        // Desregistrar el listener del sensor
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    private void checkNotificationPermission() {
        boolean areNotificationsEnabled = NotificationManagerCompat.from(this).areNotificationsEnabled();
        if (!areNotificationsEnabled) {
            redirectToNotificationSettings();
        }
    }


    private void redirectToNotificationSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        engine.resume();
        checkNotificationPermission();
        // Registrar el listener del sensor de proximidad
        if (proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.mobile.unregisterSensorListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        engine.onStop();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            float distance = event.values[0]; // Distancia medida por el sensor
            float threshold = proximitySensor.getMaximumRange() / 2; // Cambia esto según lo que desees

            Window window = getWindow();
            WindowManager.LayoutParams layoutParams = window.getAttributes();

            if (distance < threshold) {
                // Usuario está cerca, disminuir brillo
                if (!isUserClose) {
                    isUserClose = true;
                    layoutParams.screenBrightness = 0.0f; // Brillo muy bajo (mínimo: 0.0, máximo: 1.0)
                    window.setAttributes(layoutParams);
                    Toast.makeText(this, "¡Estás muy cerca de la pantalla! Disminuyendo brillo.", Toast.LENGTH_SHORT).show();

                    // Programar apertura de YouTube después de 3 segundos
                    openYouTubeTask = () -> {
                        if (isUserClose) { // Si el usuario sigue cerca
                            openYouTubeVideo("https://www.youtube.com/watch?v=EQuWxit0BTI");
                        }
                    };
                    handler.postDelayed(openYouTubeTask, 3000); // Esperar 3 segundos
                }
            } else {
                // Usuario está lejos, restaurar brillo
                if (isUserClose) {
                    isUserClose = false;
                    layoutParams.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE; // Restaurar brillo por defecto
                    window.setAttributes(layoutParams);
                    Toast.makeText(this, "Usuario lejos, restaurando brillo.", Toast.LENGTH_SHORT).show();

                    // Cancelar la tarea programada
                    if (openYouTubeTask != null) {
                        handler.removeCallbacks(openYouTubeTask);
                    }
                }
            }
        }
    }


    private void rewardUserFromNotification() {
        // Otorga la recompensa a través de SceneManager
        SceneManager.getInstance().addCoins(3);
        Toast.makeText(this, "¡Has recibido 3 monedas!", Toast.LENGTH_LONG).show();
    }


    /**
     * Abrir un video de YouTube directamente en la aplicación de YouTube o en el navegador si no está instalada.
     * @param videoUrl URL del video de YouTube.
     */
    private void openYouTubeVideo(String videoUrl) {
        Intent appIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + videoUrl.split("=")[1]));
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(videoUrl));
        appIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            // Intenta abrir la aplicación de YouTube
            startActivity(appIntent);
        } catch (Exception e) {
            // Si la aplicación de YouTube no está instalada, abre en el navegador
            startActivity(webIntent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
