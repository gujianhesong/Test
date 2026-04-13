package com.game.bomberman.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.game.bomberman.Player;

import java.util.Random;

public class RandomEnemy extends Enemy {
    private int direction;
    private Random random;
    private int changeDirectionTimer;

    public RandomEnemy(float x, float y) {
        super(x, y);
        this.random = new Random();
        this.direction = random.nextInt(4);
        this.changeDirectionTimer = 0;
        this.speed = 1.5f;
    }

    @Override
    public void update(int[][] grid, int gridWidth, int gridHeight, int cellSize, Player player) {
        changeDirectionTimer++;

        if (changeDirectionTimer > 100 || wouldHitWall(grid, gridWidth, gridHeight, cellSize)) {
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

        // Draw random enemy (bright cyan blue for better visibility)
        paint.setColor(Color.rgb(0, 191, 255)); // Deep Sky Blue - bright and vibrant
        canvas.drawCircle(drawX, drawY, size/2, paint);

        // Draw eyes with better spacing based on size
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
