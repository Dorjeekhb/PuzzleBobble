package com.practica1.desktopengine;

import com.practica1.engine.Color;
import com.practica1.engine.Font;
import com.practica1.engine.Graphics;
import com.practica1.engine.Image;
import java.awt.geom.AffineTransform;
import java.awt.Graphics2D;
import java.awt.image.BufferStrategy;
import java.awt.FontFormatException;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import java.awt.image.BufferedImage;
import java.io.File;

public class DesktopGraphics implements Graphics {
    private Graphics2D graphics2D;
    private JFrame myView;
    private BufferStrategy bufferStrategy;

    public DesktopGraphics(JFrame myView) {
        this.myView = myView;
        this.bufferStrategy = this.myView.getBufferStrategy();
        this.graphics2D = (Graphics2D) bufferStrategy.getDrawGraphics();
    }

    public boolean endFrame() {
        graphics2D.dispose();
        graphics2D = null;
        if (bufferStrategy.contentsRestored()) {
            return false;
        }
        bufferStrategy.show();
        return !bufferStrategy.contentsLost();
    }

    @Override
    public void drawText(String text, Font font, int x, int y) {
        if (graphics2D != null && font != null) {
            java.awt.Font awtFont = ((DesktopFont) font).getAwtFont();
            if (awtFont != null) {
                graphics2D.setFont(awtFont);
                graphics2D.drawString(text, x, y);
            } else {
                System.out.println("Error: La fuente awtFont es null.");
            }
        } else {
            System.out.println("Error: graphics2D o font es null.");
        }
    }



    public Font newFont(String filename, int size, boolean isBold, boolean isItalic) {
        try {
            return new DesktopFont("data/assets/fonts/" + filename, size, isBold, isItalic);
        } catch (FileNotFoundException e) {
            System.out.println("Archivo de fuente no encontrado: " + e.getMessage());
        } catch (FontFormatException e) {
            System.out.println("Formato de fuente inválido: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error de E/S al cargar la fuente: " + e.getMessage());
        }
        return null;
    }


    public DesktopImage loadImage(String path) {
        try {
            BufferedImage bufferedImage = ImageIO.read(new File(path));
            return new DesktopImage(bufferedImage);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void prepareFrame(int logicW, int logicH) {
        int w = this.getWidth();
        int h = this.myView.getHeight();
        float wProportion = (float) w / logicW;
        float hProportion = (float) h / logicH;
        float scale;

        // Determinar la escala mínima para que el contenido se ajuste
        if (wProportion > hProportion) {
            scale = hProportion;
        } else {
            scale = wProportion;
        }

        // Calcular el offset para centrar el contenido
        int offsetX = (int) ((w - (logicW * scale)) / 2);
        int offsetY = (int) ((h - (logicH * scale)) / 2);

        graphics2D = (Graphics2D) bufferStrategy.getDrawGraphics();
        clear(0XFFFFFFFF);
        this.graphics2D.setPaintMode();

        // Aplica la traducción para centrar el contenido
        this.graphics2D.translate(offsetX, offsetY);
        // Aplica la escala calculada
        this.graphics2D.scale(scale, scale);
    }



    @Override
    public Color newColor(int a, int r, int g, int b) {
        DesktopColor dColor = new DesktopColor();
        dColor.setColor(a, r, g, b);
        return dColor;
    }

    @Override
    public void clear(int color) {
        this.graphics2D.setColor(new java.awt.Color(color, true));
        this.graphics2D.fillRect(0, 0, getWidth(), myView.getHeight());
    }

    @Override
    public void drawImageWithScale(Image image, int x, int y, float scaleX, float scaleY) {
        AffineTransform originalTransform = graphics2D.getTransform();  // Guarda la transformación original

        // Aplica la escala deseada y dibuja la imagen
        this.graphics2D.translate(x, y);
        this.graphics2D.scale(scaleX, scaleY);

        if (image instanceof DesktopImage) {
            DesktopImage desktopImage = (DesktopImage) image;
            this.graphics2D.drawImage(desktopImage.getBufferedImage(), 0, 0, null);
        }

        // Restaura la transformación original para que no afecte a otros elementos
        this.graphics2D.setTransform(originalTransform);
    }

    @Override
    public void drawImage(Image image) {

    }

    @Override
    public void setColor(Color color) {
        this.graphics2D.setColor(((DesktopColor) color).getMyColor());
    }

    @Override
    public void fillRectangle(int cx, int cy, int width, int height) {
        this.graphics2D.fillRect(cx, cy, width, height);
    }

    @Override
    public void fillRoundRectangle(int cx, int cy, int width, int height, int arc, int arcHeight) {
        this.graphics2D.fillRoundRect(cx, cy, width, height, arc, arc);
    }

    @Override
    public void drawRectangle(int cx, int cy, int width, int height) {
        this.graphics2D.drawRect(cx, cy, width, height);
    }

    @Override
    public void drawRoundRectangle(int cx, int cy, int width, int height, int arc) {
        this.graphics2D.drawRoundRect(cx, cy, width, height, arc, arc);
    }

    @Override
    public void drawLine(int initX, int initY, int endX, int endY) {
        this.graphics2D.drawLine(initX, initY, endX, endY);
    }

    @Override
    public void drawHexagon(float x, float y, float radius) {
        int[] xPoints = new int[6];
        int[] yPoints = new int[6];

        // Calculamos las coordenadas de los seis vértices del hexágono
        for (int i = 0; i < 6; i++) {
            xPoints[i] = (int) (x + radius * Math.cos(Math.toRadians(60 * i + 30)));
            yPoints[i] = (int) (y + radius * Math.sin(Math.toRadians(60 * i + 30)));
        }

        // Dibujamos el hexágono
        this.graphics2D.drawPolygon(xPoints, yPoints, 6);
    }

    @Override
    public void drawCircle(float cx, float cy, float radius) {
        this.graphics2D.drawOval((int) cx, (int) cy, (int) radius * 2, (int) radius * 2);
    }

    @Override
    public void fillCircle(float cx, float cy, float radius) {
        this.graphics2D.fillOval((int) cx, (int) cy, (int) radius * 2, (int) radius * 2);
    }

    @Override
    public void setFont(Font font) {

    }

    // Método para cargar una imagen desde el sistema de archivos
    public Image newImage(String filePath) {
        String file = "data/assets/sprites/" + filePath;
        try {
            BufferedImage bufferedImage = ImageIO.read(new File(file));
            return new DesktopImage(bufferedImage);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }



    public int getWidth() {
        return myView.getWidth();
    }
    public int getHeight() {
        return myView.getHeight();
    }
}
