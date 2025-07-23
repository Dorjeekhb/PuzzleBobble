package com.practica1.androidengine;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceView;

import com.practica1.engine.Audio;
import com.practica1.engine.Engine;
import com.practica1.engine.File;
import com.practica1.engine.Graphics;
import com.practica1.engine.State;
import com.practica1.engine.TouchEvent;

import java.io.IOException;
import java.util.List;

/**
 * Clase AndroidEngine que implementa el motor de juego.
 * Esta clase centraliza la gestión de gráficos, entrada, audio y estados del juego.
 */
public class AndroidEngine implements Runnable, Engine {

    private SurfaceView surfaceView; // Vista para renderizar
    private State state; // Estado actual del juego
    private SurfaceView renderView; // Vista para dibujar gráficos
    private AndroidGraphics gr; // Gestión de gráficos
    private AndroidAudio audio; // Gestión de audio
    private AndroidInput input; // Gestión de entrada táctil
    private Thread renderThread; // Hilo para el bucle de renderizado
    private volatile boolean running; // Bandera para controlar el estado del bucle de renderizado
    private Context context; // Contexto de la aplicación
    private Canvas canvas; // Canvas para dibujar
    private List<TouchEvent> events; // Lista de eventos táctiles

    /**
     * Constructor de AndroidEngine.
     * @param renderView Vista para renderizar.
     * @param context Contexto de la aplicación.
     */
    public AndroidEngine(SurfaceView renderView, Context context) {
        this.context = context;
        this.renderView = renderView;
        this.gr = new AndroidGraphics(renderView, context); // Inicializa el motor gráfico
        this.input = new AndroidInput(gr); // Inicializa la entrada táctil
        this.renderView.setOnTouchListener(this.input); // Vincula el manejador de entrada táctil
        this.renderView.setClickable(true);
        this.renderView.setFocusable(true);
        this.audio = new AndroidAudio(context.getAssets()); // Inicializa el motor de audio
    }

    /** Métodos de la interfaz Engine **/

    @Override
    public Graphics getGraphics() {
        return gr;
    }

    @Override
    public Audio getAudio() {
        return audio;
    }

    @Override
    public void changeScene(State scene) {
        state = scene; // Cambia el estado actual del juego
    }

    @Override
    public File getAssetsFile(String path) {
        return new AndroidFile(path, context, false); // Obtiene un archivo desde los assets
    }

    @Override
    public File getInternalFile(String path) {
        return new AndroidFile(path, context, true); // Obtiene un archivo desde almacenamiento interno
    }

    // Método que lista los archivos dentro de un directorio en los assets.
    @Override
    public String[] getAssetsList(String path) {
        try {
            // Retorna la lista de archivos ubicada en la ruta especificada dentro de los assets.
            return context.getAssets().list(path);
        } catch (IOException e) {
            // Registra un error si ocurre un problema al listar los archivos.
            Log.e("LevelLoader", "Error al cargar mundos y niveles: " + e.getMessage());
            return null;
        }
    }

    /**
     * Método principal del bucle de renderizado.
     */
    @Override
    public void run() {
        if (renderThread != Thread.currentThread()) {
            throw new RuntimeException("run() should not be called directly");
        }

        // Espera activa hasta que la vista esté lista para dibujar
        while (this.running && this.renderView.getWidth() == 0);

        long lastFrameTime = System.nanoTime();
        long informePrevio = lastFrameTime;
        int frames = 0;

        canvas = gr.getCanvas();

        while (running) {
            long currentTime = System.nanoTime();
            long nanoElapsedTime = currentTime - lastFrameTime;
            lastFrameTime = currentTime;

            double elapsedTime = (double) nanoElapsedTime / 1.0E9; // Convierte a segundos

            if (this.state != null) {
                List<TouchEvent> events = this.input.getTouchEvents(); // Obtiene eventos táctiles
                for (TouchEvent e : events) {
                    realToLogic(e); // Convierte coordenadas físicas a lógicas
                }

                state.handleInput(events); // Maneja entrada
                state.update(elapsedTime); // Actualiza el estado
            }

            if (currentTime - informePrevio > 1_000_000_000L) {
                long fps = frames * 1_000_000_000L / (currentTime - informePrevio); // Calcula FPS
                frames = 0;
                informePrevio = currentTime;
            }
            ++frames;

            // Renderiza el estado actual
            gr.prepareFrame(state.getW(), state.getH());
            state.render(gr);
            gr.endFrame();
        }
    }

    /** Métodos adicionales **/

    /**
     * Detiene el motor y libera recursos.
     */
    public void onStop() {
        if (this.running) {
            this.running = false;
            try {
                if (this.renderThread != null) {
                    this.renderThread.join();
                    this.renderThread = null;
                }
            } catch (InterruptedException e) {
                Log.e("AndroidEngine", "Error al detener el hilo de renderizado: " + e.getMessage());
            }
        }

        if (canvas != null) {
            canvas = null;
        }

        if (gr != null) {
            gr.cleanup(); // Método cleanup para liberar recursos en AndroidGraphics
        }

        Log.i("AndroidEngine", "Motor detenido y recursos liberados correctamente.");
    }

    /**
     * Convierte coordenadas físicas a coordenadas lógicas.
     * @param e Evento táctil a convertir.
     */
    private void realToLogic(TouchEvent e) {
        int w = gr.getWidth();
        int h = gr.getHeight();
        float wProportion = (float) w / state.getW();
        float hProportion = (float) h / state.getH();

        // Escala mínima para ajustar contenido
        float scale = (wProportion > hProportion) ? hProportion : wProportion;

        // Offset para centrar contenido
        int offsetX = (int) ((w - (state.getW() * scale)) / 2);
        int offsetY = (int) ((h - (state.getH() * scale)) / 2);

        // Conversión de coordenadas
        e.x = (int) ((e.x - offsetX) / scale);
        e.y = (int) ((e.y - offsetY) / scale);
    }

    /**
     * Pausa el motor.
     */
    public void pause() {
        if (this.running) {
            this.running = false;
            while (true) {
                try {
                    this.renderThread.join();
                    this.renderThread = null;
                    break;
                } catch (InterruptedException ie) {
                    // No debería ocurrir
                }
            }
        }
    }

    /**
     * Reanuda el motor.
     */
    public void resume() {
        if (!this.running) {
            this.running = true;
            this.renderThread = new Thread(this);
            this.renderThread.start();
        }
    }

    /**
     * Método nativo para calcular un hash.
     * @param data Cadena a procesar.
     * @return Hash calculado.
     */
}
