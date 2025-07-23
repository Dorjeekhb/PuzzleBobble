package com.practica1.gamelogic;

import com.practica1.engine.Audio;
import com.practica1.engine.Color;
import com.practica1.engine.File;
import com.practica1.engine.Font;
import com.practica1.engine.Graphics;
import com.practica1.engine.State;
import com.practica1.engine.TouchEvent;
import com.practica1.engine.Image;
import com.practica1.engine.Mobile;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.util.List;

// Struct de estilo de mundos
class StyleData {
    String colorUnlocked; // color para niveles desbloqueados
    String colorLocked; // color para niveles bloqueados
}

public class Aventura implements State {
    // Referencias
    private SceneManager sceneManager; // referencia a SceneManager
    private Mobile mobile; // referencia a mobile
    private Audio audio; // referencia a audio
    private Graphics graphics; // referencia a graphics
    int logicWidth, logicHeight; // referencia a altura y anchura

    // Representación de niveles
    private Color[] colorUnlocked; // array de colores de niveles desbloqueados
    private Color[] colorLocked; // array de colores de niveles bloqueados
    private int currentLevel; // ultimo nivel desbloqueado
    private int spacingX = 20; // Espaciado en el eje X de las celdas de los niveles
    private int spacingY = 50; // Espaciado en el eje Y de las celdas de los niveles
    private final int cellWidth = 100; // Ancho de cada celda
    private final int cellHeight = 100; // Alto de cada celda

    // numero de filas y de columnas
    private int cols = 3;
    private int rows; // se calculan al crear la escena en funcion del numero de niveles que haya

    // Elementos esteticos
    private Image closeImage; // imagen para volver al menú
    private Image lockImage;
    private Font font; // font empleada en la escena (fff.ttf)
    private ColorEnum selectedBackgroundColor; // color de fondo

    // Variables para el scroll
    private int scrollOffsetY = 0; // Scroll offset
    private int initialTouchY; // Initial touch Y-coordinate
    private final int topMargin = 150; // Ajuste para dejar espacio al texto "Aventura"
    private final int bottomMargin = 100; // Bottom margin
    private final int SCROLL_THRESHOLD = 5; // Minimum distance to trigger scrolling

    // Variables de interpolación
    private int targetScrollOffsetY = 0;  // Desplazamiento objetivo para interpolación
    private final float SMOOTH_SCROLL_FACTOR = 0.3f;  // Factor de suavizado

    // Variables para el delay
    private float timeSinceSceneLoaded = 0f;
    private final float DELAY_TIME = 0.2f;  // delay en segundos
    private boolean canSelectLevel = false;

    public Aventura(Graphics graphics, Audio audio, Mobile mobile, ColorEnum backGroundColor) {
        // asignacion de variables
        this.graphics = graphics;
        this.audio = audio;
        this.sceneManager = SceneManager.getInstance();
        this.mobile = mobile;
        this.selectedBackgroundColor = backGroundColor;
        font = graphics.newFont("fff.ttf", 30, true, true);
        closeImage = graphics.newImage("close.png");
        lockImage = graphics.newImage("lock.png");
        currentLevel = sceneManager.getLevel();
        logicWidth = SceneManager.getInstance().getLogicWidth();
        logicHeight = SceneManager.getInstance().getLogicHeight();

        // calculo de numero de filas necesarias
        rows = sceneManager.WORLDSIZE[sceneManager.NMUNDOS - 1] / cols;
        if(sceneManager.WORLDSIZE[sceneManager.NMUNDOS - 1] % cols > 0) rows++;

        // carga de estilos
        colorLocked = new Color[sceneManager.NMUNDOS];
        colorUnlocked = new Color[sceneManager.NMUNDOS];
        for(int i = 0; i < sceneManager.NMUNDOS; i++){
            loadStyle(i + 1);
        }
    }

    @Override
    public void update(double deltaTime) {
        // breve delay para evitar entrar a niveles directamente
        timeSinceSceneLoaded += deltaTime;
        if (timeSinceSceneLoaded >= DELAY_TIME) canSelectLevel = true;

        // ajusta el desplazamiento actual hacia el desplazamiento objetivo usando un factor de suavizado
        scrollOffsetY += (targetScrollOffsetY - scrollOffsetY) * SMOOTH_SCROLL_FACTOR;

        // Calcula el desplazamiento máximo permitido
        int maxScroll = Math.max(0, (rows * (cellHeight + spacingY)) - (logicHeight - topMargin - bottomMargin));
        // Asegura que el desplazamiento no exceda los límites
        scrollOffsetY = Math.max(0, Math.min(scrollOffsetY, maxScroll));
    }

    @Override
    public void render(Graphics graphics) {
        // fondo de escena
        graphics.setColor(graphics.newColor(120, this.selectedBackgroundColor.getR(),
                this.selectedBackgroundColor.getG(), this.selectedBackgroundColor.getB()));
        graphics.fillRectangle(-20, -20, logicWidth+40, logicHeight + 40);

        // texto "Aventura" en la parte superior
        graphics.setColor(graphics.newColor(255, 0, 0, 0));
        graphics.drawText("Aventura", font, 150, 75);

        renderLevelGrid(graphics); // dibujado de las celdas

        // imagen de vuelta al menu
        graphics.drawImageWithScale(closeImage, 10, 20, 0.15f * 0.9f, 0.15f * 0.9f);
    }

    @Override
    public void handleInput(List<TouchEvent> events) {
        if (!canSelectLevel) return; // no hace nada si no ha pasado un breve periodo de tiempo
        for (TouchEvent event : events) {
            if (event.type == TouchEvent.TouchEventType.TOUCH_DOWN) {
                if (isTouchingClose(event.x, event.y)) { // volver al menú
                    sceneManager.setCurrentScene(new IntroScene(sceneManager.getEngine(), this.mobile, this.selectedBackgroundColor));
                }
                initialTouchY = event.y; // se guarda para el scroll
                int touchedLevel = getTouchedLevel(event.x, event.y); // nivel tocado
                if (touchedLevel != -1 && isLevelUnlocked(touchedLevel)) { // no hace nada si el nivel esta bloqueado
                    // se clica en el ultimo nivel
                    if (touchedLevel > currentLevel) sceneManager.setLevelName(touchedLevel);
                    /* se cargara un nivel que se dejo a medias o no en base a si es el primer nivel que se
                    juega en esa ejecucion y si coincide con el ultimo nivel que se jugo */
                    sceneManager.levelOnCourse = sceneManager.isFirstLevel && sceneManager.getLastLevelPlayed() == touchedLevel;
                    sceneManager.isFirstLevel = false; // deja de ser el primer nivel
                    sceneManager.setLastLevelPlayed(touchedLevel); // se pasa al manager cual es el ultimo nivel jugado
                    sceneManager.setAdventure(true); // se cambia el modo de juego
                    sceneManager.setCurrentScene(new Grid(graphics, audio, mobile, selectedBackgroundColor)); // nueva partida
                }
            } else if (event.type == TouchEvent.TouchEventType.TOUCH_DRAGGED) {
                int delta = event.y - initialTouchY; // distancia entre el primer toque y el actual
                if (Math.abs(delta) > SCROLL_THRESHOLD) {
                    targetScrollOffsetY -= delta;
                    initialTouchY = event.y; // actualiza para la siguiente actualizacion
                    // Calcula el desplazamiento máximo permitido
                    int maxScroll = Math.max(0, (rows * (cellHeight + spacingY)) - (logicHeight - topMargin - bottomMargin));
                    // actualizacion de la variable para el calculo del scroll sin pasarse de maxScroll
                    targetScrollOffsetY = Math.max(0, Math.min(targetScrollOffsetY, maxScroll));
                }
            }
        }
    }

    // metodo que devuelve si se toca el boton de volver al menu
    private boolean isTouchingClose(int touchX, int touchY) {
        int closeButtonWidth = Math.round(closeImage.getWidth() * 0.15f);
        int closeButtonHeight = Math.round(closeImage.getHeight() * 0.15f);
        int closeButtonRight = 10 + closeButtonWidth;
        int closeButtonBottom = 10 + closeButtonHeight;
        return touchX >= 10 && touchX <= closeButtonRight &&
                touchY >= 10 && touchY <= closeButtonBottom;
    }

    // lectura de los archivos style.json para determinar los colores de niveles bloqueados y desbloqueados
    private void loadStyle(int world) {
        try {
            // acceso al archivo
            File file = sceneManager.getEngine().getAssetsFile("levels/world" + world + "/style.json");
            String json = file.getContent();
            if (json == null){
                this.colorUnlocked[world - 1] = graphics.newColor(255, 200, 200, 200);
                this.colorLocked[world - 1] = graphics.newColor(255, 100, 100, 100);
                throw new FileNotFoundException("style.json not found for world: " + world);
            }
            Gson gson = new Gson();

            // conversion de variables del archivo a variables locales
            StyleData styleData = gson.fromJson(json, StyleData.class);
            this.colorUnlocked[world - 1] = convertHexToColor((int) Long.parseLong(styleData.colorUnlocked.replace("0x", ""), 16));
            this.colorLocked[world - 1] = convertHexToColor((int) Long.parseLong(styleData.colorLocked.replace("0x", ""), 16));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // convierte un numero en hexadecimal a un color
    private Color convertHexToColor(int hex) {
        int alpha = (hex >> 24) & 0xFF;
        int red = (hex >> 16) & 0xFF;
        int green = (hex >> 8) & 0xFF;
        int blue = hex & 0xFF;
        return graphics.newColor(alpha, red, green, blue);
    }

    // devuelve si un nivel esta desbloqueado
    private boolean isLevelUnlocked(int level) {
        if (level == 1) return true;
        return level <= currentLevel;
    }

    // renderiza las celdas de niveles
    private void renderLevelGrid(Graphics graphics) {
        // variables para ajustar las celdas de niveles
        int startX = 70;
        int startY = 160;
        int itemIndex = 1;
        int world = 0; // numero de mundo (-1) para usar los diferentes estilos

        for (int row = 0; row < rows; row++) {
            for (int col = 0; (col < cols) && (itemIndex <= sceneManager.WORLDSIZE[sceneManager.NMUNDOS - 1]); col++) {
                int x = startX + col * (cellWidth + spacingX); // Spacing en X
                int y = startY + row * (cellHeight + spacingY) - scrollOffsetY; // Spacing en Y

                if (y > topMargin && y < (logicHeight - bottomMargin)) {
                    // establecimiento de color en funcion del mundo y de su estado (des/bloqueado)
                    if (isLevelUnlocked(itemIndex)) {
                        graphics.setColor(colorUnlocked[world]);
                    } else {
                         graphics.setColor(colorLocked[world]);
                    }

                    // dibujado de celda y numero de nivel
                    graphics.fillRoundRectangle(x + 1, y + 1, cellWidth - 2, cellHeight - 2, 10, 10);
                    graphics.setColor(graphics.newColor(255, 0, 0, 0));
                    if(sceneManager.isFirstLevel && sceneManager.lastLevelPlayed == itemIndex) {
                        graphics.drawText("*", font, x + cellWidth / 3 + 25, y + cellHeight / 2);
                    }
                    graphics.drawText(String.valueOf(itemIndex), font, x + cellWidth / 3, y + cellHeight / 2);
                    if(!isLevelUnlocked(itemIndex)){
                        graphics.drawImageWithScale(lockImage, x + cellWidth / 3 + 25, y + cellHeight / 2, 0.02f, 0.02f);
                    }
                }
                itemIndex++; // indice++
                if (itemIndex > sceneManager.WORLDSIZE[world]){
                    world++; // incremento de mundo
                }

            }
        }
    }

    // devuelve que nivel se ha clicado en base a unas coordenadas
    private int getTouchedLevel(int touchX, int touchY) {
        int startX = 50;
        int startY = 120; // Ajustado para comenzar después del texto "Aventura"
        int itemIndex = 1;

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = startX + col * (cellWidth + spacingX); // Spacing en X
                int y = startY + row * (cellHeight + spacingY) - scrollOffsetY; // Spacing en Y

                if (touchX >= x && touchX <= x + cellWidth &&
                        touchY >= y && touchY <= y + cellHeight) {
                    // deteccion de mundo seleccionado
                    int cumulativeLevels = 0;
                    for (int i = 0; i < sceneManager.WORLDSIZE.length; i++) {
                        cumulativeLevels += sceneManager.WORLDSIZE[i]; // Sumar el tamaño del mundo actual
                        if (itemIndex <= cumulativeLevels) {
                            SceneManager.getInstance().setWorld("world" + (i + 1)); // Asignar el mundo dinámicamente
                            break; // Una vez encontrado el mundo correspondiente, salir del bucle
                        }
                    }
                    return itemIndex;
                }
                itemIndex++;
            }
        }
        return -1;
    }

    @Override
    public int getW() {
        return logicWidth;
    }

    @Override
    public int getH() {
        return logicHeight;
    }
}
