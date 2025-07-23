// Este paquete contiene la lógica principal del juego.
package com.practica1.gamelogic;

// Importaciones necesarias para utilizar las clases del motor y otros recursos.
import com.practica1.engine.Audio;
import com.practica1.engine.Font;
import com.practica1.engine.Graphics;
import com.practica1.engine.Image;
import com.practica1.engine.Mobile;
import com.practica1.engine.Sound;
import com.practica1.engine.State;
import com.practica1.engine.TouchEvent;
import java.util.List;
import java.util.Objects;

// Clase auxiliar para manejar la información de las frutas en la tienda.
class FruitStruct {
    String name; // Nombre de la fruta.
    Boolean purchased; // Indica si la fruta ha sido comprada.
    Boolean selected; // Indica si la fruta está seleccionada.
    Image image; // Imagen de la fruta.
    int posX; // Posición X de la fruta en la pantalla.
    int posY; // Posición Y de la fruta en la pantalla.
    Float scaleX; // Escala horizontal de la fruta.
    Float scaleY; // Escala vertical de la fruta.
    int price; // Precio de la fruta en monedas.
}

// Clase principal de la tienda, implementando la interfaz State.
public class Shop implements State {

    SceneManager sceneManager; // Gestor de escenas para cambiar entre diferentes pantallas.
    private Image closeImage; // Imagen para el botón de cerrar.
    private Font font; // Fuente para el encabezado.
    private Font fontItem; // Fuente para los nombres de los colores.
    private Mobile mobile; // Objeto para interactuar con funciones del dispositivo móvil.
    private Boolean isRunningOnEmulator;
    // Coordenadas y escalas para el botón de cerrar.
    private final int CLOSEX = 10; // Coordenada X del botón de cerrar.
    private final int CLOSEY = 10; // Coordenada Y del botón de cerrar.
    private final float CLOSESCALE = 0.15f; // Escala del botón de cerrar.

    // Configuración de la cuadrícula de colores.
    private int rows = 6; // Número de filas de colores.
    private int cols = 3; // Número de columnas de colores.
    private int spacing = 30; // Espaciado entre los elementos.
    private float imageScale = 0.22f; // Escala de las imágenes.

    // Configuración inicial de las posiciones de las frutas.
    private Image coinImage; // Imagen de la moneda usada para las compras.
    private int startX = 50; // Posición inicial X de la cuadrícula.
    private int startY = 250; // Posición inicial Y de la cuadrícula.
    private int fruitWidth = 100; // Ancho de cada fruta.
    private int fruitHeight = 100; // Altura de cada fruta.
    private final int topMargin = 10; // Margen superior para evitar superposición.
    private final int bottomMargin = 100; // Margen inferior.
    private int extraImageY = 600; // Coordenada Y adicional para posicionar frutas.

    private ColorEnum selectedBackgroundColor; // Color de fondo seleccionado.
    private boolean[] purchasedColors; // Array que indica qué colores han sido comprados.
    private FruitStruct[] fruits = {null, null, null, null, null}; // Lista de frutas disponibles en la tienda.

    // Audio y efectos de sonido.
    private Audio audio; // Motor de audio.
    private Sound cashSound; // Sonido al realizar una compra.

    private float alpha = 255; // Transparencia para efectos de desvanecimiento.
    private boolean fadingOut = false; // Indica si la pantalla se está desvaneciendo.

    int logicWidth, logicHeight; // Dimensiones lógicas de la pantalla.

    // Constructor de la tienda.
    public Shop(Graphics graphics, ColorEnum colorEnum, Mobile _mobile) {
        this.selectedBackgroundColor = colorEnum; // Establece el color de fondo seleccionado.
        this.sceneManager = SceneManager.getInstance(); // Obtiene la instancia del gestor de escenas.
        this.audio = SceneManager.getInstance().getEngine().getAudio(); // Inicializa el motor de audio.
        this.closeImage = graphics.newImage("close.png"); // Carga la imagen del botón de cerrar.
        this.coinImage = graphics.newImage("1coin.png"); // Carga la imagen de las monedas.
        this.mobile = _mobile; // Establece el dispositivo móvil asociado.
        //this.isRunningOnEmulator = this.mobile.isRunningOnEmulator();
        // Carga las fuentes necesarias.
        font = graphics.newFont("blow.ttf", 50, false, true);
        fontItem = graphics.newFont("fff.ttf", 14, false, true);

        // Inicializa los colores comprados desde el gestor de escenas.
        purchasedColors = new boolean[ColorEnum.values().length];
        for (int i = 0; i < purchasedColors.length; i++) {
            purchasedColors[i] = sceneManager.purchasedColors[i];
        }

        // Inicializa las frutas de la tienda.
        initializeFruits(graphics);

        // Carga el sonido de la caja registradora.
        cashSound = audio.newSound("cash.wav");

        // Obtiene las dimensiones lógicas de la pantalla.
        logicWidth = sceneManager.getLogicWidth();
        logicHeight = sceneManager.getLogicHeight();
    }

    // Método para inicializar las frutas disponibles en la tienda.
    private void initializeFruits(Graphics graphics) {
        // Inicializa cada fruta con sus atributos específicos como nombre, imagen, estado de compra/selección, posición y escala.
        fruits[0] = createFruit("apple", graphics.newImage("apple.png"), sceneManager.getCustomizationApple(), sceneManager.selectedApple, startX, extraImageY, 0.04f, 0.04f, 2);
        fruits[1] = createFruit("pear", graphics.newImage("pear.png"), sceneManager.getCustomizationPear(), sceneManager.selectedPear, startX + fruitWidth + spacing, extraImageY, 0.025f, 0.025f, 2);
        fruits[2] = createFruit("lemon", graphics.newImage("lemon.png"), sceneManager.getCustomizationLemon(), sceneManager.selectedLemon, startX + 2 * (fruitWidth + spacing), extraImageY, 0.03f, 0.03f, 2);
        fruits[3] = createFruit("greyBubble", graphics.newImage("grey.png"), sceneManager.getCustomizationGrey(), sceneManager.selectedGris, startX, extraImageY + fruitHeight + spacing, 0.08f, 0.08f, 2);
        fruits[4] = createFruit("blueberry", graphics.newImage("blueberry.png"), sceneManager.getCustomizationBlueBerry(), sceneManager.selectedBerry, startX + fruitWidth + spacing, extraImageY + fruitHeight + spacing, 0.15f, 0.15f, 2);
    }

    // Método para crear un objeto de tipo FruitStruct.
    private FruitStruct createFruit(String name, Image image, boolean purchased, boolean selected, int posX, int posY, float scaleX, float scaleY, int price) {
        FruitStruct fruit = new FruitStruct(); // Crea una nueva instancia de FruitStruct.
        fruit.name = name; // Asigna el nombre de la fruta.
        fruit.image = image; // Asocia la imagen correspondiente.
        fruit.purchased = purchased; // Indica si la fruta ha sido comprada.
        fruit.selected = selected; // Indica si la fruta está seleccionada.
        fruit.posX = posX; // Establece la posición X de la fruta.
        fruit.posY = posY; // Establece la posición Y de la fruta.
        fruit.scaleX = scaleX; // Establece la escala horizontal.
        fruit.scaleY = scaleY; // Establece la escala vertical.
        fruit.price = purchased ? 0 : price; // Si la fruta ya está comprada, su precio es 0.
        return fruit; // Devuelve la fruta creada.
    }

    // Método que se ejecuta en cada actualización del juego.
    @Override
    public void update(double deltaTime) {
        if (fadingOut) { // Verifica si se está realizando un desvanecimiento.
            alpha += 300 * deltaTime; // Incrementa la transparencia (fade-in).
            if (alpha >= 255) { // Si la transparencia alcanza el máximo:
                alpha = 255;
                // Cambia la escena actual a la escena de introducción.
                sceneManager.setCurrentScene(new IntroScene(sceneManager.getEngine(), sceneManager.getMobile(), selectedBackgroundColor));
            }
        } else {
            alpha -= 300 * deltaTime; // Reduce la transparencia (fade-out).
            if (alpha <= 0) {
                alpha = 0; // Evita que alpha sea menor que 0.
            }
        }
    }

    // Método que dibuja los elementos en pantalla.
    @Override
    public void render(Graphics graphics) {
        renderBackground(graphics); // Dibuja el fondo.
        renderColors(graphics); // Dibuja los colores disponibles.
        renderFruits(graphics); // Dibuja las frutas.
        renderHeader(graphics); // Dibuja el encabezado.
        renderFadeEffect(graphics); // Aplica el efecto de desvanecimiento.
    }

    // Método que renderiza el fondo de la tienda.
    private void renderBackground(Graphics graphics) {
        graphics.setColor(graphics.newColor(120, selectedBackgroundColor.getR(), selectedBackgroundColor.getG(), selectedBackgroundColor.getB())); // Establece el color de fondo.
        graphics.fillRectangle(-20, 0, logicWidth + 40, logicHeight); // Dibuja un rectángulo que cubre toda la pantalla.
    }

    // Método para renderizar los colores disponibles en la tienda.
    private void renderColors(Graphics graphics) {
        int colorIndex = 0; // Índice para iterar sobre los colores.
        ColorEnum[] colors = ColorEnum.values(); // Obtiene la lista de colores disponibles.

        for (int row = 0; row < rows; row++) { // Itera por filas.
            for (int col = 0; col < cols; col++) { // Itera por columnas.
                if (colorIndex >= colors.length) { // Si ya se han procesado todos los colores:
                    return; // Termina el renderizado.
                }

                int x = startX + col * ((int) (closeImage.getWidth() * imageScale) + spacing); // Calcula la posición X del color.
                int y = startY + row * ((int) (closeImage.getHeight() * imageScale) + spacing); // Calcula la posición Y del color.

                if (y > topMargin && y < (logicHeight - bottomMargin)) { // Verifica que el color esté dentro de los márgenes visibles.
                    ColorEnum currentColor = colors[colorIndex]; // Obtiene el color actual.
                    int colorIndexInArray = currentColor.ordinal(); // Obtiene el índice ordinal del color.

                    // Establece el color de fondo para el rectángulo del color.
                    graphics.setColor(purchasedColors[colorIndexInArray] ? graphics.newColor(200, currentColor.getR(), currentColor.getG(), currentColor.getB()) : graphics.newColor(currentColor.getA(), currentColor.getR(), currentColor.getG(), currentColor.getB()));
                    graphics.fillRoundRectangle(x, y, (int) (closeImage.getWidth() * imageScale), (int) (closeImage.getHeight() * imageScale), 20, 20); // Dibuja el rectángulo redondeado del color.

                    graphics.drawText(currentColor.name(), fontItem, x, y + (int) (closeImage.getHeight() * imageScale) + 20); // Dibuja el nombre del color.

                    if (!purchasedColors[colorIndexInArray]) { // Si el color no ha sido comprado:
                        graphics.drawImageWithScale(coinImage, x + 80, y + 100, 0.02f, 0.02f); // Dibuja el icono de moneda.
                    }

                    colorIndex++; // Incrementa el índice para el próximo color.
                }
            }
        }
    }

    // Metodo para dibujar las frutas como una matriz
    private void renderFruits(Graphics graphics) {
        for (FruitStruct fruit : fruits) {
            graphics.setColor(fruit.selected ? graphics.newColor(255, 255, 0, 128) : graphics.newColor(255, selectedBackgroundColor.getR(), selectedBackgroundColor.getG(), selectedBackgroundColor.getB()));
            graphics.fillRoundRectangle(fruit.posX, fruit.posY, fruitWidth, fruitHeight, 20, 20);

            graphics.drawImageWithScale(fruit.image, fruit.posX + (fruitWidth - (int) (fruit.image.getWidth() * fruit.scaleX)) / 2, fruit.posY + (fruitHeight - (int) (fruit.image.getHeight() * fruit.scaleY)) / 2, fruit.scaleX, fruit.scaleY);

            if (!fruit.purchased) { // Si no se ha comprado la fruta, dibuja una moneda, indicando que se puede comprar

                graphics.drawImageWithScale(coinImage, fruit.posX + 70, fruit.posY + 85, 0.02f, 0.02f);
                graphics.drawImageWithScale(coinImage, fruit.posX + 50, fruit.posY + 85, 0.02f, 0.02f);
            }
        }
    }


    // Dibuja el banner en la parte superior de la pantalla
    // Dibuja el botón de close, un rectangulo del mismo color que ha elegido el usuario pero con una opacidad mayor
    // Muestra en pantalla las monedas del usuario y una imagen de una moneda
    private void renderHeader(Graphics graphics) {
        graphics.setColor(graphics.newColor(255, selectedBackgroundColor.getR(), selectedBackgroundColor.getG(), selectedBackgroundColor.getB()));
        graphics.fillRectangle(-20, -20, logicWidth + 40, 120);
        graphics.drawImageWithScale(closeImage, CLOSEX, 20, CLOSESCALE * 0.9f, CLOSESCALE * 0.9f);
        graphics.setColor(graphics.newColor(255, 0, 0, 0));
        graphics.drawText("TIENDA", font, logicWidth / 3, 84);
        graphics.drawImageWithScale(coinImage, logicWidth - 150, 35, 0.03f, 0.03f);
        graphics.drawText(String.valueOf(sceneManager.getCoins()), font, logicWidth - 100, 84);
    }

    private void renderFadeEffect(Graphics graphics) {
        graphics.setColor(graphics.newColor(0, 0, 0, (int) alpha));
        graphics.fillRectangle(0, 0, logicWidth, logicHeight);
    }

    @Override
    public void handleInput(List<TouchEvent> events) {
        for (TouchEvent event : events) {
            if (event.type == TouchEvent.TouchEventType.TOUCH_DOWN) {
                if (isTouchingClose(event.x, event.y)) {
                    fadingOut = true;
                    return;
                }

                ColorEnum clickedColor = getClickedColor(event.x, event.y);
                if (clickedColor != null) {
                    handleColorSelection(clickedColor);
                    return;
                }

                for (FruitStruct fruit : fruits) {
                    if (isTouchingFruit(event.x, event.y, fruit.posX, fruit.posY)) {
                        purchaseFruit(fruit);
                        return;
                    }
                }
            }
        }
    }

    private void handleColorSelection(ColorEnum clickedColor) {
        int colorIndex = clickedColor.ordinal();
        if (!purchasedColors[colorIndex]) {
            if (sceneManager.getCoins() >= 1) {
                sceneManager.addCoins(-1);
                purchasedColors[colorIndex] = true;
                sceneManager.setPurchasedColor(colorIndex, true);
            } else {
                if(!this.isRunningOnEmulator) {
                    mobile.vibrateDevice(1000);
                }
            }
        }
        selectedBackgroundColor = clickedColor;
        sceneManager.changeBackgroundColor(clickedColor);
    }

    // Metodo auxiliar que devuelve la fruta que está tocando
    private boolean isTouchingFruit(int touchX, int touchY, int fruitX, int fruitY) {
        return touchX >= fruitX && touchX <= fruitX + fruitWidth && touchY >= fruitY && touchY <= fruitY + fruitHeight;
    }

    // Metodo para comprobar si se puede o no puede comprar la fruta
    private void purchaseFruit(FruitStruct fruit) {
        // Si el usuario tiene las mismas monedas o más monedas que el coste de la fruta resta el coste y compra la fruta
        if (sceneManager.getCoins() >= fruit.price) {
            sceneManager.addCoins(-fruit.price);

            if (!fruit.purchased) { // Si no se ha comprado la fruta y puede comprarla procede a comprarla
                audio.playSound(cashSound, false);
                fruit.purchased = true;
                fruit.price = 0;
                switch (fruit.name) {
                    case "apple" -> sceneManager.setApple(true);
                    case "pear" -> sceneManager.setPear(true);
                    case "lemon" -> sceneManager.setLemon(true);
                    case "greyBubble" -> sceneManager.setGrey(true);
                    case "blueberry" -> sceneManager.setBlueBerry(true);
                }
            }
            fruit.selected = !fruit.selected;
            switch (fruit.name) {
                case "apple" -> sceneManager.selectedApple = fruit.selected;
                case "pear" -> sceneManager.selectedPear = fruit.selected;
                case "lemon" -> sceneManager.selectedLemon = fruit.selected;
                case "greyBubble" -> sceneManager.selectedGris = fruit.selected;
                case "blueberry" -> sceneManager.selectedBerry = fruit.selected;
            }
        } else {
            if (!this.isRunningOnEmulator) {
                mobile.vibrateDevice(1000); // Si no se puede comprar la fruta, vibra el teléfono
            }
        }
    }


    // Metodo auxiliar para devolver el color escogido
    private ColorEnum getClickedColor(int touchX, int touchY) {
        int itemWidth = (int) (closeImage.getWidth() * imageScale);
        int itemHeight = (int) (closeImage.getHeight() * imageScale);

        int colorIndex = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (colorIndex >= ColorEnum.values().length) {
                    return null;
                }

                int x = startX + col * (itemWidth + spacing);
                int y = startY + row * (itemHeight + spacing);

                if (touchX >= x && touchX <= x + itemWidth && touchY >= y && touchY <= y + itemHeight) {
                    return ColorEnum.values()[colorIndex];
                }

                colorIndex++;
            }
        }
        return null;
    }

    // Metodo auxiliar para saber si toca el botón de cerrar
    private boolean isTouchingClose(int touchX, int touchY) {
        int closeButtonWidth = Math.round(closeImage.getWidth() * CLOSESCALE);
        int closeButtonHeight = Math.round(closeImage.getHeight() * CLOSESCALE);

        return touchX >= CLOSEX && touchX <= CLOSEX + closeButtonWidth && touchY >= CLOSEY && touchY <= CLOSEY + closeButtonHeight;
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
