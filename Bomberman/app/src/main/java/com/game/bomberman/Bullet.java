package com.game.bomberman;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Bullet {
    private float x, y;
    private int direction; // 0=up, 1=right, 2=down, 3=left
    private float speed;
    private int size;

    public Bullet(float x, float y, int direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
        this.speed = 6.0f; // Fast bullet speed
        this.size = 12; // Increased from 8 to 12
    }

    public void update() {
        switch (direction) {
            case 0: y -= speed; break; // up
            case 1: x += speed; break; // right
            case 2: y += speed; break; // down
            case 3: x -= speed; break; // left
        }
    }

    public void draw(Canvas canvas, Paint paint, int offsetX, int offsetY) {
        float drawX = x + offsetX;
        float drawY = y + offsetY;

        // Draw bullet trail (longer and more visible)
        paint.setColor(Color.YELLOW);
        paint.setAlpha(100);
        for (int i = 1; i <= 4; i++) {
            float trailX = drawX;
            float trailY = drawY;

            switch (direction) {
                case 0: trailY += i * 4; break;
                case 1: trailX -= i * 4; break;
                case 2: trailY -= i * 4; break;
                case 3: trailX += i * 4; break;
            }

            canvas.drawCircle(trailX, trailY, size/2 - i/2, paint);
        }
        paint.setAlpha(255);

        // Draw larger bullet
        paint.setColor(Color.rgb(255, 215, 0)); // Gold
        canvas.drawCircle(drawX, drawY, size, paint);

        // Bright center (larger)
        paint.setColor(Color.WHITE);
        canvas.drawCircle(drawX, drawY, size/2, paint);

        // Outer glow effect
        paint.setColor(Color.YELLOW);
        paint.setAlpha(80);
        canvas.drawCircle(drawX, drawY, size * 1.3f, paint);
        paint.setAlpha(255);
    }

    public Rect getBounds() {
        return new Rect((int)(x - size/2), (int)(y - size/2),
                (int)(x + size/2), (int)(y + size/2));
    }

    public boolean isOutOfBounds(int gridWidth, int gridHeight, int cellSize) {
        return x < 0 || x > gridWidth * cellSize || y < 0 || y > gridHeight * cellSize;
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public int getDirection() { return direction; }
}
