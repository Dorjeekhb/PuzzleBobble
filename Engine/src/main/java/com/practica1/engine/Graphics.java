package com.practica1.engine;
import java.awt.Paint;

public interface Graphics {

    Color newColor(int a, int r, int g, int b);

    Image newImage(String name);

    Font newFont(String filename, int size, boolean isBold, boolean isItalic);

    void clear(int color);


    // Método para escalar y dibujar solo una imagen
    public void drawImageWithScale(Image image, int x, int y, float scaleX, float scaleY);


    /* ○ Métodos de control de la transformación sobre el canvas
            (translate(x,y), scale(x,y); save(), restore()). Las operaciones
    de dibujado se verán afectadas por la transformación establecida. */

    void drawImage(Image image); //igual hay que crear mas drawImages

    void setColor(Color color);

    void fillRectangle(int cx, int cy, int width, int height);

    void fillRoundRectangle(int cx, int cy, int width, int height, int arc, int arcHeight);

    void drawRectangle(int cx, int cy, int width, int height);

    void drawLine(int initX, int initY, int endX, int endY);

    void drawHexagon(float x, float y, float radius);


    void fillCircle(float cx, float cy, float radius);

    void cleanup();

    void drawText(String s, Font text, int x, int y);

    public void setFont(Font font);

    public default void drawCircle(float x, float y, float radius, Paint paint) {
        Graphics canvas = null;
        canvas.drawCircle(x, y, radius, paint);
    }
}
