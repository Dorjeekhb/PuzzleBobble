package com.practica1.desktopengine;

import com.practica1.engine.Sound;
import javax.sound.sampled.Clip;

public class DesktopSound implements Sound {
    private final Clip clip;

    protected DesktopSound(Clip clip) {
        this.clip = clip;
    }

    protected Clip getSoundClip() {
        return this.clip;
    }

}
