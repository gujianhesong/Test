package com.game.bomberman.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.game.bomberman.Player;

import java.util.Random;

public class TankEnemy extends Enemy {
    private int direction;
    private Random random;
    private int changeDirectionTimer;
    private int health; // Number of hits needed to kill
    private int maxHealth;

    public TankEnemy(float x, float y) {
        super(x, y);
        this.random = new Random();
        this.direction = random.nextInt(4);
        this.changeDirectionTimer = 0;
        this.speed = 1.0f; // Slow but tough
        this.maxHealth = 3; // Needs 3 hits
        this.health = maxHealth;
    }

    @Override
    public void update(int[][] grid, int gridWidth, int gridHeight, int cellSize, Player player) {
        changeDirectionTimer++;

        if (changeDirectionTimer > 120 || wouldHitWall(grid, gridWidth, gridHeight, cellSize)) {
            direction = random.nextInt(4);
            changeDirectionTimer = 0;
        }

        float newX = x;
        float newY = y;

        switch (direction) {
            case 0: newY -= speed; break;
            case 1: newX += speed; break;
            case 2: newY += speed; break;
            case 3: newX -= speed; break;
        }

        if (!wouldHitWallAtPosition(newX, newY, grid, gridWidth, gridHeight, cellSize)) {
            x = newX;
            y = newY;
        }
    }

    private boolean wouldHitWall(int[][] grid, int gridWidth, int gridHeight, int cellSize) {
        float testX = x;
        float testY = y;

        switch (direction) {
            case 0: testY -= speed; break;
            case 1: testX += speed; break;
            case 2: testY += speed; break;
            case 3: testX -= speed; break;
        }

        return wouldHitWallAtPosition(testX, testY, grid, gridWidth, gridHeight, cellSize);
    }

    @Override
    public void draw(Canvas canvas, Paint paint, int offsetX, int offsetY) {
        float drawX = x + offsetX;
        float drawY = y + offsetY;

        // Draw tank body (dark green armor)
        paint.setColor(Color.rgb(34, 139, 34)); // Forest Green
        canvas.drawRect(drawX - size/2, drawY - size/2, drawX + size/2, drawY + size/2, paint);

        // Draw armor plates
        paint.setColor(Color.rgb(0, 100, 0)); // Darker green
        paint.setStrokeWidth(2);
        canvas.drawRect(drawX - size/2, drawY - size/2, drawX + size/2, drawY + size/2, paint);
        paint.setStrokeWidth(1);

        // Draw damage cracks based on health
        if (health < maxHealth) {
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(2);

            // First crack
            if (health <= 2) {
                canvas.drawLine(drawX - size/3, drawY - size/3, drawX, drawY, paint);
            }

            // Second crack
            if (health <= 1) {
                canvas.drawLine(drawX + size/3, drawY - size/3, drawX, drawY, paint);
                canvas.drawLine(drawX, drawY, drawX - size/4, drawY + size/3, paint);
            }

            paint.setStrokeWidth(1);
        }

        // Draw turret on top
        paint.setColor(Color.rgb(50, 180, 50)); // Lighter green
        canvas.drawCircle(drawX, drawY, size/3, paint);

        // Draw cannon barrel pointing in movement direction
        paint.setColor(Color.rgb(30, 100, 30)); // Dark green
        paint.setStrokeWidth(4);

        float barrelLength = size/2;
        float endX = drawX;
        float endY = drawY;

        switch (direction) {
            case 0: endY -= barrelLength; break;
            case 1: endX += barrelLength; break;
            case 2: endY += barrelLength; break;
            case 3: endX -= barrelLength; break;
        }

        canvas.drawLine(drawX, drawY, endX, endY, paint);
        paint.setStrokeWidth(1);

        // Draw health bar above enemy
        int barWidth = size;
        int barHeight = 4;
        int barX = (int)(drawX - barWidth/2);
        int barY = (int)(drawY - size/2 - 8);

        // Background (red)
        paint.setColor(Color.RED);
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, paint);

        // Health (green)
        float healthPercent = (float)health / maxHealth;
        paint.setColor(Color.GREEN);
        canvas.drawRect(barX, barY, barX + (int)(barWidth * healthPercent), barY + barHeight, paint);

        // Border
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, paint);
        paint.setStyle(Paint.Style.FILL);

        // Draw eyes (determined look)
        paint.setColor(Color.WHITE);
        canvas.drawCircle(drawX - size/5, drawY - size/8, size/7, paint);
        canvas.drawCircle(drawX + size/5, drawY - size/8, size/7, paint);

        paint.setColor(Color.BLACK);
        canvas.drawCircle(drawX - size/5, drawY - size/8, size/12, paint);
        canvas.drawCircle(drawX + size/5, drawY - size/8, size/12, paint);
    }

    public void takeDamage() {
        health--;
    }

    public int getHealth() {
        return health;
    }

    public boolean isDead() {
        return health <= 0;
    }
}
