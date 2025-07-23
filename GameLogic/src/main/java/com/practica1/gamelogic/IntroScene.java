package com.practica1.gamelogic;

import com.practica1.engine.*;
import java.util.List;

public class IntroScene implements State {
    // Instancias principales para gestionar las funcionalidades del juego
    private SceneManager sceneManager; // Administrador de escenas, controla la transición entre escenas
    private Graphics graphics; // Motor gráfico para dibujar en pantalla
    private Grid grid; // Escena del tablero de juego ("Grid")
    private Shop shop; // Escena de la tienda del juego
    private Audio audio; // Motor de audio para manejar efectos y música

    // Fuentes personalizadas para diferentes tipos de texto
    private Font introFont; // Fuente para el título principal
    private Font coinsFont; // Fuente para mostrar la cantidad de monedas
    private Font buttonsFont; // Fuente para los textos de los botones
    private Mobile mobile; // Indica si el dispositivo actual es un móvil

    // Constantes para el diseño gráfico de los botones y márgenes
    private static final int BUTTON_ARC = 20; // Curvatura de las esquinas de los botones
    private static final int MARGIN = 20; // Espaciado entre los botones
    private static final int rectWidth = 350; // Ancho de los botones
    private static final int rectHeight = 80; // Altura de los botones
    private static final int rectX = 75; // Posición X (horizontal) de todos los botones
    private static final int rectY = 400; // Posición Y del botón de "Juego Rápido"
    private static final int rectYTienda = 675; // Posición Y del botón de "Tienda"

    // Coordinadas para el texto del título y los botones
    private static final int PUZZLETEXTX = 100, PUZZLETEXTY = 200; // Coordenadas del texto "Puzzle"
    private static final int BOOBLETEXTX = 125, BOOBLETEXTY = 300; // Coordenadas del texto "Booble"
    private static final int TIENDATEXTX = 175, TIENDATEXTY = 740; // Coordenadas del texto "Tienda"
    private static final int AVENTURATEXTX = 150, AVENTURATEXTY = 560; // Coordenadas del texto "Aventura"
    private static final int PRAPIDATEXTX = 100, PRAPIDATEXTY = 460; // Coordenadas del texto "Juego Rápido"

    // Variables para gestionar el efecto de transición (fade-in y fade-out)
    private float alpha = 255; // Nivel de opacidad (255 = completamente opaco, 0 = completamente transparente)
    private boolean fadingOut = false; // Indica si se está realizando un fade-out para salir de la escena
    private boolean enterShop = false; // Bandera para determinar si se debe entrar a la tienda
    private ColorEnum backgroundColor; // Color de fondo de la escena

    // Dimensiones lógicas del juego
    int logicWidth, logicHeight;

    // Constructor principal: recibe el motor del juego, información móvil y el color de fondo
    public IntroScene(Engine engine, Mobile mobile, ColorEnum backgroundColor) {
        // Inicialización de instancias principales
        this.sceneManager = SceneManager.getInstance(); // Obtiene el gestor de escenas
        this.graphics = engine.getGraphics(); // Obtiene el motor gráfico
        this.audio = engine.getAudio(); // Obtiene el motor de audio
        this.mobile = mobile; // Indica si el dispositivo es móvil
        this.backgroundColor = backgroundColor; // Establece el color de fondo

        // Carga de fuentes personalizadas para el texto
        introFont = graphics.newFont("blow.ttf", 100, true, false); // Fuente grande para títulos
        buttonsFont = graphics.newFont("fff.ttf", 35, true, false); // Fuente mediana para botones
        coinsFont = graphics.newFont("blow.ttf", 35, true, false); // Fuente para mostrar monedas

        // Obtención de dimensiones lógicas del juego (definidas en SceneManager)
        logicWidth = sceneManager.getLogicWidth(); // Ancho lógico
        logicHeight = sceneManager.getLogicHeight(); // Altura lógica
    }

    // Constructor alternativo: utiliza un color por defecto (gris) cuando no se especifica
    public IntroScene(Engine engine, Mobile mobile) {
        this(engine, mobile, ColorEnum.GRAY); // Llama al constructor principal con el color gris por defecto
    }

    @Override
    public void update(double deltaTime) {
        // Lógica para gestionar el efecto de fade-in y fade-out
        if (fadingOut || enterShop) {
            alpha += 200 * deltaTime; // Incrementa la opacidad para el fade-out
            if (alpha >= 255) { // Cuando la opacidad alcanza su máximo
                alpha = 255; // Asegura que no supere 255

                // Cambia a la escena correspondiente dependiendo de la acción
                if (!enterShop) sceneManager.setCurrentScene(grid); // Cambia a la escena de juego
                else sceneManager.setCurrentScene(new Shop(graphics, this.backgroundColor, this.mobile)); // Cambia a la tienda
            }
        } else {
            alpha -= 100 * deltaTime; // Disminuye la opacidad para el fade-in
            if (alpha <= 0) alpha = 0; // Limita la opacidad mínima a 0
        }
    }

    @Override
    public void render(Graphics graphics) {
        // Dibuja el fondo extendido para cubrir los bordes de la pantalla
        int marginX = 200, marginY = 200; // Márgenes adicionales para evitar bordes visibles
        graphics.setColor(graphics.newColor(120, this.backgroundColor.getR(), this.backgroundColor.getG(), this.backgroundColor.getB())); // Color del fondo
        graphics.fillRectangle(-marginX, -marginY, logicWidth + 2 * marginX, logicHeight + 2 * marginY); // Rellena el fondo

        // Dibuja los botones de la interfaz
        graphics.setColor(graphics.newColor(this.backgroundColor.getA(), this.backgroundColor.getR(), this.backgroundColor.getG(), this.backgroundColor.getB())); // Color de los botones
        graphics.fillRoundRectangle(rectX, rectY, rectWidth, rectHeight, BUTTON_ARC, BUTTON_ARC); // Botón "Juego Rápido"
        graphics.fillRoundRectangle(rectX, rectY + rectHeight + MARGIN, rectWidth, rectHeight, BUTTON_ARC, BUTTON_ARC); // Botón "Aventura"
        graphics.fillRoundRectangle(rectX, rectYTienda, rectWidth, rectHeight, BUTTON_ARC, BUTTON_ARC); // Botón "Tienda"

        // Dibuja el texto principal (títulos y monedas)
        graphics.setColor(graphics.newColor(255, 0, 0, 0)); // Color negro para el texto
        graphics.drawText("Puzzle", introFont, PUZZLETEXTX, PUZZLETEXTY); // Texto "Puzzle"
        graphics.drawText("Booble", introFont, BOOBLETEXTX, BOOBLETEXTY); // Texto "Booble"
        graphics.drawText("Coins:" + sceneManager.getCoins(), coinsFont, logicWidth / 4 - 100, logicHeight - 50); // Monedas del jugador

        // Dibuja el texto en los botones
        graphics.drawText("Aventura", buttonsFont, AVENTURATEXTX, AVENTURATEXTY); // Texto "Aventura"
        graphics.drawText("Juego Rápido", buttonsFont, PRAPIDATEXTX, PRAPIDATEXTY); // Texto "Juego Rápido"
        graphics.drawText("Tienda", buttonsFont, TIENDATEXTX, TIENDATEXTY); // Texto "Tienda"

        // Aplica el efecto de desvanecimiento (fade) si es necesario
        graphics.setColor(graphics.newColor((int) alpha, 0, 0, 0)); // Color negro con opacidad dinámica
        graphics.fillRectangle(-marginX, -marginY, logicWidth + 2 * marginX, logicHeight + 2 * marginY); // Cubre toda la pantalla
    }

    @Override
    public void handleInput(List<TouchEvent> events) {
        // Maneja los eventos táctiles (toques en la pantalla)
        for (TouchEvent event : events) {
            if (event.type == TouchEvent.TouchEventType.TOUCH_DOWN) {
                if (isTouchPRapida(event.x, event.y)) {
                    // Inicia el modo "Juego Rápido"
                    SceneManager.getInstance().setAdventure(false); // Desactiva el modo aventura
                    sceneManager.setCurrentScene(new Grid(graphics, audio, mobile, backgroundColor)); // Cambia a la escena "Grid"
                } else if (isTouchingShopButton(event.x, event.y)) {
                    // Activa la bandera para entrar a la tienda
                    enterShop = true;
                } else if (isTouchAventura(event.x, event.y)) {
                    // Inicia el modo "Aventura"
                    SceneManager.getInstance().setCurrentScene(new Aventura(graphics, audio, mobile, backgroundColor)); // Cambia a la escena "Aventura"
                }
            }
        }
    }

    // Métodos auxiliares para detectar toques en áreas específicas (botones)

    // Verifica si el toque fue en el botón "Juego Rápido"
    private boolean isTouchPRapida(int touchX, int touchY) {
        return touchX >= rectX && touchX <= (rectX + rectWidth) && touchY >= rectY && touchY <= (rectY + rectHeight);
    }

    // Verifica si el toque fue en el botón "Aventura"
    private boolean isTouchAventura(int touchX, int touchY) {
        return touchX >= rectX && touchX <= (rectX + rectWidth) && touchY >= rectY + rectHeight + MARGIN && touchY <= (rectY + rectHeight + MARGIN + rectHeight);
    }

    // Verifica si el toque fue en el botón "Tienda"
    private boolean isTouchingShopButton(int touchX, int touchY) {
        return touchX >= rectX && touchX <= (rectX + rectWidth) && touchY >= rectYTienda && touchY <= (rectYTienda + rectHeight);
    }

    // Métodos para obtener las dimensiones lógicas del juego (ancho y alto)
    @Override
    public int getW() {
        return sceneManager.getLogicWidth(); // Devuelve el ancho lógico
    }

    @Override
    public int getH() {
        return sceneManager.getLogicHeight(); // Devuelve la altura lógica
    }
}
