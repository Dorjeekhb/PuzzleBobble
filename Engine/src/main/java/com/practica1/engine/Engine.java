package com.practica1.engine;


public interface Engine {

    Graphics getGraphics();
    Audio getAudio();

    // Devuelve un File que este en assets
    File getAssetsFile(String path);

    // Devuelve un File de memoria interna
    File getInternalFile(String path);

    // Devuelve la lista de carpetas que hay en ese path en assets
    String[] getAssetsList(String path);

    void changeScene(State scene);
}
