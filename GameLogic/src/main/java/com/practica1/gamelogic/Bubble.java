package com.practica1.gamelogic;

import com.practica1.engine.Color;
import com.practica1.engine.Graphics;

import java.util.Random;

public class Bubble {
    // Variables para representar la burbuja
    protected float ballX, ballY, ballRadius;
    ColorEnum colorBubble; // color de la burbuja
    public boolean hex = false; // booleano para dibujar el hexagono en el que esta contenida la burbuja
    boolean alredyPainted; // booleano para dibujar la burbuja una unica vez

    private int xIndex, yIndex; // indices logicos de la burbuja en el grid

    public Bubble(int posx, int posy, int radius, int ix, int iy) {
        // inicializacion de variables
        this.ballX = posx;
        this.ballY = posy;
        this.ballRadius = radius;
        this.xIndex = ix;
        this.yIndex = iy;
    }

    // renderizado de la burbuja y/o hexagono
    public void render(Graphics graphics, Grid grid) {
        if (colorBubble != null) { // si la burbuja no tiene color no se pinta nada
            alredyPainted = false; // actualizacion de la variable de control

            // seleccion de imagen en caso de que haya customizacion desbloqueada
            if (colorBubble == ColorEnum.RED && SceneManager.getInstance().selectedApple) { // rojo
                grid.drawApple(graphics, (int)ballX, (int)ballY);
                alredyPainted = true;
            }
            else if (colorBubble == ColorEnum.YELLOW && SceneManager.getInstance().selectedLemon) { // amarillo
                grid.drawLemon(graphics, (int)ballX, (int)ballY);
                alredyPainted = true;
            }
            else if (colorBubble == ColorEnum.GREEN && SceneManager.getInstance().selectedPear) { // verde
                grid.drawPear(graphics, (int)ballX, (int)ballY);
                alredyPainted = true;
            }
            else if (colorBubble == ColorEnum.BLUE && SceneManager.getInstance().selectedBerry) { // azul
                grid.drawBlueberry(graphics, (int)ballX, (int)ballY);
                alredyPainted = true;
            }
            else if (colorBubble == ColorEnum.GRAY && SceneManager.getInstance().selectedGris) { // gris
                grid.drawGray(graphics, (int)ballX, (int)ballY);
                alredyPainted = true;
            }

            if (!alredyPainted) { // si no se ha pintado (no esta customizado el color)
                // renderiza burbujas normales sin customizar
                Color bubbleColor = graphics.newColor(colorBubble.getA(), colorBubble.getR(), colorBubble.getG(), colorBubble.getB());
                graphics.setColor(bubbleColor);
                graphics.fillCircle(ballX, ballY, ballRadius);
            }
        }
        if (hex) { //dibujado de hexagonos
            graphics.setColor(graphics.newColor(255, 0, 0, 0));
            graphics.drawHexagon(ballX + ballRadius, ballY + ballRadius, ballRadius + 3);
        }
    }

    // cambio de color de la burbuja
    public void setBubbleColor(ColorEnum color){
        this.colorBubble = color;
    }

    // cambio de variable para renderizar hexagono a true
    public void showHex(){
        hex = true;
    }

    // cambio de variable para renderizar hexagono a true
    public void hideHex(){
        hex = false;
    }

    // GETTERS
    public float getBallX() {
        return ballX;
    }

    public float getBallY() {
        return ballY;
    }

    public float getBallRadius() {
        return ballRadius;
    }

    public ColorEnum getColor(){
        return this.colorBubble;
    }

    public int getCol() { return xIndex; }

    public int getRow() { return yIndex; }
}
