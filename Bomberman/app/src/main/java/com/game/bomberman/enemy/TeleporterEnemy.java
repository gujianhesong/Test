package com.game.bomberman.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.game.bomberman.Player;

import java.util.Random;

public class TeleporterEnemy extends Enemy {
    private Random random;
    private int teleportTimer;
    private int teleportInterval; // Random interval between 5-8 seconds
    private int blinkTimer;
    private boolean isVisible;
    private Player targetPlayer;
    private int direction;
    private int moveTimer;

    public TeleporterEnemy(float x, float y, Player player) {
        super(x, y);
        this.targetPlayer = player;
        this.random = new Random();
        this.teleportTimer = 0;
        this.blinkTimer = 0;
        this.isVisible = true;
        this.speed = 1.2f; // Slow movement between teleports
        this.direction = random.nextInt(4);
        this.moveTimer = 0;
        // Set initial random interval (300-480 frames = 5-8 seconds at 60 FPS)
        this.teleportInterval = 300 + random.nextInt(181);
    }

    @Override
    public void update(int[][] grid, int gridWidth, int gridHeight, int cellSize, Player player) {
        teleportTimer++;

        // Move slowly between teleports
        moveTimer++;
        if (moveTimer > 60) { // Change direction every second
            direction = random.nextInt(4);
            moveTimer = 0;
        }

        // Try to move in current direction
        float newX = x;
        float newY = y;

        switch (direction) {
            case 0: newY -= speed; break;
            case 1: newX += speed; break;
            case 2: newY += speed; break;
            case 3: newX -= speed; break;
        }

        // Move if no collision
        if (!wouldHitWallAtPosition(newX, newY, grid, gridWidth, gridHeight, cellSize)) {
            x = newX;
            y = newY;
        } else {
            // Hit wall, change direction
            direction = random.nextInt(4);
        }

        // Blink before teleporting (last 1 second before teleport)
        if (teleportTimer >= teleportInterval - 60) {
            blinkTimer++;
            // Blink every 5 frames
            isVisible = (blinkTimer / 5) % 2 == 0;
        } else {
            isVisible = true;
        }

        // Teleport when timer reaches interval
        if (teleportTimer >= teleportInterval) {
            teleport(grid, gridWidth, gridHeight, cellSize);
            teleportTimer = 0;
            blinkTimer = 0;
            isVisible = true;
            direction = random.nextInt(4);
            // Set new random interval (300-480 frames = 5-8 seconds)
            teleportInterval = 300 + random.nextInt(181);
        }
    }

    private void teleport(int[][] grid, int gridWidth, int gridHeight, int cellSize) {
        // Find a valid empty position
        int attempts = 0;
        int newX, newY;

        do {
            newX = (int)(Math.random() * (gridWidth - 2)) + 1;
            newY = (int)(Math.random() * (gridHeight - 2)) + 1;
            attempts++;
        } while ((grid[newY][newX] != 0 || isTooCloseToPlayer(newX, newY, cellSize)) && attempts < 50);

        if (attempts < 50) {
            x = newX * cellSize + cellSize / 2;
            y = newY * cellSize + cellSize / 2;
        }
    }

    private boolean isTooCloseToPlayer(int gridX, int gridY, int cellSize) {
        // Don't teleport too close to player (within 3 cells)
        float playerGridX = targetPlayer.getX() / cellSize;
        float playerGridY = targetPlayer.getY() / cellSize;

        float distance = (float)Math.sqrt(
                Math.pow(gridX - playerGridX, 2) +
                        Math.pow(gridY - playerGridY, 2)
        );

        return distance < 3;
    }

    @Override
    public void draw(Canvas canvas, Paint paint, int offsetX, int offsetY) {
        if (!isVisible) return; // Don't draw when blinking out

        float drawX = x + offsetX;
        float drawY = y + offsetY;

        // Draw teleportation effect (glowing aura)
        if (teleportTimer >= teleportInterval - 30) {
            // Pulsing glow effect before teleport
            float pulse = (float)Math.sin(teleportTimer * 0.5) * 5 + 5;
            paint.setColor(Color.YELLOW);
            paint.setAlpha(100);
            canvas.drawCircle(drawX, drawY, size/2 + pulse, paint);
            paint.setAlpha(255);
        }

        // Draw enemy body (yellow/orange)
        paint.setColor(Color.rgb(255, 165, 0)); // Orange
        canvas.drawCircle(drawX, drawY, size/2, paint);

        // Draw teleportation rings
        paint.setColor(Color.YELLOW);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);

        // Rotating rings effect
        float ringOffset = (teleportTimer % 30) / 30.0f * 10;
        canvas.drawCircle(drawX, drawY, size/2 + ringOffset, paint);
        canvas.drawCircle(drawX, drawY, size/2 - ringOffset, paint);

        paint.setStyle(Paint.Style.FILL);

        // Draw eyes (mysterious look)
        int eyeSpacing = size / 4;
        int eyeSize = size / 5;
        int pupilSize = eyeSize / 2;

        paint.setColor(Color.WHITE);
        canvas.drawCircle(drawX - eyeSpacing, drawY - eyeSpacing/3, eyeSize, paint);
        canvas.drawCircle(drawX + eyeSpacing, drawY - eyeSpacing/3, eyeSize, paint);

        // Spiral pupils
        paint.setColor(Color.BLACK);
        canvas.drawCircle(drawX - eyeSpacing, drawY - eyeSpacing/3, pupilSize, paint);
        canvas.drawCircle(drawX + eyeSpacing, drawY - eyeSpacing/3, pupilSize, paint);

        // Draw sparkles around enemy
        paint.setColor(Color.YELLOW);
        for (int i = 0; i < 4; i++) {
            float angle = (teleportTimer * 0.1f + i * 90) * (float)Math.PI / 180;
            float sparkleX = drawX + (float)Math.cos(angle) * (size/2 + 5);
            float sparkleY = drawY + (float)Math.sin(angle) * (size/2 + 5);
            canvas.drawCircle(sparkleX, sparkleY, 2, paint);
        }
    }
}
