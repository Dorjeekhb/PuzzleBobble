package com.practica1.desktopengine;

import com.practica1.engine.Audio;
import com.practica1.engine.Sound;

import java.io.File;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class DesktopAudio implements Audio {
    String root = "data/assets/sounds/";

    @Override
    public Sound newSound(String file) {
        Clip clip = null;
        try {
            clip = AudioSystem.getClip();
            final AudioInputStream ais = AudioSystem.getAudioInputStream(new File(root + file));
            clip.open(ais);
            ais.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new DesktopSound(clip);
    }

    @Override
    public void playSound(Sound s, boolean loop) {
        if (s instanceof DesktopSound) {
            Clip clip = ((DesktopSound) s).getSoundClip();
            if (clip != null) {
                clip.stop();
                clip.setFramePosition(0);
                clip.loop(loop ? Clip.LOOP_CONTINUOUSLY : 0);
                clip.start();
            }
        }
    }

    @Override
    public void stopSound(Sound s) {
        if (s instanceof DesktopSound) {
            Clip clip = ((DesktopSound) s).getSoundClip();
            if (clip != null) {
                clip.stop();
            }
        }
    }

    @Override
    public void mute() {
        // Implementar método para silenciar todos los sonidos si es necesario
    }

    @Override
    public void unmute() {
        // Implementar método para des-silenciar todos los sonidos si es necesario
    }

    public void setVolume(Sound s, float volume) {
        if (s instanceof DesktopSound) {
            Clip clip = ((DesktopSound) s).getSoundClip();
            if (clip != null) {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(volume);
            }
        }
    }
}
