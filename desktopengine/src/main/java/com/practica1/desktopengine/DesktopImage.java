package com.practica1.desktopengine;

import com.practica1.engine.Image;
import java.awt.image.BufferedImage;

public class DesktopImage implements Image {
    private BufferedImage bufferedImage;

    // Constructor que recibe el BufferedImage cargado
    public DesktopImage(BufferedImage bufferedImage) {
        this.bufferedImage = bufferedImage;
    }

    // Método para obtener el BufferedImage
    public BufferedImage getBufferedImage() {
        return bufferedImage;
    }

    @Override
    public int getWidth() {
        return bufferedImage.getWidth();
    }

    @Override
    public int getHeight() {
        return bufferedImage.getHeight();
    }
}
