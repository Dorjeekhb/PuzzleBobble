package com.practica1.engine;

public class TouchEvent {
    public int finger;
    public int x;
    public int y;
    public int startY;

    public static enum TouchEventType {
        TOUCH_DOWN,
        TOUCH_UP,
        TOUCH_DRAGGED
    }

    public TouchEventType type;
}
