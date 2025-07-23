package com.practica1.gamelogic;

import com.practica1.engine.Audio;
import com.practica1.engine.Color;
import com.practica1.engine.Font;
import com.practica1.engine.Graphics;
import com.practica1.engine.Image;
import com.practica1.engine.Mobile;
import com.practica1.engine.Sound;
import com.practica1.engine.State;
import com.practica1.engine.TouchEvent;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.List;
import com.practica1.engine.File;
import java.util.Random;

class LevelData {
    List<Integer> bubblesToLaunch;
    int[][] initialBoard;
}

public class Grid implements State {

    private SceneManager sceneManager;
    private Graphics graphics;
    private Mobile mobile;
    private Audio audio;
    int logicWidth, logicHeight;

    // --CONSTANTES--
    private static final int BUBBLE_RADIUS = 22;  // Radio de las burbujas
    private static final int BOUND_WIDTH = 30;  // Anchura de los bordes
    private static final int TOP_MARGIN = 75;  // Anchura de los bordes
    private static final int TOP_BOUNDARY = TOP_MARGIN + BUBBLE_RADIUS;
    private static final int ROWS = 5; // COLUMNAS RELLENAS
    private static final int TOTALROWS = 21; //COLUMNAS TOTALES
    private static final int COLUMNS = 10;
    private static final int PLAYER_INIT_POS = -97;
    private static final float PLAYER_LIFE_TIME = 5.0f;
    private static final int MAX_ALLOWED_ROW = 18; // Define la fila máxima permitida antes de activar la derrota
    private static final int LOSE_LINE_Y = TOP_MARGIN + BOUND_WIDTH + MAX_ALLOWED_ROW * (BUBBLE_RADIUS * 2) - BUBBLE_RADIUS;

    // -- ANIMACION DE VICTORIA
    private boolean victoryAnimationActive = false; // Bandera para controlar si la animación de victoria está activa
    private List<PlayerBubble> victoryBubbles; // Lista de burbujas animadas al ganar
    private float victoryAnimationTime = 0; // Tiempo acumulado para la animación

    // -- VARIABLES DE BURBUJAS
    private Bubble[][] bubbleList;  // Matriz de burbujas en el grid
    private List<ColorEnum> bubblesToThrow;  // array de burbujas a lanzar
    private PlayerBubble playerBubble;  // Burbuja actual del jugador
    private float initialPlayerBubbleY;  // Posición inicial de la burbuja del jugador
    private ColorEnum nextBubbleColor; // color de la siguiente burbuja

    // Variables para el efecto de Fade In
    private float alpha = 255; // Opacidad inicial (máxima)
    private boolean fadingIn = true; // Bandera para controlar el Fade In

    // Variables de texto
    private Font scoreFont;
    private Font countdownFont;
    private int score = 0;

    // Sonidos
    private Sound gameOverSound;
    private Sound matchSound;
    private Sound winSound;
    private Sound collisionSound;  // Sonido de colisión

    private static final Random random = new Random(); // Posiciones de las imágenes


    // Posiciones de las imágenes en las esquinas
    private int imageLeftX, imageLeftY;    // Coordenadas para la imagen en la esquina superior izquierda
    private int imageRightY;   // Coordenadas para la imagen en la esquina superior derecha
    private Image gridImage;   // Imagen en la esquina superior izquierda
    private Image gridImage2;  // Imagen en la esquina superior derecha
    private ColorEnum backgroundColor;

    // Variable para indicar si una colisión ha ocurrido y el sonido ha sido reproducido

    // Representacion de burbujas
    private boolean hexagons = false;
    private Image redBubbleImage;
    private Image yellowBubbleImage;
    private Image greenBubbleImage;
    private Image greyBubbleImage;
    private Image blueBubbleImage;
    private boolean hexFull = false;

    // Variables para delay inicial
    private static final float INITIAL_LAUNCH_DELAY = 3.0f; // Retraso
    private float elapsedTimeSinceStart = 0.0f; // para evitar que tire una bola nda más empezar

    // Variables para iniciar nivel
    private String le = String.valueOf(SceneManager.getInstance().getLastLevelPlayed()); // nivel a jugar
    private boolean adventure; // modo de juego

    // CONSTRUCTORA
    public Grid(Graphics graphics, Audio audio, Mobile mobile, ColorEnum backgroundColor) {
        // asignacion de variables
        sceneManager = SceneManager.getInstance();
        this.audio = audio;
        this.mobile = mobile;
        this.graphics = graphics;
        logicWidth = sceneManager.getLogicWidth();
        logicHeight = sceneManager.getLogicHeight();

        // carga de recursos
        this.gridImage = graphics.newImage("close.png");    // Imagen para la esquina superior izquierda
        this.gridImage2 = graphics.newImage("hex_empty.png");   // Imagen para la esquina superior derecha
        matchSound = audio.newSound("correct.wav");
        winSound = audio.newSound("win1.wav");
        redBubbleImage = graphics.newImage("apple.png");
        yellowBubbleImage = graphics.newImage("lemon.png");
        greenBubbleImage = graphics.newImage("pear.png");
        blueBubbleImage = graphics.newImage("blueberry.png");
        greyBubbleImage = graphics.newImage("grey.png");
        scoreFont = graphics.newFont("blow.ttf", 40, false, false);
        countdownFont = graphics.newFont("blow.ttf", 80, true, true);
        collisionSound = audio.newSound("ballAttach.wav"); // Sonido de colisión entre burbujas
        gameOverSound = audio.newSound("gameOver.wav");

        // posiciones de imagenes
        this.imageLeftX = 20;
        this.imageLeftY = 20;
        this.imageRightY = 20;

        // color de fondo
        this.backgroundColor = backgroundColor;

        // inicializacion de arrays
        bubbleList = new Bubble[TOTALROWS][COLUMNS];
        bubblesToThrow = new ArrayList<ColorEnum>();

        //modo de juego
        adventure = sceneManager.getAdventure();
        if (adventure){
            // nivel en curso guardado
            if (sceneManager.levelOnCourse) loadLevelOnCourse(BUBBLE_RADIUS, TOP_MARGIN + BOUND_WIDTH);
            // nivel por defecto
            else loadLevelFromJson("levels/" + sceneManager.getWorld() + "/level" + le + ".json",
                    BUBBLE_RADIUS, TOP_MARGIN + BOUND_WIDTH);
        }
        // nivel aleatorio (partida rapida)
        else generateBubbleRows(ROWS, COLUMNS, BUBBLE_RADIUS, TOP_MARGIN + BOUND_WIDTH);

        spawnNewPlayerBubble(); // spawneo de la burbuja del jugador

    }

    @Override
    public void update(double deltaTime) {
        elapsedTimeSinceStart += (float) deltaTime; // cuenta atras para empezar

        if (victoryAnimationActive) { // animacion de victoria
            victoryAnimationTime += (float) deltaTime;

            for (PlayerBubble bubble : victoryBubbles) {
                bubble.ballY += (float) (bubble.ballSpeedY * deltaTime); // Usa la velocidad de cada burbuja

                // Si la burbuja cae fuera de la pantalla, la reposicionamos arriba con un nuevo X aleatorio
                if (bubble.ballY > logicHeight) {
                    bubble.ballY = -BUBBLE_RADIUS; // Vuelve a la parte superior
                    bubble.ballX = random.nextInt(logicWidth - BUBBLE_RADIUS * 2); // Nueva posición X aleatoria
                }
            }

            // Verificar si la animación ha terminado
            if (victoryAnimationTime >= 3) {
                victoryAnimationActive = false; // Desactiva la animación
                if(adventure) {
                    sceneManager.setCurrentScene(new VictoryScene(graphics, audio, score, mobile, this.backgroundColor, true));
                }
                else {
                    sceneManager.setCurrentScene(new VictoryScene(graphics, audio, score, mobile, this.backgroundColor, false));
                }
            }
            return; // Evita otras actualizaciones mientras la animación está activa
        }

        // fade in
        if (fadingIn) {
            alpha -= 200 * deltaTime;
            if (alpha <= 0) {
                alpha = 0;
                fadingIn = false;
            }
        }

        if (playerBubble != null) {
            if (elapsedTimeSinceStart >= INITIAL_LAUNCH_DELAY) {
                if (!playerBubble.isMoving()) {
                    playerBubble.setLine(); // pintar linea al apuntar
                }
                playerBubble.update(deltaTime); // actualizar la pelota del jugador
                checkPlayerOutOfBounds(); // comprobar condicion de derrota
            }

            if (playerBubble.getMoveTime() > PLAYER_LIFE_TIME) {
                playerBubble = null;
                spawnNewPlayerBubble();
            }
        }

        checkCollisions(); // actualizacion de colisiones
        checkGameOver(); // comprobar si se ha perdido la partida
        checkVictory(); // comprobar si se ha ganado la partida
    }


    private void playWinSound(){
        this.audio.playSound(winSound, false);
    }


    @Override
    public void render(Graphics graphics) {

        // fondo
        graphics.setColor(graphics.newColor(120, backgroundColor.getR(), backgroundColor.getG(), backgroundColor.getB()));
        graphics.fillRectangle(-20, -20, sceneManager.getLogicWidth() + 40, sceneManager.getLogicHeight() + 40);

        if (victoryAnimationActive) { // animacion de victoria
            for (PlayerBubble bubble : victoryBubbles) {
                bubble.render(graphics, this); // Renderizar cada burbuja
            }
            return; // Evita renderizar el resto del juego durante la animación
        }

        // imagenes de las esquinas
        graphics.drawImageWithScale(gridImage, imageLeftX, imageLeftY, 0.1f, 0.1f);
        graphics.drawImageWithScale(gridImage2, logicWidth - 50, imageRightY, 0.1f, 0.1f);

        // renderizado de las burbujas
        for (int row = 0; row < TOTALROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                Bubble bubble = bubbleList[row][col];
                if (bubble != null) {
                    bubble.render(graphics, this);
                }
            }
        }

        // dibujado de texto
        graphics.setColor(graphics.newColor(255, 0, 0, 0));
        graphics.setFont(scoreFont);
        graphics.drawText("Score:" + score, scoreFont, logicWidth / 2 - 170, 60);

        // renderizado de texto e imagen de next Bubble
        renderNextPlayerBubble(graphics);

        if (elapsedTimeSinceStart < INITIAL_LAUNCH_DELAY) { // texto de la cuenta atras
            int countdown = (int) Math.ceil(INITIAL_LAUNCH_DELAY - elapsedTimeSinceStart);
            if (playerBubble != null) {
                int countdownX = (int) playerBubble.getBallX() - 20;
                int countdownY = (int) playerBubble.getBallY() - BUBBLE_RADIUS - 40;
                graphics.setColor(graphics.newColor(255, 0, 0, 0));
                graphics.setFont(countdownFont);
                graphics.drawText("" + countdown, countdownFont, countdownX + 10, countdownY);
            }
        }

        if (playerBubble != null) { // renderizado del playerbubble
            if (!playerBubble.isMoving()) {
                playerBubble.setLine();
            }
            playerBubble.render(graphics, this);
        }

        // linea de abajo del grid
        graphics.setColor(graphics.newColor(255, 255, 0, 0));
        graphics.drawLine(0, LOSE_LINE_Y, sceneManager.getLogicWidth(), LOSE_LINE_Y);

        // renderizado de los bordes grises
        renderBounds(graphics);

        if (fadingIn) { // render de fade in
            graphics.setColor(graphics.newColor((int) alpha, 0, 0, 0));
            graphics.fillRectangle(0, -500, sceneManager.getLogicWidth() + 100, sceneManager.getLogicHeight() + 500);
        }
    }

    @Override
    public void handleInput(List<TouchEvent> events) {
        // Si la burbuja del jugador está en movimiento, no procesar input
        if (playerBubble != null && playerBubble.isMoving()) return;

        for (TouchEvent event : events) {
            if (playerBubble != null && elapsedTimeSinceStart >= INITIAL_LAUNCH_DELAY) {
                playerBubble.setLine();  // Mostrar línea de lanzamiento mientras se interactúa
            }
            if (event.type == TouchEvent.TouchEventType.TOUCH_DOWN) {
                // Verifica si se ha tocado el botón para mostrar/ocultar hexágonos
                if (isTouchOnHexEmpty(event.x, event.y)) toggleHexagonDisplay();
                // Verifica si se ha tocado el botón para volver al menu
                if (isTouchOnClose(event.x, event.y)) {
                    sceneManager.setCurrentScene(new IntroScene(sceneManager.getEngine(), this.mobile, backgroundColor));
                }
            }
            if (elapsedTimeSinceStart >= INITIAL_LAUNCH_DELAY && insideLimits(event.x, event.y)) {
                // Ajusta la dirección de lanzamiento mientras se arrastra
                if (event.type == TouchEvent.TouchEventType.TOUCH_DRAGGED) {
                    if (playerBubble != null && !playerBubble.isMoving()) {
                        playerBubble.setLaunchDirection(event.x, event.y);
                    }
                }
                // Lógica para lanzar la burbuja cuando se suelta el dedo
                if (event.type == TouchEvent.TouchEventType.TOUCH_UP) {
                    if (playerBubble != null && !playerBubble.isMoving()) {
                        playerBubble.setLaunchDirection(event.x, event.y);
                        playerBubble.launch();  // Lanzar la burbuja en la dirección establecida
                    }
                }
            }
        }
    }

    // mostrar o esconder los hexagonos
    private void toggleHexagonDisplay() {
        // cambiar el estado
        hexagons = !hexagons;

        // actualizacion de bubbles
        for (int row = 0; row < TOTALROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                Bubble bubble = bubbleList[row][col];
                if (bubble != null) {
                    if (hexagons) bubble.showHex();
                    else bubble.hideHex();
                }
            }
        }

        // cambiar la imagen
        if (hexagons) gridImage2 = graphics.newImage("hex_full.png");
        else gridImage2 = graphics.newImage("hex_empty.png");
    }

    // Renderiza la burbuja siguiente del jugador
    private void renderNextPlayerBubble(Graphics graphics) {
        if (nextBubbleColor != null) {
            // Determina la posición para mostrar el texto "Next:"
            int textX = logicWidth / 2 + 40;
            int textY = 60;

            // Determina la posición para la burbuja
            int bubbleX = textX + 100;
            int bubbleY = textY - 35;

            // Dibuja el texto "Next:"
            graphics.setColor(graphics.newColor(255, 0, 0, 0)); // Negro
            graphics.drawText("Next:", scoreFont, textX, textY);

            boolean notUnlocked = false;
            switch (nextBubbleColor) { // si la burbuja esta customizada
                case RED:
                    if(sceneManager.selectedApple) drawApple(graphics, bubbleX, bubbleY);
                    else notUnlocked = true;
                    break;
                case YELLOW:
                    if(sceneManager.selectedLemon) drawLemon(graphics, bubbleX, bubbleY);
                    else notUnlocked = true;
                    break;
                case GREEN:
                    if(sceneManager.selectedPear) drawPear(graphics, bubbleX, bubbleY);
                    else notUnlocked = true;
                    break;
                case BLUE:
                    if(sceneManager.selectedBerry) drawBlueberry(graphics, bubbleX, bubbleY);
                    else notUnlocked = true;
                    break;
                case GRAY:
                    if(sceneManager.selectedGris) drawGray(graphics, bubbleX, bubbleY);
                    else notUnlocked = true;
                    break;
            }
            if (notUnlocked){ // si la burbuja no esta customizada
                Color bubbleColor = graphics.newColor(nextBubbleColor.getA(),
                        nextBubbleColor.getR(), nextBubbleColor.getG(), nextBubbleColor.getB());
                graphics.setColor(bubbleColor);
                graphics.fillCircle(bubbleX, bubbleY, BUBBLE_RADIUS);
            }
        }
    }

    // metodo para cargar un nivel desde un json
    public void loadLevelFromJson(String jsonPath, int radius, int startY) {
        // acceso al archivo
        Gson gson = new Gson();
        File file = sceneManager.getEngine().getAssetsFile(jsonPath);
        String json = file.getContent();


        if (json != null) {
                // Leer el JSON y mapearlo a un objeto LevelData
                LevelData levelData = gson.fromJson(json, LevelData.class);

            // Configurar el tablero inicial basado en `initialBoard`
            for (int row = 0; row < levelData.initialBoard.length; row++) {
                for (int col = 0; col < levelData.initialBoard[row].length; col++) {
                    int colorId = levelData.initialBoard[row][col];
                    int posX = BOUND_WIDTH + col * radius * 2 + (row % 2 == 0 ? 0 : radius);
                    int posY = startY + row * (radius * 2 - 6);
                    ColorEnum color = getColorFromId(colorId);
                    Bubble bubble = new Bubble(posX, posY, radius, col, row);
                    bubble.setBubbleColor(color);
                    // Asigna la burbuja en tu estructura de datos (ej., `bubbleList`)
                    bubbleList[row][col] = bubble;
                }
            }
            // lista de colores playerbubble
            for (int colorId : levelData.bubblesToLaunch) {
                ColorEnum color = getColorFromId(colorId);
                bubblesToThrow.add(color);
            }
        }
    }

    // cargar un nivel que se dejo a medias
    private void loadLevelOnCourse(int radius, int startY) {
        // Configurar el tablero inicial basado en `initialBoard`
        for (int row = 0; row < sceneManager.getCurrentGrid().length; row++) {
            for (int col = 0; col < sceneManager.getCurrentGrid()[row].length; col++) {
                int colorId = sceneManager.getCurrentGrid()[row][col];
                int posX = BOUND_WIDTH + col * radius * 2 + (row % 2 == 0 ? 0 : radius);
                int posY = startY + row * (radius * 2 - 6);
                ColorEnum color = getColorFromId(colorId);
                Bubble bubble = new Bubble(posX, posY, radius, col, row);
                bubble.setBubbleColor(color);
                // Asigna la burbuja en tu estructura de datos (ej., `bubbleList`)
                bubbleList[row][col] = bubble;
            }
        }
        // lista de colores playerbubble
        for (int colorId : sceneManager.getCurrentBubblesToLaunch()) {
            ColorEnum color = getColorFromId(colorId);
            bubblesToThrow.add(color);
        }
    }

    // de ID a color
    public ColorEnum getColorFromId(int id){
        ColorEnum color;
        if (id == 1) color = ColorEnum.RED;
        else if (id == 2) color = ColorEnum.GREEN;
        else if (id == 3) color = ColorEnum.BLUE;
        else if (id == 4) color = ColorEnum.YELLOW;
        else if (id == 5) color = ColorEnum.GRAY;
        else color = null;
        return color;
    }

    // de color a ID
    public int getIdFromColor(ColorEnum color){
        int id = 0;
        if (color == ColorEnum.RED) id = 1;
        else if (color == ColorEnum.GREEN) id = 2;
        else if (color == ColorEnum.BLUE) id = 3;
        else if (color == ColorEnum.YELLOW) id = 4;
        else if (color == ColorEnum.GRAY) id = 5;
        return id;
    }

    // genera una partida aleatoria
    public void generateBubbleRows(int rows, int columns, int radius, int startY) {
        int spacing = radius * 2;  // Espaciado entre burbujas

        for (int row = 0; row < TOTALROWS; row++) {
            // Ajuste en la cantidad de columnas para las filas pares
            int adjustedColumns = (row % 2 == 0) ? columns : columns - 1;

            for (int col = 0; col < adjustedColumns; col++) {
                // Desplazamiento en x para las filas pares
                int posX = BOUND_WIDTH + col * spacing + (row % 2 == 0 ? 0 : radius);
                int posY = startY + row * (spacing - 6);

                // Crea la burbuja y asigna su posición en la matriz
                Bubble bubble = new Bubble(posX, posY, radius, col, row);

                // Asigna un color aleatorio a las primeras 5 filas
                if (row < rows) {
                    ColorEnum randomColor = ColorEnum.values()[random.nextInt(ColorEnum.values().length - 1)];
                    bubble.setBubbleColor(randomColor);
                }

                // Añade la burbuja a la matriz bidimensional
                bubbleList[row][col] = bubble;
            }
        }
    }

    // Coge una burbuja a través de unas coordenadas
    public Bubble getBubbleFromCoordinates(int x, int y) {
        for (int row = 0; row < TOTALROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                Bubble bubble = bubbleList[row][col];
                if (bubble != null) {
                    int bubbleX = (int)bubble.getBallX();
                    int bubbleY = (int)bubble.getBallY();
                    int radius = (int)bubble.getBallRadius();

                    // Verifica si el punto está dentro del radio de la burbuja
                    if (Math.pow(x - bubbleX, 2) + Math.pow(y - bubbleY, 2) <= Math.pow(radius + 3, 2)) {
                        return bubble;
                    }
                }
            }
        }
        return null;  // No se encontró una burbuja en esas coordenadas
    }

    // Añade la burbuja del jugador al grid
    public void addPlayerBubble(PlayerBubble bubble) {
        playerBubble = bubble;
    }

    // Genera una nueva burbuja del jugador en la posición inicial
    private void spawnNewPlayerBubble() {
        if (!bubblesToThrow.isEmpty() || !adventure) {
            // posicion de la burbuja
            int centerX = sceneManager.getLogicWidth() / 2 - BUBBLE_RADIUS;
            int bottomY = sceneManager.getLogicHeight() + PLAYER_INIT_POS;

            ColorEnum currentColor;
            if (adventure && !bubblesToThrow.isEmpty()) {
                currentColor = bubblesToThrow.get(0); // siguiente color del array
            } else { // color aleatorio en caso de no ser aventura
                currentColor = nextBubbleColor != null ? nextBubbleColor : ColorEnum.values()[random.nextInt(ColorEnum.values().length - 1)];
            }

            // Establecer el próximo color para el modo rápido o el siguiente en la lista para aventura
            nextBubbleColor = adventure ? (bubblesToThrow.size() <= 1 ? null : bubblesToThrow.get(1))
                    : ColorEnum.values()[random.nextInt(ColorEnum.values().length - 1)];

            // Generar la burbuja del jugador
            playerBubble = new PlayerBubble(centerX, bottomY, BUBBLE_RADIUS, currentColor, TOP_MARGIN, BOUND_WIDTH);
            addPlayerBubble(playerBubble);

            // Restablecer la posición inicial
            if (initialPlayerBubbleY == 0) {
                initialPlayerBubbleY = bottomY;
            }
        }
    }

    // Verifica si la burbuja del jugador ha salido de los límites de la pantalla
    private void checkPlayerOutOfBounds() {
        // Si la burbuja del jugador se sale de la pantalla, la elimina y genera una nueva
        if (playerBubble.getBallX() < 0 || playerBubble.getBallX() > logicWidth ||
                playerBubble.getBallY() < 0 || playerBubble.getBallY() > logicHeight) {

            playerBubble = null;  // Elimina la burbuja del jugador actual
            spawnNewPlayerBubble();  // Genera una nueva burbuja del jugador
        }
    }

    // incremento de score
    public void increaseScore(int points) {
        score += points;
    }

    // Comprueba las colisiones
    private void checkCollisions() {
        for (int row = 0; row < TOTALROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                Bubble bubble = bubbleList[row][col];
                if (bubble != null && bubble.getColor() != null && playerBubble != null && isColliding(playerBubble, bubble)) {

                    // Lógica existente para manejar la colisión
                    Bubble b = getBubbleFromCoordinates((int) playerBubble.getBallX(), (int) playerBubble.getBallY());
                    if (b != null) {
                        b.setBubbleColor(playerBubble.getColor());

                        // Verificar si la burbuja está en una fila mayor o igual a MAX_ALLOWED_ROW
                        if (b.getRow() >= MAX_ALLOWED_ROW) {
                            gameOver();
                            return; // Sale de la función tras activar la derrota
                        }

                        playerBubble = null; // Resetear la burbuja del jugador
                        if(adventure)bubblesToThrow.remove(0); // eliminar la bola cuando colisiona;
                        spawnNewPlayerBubble();

                        // actualizar las burbujas adyacentes
                        List<Bubble> matchingGroup = new ArrayList<>();
                        findMatchingGroup(b.getRow() - 1, b.getCol(), b.getColor(), matchingGroup); // Arriba
                        findMatchingGroup(b.getRow() + 1, b.getCol(), b.getColor(), matchingGroup); // Abajo
                        findMatchingGroup(b.getRow(), b.getCol() - 1, b.getColor(), matchingGroup); // Izquierda
                        findMatchingGroup(b.getRow(), b.getCol() + 1, b.getColor(), matchingGroup); // Derecha

                        // si esta en una fila par o impar cambia las adyacentes a las que tiene que actualizar
                        if (b.getRow() % 2 == 0) {
                            findMatchingGroup(b.getRow() - 1, b.getCol() - 1, b.getColor(), matchingGroup); // Arriba izquierda
                            findMatchingGroup(b.getRow() + 1, b.getCol() - 1, b.getColor(), matchingGroup); // Abajo izquierda
                        } else {
                            findMatchingGroup(b.getRow() - 1, b.getCol() + 1, b.getColor(), matchingGroup); // Arriba derecha
                            findMatchingGroup(b.getRow() + 1, b.getCol() + 1, b.getColor(), matchingGroup); // Abajo derecha
                        }

                        if (matchingGroup.size() >= 3) { // se borran las burbujas cuyo grupo sea mayor a 3
                            audio.playSound(matchSound, false); // Reproducir sonido de coincidencia
                            clearBubbles(matchingGroup);
                        }
                        else audio.playSound(collisionSound, false); // Reproducir sonido estándar de colisión

                        // Actualizar burbujas flotantes
                        updateFloatingBubbles();
                    }
                    return;
                }
            }
        }
        checkPlayerBubbleAtTop(); // colision con el borde superior
    }

    // Verifica si una burbuja llega a la parte superior sin colisionar
    private void checkPlayerBubbleAtTop() {
        if (playerBubble != null && playerBubble.getBallY() <= TOP_BOUNDARY) { // posicion de la pelota en el borde superior
            int col = (int) (playerBubble.getBallX() / (BUBBLE_RADIUS * 2)); // Calcula la columna
            bubbleList[0][col].setBubbleColor(playerBubble.getColor());
            List<Bubble> matchingGroup = new ArrayList<>();
            // actualizacion de adyacentes
            findMatchingGroup(0, col - 1, bubbleList[0][col].getColor(), matchingGroup);  // Izquierda
            findMatchingGroup(0, col + 1,  bubbleList[0][col].getColor(), matchingGroup);  // Derecha

            if (matchingGroup.size() >= 3) { // limpieza en caso de 3 pegadas del mismo color
                clearBubbles(matchingGroup);
            }

            // Actualizar burbujas flotantes
            updateFloatingBubbles();
            playerBubble = null; // Reinicia la burbuja del jugador
            spawnNewPlayerBubble();
        }
    }

    // Comprueba si dos burbujas están colisionando
    private boolean isColliding(PlayerBubble b1, Bubble b2) {
        // Primera verificación rápida: Compara si las burbujas están demasiado lejos
        // Si la distancia horizontal o vertical entre los centros de las burbujas
        // es mayor que el doble del radio de las burbujas, no hay colisión.
        if (Math.abs(b1.getBallX() - b2.getBallX()) > BUBBLE_RADIUS * 2 ||
                Math.abs(b1.getBallY() - b2.getBallY()) > BUBBLE_RADIUS * 2) {
            return false;
        }

        // Cálculo preciso de la distancia entre los centros de las burbujas
        float dx = b1.getBallX() - b2.getBallX(); // Diferencia en la posición X
        float dy = b1.getBallY() - b2.getBallY(); // Diferencia en la posición Y
        float combinedRadius = b1.getBallRadius() + b2.getBallRadius(); // Suma de los radios

        // Comprobación exacta: Calcula si la distancia al cuadrado entre los centros
        // es menor o igual a la suma de los radios al cuadrado. Si es así, las burbujas
        // están tocándose o colisionando.
        return dx * dx + dy * dy <= combinedRadius * combinedRadius;
    }

    // Encuentra las burbujas adyacentes del mismo color recursivamente
    private void findMatchingGroup(int row, int col, ColorEnum color, List<Bubble> group) {
        if (row < 0 || row >= TOTALROWS || col < 0 || col >= COLUMNS) return;

        Bubble bubble = bubbleList[row][col];
        if (bubble == null || bubble.getColor() != color || group.contains(bubble)) return;

        group.add(bubble);

        // Llamadas recursivas a las burbujas adyacentes
        findMatchingGroup(row - 1, col, color, group);  // Arriba
        findMatchingGroup(row + 1, col, color, group);  // Abajo
        findMatchingGroup(row, col - 1, color, group);  // Izquierda
        findMatchingGroup(row, col + 1, color, group);  // Derecha

        if(row % 2 == 0){
            findMatchingGroup(row - 1, col - 1, color, group);  // Arriba
            findMatchingGroup(row + 1, col - 1, color, group);  // Abajo
        }
        else {
            findMatchingGroup(row - 1, col + 1, color, group);  // Arriba
            findMatchingGroup(row + 1, col + 1, color, group);  // Abajo
        }
    }

    // Elimina las burbujas en el grupo dado
    private void clearBubbles(List<Bubble> group) {
        for (Bubble bubble : group) {
            bubble.setBubbleColor(null);  // Elimina el color de la burbuja
            increaseScore(10);
        }
    }

    // Marca las burbujas conectadas al techo
    private void markConnectedToCeiling(boolean[][] visited, int row, int col) {
        if (row < 0 || row >= TOTALROWS || col < 0 || col >= COLUMNS || visited[row][col]) return;

        Bubble bubble = bubbleList[row][col];
        if (bubble == null || bubble.getColor() == null) return;

        visited[row][col] = true;

        markConnectedToCeiling(visited, row - 1, col);  // Arriba
        markConnectedToCeiling(visited, row + 1, col);  // Abajo
        markConnectedToCeiling(visited, row, col - 1);  // Izquierda
        markConnectedToCeiling(visited, row, col + 1);  // Derecha

        if(row % 2 == 0){
            markConnectedToCeiling(visited, row - 1, col - 1);  // Arriba
            markConnectedToCeiling(visited, row + 1, col - 1);  // Abajo
        }
        else {
            markConnectedToCeiling(visited, row - 1, col + 1);  // Arriba
            markConnectedToCeiling(visited, row + 1, col + 1);  // Abajo
        }
    }

    // Actualiza las burbujas flotantes
    private void updateFloatingBubbles() {
        boolean[][] visited = new boolean[TOTALROWS][COLUMNS];

        // Marca las burbujas conectadas al techo
        for (int col = 0; col < COLUMNS; col++) {
            markConnectedToCeiling(visited, 0, col);
        }

        // Elimina las burbujas que no están conectadas al techo
        for (int row = 0; row < TOTALROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                if (!visited[row][col] && bubbleList[row][col] != null && bubbleList[row][col].getColor() != null) {
                    bubbleList[row][col].setBubbleColor(null);  // Elimina la burbuja
                    increaseScore(10);  // Suma 10 puntos por cada burbuja flotante eliminada
                }
            }
        }
    }

    // renderizado de los bordes grises
    void renderBounds(Graphics graphics){
        ColorEnum boundColor = ColorEnum.GRAY;
        Color colorBound = graphics.newColor(boundColor.getA(), boundColor.getR(),boundColor.getG(), boundColor.getB());
        graphics.setColor(colorBound);
        // ARRIBA
        graphics.fillRectangle(0, TOP_MARGIN, sceneManager.getLogicWidth(), BOUND_WIDTH);
        // IZQUIERDA
        graphics.fillRectangle(0, TOP_MARGIN + BOUND_WIDTH, BOUND_WIDTH,sceneManager.getLogicHeight() - BOUND_WIDTH - TOP_MARGIN);
        // DERECHA
        graphics.fillRectangle(sceneManager.getLogicWidth() - BOUND_WIDTH, TOP_MARGIN + BOUND_WIDTH, BOUND_WIDTH,sceneManager.getLogicHeight() - BOUND_WIDTH - TOP_MARGIN);
    }

    // Método para verificar si se ha tocado la imagen "hex_empty" en la esquina superior derecha
    private boolean isTouchOnHexEmpty(int touchX, int touchY) {
        float imageWidth = gridImage2.getWidth() * 0.1f;// 0.1f es la escala a la que está la imagen
        float imageHeight = gridImage2.getHeight() * 0.1f;
        float imagePosx = logicWidth - 50;
        float imagePosY = imageRightY + 10;

        return touchX >= imagePosx && touchX <= (imagePosx + imageWidth) &&
                touchY >= imagePosY && touchY <= (imagePosY + imageHeight);
    }

    // Método para verificar si se ha tocado la imagen "close.png" en la esquina superior izquierda
    private boolean isTouchOnClose(int touchX, int touchY) {

        float imageWidth = gridImage.getWidth() * 0.1f;// 0.1f es la escala a la que está la imagen
        float imageHeight = gridImage.getHeight() * 0.1f;

        return touchX >= imageLeftX && touchX <= (imageLeftX + imageWidth) &&
                touchY >= imageLeftY && touchY <= (imageLeftY + imageHeight);
    }

    // Solo lanza la burbuja dentro de los límites
    private boolean insideLimits(int x, int y){
        // Coordenadas están dentro del área de juego, excluyendo la barra superior y los márgenes
        return (x >= BOUND_WIDTH && x <= logicWidth - BOUND_WIDTH &&
                y >= TOP_MARGIN + BOUND_WIDTH && y <= logicHeight - BOUND_WIDTH);
    }

    // Comprueba si se ha perdido el juego
    private void checkGameOver() {
        // Condición de derrota por alcanzar la línea de pérdida
        for (int row = 0; row < TOTALROWS; row++) {
            for (int col = 0; col < COLUMNS; col++) {
                Bubble bubble = bubbleList[row][col];
                if (bubble != null && bubble.getColor() != null && bubble.getBallY() + BUBBLE_RADIUS >= LOSE_LINE_Y) {
                    gameOver(); // porque la bola este muy abajo
                    return; // Salir inmediatamente tras activar Game Over
                }
            }
        }
        // Condición específica para modo aventura, si quedan burbujas en la matriz y ya no quedan para lanzar
        if (adventure && playerBubble == null && bubblesToThrow.isEmpty()) {
            // Verifica si todavía quedan burbujas en la matriz
            for (int row = 0; row < TOTALROWS; row++) {
                for (int col = 0; col < COLUMNS; col++) {
                    Bubble bubble = bubbleList[row][col];
                    if (bubble != null && bubble.getColor() != null) {
                        gameOver();
                        return; // Salir tras activar Game Over
                    }
                }
            }
        }
    }

    // metodo que cambia a escena de gameOver
    private void gameOver() {
        playerBubble = null;  // Elimina cualquier burbuja controlable
        audio.playSound(gameOverSound, false);
        sceneManager.setCurrentScene(new GameOverScene(graphics, audio, score, mobile, this.backgroundColor));
    }

    // Comprueba la condición de victoria
    private void checkVictory(){
        for(int i = 0; i < COLUMNS; i++){
            if(bubbleList[0][i] != null && bubbleList[0][i].getColor() != null){
                return;
            }
        }
        victory();
    }

    // Inicia la escena de Victoria
    private void victory() {
        this.playWinSound();
        playerBubble = null; // Elimina cualquier burbuja controlable
        victoryBubbles = new ArrayList<>(); // Inicializa la lista de burbujas
        victoryAnimationActive = true; // Activa la animación de victoria
        victoryAnimationTime = 0; // Resetea el tiempo de la animación

        // Generar burbujas al azar
        Random random = new Random();
        int bubbleCount = 20; // Número de burbujas que caen
        int maxSpeed = 200; // Velocidad máxima de caída
        int minSpeed = 50;  // Velocidad mínima de caída

        for (int i = 0; i < bubbleCount; i++) {
            int x = random.nextInt(logicWidth - BUBBLE_RADIUS * 2); // Posición X aleatoria dentro de los límites
            int y = -random.nextInt(logicHeight / 2); // Inicia fuera de la pantalla, con un rango aleatorio
            ColorEnum color = ColorEnum.values()[random.nextInt(ColorEnum.values().length -1)];

            // Crear burbuja con velocidad aleatoria y marcarla como victory bubble
            PlayerBubble bubble = new PlayerBubble(x, y, BUBBLE_RADIUS, color, true);
            bubble.setSpeed(0, random.nextInt(maxSpeed - minSpeed + 1) + minSpeed); // Velocidad aleatoria de caída
            victoryBubbles.add(bubble);
        }
    }

    // -- DIBUJADO DE BURBUJAS PERSONALIZADAS CON ESCALADO CORRECTO --
    public void drawBlueberry(Graphics graphics, int ballX, int ballY){
        float proportion = (float) BUBBLE_RADIUS / blueBubbleImage.getWidth();
        float scale = 2f * proportion;
        graphics.drawImageWithScale(blueBubbleImage, (int)ballX, (int)ballY,scale, scale);
    }

    public void drawLemon(Graphics graphics, int ballX, int ballY){
        float proportion = (float) BUBBLE_RADIUS / yellowBubbleImage.getWidth();
        float scale = 2f * proportion;
        int offsetY = -2;
        graphics.drawImageWithScale(yellowBubbleImage, (int) ballX, (int) ballY + offsetY, scale, scale * 0.8f);
    }

    public void drawApple(Graphics graphics, int ballX, int ballY){
        float proportion = (float) BUBBLE_RADIUS / redBubbleImage.getWidth();
        float scale = 2f * proportion;
        int offsetY = -3;
        graphics.drawImageWithScale(redBubbleImage, (int)ballX, (int)ballY + offsetY,scale * 1.1f, scale);
    }

    public void drawPear(Graphics graphics, int ballX, int ballY){
        float proportion = (float) BUBBLE_RADIUS / greenBubbleImage.getWidth();
        float scale = 2f * proportion;
        int offsetY = -8;
        graphics.drawImageWithScale(greenBubbleImage, (int)ballX, (int)ballY + offsetY,scale * 1.05f, scale * 0.7f);
    }

    public void drawGray(Graphics graphics, int ballX, int ballY){
        float proportion = (float) BUBBLE_RADIUS / greyBubbleImage.getWidth();
        float scale = 2f * proportion;
        graphics.drawImageWithScale(greyBubbleImage, (int)ballX, (int)ballY,scale, scale);
    }

    // -- METODOS DE GUARDADO AL SALIR EN MEDIO DEL NIVEL --
    public List<Integer> getBubblesToLaunch(){
        List<Integer> listToReturn = new ArrayList<Integer>();
        for(int i = 0; i < bubblesToThrow.size(); i++){
            listToReturn.add(getIdFromColor(bubblesToThrow.get(i)));
        }
        return listToReturn;
    }

    public int [][] getCurrentGrid(){
        int[][] boardToReturn = new int[TOTALROWS][COLUMNS];
        for(int i = 0; i < bubbleList.length; i++){
            for(int j = 0; j < bubbleList[i].length; j++){
                if (bubbleList[i][j] != null) {
                    boardToReturn[i][j] = getIdFromColor(bubbleList[i][j].getColor());
                } else {
                    boardToReturn[i][j] = 0; // vacio
                }
            }
        }
        return boardToReturn;
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
