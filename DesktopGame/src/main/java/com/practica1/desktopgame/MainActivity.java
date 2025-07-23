package com.practica1.desktopgame;

import com.practica1.desktopengine.DesktopEngine;
import com.practica1.engine.State;
import com.practica1.gamelogic.IntroScene;
import com.practica1.gamelogic.Scene;

import javax.swing.JFrame;

public class MainActivity {

    public static void main(String[] args) {
        JFrame renderView = new JFrame("Bubble");

        renderView.setSize(500, 1000);
        renderView.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        renderView.setIgnoreRepaint(true);
        renderView.setVisible(true);

        // Intentamos crear el buffer strategy con 2 buffers.
        int intentos = 100;
        while (intentos-- > 0) {
            try {
                renderView.createBufferStrategy(2);
                break;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (intentos == 0) {
            System.err.println("No pude crear la BufferStrategy");
            return;
        }

        DesktopEngine engine = new DesktopEngine(renderView);
        State scene = new Scene(engine); // Pasar el DesktopEngine a la escena
        engine.setState(scene);
        engine.resume();
    }
}
