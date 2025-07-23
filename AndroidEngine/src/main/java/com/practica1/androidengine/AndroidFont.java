package com.practica1.androidengine;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import com.practica1.engine.Font;

/**
 * Clase AndroidFont que implementa la interfaz Font.
 * Esta clase se utiliza para manejar fuentes en el motor del juego, permitiendo personalizar
 * características como tamaño, estilo (negrita o cursiva) y la fuente específica cargada desde los assets.
 */
public class AndroidFont implements Font {
    private Typeface tfont; // Objeto Typeface que representa la fuente
    private int size; // Tamaño de la fuente
    private String filename; // Nombre del archivo de la fuente
    boolean isbold; // Indica si la fuente es en negrita
    boolean isitalicM; // Indica si la fuente es en cursiva

    /**
     * Constructor de AndroidFont que inicializa la fuente con valores predeterminados (sin negrita ni cursiva).
     * @param aM AssetManager utilizado para cargar archivos desde los recursos de la aplicación.
     * @param file Nombre del archivo de la fuente (ubicado en los assets).
     * @param size Tamaño de la fuente.
     */
    AndroidFont(AssetManager aM, String file, int size) {
        this(aM, file, size, false, false); // Llama al constructor principal con negrita y cursiva como `false`.
    }

    /**
     * Constructor principal de AndroidFont que permite especificar estilos de fuente.
     * @param assets AssetManager utilizado para cargar archivos desde los recursos de la aplicación.
     * @param file Nombre del archivo de la fuente (ubicado en los assets).
     * @param size Tamaño de la fuente.
     * @param bold `true` si la fuente debe ser negrita, `false` en caso contrario.
     * @param italic `true` si la fuente debe ser cursiva, `false` en caso contrario.
     */
    AndroidFont(AssetManager assets, String file, int size, boolean bold, boolean italic) {
        // Carga la fuente desde el archivo especificado en los assets
        this.tfont = Typeface.createFromAsset(assets, file);
        this.size = size; // Asigna el tamaño de la fuente
        this.filename = file; // Guarda el nombre del archivo de la fuente
        this.isbold = bold; // Indica si la fuente es negrita
        this.isitalicM = italic; // Indica si la fuente es cursiva

        // Aplica estilos adicionales según los valores de `bold` y `italic`
        if (bold && italic) {
            // Combina negrita y cursiva
            this.tfont = Typeface.create(tfont, Typeface.BOLD_ITALIC);
        } else if (bold) {
            // Solo negrita
            this.tfont = Typeface.create(tfont, Typeface.BOLD);
        } else if (italic) {
            // Solo cursiva
            this.tfont = Typeface.create(tfont, Typeface.ITALIC);
        }
    }

    /**
     * Devuelve el tamaño de la fuente.
     * @return Tamaño de la fuente.
     */
    @Override
    public int getSize() {
        return this.size;
    }

    /**
     * Devuelve el objeto Typeface asociado con esta fuente.
     * @return Objeto Typeface que representa la fuente.
     */
    public Typeface getFont() {
        return this.tfont;
    }
}
