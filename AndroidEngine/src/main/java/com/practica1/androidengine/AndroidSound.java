package com.practica1.androidengine;

import com.practica1.engine.Sound; // Importa la interfaz Sound, que este clase implementa

/**
 * Clase que representa un sonido en la plataforma Android. Implementa la interfaz `Sound`.
 * Esta clase se utiliza para manejar los sonidos en el juego, proporcionando una forma de
 * cargar, verificar y gestionar identificadores de sonido.
 */
public class AndroidSound implements Sound {
    private int id; // Identificador único del sonido
    private boolean loaded; // Indica si el sonido está cargado y listo para usarse

    /**
     * Constructor protegido de la clase `AndroidSound`.
     * Se utiliza para inicializar el sonido con un identificador específico.
     * @param soundId El identificador único del sonido.
     */
    protected AndroidSound(int soundId) {
        this.id = soundId; // Asigna el identificador al sonido
        this.loaded = false; // Inicialmente, el sonido no está cargado
    }

    /**
     * Método protegido para obtener el identificador único del sonido.
     * @return El identificador único del sonido.
     */
    protected int getSoundID() {
        return this.id;
    }

    /**
     * Establece el estado de carga del sonido.
     * @param loaded `true` si el sonido está cargado, `false` en caso contrario.
     */
    public void setLoaded(boolean loaded) {
        this.loaded = loaded; // Cambia el estado de carga del sonido
    }

    /**
     * Verifica si el sonido está cargado y listo para ser reproducido.
     * @return `true` si el sonido está cargado, `false` si no lo está.
     */
    public boolean isLoaded() {
        return loaded; // Devuelve el estado actual de carga del sonido
    }
}
