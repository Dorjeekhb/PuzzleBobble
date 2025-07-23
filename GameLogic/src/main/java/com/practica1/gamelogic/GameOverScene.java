package com.practica1.gamelogic;

import com.practica1.engine.Graphics;
import com.practica1.engine.Mobile;
import com.practica1.engine.State;
import com.practica1.engine.TouchEvent;
import com.practica1.engine.Audio;
import com.practica1.engine.Font;

import java.util.List;

public class GameOverScene implements State {
    // Referencias
    private SceneManager sceneManager; // referencia a SceneManager
    private Graphics graphics; // referencia a graphics
    private Audio audio; // referencia a audio
    private Mobile mobile; // Añade esta referencia
    int logicHeight, logicWidth; // referencia a altura y anchura

    // Texto
    private Font gameOverFont; // font
    private int textX, textY; // posicion del texto
    private int score; // puntuacion

    // variables fade out
    private float alpha = 255;
    private boolean fadingOut = false;

    // color de fondo
    private ColorEnum selectedBackgroundColor;

    public GameOverScene(Graphics graphics, Audio audio, int score, Mobile mobile, ColorEnum backgroundColor) {
        // asignacion de variables
        this.sceneManager = SceneManager.getInstance();
        this.graphics = graphics;
        this.audio = audio;
        this.score = score;
        this.mobile = mobile;
        this.selectedBackgroundColor = backgroundColor; // Store the color
        logicHeight = sceneManager.getLogicHeight();
        logicWidth = sceneManager.getLogicWidth();
        gameOverFont = graphics.newFont("fff.ttf", 48, true, true);
        textX = 50;
        textY = 300;

        SceneManager.getInstance().addCoins(-5); // se restan monedas al haber perdido
    }

    @Override
    public void update(double deltaTime) {
        if (fadingOut) { // fade
            alpha += (float) (100 * deltaTime);
            if (alpha >= 255) {
                alpha = 255;
                fadingOut = false; // Reset fadingOut para permitir nuevas recompensas
            }
        } else {
            alpha -= (float) (100 * deltaTime);
            if (alpha <= 0) {
                alpha = 0;
            }
        }
    }

    @Override
    public void render(Graphics graphics) {
        // Fondo
        graphics.setColor(graphics.newColor(120, selectedBackgroundColor.getR(), selectedBackgroundColor.getG(), selectedBackgroundColor.getB()));
        graphics.fillRectangle(-20, -80, sceneManager.getLogicWidth()+40, sceneManager.getLogicHeight()+120);

        // Texto "GAME OVER!"
        graphics.setFont(gameOverFont);
        graphics.setColor(graphics.newColor(255, 0, 0, 0));
        graphics.drawText("GAME OVER!", gameOverFont, textX, textY);

        // Puntuación
        graphics.drawText("SCORE: " + score, gameOverFont, textX, textY + 100);

        // Botón "Retry" con bordes redondeados mucho más abajo
        int retryButtonX = sceneManager.getLogicWidth() / 2 - 125; // Centrado horizontalmente
        int retryButtonY = sceneManager.getLogicHeight() - 150; // Mucho más abajo
        int retryButtonWidth = 250; // Ajustar ancho
        int retryButtonHeight = 80; // Ajustar alto
        int arcWidth = 30; // Redondeo en esquinas
        int arcHeight = 30;
        graphics.setColor(graphics.newColor(255, this.selectedBackgroundColor.getR(),
                this.selectedBackgroundColor.getG(), this.selectedBackgroundColor.getB()));
        graphics.fillRoundRectangle(retryButtonX, retryButtonY, retryButtonWidth, retryButtonHeight, arcWidth, arcHeight);

        // Texto del botón "Retry"
        String retryText = "Retry";
        graphics.setColor(graphics.newColor(255, 0, 0, 0)); // Negro
        int retryTextX = retryButtonX + retryButtonWidth / 2 - 50; // Centrando el texto horizontalmente
        int retryTextY = retryButtonY + retryButtonHeight / 2 + 25; // Centrando verticalmente
        graphics.drawText(retryText, gameOverFont, retryTextX - 40, retryTextY);

        // Botón "Menu" mucho más abajo
        int menuButtonX = 50; // Botón de menú existente
        int menuButtonY = SceneManager.getInstance().getLogicHeight() - 400; // Mucho más abajo
        int menuButtonWidth = 400;
        int menuButtonHeight = 120;
        int arcWidthMenu = 40;
        int arcHeightMenu = 40;
        graphics.setColor(graphics.newColor(255, this.selectedBackgroundColor.getR(), this.selectedBackgroundColor.getG(), this.selectedBackgroundColor.getB())); // Fondo amarillo
        graphics.fillRoundRectangle(menuButtonX, menuButtonY, menuButtonWidth, menuButtonHeight, arcWidthMenu, arcHeightMenu);

        // Texto del boton "MENU"
        String buttonText = "MENU";
        graphics.setColor(graphics.newColor(255, 0, 0, 0)); // Texto negro
        int menuTextX = menuButtonX + menuButtonWidth / 2 - 90; // Centrado horizontalmente
        int menuTextY = menuButtonY + menuButtonHeight / 2 + 15; // Centrando verticalmente
        graphics.drawText(buttonText, gameOverFont, menuTextX, menuTextY + 10);

        // Draw a fade effect
        graphics.setColor(graphics.newColor((int) alpha, 0, 0, 0));
        graphics.fillRectangle(-20, -80, sceneManager.getLogicWidth()+40, sceneManager.getLogicHeight()+120);
    }

    @Override
    public void handleInput(List<TouchEvent> events) {
        for (TouchEvent event : events) {
            if (event.type == TouchEvent.TouchEventType.TOUCH_DOWN) {
                if (isTouchOnMenu(event.x, event.y)) { // vuelta al menu
                    sceneManager.setCurrentScene(new IntroScene(SceneManager.getInstance().getEngine(), mobile, this.selectedBackgroundColor));
                }
                else if(isTouchOnRetry(event.x, event.y)){ // reiniciar nivel
                    retryLevel();
                }
            }
        }
    }

    // se crea una escena grid con el mismo nivel que habia
    private void retryLevel() {
        SceneManager.getInstance().setCurrentScene(new Grid(graphics, audio, mobile, this.selectedBackgroundColor));
    }

    // devuelve si se ha pulsado en el boton retry
    private boolean isTouchOnRetry(int touchX, int touchY) {
        // tamaños y coordenadas del boton
        int retryButtonX = SceneManager.getInstance().getLogicWidth() / 2 - 125;
        int retryButtonY = SceneManager.getInstance().getLogicHeight() - 150; // Nuevo Y
        int retryButtonWidth = 250;
        int retryButtonHeight = 80;

        // Verifica si las coordenadas del toque están dentro de los límites del botón "Retry"
        return touchX >= retryButtonX && touchX <= (retryButtonX + retryButtonWidth) &&
                touchY >= retryButtonY && touchY <= (retryButtonY + retryButtonHeight);
    }

    // Método para comprobar si toca el botón que le lleva la menú
    private boolean isTouchOnMenu(int touchX, int touchY) {
        // Coordenadas y dimensiones del botón "Menu" definidas en el método render
        int menuButtonX = 50; // Botón de menú existente
        int menuButtonY = SceneManager.getInstance().getLogicHeight() - 400; // Mucho más abajo
        int menuButtonWidth = 400;
        int menuButtonHeight = 120;

        // Verifica si las coordenadas del toque están dentro de los límites del botón "Menu"
        return touchX >= menuButtonX && touchX <= (menuButtonX + menuButtonWidth) &&
                touchY >= menuButtonY && touchY <= (menuButtonY + menuButtonHeight);
    }

    @Override
    public int getW() {
        return logicWidth; // Ancho fijo
    }

    @Override
    public int getH() {
        return logicHeight; // Alto fijo
    }
}
