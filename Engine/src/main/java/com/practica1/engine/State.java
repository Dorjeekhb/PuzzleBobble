package com.practica1.engine;

import java.util.List;

public interface State {

    public void update(double deltaTime);

    public void render(Graphics graphics);

    public void handleInput(List<TouchEvent> events);

    public int getW();

    public int getH();
}
