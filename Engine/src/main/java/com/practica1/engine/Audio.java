package com.practica1.engine;

public interface Audio {

    public Sound newSound(String file);

    public void playSound(Sound s, boolean loop);

    public void mute();

    public void stopSound(Sound s);
}
