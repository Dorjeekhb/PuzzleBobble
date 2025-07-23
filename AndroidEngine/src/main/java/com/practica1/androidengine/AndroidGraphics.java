package com.practica1.androidengine;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.practica1.engine.Graphics;
import com.practica1.engine.Image;
import com.practica1.engine.Color;
import com.practica1.engine.Font;

import java.io.IOException;
import java.io.InputStream;


public class AndroidGraphics implements Graphics {

    private SurfaceView myView;
    private Paint paint;
    private Paint ball1Color, ball2Color;
    private Canvas canvas;
    private Context context;
    private SurfaceHolder holder;
    private float scale;
    private float offsetX, offsetY;


    public AndroidGraphics(SurfaceView myView, Context context) {
        this.myView = myView;
        this.context = context;  // Inicializar el contexto aquí
        this.holder = this.myView.getHolder();
        this.paint = new Paint();
        this.paint.setColor(0xFF000000);
        this.paint.setAntiAlias(true);

    }



    // Método para cargar una imagen desde los assets
    @Override
    public Image newImage(String assetName) {
        AssetManager assetManager = context.getAssets();
        try (InputStream inputStream = assetManager.open("sprites/" + assetName)) {
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            return new AndroidImage(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    // Método para dibujar la imagen en el Canvas

    public void endFrame() {
        this.holder.unlockCanvasAndPost(canvas);
    }

    public void prepareFrame(int logicW, int logicH) {
        while (!this.holder.getSurface().isValid());
        this.canvas = this.holder.lockHardwareCanvas();

        int w = this.canvas.getWidth();
        int h = this.canvas.getHeight();
        float wProportion = (float) w / logicW;
        float hProportion = (float) h / logicH;

        // Calcula la escala adecuada y los offsets para centrar la imagen
        scale = Math.min(wProportion, hProportion);
        offsetX = (w - (logicW * scale)) / 2;
        offsetY = (h - (logicH * scale)) / 2;

        clear(0xFFFFFFFF);
        this.canvas.translate(offsetX, offsetY);
        this.canvas.scale(scale, scale);
    }

    public float realToLogicX(float realX) {
        return (realX - offsetX) / scale;
    }

    public float realToLogicY(float realY) {
        return (realY - offsetY) / scale;
    }

    // Métodos adicionales para capturar input
    public float logicToRealX(float logicX) {
        return logicX * scale + offsetX;
    }

    public float logicToRealY(float logicY) {
        return logicY * scale + offsetY;
    }

    @Override
    public Color newColor(int a, int r, int g, int b) {
        AndroidColor aColor = new AndroidColor();
        aColor.setColor(a, r, g, b);
        return aColor;
    }


    @Override
    public Font newFont(String filename, int size, boolean isBold, boolean isItalic) {
        return new AndroidFont(context.getAssets(), "fonts/" + filename, size, isBold, isItalic);
    }

    @Override
    public void clear(int color) {
        canvas.drawColor(color);
    }

    @Override
    public void drawImageWithScale(Image image, int x, int y, float scaleX, float scaleY) {
        if (image instanceof AndroidImage) {
            AndroidImage androidImage = (AndroidImage) image;
            Bitmap bitmap = androidImage.getBitmap();

            // Guardar el estado actual del Canvas
            canvas.save();

            // Trasladar y escalar el Canvas para dibujar la imagen escalada
            canvas.translate(x, y);
            canvas.scale(scaleX, scaleY);

            // Dibujar la imagen escalada en las coordenadas ajustadas
            canvas.drawBitmap(bitmap, 0, 0, null);

            // Restaurar el estado del Canvas
            canvas.restore();
        }
    }

    @Override
    public void drawImage(Image image) {

    }

    @Override
    public void setColor(Color color) {
        this.paint.setColor(((AndroidColor)color).getMyColor());
    }


    @Override
    public void fillRectangle(int cx, int cy, int width, int height) {
        this.paint.setStyle(Paint.Style.FILL);
        this.canvas.drawRect(cx, cy, cx + width, cy + height, this.paint);
    }

    @Override
    public void fillRoundRectangle(int cx, int cy, int width, int height, int arc, int arcHeight) {
        this.paint.setStyle(Paint.Style.FILL);
        this.canvas.drawRoundRect(cx, cy, cx + width, cy + height, arc, arc, this.paint);
    }

    @Override
    public void drawRectangle(int cx, int cy, int width, int height) {
        this.paint.setStyle(Paint.Style.STROKE);
        this.canvas.drawRect(cx, cy, cx + width, cy + height, this.paint);
    }


    @Override
    public void drawLine(int initX, int initY, int endX, int endY) {
        this.canvas.drawLine(initX, initY, endX, endY, this.paint);
    }

    @Override
    public void drawHexagon(float x, float y, float radius) {
        int[] xPoints = new int[6];
        int[] yPoints = new int[6];

        // Calculamos las coordenadas de los seis vértices del hexágono
        for (int i = 0; i < 6; i++) {
            xPoints[i] = (int) (x + radius * Math.cos(Math.toRadians(60 * i + 30)));
            yPoints[i] = (int) (y + radius * Math.sin(Math.toRadians(60 * i + 30)));
            // Dibujamos el hexágono
            if (i != 0) drawLine(xPoints[i], yPoints[i], xPoints[i - 1], yPoints[i - 1]);
        }
        drawLine(xPoints[0], yPoints[0], xPoints[5], yPoints[5]);
    }


    @Override
    public void fillCircle(float cx, float cy, float radius) {
        this.paint.setStyle(Paint.Style.FILL);
        canvas.drawCircle(cx + radius, cy + radius, radius, this.paint);
    }

    @Override
    public void cleanup() {
        // Liberar el Canvas
        if (canvas != null) {
            canvas = null;
        }
        //libera las images
        if (holder != null) {
            holder = null;
        }
        if (paint != null) {
            paint = null;
        }
}


    @Override
    public void drawText(String text, Font font, int x, int y) {
        if (canvas != null && font != null) {
            paint.setTypeface(((AndroidFont) font).getFont());
            paint.setTextSize(font.getSize());
            paint.setAntiAlias(true);
            canvas.drawText(text, x, y, paint);
        } else {
            System.out.println("Error: canvas o font es null.");
        }
    }


    @Override
    public void setFont(Font font) {

    }

    public Canvas getCanvas() { return canvas; }

    public int getWidth() {
        return myView.getWidth();
    }
    public int getHeight() {
        return myView.getHeight();
    }
}
