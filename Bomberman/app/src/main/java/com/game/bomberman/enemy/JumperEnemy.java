package com.game.bomberman.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.game.bomberman.Player;

import java.util.Random;

public class JumperEnemy extends Enemy {
    private int direction;
    private Random random;
    private int changeDirectionTimer;
    private boolean isJumping;
    private int jumpTimer;
    private static final int JUMP_DURATION = 30; // Jump lasts 30 frames (~0.5 seconds)

    public JumperEnemy(float x, float y) {
        super(x, y);
        this.random = new Random();
        this.direction = random.nextInt(4);
        this.changeDirectionTimer = 0;
        this.speed = 1.8f; // Slightly faster than random enemy
        this.isJumping = false;
        this.jumpTimer = 0;
    }

    @Override
    public void update(int[][] grid, int gridWidth, int gridHeight, int cellSize, Player player) {
        changeDirectionTimer++;

        // Handle jumping state
        if (isJumping) {
            jumpTimer++;

            // Continue moving in current direction while jumping (ignore walls)
            float newX = x;
            float newY = y;

            switch (direction) {
                case 0: newY -= speed; break;
                case 1: newX += speed; break;
                case 2: newY += speed; break;
                case 3: newX -= speed; break;
            }

            // Keep within bounds
            x = Math.max(size/2, Math.min(newX, gridWidth * cellSize - size/2));
            y = Math.max(size/2, Math.min(newY, gridHeight * cellSize - size/2));

            // End jump
            if (jumpTimer >= JUMP_DURATION) {
                isJumping = false;
                jumpTimer = 0;
                changeDirectionTimer = 0;
            }
            return;
        }

        // Normal movement (respect walls)
        if (changeDirectionTimer > 80 || wouldHitWall(grid, gridWidth, gridHeight, cellSize)) {
            direction = random.nextInt(4);
            changeDirectionTimer = 0;

            // Chance to jump when changing direction (30% chance)
            if (random.nextDouble() < 0.3) {
                isJumping = true;
                jumpTimer = 0;
            }
        }

        float newX = x;
        float newY = y;

        switch (direction) {
            case 0: newY -= speed; break;
            case 1: newX += speed; break;
            case 2: newY += speed; break;
            case 3: newX -= speed; break;
        }

        // Only move if no collision (when not jumping)
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

        if (isJumping) {
            // Draw jumping effect (elevated with shadow)
            float jumpHeight = (float)Math.sin((jumpTimer / (float)JUMP_DURATION) * Math.PI) * 10;

            // Draw shadow
            paint.setColor(Color.rgb(100, 100, 100));
            paint.setAlpha(100);
            canvas.drawCircle(drawX, drawY + 5, size/2, paint);
            paint.setAlpha(255);

            // Draw enemy body (green with wings)
            paint.setColor(Color.GREEN);
            canvas.drawCircle(drawX, drawY - jumpHeight, size/2, paint);

            // Draw wings
            paint.setColor(Color.WHITE);
            float wingOffset = (float)Math.sin(jumpTimer * 0.3) * 5;
            canvas.drawOval(drawX - size/2 - 5, drawY - jumpHeight - size/4 + wingOffset,
                    drawX - size/4, drawY - jumpHeight + wingOffset, paint);
            canvas.drawOval(drawX + size/4, drawY - jumpHeight - size/4 - wingOffset,
                    drawX + size/2 + 5, drawY - jumpHeight - wingOffset, paint);

            // Draw eyes
            paint.setColor(Color.WHITE);
            canvas.drawCircle(drawX - size/5, drawY - jumpHeight - size/8, size/6, paint);
            canvas.drawCircle(drawX + size/5, drawY - jumpHeight - size/8, size/6, paint);
            paint.setColor(Color.BLACK);
            canvas.drawCircle(drawX - size/5, drawY - jumpHeight - size/8, size/12, paint);
            canvas.drawCircle(drawX + size/5, drawY - jumpHeight - size/8, size/12, paint);
        } else {
            // Draw normal enemy (green)
            paint.setColor(Color.GREEN);
            canvas.drawCircle(drawX, drawY, size/2, paint);

            // Draw small wings on back
            paint.setColor(Color.WHITE);
            canvas.drawOval(drawX - size/3, drawY - size/6, drawX - size/6, drawY, paint);
            canvas.drawOval(drawX + size/6, drawY - size/6, drawX + size/3, drawY, paint);

            // Draw eyes
            int eyeSpacing = size / 4;
            int eyeSize = size / 6;
            int pupilSize = eyeSize / 2;

            paint.setColor(Color.WHITE);
            canvas.drawCircle(drawX - eyeSpacing, drawY - eyeSpacing/2, eyeSize, paint);
            canvas.drawCircle(drawX + eyeSpacing, drawY - eyeSpacing/2, eyeSize, paint);
            paint.setColor(Color.BLACK);
            canvas.drawCircle(drawX - eyeSpacing, drawY - eyeSpacing/2, pupilSize, paint);
            canvas.drawCircle(drawX + eyeSpacing, drawY - eyeSpacing/2, pupilSize, paint);
        }
    }
}
