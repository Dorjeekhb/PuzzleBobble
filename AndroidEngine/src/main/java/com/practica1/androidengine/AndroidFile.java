package com.practica1.androidengine;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.practica1.engine.File;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * Clase AndroidFile que implementa la interfaz File.
 * Se utiliza para manejar archivos en Android, ya sea desde los assets (archivos externos)
 * o desde el almacenamiento interno de la aplicación.
 */
public class AndroidFile implements File {

    private String path; // Ruta relativa del archivo
    private Context context; // Contexto de la aplicación, necesario para acceder a archivos y recursos
    private boolean internal; // Indica si el archivo se maneja desde almacenamiento interno o assets

    /**
     * Constructor de la clase AndroidFile.
     * @param filePath Ruta relativa del archivo.
     * @param fileContext Contexto de la aplicación para acceder a recursos y almacenamiento.
     * @param internalFile `true` si el archivo está en almacenamiento interno, `false` si está en los assets.
     */
    AndroidFile(String filePath, Context fileContext, boolean internalFile) {
        path = filePath; // Asigna la ruta del archivo
        context = fileContext; // Asigna el contexto de la aplicación
        internal = internalFile; // Indica el tipo de almacenamiento del archivo
    }

    /**
     * Obtiene el contenido del archivo como una cadena.
     * Si el archivo está en los assets, lo lee directamente.
     * Si está en almacenamiento interno, usa un método especializado.
     * @return Contenido del archivo como cadena o `null` si ocurre un error.
     */
    @Override
    public String getContent() {
        if (!internal) { // Si el archivo está en los assets
            AssetManager assetManager = context.getAssets();
            try (InputStream is = assetManager.open(path)) {
                int size = is.available(); // Obtiene el tamaño del archivo
                byte[] buffer = new byte[size]; // Crea un buffer para leer los datos
                is.read(buffer); // Lee los datos en el buffer
                return new String(buffer, StandardCharsets.UTF_8); // Devuelve los datos como cadena
            } catch (IOException e) {
                Log.e("Archivo", "Error al leer el archivo: " + e.getMessage());
                return null;
            }
        } else { // Si el archivo está en almacenamiento interno
            return readJsonFromInternalStorage();
        }
    }

    /**
     * Establece el contenido del archivo, escribiendo datos en él.
     * Escribe en almacenamiento interno o en archivos generales según el indicador `internal`.
     * @param data Cadena de texto a escribir en el archivo.
     */
    @Override
    public void setContent(String data) {
        if (!internal) { // Si el archivo no es interno
            java.io.File file = new java.io.File(context.getFilesDir(), path);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(data.getBytes(StandardCharsets.UTF_8)); // Escribe los datos en UTF-8
                fos.flush(); // Asegura que los datos se escriban en el archivo
                Log.d("Archivo", "Contenido escrito correctamente en el archivo: " + file.getAbsolutePath());
            } catch (IOException e) {
                Log.e("Archivo", "Error al escribir en el archivo: " + e.getMessage());
            }
        } else { // Si el archivo es interno
            saveJsonToInternalStorage(data);
        }
    }

    /**
     * Guarda una cadena JSON en almacenamiento interno.
     * @param jsonString Cadena de texto JSON a guardar.
     */
    private void saveJsonToInternalStorage(String jsonString) {
        try {
            FileOutputStream fos = context.openFileOutput(path, Context.MODE_PRIVATE);
            fos.write(jsonString.getBytes()); // Escribe los datos en el archivo
            fos.close(); // Cierra el archivo
            Log.d("JsonSave", "JSON saved to internal storage.");
        } catch (IOException e) {
            Log.e("JsonSave", "Error saving JSON to internal storage", e);
        }
    }

    /**
     * Lee una cadena JSON desde el almacenamiento interno.
     * @return Cadena JSON leída o `null` si ocurre un error.
     */
    private String readJsonFromInternalStorage() {
        try {
            FileInputStream fis = context.openFileInput(path);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;

            // Lee línea por línea y construye la cadena completa
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            fis.close(); // Cierra el archivo
            Log.d("JsonRead", "JSON read from internal storage: " + sb.toString());
            return sb.toString(); // Devuelve la cadena JSON completa
        } catch (FileNotFoundException e) {
            Log.d("JsonRead", "JSON file not found. This may be the first run."); // Archivo no encontrado (primera ejecución)
            return null;
        } catch (IOException e) {
            Log.e("JsonRead", "Error reading JSON from internal storage", e); // Error de lectura
            return null;
        }
    }
}
