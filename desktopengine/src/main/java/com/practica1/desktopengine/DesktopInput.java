package com.practica1.desktopengine;

import com.practica1.engine.Input;
import com.practica1.engine.TouchEvent;
import org.w3c.dom.events.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;


public class DesktopInput implements Input, MouseListener, MouseMotionListener {

    private List<TouchEvent> events;
    private List<TouchEvent> pendingEvents;

    public DesktopInput(){
        this.events = new ArrayList<>();
        this.pendingEvents = new ArrayList<>();
    }

    @Override
    public synchronized List<TouchEvent> getTouchEvents() {
        this.events.clear();
        this.events.addAll(this.pendingEvents);
        this.pendingEvents.clear();
        return this.events;
    }

    @Override
    public void mouseClicked(java.awt.event.MouseEvent mouseEvent) {

    }

    @Override
    public void mousePressed(java.awt.event.MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == java.awt.event.MouseEvent.BUTTON1) {
            TouchEvent event = new TouchEvent();
            event.type = TouchEvent.TouchEventType.TOUCH_DOWN;
            event.finger = 0;
            event.x = mouseEvent.getX();
            event.y = mouseEvent.getY();

            synchronized (this) {
                this.pendingEvents.add(event);
            }
            System.out.println("Left mouse button pressed at: " + event.x + ", " + event.y);
        }
    }


    @Override
    public void mouseReleased(java.awt.event.MouseEvent mouseEvent) {
        if(mouseEvent.getButton() == java.awt.event.MouseEvent.BUTTON1) {
            TouchEvent event;
            event = new TouchEvent();
            event.type = TouchEvent.TouchEventType.TOUCH_UP;
            event.finger = 0;
            event.x = mouseEvent.getX();
            event.y = mouseEvent.getY();

            synchronized (this) {
                this.pendingEvents.add(event);
            }
        }
    }

    @Override
    public void mouseEntered(java.awt.event.MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(java.awt.event.MouseEvent mouseEvent) {

    }

    @Override
    public void mouseDragged(java.awt.event.MouseEvent mouseEvent) {
        TouchEvent event;
        event = new TouchEvent();
        event.type = TouchEvent.TouchEventType.TOUCH_DRAGGED;
        event.finger = 0;
        event.x = mouseEvent.getX();
        event.y = mouseEvent.getY();

        synchronized (this) {
            this.pendingEvents.add(event);
        }
    }

    @Override
    public void mouseMoved(java.awt.event.MouseEvent mouseEvent) {

    }
}
