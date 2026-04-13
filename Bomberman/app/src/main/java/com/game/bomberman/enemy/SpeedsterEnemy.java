package com.game.bomberman.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.game.bomberman.Player;

import java.util.Random;

public class SpeedsterEnemy extends Enemy {
    private int direction;
    private Random random;
    private float trailX, trailY; // For speed trail effect

    public SpeedsterEnemy(float x, float y) {
        super(x, y);
        this.random = new Random();
        this.direction = random.nextInt(4);
        this.speed = 4.5f; // Very fast - 3x normal speed
        this.trailX = x;
        this.trailY = y;
    }

    @Override
    public void update(int[][] grid, int gridWidth, int gridHeight, int cellSize, Player player) {
        // Save previous position for trail effect
        trailX = x;
        trailY = y;

        // Calculate next position
        float newX = x;
        float newY = y;

        switch (direction) {
            case 0: newY -= speed; break; // up
            case 1: newX += speed; break; // right
            case 2: newY += speed; break; // down
            case 3: newX -= speed; break; // left
        }

        // Check if would hit wall or out of bounds
        if (wouldHitWallAtPosition(newX, newY, grid, gridWidth, gridHeight, cellSize)) {
            // When hitting wall, choose new direction with some randomness
            int[] possibleDirections;

            // Get current axis
            boolean isHorizontal = (direction == 1 || direction == 3);

            if (isHorizontal) {
                // Was moving horizontally, can go up, down, or reverse
                possibleDirections = new int[]{0, 2, (direction + 2) % 4};
            } else {
                // Was moving vertically, can go left, right, or reverse
                possibleDirections = new int[]{1, 3, (direction + 2) % 4};
            }

            // Shuffle directions for randomness
            for (int i = possibleDirections.length - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                int temp = possibleDirections[i];
                possibleDirections[i] = possibleDirections[j];
                possibleDirections[j] = temp;
            }

            // Try each direction until finding a valid one
            boolean moved = false;
            for (int newDir : possibleDirections) {
                newX = x;
                newY = y;

                switch (newDir) {
                    case 0: newY -= speed; break;
                    case 1: newX += speed; break;
                    case 2: newY += speed; break;
                    case 3: newX -= speed; break;
                }

                if (!wouldHitWallAtPosition(newX, newY, grid, gridWidth, gridHeight, cellSize)) {
                    direction = newDir;
                    moved = true;
                    break;
                }
            }

            // If all directions blocked (shouldn't happen), reverse
            if (!moved) {
                direction = (direction + 2) % 4;
            }
        }

        // Move to new position
        x = newX;
        y = newY;
    }

    @Override
    public void draw(Canvas canvas, Paint paint, int offsetX, int offsetY) {
        float drawX = x + offsetX;
        float drawY = y + offsetY;
        float trailDrawX = trailX + offsetX;
        float trailDrawY = trailY + offsetY;

        // Draw speed trail (motion blur effect)
        paint.setColor(Color.YELLOW);
        paint.setAlpha(80);

        // Draw multiple trail circles for motion blur
        for (int i = 1; i <= 3; i++) {
            float alpha = 80 - (i * 20);
            paint.setAlpha((int)alpha);
            float trailX = drawX - (drawX - trailDrawX) * (i * 0.3f);
            float trailY = drawY - (drawY - trailDrawY) * (i * 0.3f);
            canvas.drawCircle(trailX, trailY, size/2 - i, paint);
        }

        paint.setAlpha(255);

        // Draw enemy body (bright yellow/white)
        paint.setColor(Color.rgb(255, 255, 100)); // Bright yellow
        canvas.drawCircle(drawX, drawY, size/2, paint);

        // Draw speed lines
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(2);

        float lineLength = 8;
        switch (direction) {
            case 0: // up - lines below
                canvas.drawLine(drawX - size/3, drawY + size/4, drawX - size/3, drawY + size/4 + lineLength, paint);
                canvas.drawLine(drawX + size/3, drawY + size/4, drawX + size/3, drawY + size/4 + lineLength, paint);
                break;
            case 1: // right - lines on left
                canvas.drawLine(drawX - size/4 - lineLength, drawY - size/3, drawX - size/4, drawY - size/3, paint);
                canvas.drawLine(drawX - size/4 - lineLength, drawY + size/3, drawX - size/4, drawY + size/3, paint);
                break;
            case 2: // down - lines above
                canvas.drawLine(drawX - size/3, drawY - size/4 - lineLength, drawX - size/3, drawY - size/4, paint);
                canvas.drawLine(drawX + size/3, drawY - size/4 - lineLength, drawX + size/3, drawY - size/4, paint);
                break;
            case 3: // left - lines on right
                canvas.drawLine(drawX + size/4, drawY - size/3, drawX + size/4 + lineLength, drawY - size/3, paint);
                canvas.drawLine(drawX + size/4, drawY + size/3, drawX + size/4 + lineLength, drawY + size/3, paint);
                break;
        }

        paint.setStrokeWidth(1);

        // Draw eyes (determined look)
        int eyeSpacing = size / 4;
        int eyeSize = size / 6;
        int pupilSize = eyeSize / 2;

        paint.setColor(Color.BLACK);
        canvas.drawCircle(drawX - eyeSpacing, drawY - eyeSpacing/3, eyeSize, paint);
        canvas.drawCircle(drawX + eyeSpacing, drawY - eyeSpacing/3, eyeSize, paint);

        // White highlights in eyes
        paint.setColor(Color.WHITE);
        canvas.drawCircle(drawX - eyeSpacing + 2, drawY - eyeSpacing/3 - 2, pupilSize, paint);
        canvas.drawCircle(drawX + eyeSpacing + 2, drawY - eyeSpacing/3 - 2, pupilSize, paint);
    }
}
