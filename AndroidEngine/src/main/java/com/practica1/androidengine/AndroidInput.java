package com.practica1.androidengine;

import android.view.MotionEvent;
import android.view.View;

import com.practica1.engine.Input;
import com.practica1.engine.TouchEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase AndroidInput que implementa la interfaz Input y maneja eventos táctiles (Touch) y clics.
 * También actúa como un listener para eventos de la vista.
 */
public class AndroidInput implements Input, View.OnTouchListener, View.OnClickListener {

    private AndroidEngine androidEngine; // Referencia opcional al motor de Android
    private AndroidGraphics render; // Referencia al motor gráfico
    private List<TouchEvent> events; // Lista de eventos procesados
    private List<TouchEvent> pendingEvents; // Lista de eventos en cola para procesar

    /**
     * Constructor de la clase AndroidInput.
     * @param render Motor gráfico para realizar conversiones o actualizaciones relacionadas con el renderizado.
     */
    public AndroidInput(AndroidGraphics render) {
        this.events = new ArrayList<>(); // Inicializa la lista de eventos procesados
        this.pendingEvents = new ArrayList<>(); // Inicializa la lista de eventos pendientes
        this.render = render; // Asigna el motor gráfico
    }

    /**
     * Método que se ejecuta al detectar un evento táctil.
     * Se llama automáticamente cuando el usuario interactúa con la pantalla táctil.
     * @param v La vista asociada al evento táctil.
     * @param event El evento táctil generado.
     * @return Devuelve `true` para indicar que el evento ha sido manejado.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getActionMasked(); // Obtiene el tipo de acción (e.g., DOWN, MOVE, UP)
        int pointerIndex = event.getActionIndex(); // Índice del puntero que generó el evento
        int pointerId = event.getPointerId(pointerIndex); // ID del puntero único
        TouchEvent touchEvent; // Objeto para almacenar información del evento

        // Manejar diferentes tipos de acciones táctiles
        switch (action) {
            case MotionEvent.ACTION_DOWN: // Primer contacto de un dedo
            case MotionEvent.ACTION_POINTER_DOWN: // Contacto de dedos adicionales
                android.util.Log.d("INPUT", "ACTION_DOWN detected");
                touchEvent = new TouchEvent();
                touchEvent.type = TouchEvent.TouchEventType.TOUCH_DOWN; // Tipo del evento: PRESIONAR
                touchEvent.finger = pointerId; // ID del dedo que generó el evento
                touchEvent.x = (int) event.getX(pointerIndex); // Coordenada X del toque
                touchEvent.y = (int) event.getY(pointerIndex); // Coordenada Y del toque
                touchEvent.startY = touchEvent.y; // Guarda la posición inicial en Y
                synchronized (this) { // Asegura que la lista sea accesible de manera segura
                    this.pendingEvents.add(touchEvent); // Añade el evento a la cola de pendientes
                }
                break;

            case MotionEvent.ACTION_MOVE: // Desplazamiento de los dedos
                android.util.Log.d("INPUT", "ACTION_MOVE detected");
                touchEvent = new TouchEvent();
                touchEvent.type = TouchEvent.TouchEventType.TOUCH_DRAGGED; // Tipo del evento: ARRASTRAR
                touchEvent.finger = pointerId; // ID del dedo que generó el evento
                touchEvent.x = (int) event.getX(pointerIndex); // Coordenada X del desplazamiento
                touchEvent.y = (int) event.getY(pointerIndex); // Coordenada Y del desplazamiento
                // Mantiene la posición inicial en Y si ya existe un evento previo
                touchEvent.startY = this.events.isEmpty() ? touchEvent.y : this.events.get(0).startY;
                synchronized (this) {
                    this.pendingEvents.add(touchEvent); // Añade el evento a la cola de pendientes
                }
                break;

            case MotionEvent.ACTION_UP: // Levantamiento del dedo
            case MotionEvent.ACTION_POINTER_UP: // Levantamiento de dedos adicionales
                android.util.Log.d("INPUT", "ACTION_UP detected");
                touchEvent = new TouchEvent();
                touchEvent.type = TouchEvent.TouchEventType.TOUCH_UP; // Tipo del evento: SOLTAR
                touchEvent.finger = pointerId; // ID del dedo que generó el evento
                touchEvent.x = (int) event.getX(pointerIndex); // Coordenada X al soltar
                touchEvent.y = (int) event.getY(pointerIndex); // Coordenada Y al soltar
                synchronized (this) {
                    this.pendingEvents.add(touchEvent); // Añade el evento a la cola de pendientes
                }
                break;
        }
        return true; // Indica que el evento fue manejado correctamente
    }

    /**
     * Devuelve la lista de eventos táctiles procesados.
     * Mueve todos los eventos pendientes a la lista de eventos y luego limpia los pendientes.
     * @return Lista de eventos táctiles procesados.
     */
    @Override
    public List<TouchEvent> getTouchEvents() {
        synchronized (this) {
            this.events.clear(); // Limpia la lista actual de eventos
            this.events.addAll(this.pendingEvents); // Mueve los eventos pendientes a la lista procesada
            this.pendingEvents.clear(); // Limpia la lista de eventos pendientes
        }
        return this.events; // Devuelve la lista de eventos procesados
    }

    /**
     * Método para manejar clics en la vista.
     * Actualmente no implementa ninguna lógica.
     * @param v La vista que fue clicada.
     */
    @Override
    public void onClick(View v) {
        // Este método está vacío pero puede ser implementado según las necesidades.
    }
}
