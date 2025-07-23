package com.practica1.desktopengine;

import com.practica1.engine.Font;

import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class DesktopFont implements Font {
    private java.awt.Font awtFont;

    public DesktopFont(String file, float size, boolean bold, boolean italic) throws FileNotFoundException, FontFormatException, IOException {
        InputStream is = new FileInputStream(file);
        java.awt.Font baseFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, is);
        int style = java.awt.Font.PLAIN;
        if (bold) style |= java.awt.Font.BOLD;
        if (italic) style |= java.awt.Font.ITALIC;

        this.awtFont = baseFont.deriveFont(style, size);

        // Registrar la fuente en el entorno gráfico
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(this.awtFont);
    }

    public java.awt.Font getAwtFont() {
        return this.awtFont;
    }

    @Override
    public int getSize() {
        return this.awtFont.getSize();
    }

}
