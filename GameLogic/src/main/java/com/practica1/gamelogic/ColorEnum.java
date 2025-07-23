package com.practica1.gamelogic;

// clase para usar colores genericos sin tener que crearlos
public enum ColorEnum {
    RED(255, 255, 0, 0),
    GREEN(255, 0, 255, 0),
    BLUE(255, 0, 0, 255),
    YELLOW(255, 255, 255, 0),
    GRAY(255, 87, 88, 87),
    WHITE(255, 255, 255, 255);

    private int a, r, g, b;

    ColorEnum(int a, int r, int g, int b) {
        this.a = a;
        this.r = r;
        this.g = g;
        this.b = b;
    }

    // GETTERS
    public int getA() {
        return a;
    }

    public int getR() { return r; }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;
    }

}