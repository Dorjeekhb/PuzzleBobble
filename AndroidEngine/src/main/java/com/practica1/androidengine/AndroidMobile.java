package com.practica1.androidengine;

import android.content.Context;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.PixelCopy;
import android.app.Activity;
import android.util.Log;
import android.view.SurfaceView;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import java.util.concurrent.TimeUnit;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.practica1.engine.Mobile;

import android.widget.FrameLayout;
import com.practica1.engine.RewardListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class AndroidMobile implements Mobile, SensorEventListener {
    private AdView adView;
    private Activity activity;
    private SurfaceView surfaceView;
    private RewardedAd rewardedAd;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 15.0f; // Ajusta el umbral de detección
    private long lastShakeTime = 0;

    public AndroidMobile(Activity activity, SurfaceView surfaceView, AdView adView) {
        this.activity = activity;
        this.adView = adView;
        this.surfaceView = surfaceView;
        MobileAds.initialize(activity, initializationStatus -> {
        });

        // Inicializar el SensorManager y el Acelerómetro
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
            } else {
                Log.e("AndroidMobile", "Acelerómetro no disponible en el dispositivo.");
            }
        }

        // Only load the ad if adView is not null
        if (this.adView != null) {
            loadBannerAd();
        } else {
            Log.e("AndroidMobile", "AdView is null. Cannot load banner ad.");
        }
        loadRewardedAd();
        this.makeNotification();
    }


    private void loadBannerAd() {
        // Use the existing adView directly
        AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
        this.adView.loadAd(adRequest);
    }

    @Override
    public void makeNotification() {
        String channelID = "CHANNEL_ID_NOTIFICATION";
        String channelName = "Notification Channel";

        // Create the notification channel if Android Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            channel.setDescription("Notifications for the app");
            NotificationManager notificationManager = activity.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }



    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(activity, "ca-app-pub-3940256099942544/5224354917", adRequest, new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd ad) {
                Log.i("BUBBLE", "Rewarded ad loaded successfully.");
                rewardedAd = ad;

                // Set a callback to handle events for the full-screen content
                rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdShowedFullScreenContent() {
                        Log.i("BUBBLE", "Rewarded ad is showing.");
                    }


                    @Override
                    public void onAdDismissedFullScreenContent() {
                        Log.i("BUBBLE", "Rewarded ad was dismissed.");
                        // Load the next ad after the current one is dismissed
                        loadRewardedAd();
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        Log.e("BUBBLE", "Failed to show rewarded ad: " + adError.getMessage());
                    }
                });
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                Log.e("BUBBLE", "Failed to load rewarded ad: " + loadAdError.getMessage());
                rewardedAd = null;
            }
        });
    }


    @Override
    public void vibrateDevice(long duration) {
        // Obtener el servicio de vibración
        Vibrator vibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null && vibrator.hasVibrator()) { // Verificar si el dispositivo tiene un motor de vibración
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(new long[]{0, 200, 100, 300}, -1)); // Patrón de vibración
}
            } else {
            Log.e("AndroidMobile", "El dispositivo no soporta vibración.");
        }
    }

    @Override
    public void showRewardedAd(RewardListener listener) {
        if (rewardedAd != null) {
            Activity activityContext = this.activity;
            // Ensure the ad is shown on the main UI thread
            this.activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("BUBBLE", "showRewardedAd: success. Showing rewardedAd");
                    rewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                        @Override
                        public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                            // Notify the listener when the reward is earned
                            if (listener != null) {
                                listener.onReward();
                            }
                        }
                    });
                }
            });
        } else {
            Log.i("BUBBLE", "showRewardedAd: Ad not loaded yet.");
        }
    }

    @Override
    public void shareMessage(String title, String text) {
        // Create an intent for sharing text
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);
        // Create a chooser to let the user pick the app to share with
        activity.startActivity(Intent.createChooser(shareIntent, null));
    }

    @Override
    public void scheduleNotificationWithWorkManager() {
        // One time worker para triggear una notificación a los x segundos
        OneTimeWorkRequest notificationWorkRequest = new OneTimeWorkRequest.Builder(NotificationWorker.class)
                .setInitialDelay(2, TimeUnit.HOURS)
                .build();

        // mete la instancia del oneWorkRequest en work manager
        WorkManager.getInstance(activity.getApplicationContext()).enqueue(notificationWorkRequest);
    }


    // Método para hacer una captura de pantalla y compartir dicha imagen
    @Override
    public void shareImage(int level, int points) {
        // Crear un bitmap para almacenar la captura de pantalla del SurfaceView
        Bitmap bitmap = Bitmap.createBitmap(surfaceView.getWidth(), surfaceView.getHeight(), Bitmap.Config.ARGB_8888);

        // Utilizar PixelCopy para capturar el contenido del SurfaceView
        PixelCopy.request(surfaceView, bitmap, copyResult -> {
            // Si la captura es exitosa, proceder a guardar la imagen
            if (copyResult == PixelCopy.SUCCESS) {
                // Crear un directorio de caché para almacenar la imagen temporalmente
                File cachePath = new File(activity.getCacheDir(), "images");
                cachePath.mkdirs(); // Asegurarse de que el directorio exista

                // Crear un archivo para almacenar la captura de pantalla
                File imageFile = new File(cachePath, "screenshot.png");

                // Guardar el bitmap en el archivo como una imagen PNG
                try (FileOutputStream stream = new FileOutputStream(imageFile)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                // Obtener el URI del archivo de imagen guardado para compartir
                Uri contentUri = FileProvider.getUriForFile(activity, activity.getPackageName() + ".fileprovider", imageFile);

                // Crear un intento para compartir la imagen
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("image/png"); // Especificar el tipo de contenido como imagen PNG
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri); // Agregar el URI de la imagen como extra

                // Agregar un texto personalizado al mensaje
                String shareMessage = "¡Mira mi puntuación de " + points + " puntos en el nivel " + level + "! ¿Puedes superarla?";
                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);

                // Otorgar permiso temporal de lectura al URI para que otras aplicaciones puedan acceder a la imagen
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Iniciar un selector para que el usuario elija la aplicación con la que desea compartir la imagen
                activity.startActivity(Intent.createChooser(shareIntent, "Compartir puntuación"));
            }
        }, new Handler(Looper.getMainLooper())); // Ejecutar el proceso de captura en el hilo principal
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
    }



    @Override
    public boolean isRunningOnEmulator() {
        String fingerprint = android.os.Build.FINGERPRINT;
        String model = android.os.Build.MODEL;
        String manufacturer = android.os.Build.MANUFACTURER;
        String brand = android.os.Build.BRAND;
        String device = android.os.Build.DEVICE;
        String product = android.os.Build.PRODUCT;

        return fingerprint.startsWith("generic") || fingerprint.startsWith("unknown") ||
                model.contains("google_sdk") || model.contains("Emulator") || model.contains("Android SDK built for x86") ||
                manufacturer.contains("Genymotion") ||
                (brand.startsWith("generic") && device.startsWith("generic")) ||
                "google_sdk".equals(product);
    }


    private void onShakeDetected() {
        vibrateDevice(500); // Vibrar por 500 ms
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void unregisterSensorListener() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }
}
