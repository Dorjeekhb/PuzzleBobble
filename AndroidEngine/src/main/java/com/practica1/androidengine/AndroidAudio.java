package com.practica1.androidengine;


import android.content.res.AssetManager;
import android.media.SoundPool;
import android.content.res.AssetFileDescriptor;

import com.practica1.engine.Audio;
import com.practica1.engine.Sound;

import java.io.IOException;

/**
 * Clase AndroidAudio que implementa la interfaz Audio.
 * Se encarga de manejar la carga y reproducción de sonidos en el motor de juego utilizando `SoundPool`.
 */
public class AndroidAudio implements Audio {

    private AssetManager assetManager; // Manejador de los archivos de assets
    private SoundPool soundPool; // Manejador de sonidos para reproducción eficiente
    private final String root = "sounds/"; // Ruta raíz donde se encuentran los archivos de sonido
    private final float MAX_VOLUME = 1.0f; // Volumen máximo para los sonidos

    /**
     * Constructor protegido de AndroidAudio.
     * Inicializa el `SoundPool` y asigna el `AssetManager` para cargar sonidos desde los assets.
     * @param assetManager AssetManager para acceder a los archivos de sonido.
     */
    protected AndroidAudio(AssetManager assetManager) {
        this.assetManager = assetManager;

        // Crea un SoundPool con un máximo de 10 flujos simultáneos
        this.soundPool = new SoundPool.Builder()
                .setMaxStreams(10) // Define el número máximo de flujos de sonido
                .build();
    }

    /**
     * Carga un nuevo sonido desde los assets.
     * @param file Nombre del archivo de sonido dentro del directorio `sounds/`.
     * @return Objeto Sound asociado al archivo cargado.
     */
    @Override
    public Sound newSound(String file) {
        AndroidSound sound = null; // Objeto para almacenar el sonido cargado
        int soundID = -1; // Identificador del sonido

        try {
            // Obtiene un descriptor del archivo en los assets
            AssetFileDescriptor assetDescriptor = this.assetManager.openFd(root + file);

            // Carga el sonido en el SoundPool y obtiene su ID
            soundID = soundPool.load(assetDescriptor, 1);

            // Crea un objeto AndroidSound asociado al ID
            sound = new AndroidSound(soundID);
        } catch (IOException e) {
            // Lanza una excepción si el sonido no se puede cargar
            throw new RuntimeException("No se pudo cargar el sonido " + file);
        }

        return sound; // Devuelve el objeto Sound
    }

    /**
     * Reproduce un sonido.
     * @param s Objeto Sound a reproducir.
     * @param loop Si es `true`, el sonido se reproducirá en bucle; si es `false`, no.
     */
    @Override
    public void playSound(Sound s, boolean loop) {
        int loopFlag = loop ? -1 : 0; // -1 para bucle infinito, 0 para no repetir

        // Reproduce el sonido utilizando el ID asociado
        int id = this.soundPool.play(
                ((AndroidSound) s).getSoundID(), // ID del sonido
                MAX_VOLUME, // Volumen en el canal izquierdo
                MAX_VOLUME, // Volumen en el canal derecho
                1, // Prioridad del sonido
                loopFlag, // Indica si debe repetirse
                1 // Velocidad de reproducción (1 = normal)
        );
    }

    /**
     * Detiene un sonido en reproducción.
     * @param s Objeto Sound a detener.
     */
    @Override
    public void stopSound(Sound s) {
        this.soundPool.stop(((AndroidSound) s).getSoundID());
    }

    /**
     * Silencia todos los sonidos del SoundPool.
     */
    @Override
    public void mute() {
        soundPool.setVolume(0, 0, 0); // Establece el volumen a 0 para todos los sonidos
    }
}
