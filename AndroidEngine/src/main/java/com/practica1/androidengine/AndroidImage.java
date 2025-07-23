package com.practica1.androidengine;

import android.graphics.Bitmap;
import com.practica1.engine.Image;

public class AndroidImage implements Image {
    private Bitmap bitmap;

    // Constructor que recibe el Bitmap cargado
    public AndroidImage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    // Método para obtener el Bitmap
    public Bitmap getBitmap() {
        return bitmap;
    }



    @Override
    public int getWidth() {
        return bitmap.getWidth();
    }

    @Override
    public int getHeight() {
        return bitmap.getHeight();
    }
}
