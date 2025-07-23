package com.practica1.gamelogic;

import com.google.gson.Gson;
import com.practica1.engine.Engine;
import com.practica1.engine.File;
import com.practica1.engine.Mobile;
import com.practica1.engine.State;
import java.util.List;

// Clase que representa el estado del juego para guardar y cargar progreso
class GameState {
    int nCoins; // Número de monedas del jugador
    int currentLevel; // Nivel actual
    int lastLevel; // Último nivel jugado
    String backgroundColor; // Color de fondo actual
    boolean apple, lemon, berry, pear, gray; // Estados de desbloqueo de colores
    boolean s_apple, s_lemon, s_berry, s_pear, s_gray; // Estados de selección de colores
    List<Integer> bubblesToLaunch; // Lista de burbujas pendientes por lanzar
    int[][] initialBoard; // Estado inicial del tablero
    boolean[] purchasedColors; // Colores comprados
}

public class SceneManager {
    // Singleton: instancia única de SceneManager
    private static SceneManager instance = null;

    private Engine engine; // Motor del juego
    private Mobile mobile; // Información sobre el dispositivo móvil
    public String world; // Mundo actual
    private File file; // Archivo de guardado
    private String levelName; // Nombre del nivel actual
    private ColorEnum initialColor = ColorEnum.GRAY; // Color inicial por defecto
    private int logicWidth, logicHeight; // Dimensiones lógicas del juego
    private State currentScene; // Escena actual
    public boolean isFirstLevel = true; // Indica si es el primer nivel de la ejecución
    public boolean[] purchasedColors; // Array de colores comprados

    // Variables de guardado
    public int lastLevelPlayed = 0; // Último nivel jugado
    public int level = 1; // Nivel actual
    public static final int BANNER_HEIGHT = 150; // Altura fija del banner
    private int coins = 10; // Monedas iniciales
    private ColorEnum bColor = ColorEnum.WHITE; // Color de fondo predeterminado
    private boolean customizationApple, customizationLemon, customizationBerry, customizationPear, customizarGris; // Colores desbloqueados
    public boolean selectedApple, selectedLemon, selectedBerry, selectedPear, selectedGris; // Colores seleccionados
    private List<Integer> currentBubblesToLaunch; // Lista de burbujas actuales
    private int[][] currentBoard; // Tablero actual

    public boolean adventure; // Indica si el modo aventura está activo
    public boolean levelOnCourse = false; // Indica si un nivel está en curso
    private boolean shouldUpdateLevels = true; // Controla si los niveles deben actualizarse

    // Variables relacionadas con mundos y niveles
    public int NMUNDOS; // Número total de mundos
    public int[] WORLDSIZE; // Tamaño de cada mundo en niveles

    // Método para obtener la instancia única del Singleton
    public static synchronized SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    // Constructor privado para el Singleton
    private SceneManager() {
        logicWidth = 0;
        logicHeight = 0;
        currentScene = null;
        engine = null;
        purchasedColors = new boolean[ColorEnum.values().length]; // Inicializar array de colores
        for (int i = 0; i < purchasedColors.length; i++) {
            purchasedColors[i] = false; // Marcar todos los colores como no comprados
        }
    }

    // Inicializa el SceneManager con el motor, dimensiones y archivo de guardado
    public void Init(Engine engine, Mobile mobile, int width, int height, String jsonPath) {
        this.engine = engine;
        logicWidth = width;
        logicHeight = height;

        // Carga los mundos y niveles desde los assets
        loadWorldsAndLevels();

        // Carga el progreso guardado
        file = engine.getInternalFile(jsonPath);
        shouldUpdateLevels = false;
        loadFile();
        shouldUpdateLevels = true;

        // Configura la escena inicial como IntroScene
        currentScene = new IntroScene(engine, mobile, bColor);
        engine.changeScene(currentScene);
    }

    // Carga los mundos y niveles desde los assets
    private void loadWorldsAndLevels() {
        int worldCount = 0;
        String[] worlds = engine.getAssetsList("levels"); // Obtiene la lista de mundos
        for (String world : worlds) {
            if (world.startsWith("world")) { // Filtra carpetas con el prefijo "world"
                worldCount++;
            }
        }
        NMUNDOS = worldCount; // Número total de mundos
        WORLDSIZE = new int[NMUNDOS];
        int nLevels = 0;
        for (int i = 0; i < worldCount; i++) {
            String[] levelsInWorld = engine.getAssetsList("levels/world" + (i + 1));
            for (String levelInWorld : levelsInWorld) {
                if (levelInWorld.startsWith("level")) { // Filtra niveles con el prefijo "level"
                    nLevels++;
                }
            }
            WORLDSIZE[i] = nLevels; // Tamaño del mundo en niveles
        }
    }

    // Carga el progreso guardado desde un archivo JSON
    private void loadFile() {
        String json = file.getContent();

        // Si el archivo no está vacío, carga el estado del juego
        if (json != null) {
            Gson gson = new Gson();
            GameState gameState = gson.fromJson(json, GameState.class);
            coins = gameState.nCoins;
            level = gameState.currentLevel;
            if (level < 1) level = 1;

            // Configura el color de fondo
            bColor = (gameState.backgroundColor == null) ? ColorEnum.GRAY : strToColorEnum(gameState.backgroundColor);

            // Carga los colores comprados y seleccionados
            purchasedColors = (gameState.purchasedColors != null) ? gameState.purchasedColors : purchasedColors;
            customizationApple = gameState.apple;
            customizationLemon = gameState.lemon;
            customizationBerry = gameState.berry;
            customizationPear = gameState.pear;
            customizarGris = gameState.gray;
            selectedApple = gameState.s_apple;
            selectedLemon = gameState.s_lemon;
            selectedBerry = gameState.s_berry;
            selectedPear = gameState.s_pear;
            selectedGris = gameState.s_gray;

            currentBubblesToLaunch = gameState.bubblesToLaunch;

            // Si no hay burbujas pendientes, no hay partidas a medias
            lastLevelPlayed = (currentBubblesToLaunch == null) ? 0 : gameState.lastLevel;
            currentBoard = gameState.initialBoard;
        }
    }

    // Guarda el estado del juego en un archivo JSON
    public void saveFile(String jsonPath) {
        // Crear un nuevo estado del juego
        GameState gS = new GameState();
        gS.nCoins = coins;
        gS.currentLevel = level;
        gS.lastLevel = lastLevelPlayed;
        gS.backgroundColor = colorEnumToStr(bColor);
        gS.apple = customizationApple;
        gS.lemon = customizationLemon;
        gS.berry = customizationBerry;
        gS.pear = customizationPear;
        gS.gray = customizarGris;
        gS.s_apple = selectedApple;
        gS.s_lemon = selectedLemon;
        gS.s_berry = selectedBerry;
        gS.s_pear = selectedPear;
        gS.s_gray = selectedGris;
        gS.purchasedColors = purchasedColors;

        // Si estamos en modo aventura, guarda el estado del tablero
        if (adventure && currentScene instanceof Grid grid) {
            gS.bubblesToLaunch = grid.getBubblesToLaunch();
            gS.initialBoard = grid.getCurrentGrid();
        }

        // Convierte el estado del juego a JSON y lo guarda en el archivo
        Gson gson = new Gson();
        String json = gson.toJson(gS);
        File file = engine.getInternalFile(jsonPath);
        file.setContent(json);
    }

    // Añade niveles completados
    public void addLevel(int num) {
        if (shouldUpdateLevels) {
            level += num;
        }
    }

    // Establece el nivel actual
    public void setLevelName(int num) {
        if (shouldUpdateLevels) {
            level = num;
        }
    }

    // Métodos relacionados con monedas
    public int getCoins() {
        return coins;
    }

    public void addCoins(int Coins) {
        coins += Coins;
        if (coins < 0) coins = 0; // Capar a 0
    }

    // Métodos para establecer y obtener el mundo actual
    public void setWorld(String numberWorld) {
        world = numberWorld;
    }

    public String getWorld() {
        return world;
    }

    // Cambia la escena actual
    public void setCurrentScene(State newScene) {
        if (newScene != null) {
            currentScene = newScene;
            engine.changeScene(currentScene);
        }
    }

    // Getters de dimensiones lógicas
    public int getLogicHeight() {
        return logicHeight;
    }

    public int getLogicWidth() {
        return logicWidth;
    }

    // Devuelve la instancia del motor del juego
    public Engine getEngine() {
        return engine;
    }

    public Mobile getMobile() {
        return mobile;
    }

    // Cambia el color de fondo
    public void changeBackgroundColor(ColorEnum color) {
        bColor = color;
    }

    // Métodos para obtener el estado actual del tablero
    public List<Integer> getCurrentBubblesToLaunch() {
        return currentBubblesToLaunch;
    }

    public int[][] getCurrentGrid() {
        return currentBoard;
    }

    // Métodos relacionados con el último nivel jugado
    public void setLastLevelPlayed(int lv) {
        lastLevelPlayed = lv;
    }

    public int getLastLevelPlayed() {
        return lastLevelPlayed;
    }

    public int getLevel() {
        return level;
    }

    public Boolean getAdventure() {
        return adventure;
    }

    // Métodos para personalización
    public boolean getCustomizationApple() {
        return customizationApple;
    }

    public boolean getCustomizationGrey() {
        return customizarGris;
    }

    public boolean getCustomizationPear() {
        return customizationPear;
    }

    public boolean getCustomizationBlueBerry() {
        return customizationBerry;
    }

    public boolean getCustomizationLemon() {
        return customizationLemon;
    }

    public void setPurchasedColor(int index, boolean estado) {
        purchasedColors[index] = estado;
    }

    public void setApple(boolean estado) {
        customizationApple = estado;
    }

    public void setLemon(boolean estado) {
        customizationLemon = estado;
    }

    public void setGrey(Boolean estado) {
        customizarGris = estado;
    }

    public void setBlueBerry(Boolean estado) {
        customizationBerry = estado;
    }

    public void setPear(boolean estado) {
        customizationPear = estado;
    }

    public void setAdventure(boolean estado) {
        adventure = estado;
    }

    // Conversor de String a ColorEnum
    public ColorEnum strToColorEnum(String str) {
        return switch (str) {
            case "RED" -> ColorEnum.RED;
            case "BLUE" -> ColorEnum.BLUE;
            case "YELLOW" -> ColorEnum.YELLOW;
            case "GREEN" -> ColorEnum.GREEN;
            case "WHITE" -> ColorEnum.WHITE;
            default -> ColorEnum.GRAY;
        };
    }

    // Conversor de ColorEnum a String
    public String colorEnumToStr(ColorEnum col) {
        return switch (col) {
            case RED -> "RED";
            case BLUE -> "BLUE";
            case YELLOW -> "YELLOW";
            case GREEN -> "GREEN";
            case WHITE -> "WHITE";
            default -> "GRAY";
        };
    }
}