package com.practica1.engine;

public interface Mobile {

    void makeNotification();

    void vibrateDevice(long duration);

    public void showRewardedAd(RewardListener listener);
    public void shareMessage(String title, String text);

    void scheduleNotificationWithWorkManager();

    void shareImage(int level, int points);

    boolean isRunningOnEmulator();

    // Desregistra el listener del sensor cuando no es necesario
    void unregisterSensorListener();
}
