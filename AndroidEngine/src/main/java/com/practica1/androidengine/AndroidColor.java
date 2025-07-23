package com.practica1.androidengine;

import com.practica1.engine.Color;

/**
 * Clase AndroidColor que implementa la interfaz Color.
 * Esta clase se utiliza para manejar colores en el contexto del motor de juego,
 * proporcionando la capacidad de definir colores RGBA y obtenerlos como un valor ARGB.
 */
public class AndroidColor implements Color {

    android.graphics.Color myColor; // Objeto de la clase Color de Android para manejar colores.

    /**
     * Constructor predeterminado de AndroidColor.
     * Inicializa el objeto `myColor` con un color por defecto.
     */
    public AndroidColor() {
        myColor = new android.graphics.Color(); // Inicializa el color
    }

    /**
     * Establece un color utilizando valores RGBA.
     * Convierte los valores proporcionados en el rango [0-255] a un formato flotante [0-1],
     * necesario para crear un objeto `Color` de Android.
     * @param a Canal alfa (transparencia) en el rango [0-255].
     * @param r Valor del canal rojo en el rango [0-255].
     * @param g Valor del canal verde en el rango [0-255].
     * @param b Valor del canal azul en el rango [0-255].
     */
    @Override
    public void setColor(int a, int r, int g, int b) {
        float alpha = a / 255.0f; // Convierte el valor alfa a [0-1]
        float red = r / 255.0f;   // Convierte el valor rojo a [0-1]
        float green = g / 255.0f; // Convierte el valor verde a [0-1]
        float blue = b / 255.0f;  // Convierte el valor azul a [0-1]
        myColor = android.graphics.Color.valueOf(red, green, blue, alpha); // Crea un nuevo color con estos valores
    }

    /**
     * Obtiene el color actual como un valor entero ARGB.
     * @return Color actual representado en formato ARGB (32 bits).
     */
    public int getMyColor() {
        return myColor.toArgb(); // Convierte el color actual a formato ARGB
    }
}
