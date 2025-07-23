package com.practica1.gamelogic;

import com.practica1.engine.Color;
import com.practica1.engine.Graphics;

public class PlayerBubble {
    // Variables principales de la burbuja
    protected float ballX, ballY, ballRadius; // Coordenadas y radio de la burbuja
    public float ballSpeedX, ballSpeedY; // Velocidades en X e Y
    ColorEnum colorBubble; // Color de la burbuja

    // Constantes de configuración
    private static final float PLAYER_SPEED = 150;  // Velocidad de la burbuja
    private static final float LINE_LENGTH = 0.5f;  // Longitud de la línea de guía

    // Estado de la burbuja
    private boolean isMoving = false;  // Indica si la burbuja está en movimiento
    private float boundWidth;          // Ancho del límite de movimiento
    private boolean isVictoryBubble = false;  // Indica si es una burbuja de victoria
    private float moveTime = 0;        // Tiempo en movimiento
    private boolean line;             // Indica si se muestra la línea guía

    // Constructor principal
    public PlayerBubble(int posx, int posy, int radius, ColorEnum color, float topMargin, float boundWidth) {
        this.ballX = posx; // Posición inicial X
        this.ballY = posy; // Posición inicial Y
        this.ballRadius = radius; // Radio de la burbuja
        this.colorBubble = color; // Color de la burbuja
        setSpeed(0, 0);  // Inicia sin velocidad
        this.boundWidth = boundWidth; // Límite de movimiento
        this.line = false; // Línea de guía desactivada
    }

    // Constructor adicional para burbujas de victoria
    public PlayerBubble(int posx, int posy, int radius, ColorEnum color, boolean isVictoryBubble) {
        this.ballX = posx;
        this.ballY = posy;
        this.ballRadius = radius;
        this.colorBubble = color;
        this.isVictoryBubble = isVictoryBubble;
        setSpeed(0, 0);  // Inicia sin velocidad
    }

    // Método para ajustar la velocidad de la burbuja
    public void setSpeed(float speedX, float speedY) {
        this.ballSpeedX = speedX;
        this.ballSpeedY = speedY;
    }

    // Configura la dirección de lanzamiento de la burbuja
    public void setLaunchDirection(float x, float y) {
        float dx = x - getBallX() - ballRadius; // Distancia en X desde el centro
        float dy = y - getBallY() - ballRadius; // Distancia en Y desde el centro
        float distance = (float) Math.sqrt(dx * dx + dy * dy); // Distancia total al objetivo

        // Ángulos mínimos y máximos permitidos para el lanzamiento
        double minAngle = Math.toRadians(15); // 15 grados
        double maxAngle = Math.toRadians(165); // 165 grados

        // Calcula el ángulo actual del lanzamiento
        double angle = Math.atan2(dy, dx);

        // Ajusta el ángulo al rango permitido
        if (angle < -maxAngle) {
            angle = -maxAngle;
        } else if (angle > -minAngle) {
            angle = -minAngle;
        }

        // Convierte el ángulo ajustado a componentes X e Y
        dx = (float) (Math.cos(angle) * distance);
        dy = (float) (Math.sin(angle) * distance);

        // Configura la velocidad en función del ángulo ajustado
        if (distance > 0) {
            float vx = (dx / distance) * PLAYER_SPEED;
            float vy = (dy / distance) * PLAYER_SPEED;
            setSpeed(vx * 2, vy * 2); // Duplica la velocidad para mayor impacto
        }
    }

    // Métodos de acceso para las propiedades de la burbuja
    public float getBallX() {
        return ballX;
    }

    public float getBallY() {
        return ballY;
    }

    public float getBallRadius() {
        return ballRadius;
    }

    public ColorEnum getColor() {
        return colorBubble;
    }

    // Método para lanzar la burbuja
    public void launch() {
        isMoving = true; // Activa el estado de movimiento
        line = false;    // Desactiva la línea guía
        moveTime = 0;    // Reinicia el tiempo de movimiento
    }

    // Método para activar la línea guía
    public void setLine() {
        line = true;
    }

    // Método para detener el movimiento de la burbuja
    public void stop() {
        isMoving = false; // Detiene el movimiento
        setSpeed(0, 0);   // Reinicia la velocidad
        moveTime = 0;     // Reinicia el tiempo de movimiento
    }

    // Actualiza la posición y estado de la burbuja
    public void update(double deltaTime) {
        if (isMoving) {
            float speedMultiplier = 2.0f; // Multiplicador de velocidad
            ballX += (float) (ballSpeedX * deltaTime * speedMultiplier); // Actualiza posición X
            ballY += (float) (ballSpeedY * deltaTime * speedMultiplier); // Actualiza posición Y
            moveTime += (float) deltaTime; // Incrementa el tiempo de movimiento

            // Detecta colisiones con los bordes
            int screenWidth = SceneManager.getInstance().getLogicWidth();

            // Rebota en los bordes izquierdo y derecho
            if (getBallX() <= boundWidth || getBallX() + ballRadius * 2 >= screenWidth - boundWidth) {
                ballSpeedX = -ballSpeedX; // Invierte la velocidad en X
            }
        }
    }

    // Comprueba si la burbuja está en movimiento
    public boolean isMoving() {
        return isMoving;
    }

    // Devuelve el tiempo total en movimiento
    public float getMoveTime() {
        return moveTime;
    }

    // Renderiza la burbuja en pantalla
    public void render(Graphics graphics, Grid grid) {
        // Dibuja la línea guía si la burbuja no está en movimiento ni es de victoria
        if (!isVictoryBubble && !isMoving) {
            Color lineColor = graphics.newColor(255, 0, 0, 0); // Negro
            graphics.setColor(lineColor);

            double lengthFactor = LINE_LENGTH; // Longitud de la línea

            double dx = ballSpeedX * lengthFactor;
            double dy = ballSpeedY * lengthFactor;

            graphics.drawLine(
                    (int) ballX + (int) ballRadius, // Punto inicial X
                    (int) ballY + (int) ballRadius, // Punto inicial Y
                    (int) (ballX + dx + ballRadius), // Punto final X
                    (int) (ballY + dy + ballRadius)  // Punto final Y
            );
        }

        boolean alreadyPainted = false; // Bandera para saber si ya se dibujó la burbuja

        // Dibuja la burbuja personalizada según el color y configuración
        if (colorBubble == ColorEnum.RED && SceneManager.getInstance().selectedApple) {
            grid.drawApple(graphics, (int) ballX, (int) ballY);
            alreadyPainted = true;
        } else if (colorBubble == ColorEnum.YELLOW && SceneManager.getInstance().selectedLemon) {
            grid.drawLemon(graphics, (int) ballX, (int) ballY);
            alreadyPainted = true;
        } else if (colorBubble == ColorEnum.GREEN && SceneManager.getInstance().selectedPear) {
            grid.drawPear(graphics, (int) ballX, (int) ballY);
            alreadyPainted = true;
        } else if (colorBubble == ColorEnum.GRAY && SceneManager.getInstance().selectedGris) {
            grid.drawGray(graphics, (int) ballX, (int) ballY);
            alreadyPainted = true;
        } else if (colorBubble == ColorEnum.BLUE && SceneManager.getInstance().selectedBerry) {
            grid.drawBlueberry(graphics, (int) ballX, (int) ballY);
            alreadyPainted = true;
        }

        // Dibuja una burbuja estándar si no hay personalización
        if (!alreadyPainted) {
            Color bubbleColor = graphics.newColor(colorBubble.getA(), colorBubble.getR(), colorBubble.getG(), colorBubble.getB()); // Color de la burbuja
            graphics.setColor(bubbleColor);
            graphics.fillCircle(ballX, ballY, ballRadius); // Dibuja un círculo sólido
        }
    }
}
