package com.practica1.gamelogic;

import com.practica1.engine.*;
import java.util.List;

// Clase que representa la escena de victoria en el juego.
public class VictoryScene implements State {
    private SceneManager sceneManager; // Gestor de escenas para cambiar entre ellas.
    private Graphics graphics; // Motor gráfico para renderizado.
    private Audio audio; // Motor de audio para efectos de sonido.
    private Sound coinSound; // Sonido al ganar monedas.
    private Sound winSound; // Sonido de victoria
    private Image shareButtonImage; // Imagen del botón de compartir.
    private Image coinImage; // Imagen de las monedas grandes.
    private ColorEnum colors; // Color del fondo de la escena.
    private Image oneCoinImage; // Imagen de una moneda pequeña.
    private Font victoryFont; // Fuente para el texto de "¡Victoria!".
    private Font rewardFont; // Fuente para el texto de recompensa.
    private Font scoreFont; // Fuente para el texto del puntaje y monedas.
    private Image button; // Imagen del botón para volver al menú.
    private float textX, textY; // Coordenadas para los textos.
    private int score; // Puntaje del jugador.
    private boolean isRunningOnEmulator; // Booleano para saber si es AVD o no
    private Mobile mobile; // Objeto para interactuar con el dispositivo móvil.
    private Boolean touchedAd = false; // Indica si se ha tocado el anuncio de recompensa.
    private float alpha = 255; // Opacidad inicial para el efecto de fade-in.
    private boolean fadingOut = false; // Bandera para controlar el fade-out.

    private static final int BUTTON_ARC = 20; // Radio de los bordes redondeados del botón.
    private static final int MARGIN = 20; // Margen entre elementos.
    private static final int rectWidth = 350; // Ancho del rectángulo del botón.
    private static final int rectHeight = 80; // Altura del rectángulo del botón.
    private static final int rectX = 75; // Posición X del botón.
    private static final int rectYTienda = 675; // Posición Y del botón "SIGUIENTE".

    private static final int VICTORYTEXTX = 50; // Coordenada X del texto "¡Victoria!".
    private static final int VICTORYTEXTY = 150; // Coordenada Y del texto "¡Victoria!".
    private static final int COINSTEXTX = 100; // Coordenada X del texto de monedas.
    private static final int COINSTEXTY = 280; // Coordenada Y del texto de monedas.
    private boolean isAdventure; // Indica si la escena pertenece al modo aventura.

    int logicWidth, logicHeight; // Dimensiones lógicas de la pantalla.

    // Constructor de la escena de victoria.
    public VictoryScene(Graphics graphics, Audio audio, int score, Mobile mobile, ColorEnum colors, boolean Adventure) {
        this.sceneManager = SceneManager.getInstance(); // Obtiene la instancia del gestor de escenas.
        this.graphics = graphics; // Inicializa el motor gráfico.
        this.audio = audio; // Inicializa el motor de audio.
        this.colors = colors; // Color del fondo.
        this.score = score; // Puntaje obtenido.
        this.mobile = mobile; // Inicializa el dispositivo móvil.
        isAdventure = Adventure; // Establece si es modo aventura.
        //this.isRunningOnEmulator = this.mobile.isRunningOnEmulator();
        // Carga las imágenes necesarias.
        this.shareButtonImage = graphics.newImage("share.png");
        this.coinImage = graphics.newImage("coin.png");
        this.oneCoinImage = graphics.newImage("1coin.png");

        // Carga las fuentes.
        rewardFont = graphics.newFont("fff.ttf", 24, true, true);
        victoryFont = graphics.newFont("blow.ttf", 95, true, true);
        scoreFont = graphics.newFont("fff.ttf", 40, false, false);

        this.button = graphics.newImage("menu.png"); // Botón para volver al menú.

        // Avanza el nivel si es el último jugado.
        if (sceneManager.getLastLevelPlayed() == sceneManager.getLevel()) sceneManager.addLevel(1);

        // Carga el sonido de monedas.
        coinSound = audio.newSound("retroCoin.wav");
        winSound = audio.newSound("win1.wav");

        // Añade monedas por ganar.
        sceneManager.addCoins(1);

        // Obtiene las dimensiones lógicas de la pantalla.
        logicWidth = sceneManager.getLogicWidth();
        logicHeight = sceneManager.getLogicHeight();

        // Vibra el dispositivo al iniciar la escena.
        if(!isRunningOnEmulator) {
            this.mobile.vibrateDevice(100);
        }
    }

    @Override
    public void update(double deltaTime) {
        // Controla la opacidad para los efectos de fade-in y fade-out.
        if (fadingOut) {
            alpha += 100 * deltaTime; // Incrementa la opacidad (fade-in).
            if (alpha >= 255) {
                alpha = 255;
                sceneManager.setCurrentScene(new IntroScene(sceneManager.getEngine(), mobile)); // Cambia a la escena de introducción.
            }
        } else {
            alpha -= 100 * deltaTime; // Reduce la opacidad (fade-out).
            if (alpha <= 0) {
                alpha = 0;
            }
        }
    }

    @Override
    public void render(Graphics graphics) {
        // Establece el color de fondo según el color seleccionado.
        graphics.setColor(graphics.newColor(120, this.colors.getR(), this.colors.getG(), this.colors.getB()));
        graphics.fillRectangle(-20, -80, sceneManager.getLogicWidth() + 40, sceneManager.getLogicHeight() + 120);

        // Renderiza el texto "¡Victoria!".
        graphics.setFont(victoryFont);
        graphics.setColor(graphics.newColor(255, 0, 0, 0));
        graphics.drawText("¡Victoria!", victoryFont, VICTORYTEXTX, VICTORYTEXTY);

        // Renderiza el botón de compartir y los textos de puntaje y monedas.
        graphics.drawImageWithScale(shareButtonImage, 300, logicHeight - 250, 0.1f, 0.1f);
        graphics.drawText("SCORE: " + score, scoreFont, COINSTEXTX, COINSTEXTY + 90);
        graphics.drawText("COINS: " + sceneManager.getCoins(), scoreFont, COINSTEXTX, COINSTEXTY + 170);

        if (!touchedAd) {
            graphics.drawText("+ 1", rewardFont, 30, logicHeight - 265);
            graphics.drawImageWithScale(oneCoinImage, 35, logicHeight - 265, 0.02f, 0.02f);
            graphics.drawImageWithScale(coinImage, 50, logicHeight - 265, 0.15f, 0.15f);
        }

        // Renderiza el botón "MENU".
        graphics.setColor(graphics.newColor(105, this.colors.getR(), this.colors.getG(), this.colors.getB()));
        graphics.fillRoundRectangle(80, logicHeight / 2, 350, 75, 50, 50);
        graphics.setColor(graphics.newColor(255, 0, 0, 0));
        graphics.drawText("MENU", scoreFont, logicWidth / 3, 560);

        // Renderiza el botón "SIGUIENTE" si es modo aventura.
        if(isAdventure) {
            if (sceneManager.lastLevelPlayed != sceneManager.WORLDSIZE[sceneManager.NMUNDOS - 1]){
                graphics.setColor(graphics.newColor(105, this.colors.getR(), this.colors.getG(), this.colors.getB())); // Set color for rectangle
                graphics.fillRoundRectangle(80, 600, 350, 75, 50, 50);
                graphics.setColor(graphics.newColor(255, 0, 0, 0));
                graphics.drawText("SIGUIENTE", scoreFont, 120, 660);
            }
            else { // si es el ultimo nivel no se muestra siguiente
                graphics.setColor(graphics.newColor(255, 0, 0, 0));
                graphics.drawText("FIN DEL JUEGO", scoreFont, 60, 660);
            }
        }

        // Renderiza el efecto de fade-in/out.
        graphics.setColor(graphics.newColor((int) alpha, 0, 0, 0));
        graphics.fillRectangle(-20, -80, logicWidth + 40, logicHeight + 120);
    }

    @Override
    public void handleInput(List<TouchEvent> events) {
        for (TouchEvent event : events) {
            if (event.type == TouchEvent.TouchEventType.TOUCH_DOWN) {
                // Verifica si se tocó el botón "MENU".
                if (isTouchOnRectangle(event.x, event.y)) {
                    sceneManager.setCurrentScene(new IntroScene(sceneManager.getEngine(), this.mobile, this.colors));
                }
                // Verifica si se tocó el botón "SIGUIENTE".
                else if (event.x >= 90 && event.x <= (90 + 350) &&
                        event.y >= 600 && event.y <= (600 + 75) &&
                        sceneManager.lastLevelPlayed != sceneManager.WORLDSIZE[sceneManager.NMUNDOS - 1]) {
                    if (isAdventure) {
                        if (sceneManager.getLevel() == sceneManager.lastLevelPlayed) sceneManager.addLevel(1);
                        sceneManager.setLastLevelPlayed(sceneManager.getLastLevelPlayed() + 1);
                        sceneManager.levelOnCourse = false;
                        sceneManager.setCurrentScene(new Grid(graphics, audio, mobile, this.colors));
                    }
                }
                // Verifica si se tocó el botón de compartir.
                else if (isTouchOnShareButton(event.x, event.y)) {
                    mobile.shareImage(sceneManager.lastLevelPlayed, score);
                    //mobile.shareMessage("Puzzle Booble!", "Score: " + sceneManager.getCoins());
                }
                // Verifica si se tocó el icono de monedas.
                else if (isTouchingCoin(event.x, event.y)) {
                    mobile.showRewardedAd(new RewardListener() {
                        @Override
                        public void onReward() {
                            sceneManager.addCoins(1);
                            audio.playSound(coinSound, false);
                            touchedAd = true;
                        }
                    });
                }
            }
        }
    }



    // Método para verificar si se tocó el icono de monedas.
    private boolean isTouchingCoin(int touchX, int touchY) {
        if (touchedAd) return false;
        float scaleFactor = 0.15f;
        float imageWidth = coinImage.getWidth() * scaleFactor;
        float imageHeight = coinImage.getHeight() * scaleFactor;
        float imagePosX = 50;
        float imagePosY = 750;
        return touchX >= imagePosX && touchX <= (imagePosX + imageWidth) &&
                touchY >= imagePosY && touchY <= (imagePosY + imageHeight);
    }

    // Método para verificar si se tocó el botón de compartir.
    private boolean isTouchOnShareButton(int touchX, int touchY) {
        float scaleFactor = 0.1f;
        int shareButtonWidth = (int) (shareButtonImage.getWidth() * scaleFactor);
        int shareButtonHeight = (int) (shareButtonImage.getHeight() * scaleFactor);
        int shareButtonX = 300;
        int shareButtonY = 750;
        return touchX >= shareButtonX && touchX <= (shareButtonX + shareButtonWidth) &&
                touchY >= shareButtonY && touchY <= (shareButtonY + shareButtonHeight);
    }

    // Método para verificar si se tocó el botón "MENU".
    private boolean isTouchOnRectangle(int touchX, int touchY) {
        float imageWidth = button.getWidth() * 0.3f;
        float imageHeight = button.getHeight() * 0.3f;
        float imagePosX = 60;
        float imagePosY = 500;
        return touchX >= imagePosX && touchX <= (imagePosX + imageWidth) &&
                touchY >= imagePosY && touchY <= (imagePosY + imageHeight);
    }

    // Devuelve el ancho
    @Override
    public int getW() {
        return logicWidth;
    }

    // Devuelve el alto
    @Override
    public int getH() {
        return logicHeight;
    }
}
