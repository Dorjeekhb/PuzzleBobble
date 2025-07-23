package com.practica1.desktopengine;

import com.practica1.engine.Audio;
import com.practica1.engine.Engine;
import com.practica1.engine.Graphics;
import com.practica1.engine.Input;
import com.practica1.engine.State;
import com.practica1.engine.TouchEvent;

import java.util.List;
import javax.swing.JFrame;

public class DesktopEngine implements Runnable, Engine {

    private final JFrame myView;
    private DesktopGraphics gr;
    private DesktopInput input;
    private DesktopAudio audio;
    private Thread renderThread;
    private volatile boolean running;
    private State state;
    private static final int TARGET_FPS = 60;
    private static final double TIME_PER_FRAME = 1_000_000_000.0 / TARGET_FPS;

    public DesktopEngine(JFrame myView) {
        this.myView = myView;
        this.gr = new DesktopGraphics(myView);
        this.input = new DesktopInput();
        this.audio = new DesktopAudio();  // Inicializar el sistema de audio
        myView.addMouseListener(input);
        myView.addMouseMotionListener(input);
    }

    @Override
    public void run() {
        if (renderThread != Thread.currentThread()) {
            throw new RuntimeException("run() should not be called directly");
        }

        while (this.running && gr.getWidth() == 0);

        long lastFrameTime = System.nanoTime();
        long informePrevio = lastFrameTime;
        int frames = 0;

        while (running) {
            long currentTime = System.nanoTime();
            long nanoElapsedTime = currentTime - lastFrameTime;
            lastFrameTime = currentTime;

            double elapsedTime = (double) nanoElapsedTime / 1.0E9;

            if (this.state != null) {
                List<TouchEvent> events = this.input.getTouchEvents();
                for (TouchEvent e : events) {
                    realToLogic(e);
                }

                state.handleInput(events);
                state.update(elapsedTime);
            }

            do {
                gr.prepareFrame(state.getW(), state.getH());
                state.render(gr);
            } while (!gr.endFrame());

            frames++;

            if (currentTime - informePrevio > 1_000_000_000L) {
                long fps = frames * 1_000_000_000L / (currentTime - informePrevio);
                //System.out.println("" + fps + " fps");
                frames = 0;
                informePrevio = currentTime;
            }

            long frameTime = System.nanoTime() - currentTime;
            if (frameTime < TIME_PER_FRAME) {
                try {
                    Thread.sleep((long) ((TIME_PER_FRAME - frameTime) / 1_000_000.0));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void realToLogic(TouchEvent e) {
        int w = gr.getWidth();
        int h = gr.getHeight();
        float wProportion = (float) w / state.getW();
        float hProportion = (float) h / state.getH();

        // Determinar la escala mínima para que el contenido se ajuste
        float scale = (wProportion > hProportion) ? hProportion : wProportion;

        // Calcular el offset para centrar el contenido
        int offsetX = (int) ((w - (state.getW() * scale)) / 2);
        int offsetY = (int) ((h - (state.getH() * scale)) / 2);

        // Convierte las coordenadas ajustadas a coordenadas lógicas
        e.x = (int) ((e.x - offsetX) / scale);
        e.y = (int) ((e.y - offsetY) / scale);

        // Imprime para depurar
        //System.out.println("Converted to logical X: " + e.x + ", Y = " + e.y);
    }


    public void setState(State scene) {
        this.state = scene;
    }

    public void resume() {
        if (!this.running) {
            this.running = true;
            this.renderThread = new Thread(this);
            this.renderThread.start();
        }
    }

    public void pause() {
        if (this.running) {
            this.running = false;
            while (true) {
                try {
                    this.renderThread.join();
                    this.renderThread = null;
                    break;
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
    }

    @Override
    public Graphics getGraphics() {
        return gr;
    }

    @Override
    public Input getInput() {
        return input;
    }

    @Override
    public Audio getAudio() {
        return audio;
    }

    @java.lang.Override
    public String getFilePath(String path) {
        return null;
    }

    @java.lang.Override
    public void changeScene(State scene) {

    }

    @Override
    public String getHashFromThis(String data) {
        return "";
    }
}
