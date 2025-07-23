package com.practica1.desktopengine;

import com.practica1.engine.Color;

public class DesktopColor implements Color {
    private java.awt.Color myColor;

    public DesktopColor(){
        myColor = null;
    }

    @Override
    public void setColor(int a, int r, int g, int b) {
        myColor = new java.awt.Color(r,g,b,a);
    }

    public java.awt.Color getMyColor() {
        return  myColor;
    }
}
