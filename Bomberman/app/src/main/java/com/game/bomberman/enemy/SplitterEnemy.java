package com.game.bomberman.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.game.bomberman.Player;

import java.util.Random;

public class SplitterEnemy extends Enemy {
    private int direction;
    private Random random;
    private int changeDirectionTimer;
    private boolean isSmall; // Whether this is a split (small) version

    public SplitterEnemy(float x, float y) {
        super(x, y);
        this.random = new Random();
        this.direction = random.nextInt(4);
        this.changeDirectionTimer = 0;
        this.speed = 2.0f; // Faster than normal
        this.isSmall = false;
    }

    // Constructor for small split enemies
    public SplitterEnemy(float x, float y, boolean isSmall) {
        super(x, y);
        this.random = new Random();
        this.direction = random.nextInt(4);
        this.changeDirectionTimer = 0;
        this.speed = 2.5f; // Even faster when small
        this.isSmall = isSmall;

        // Make small enemies smaller
        if (isSmall) {
            this.baseSize = 18;
            this.size = (int)(this.baseSize * 0.65f);
        }
    }

    @Override
    public void update(int[][] grid, int gridWidth, int gridHeight, int cellSize, Player player) {
        changeDirectionTimer++;

        // Small enemies change direction more frequently
        int changeThreshold = isSmall ? 60 : 80;

        if (changeDirectionTimer > changeThreshold || wouldHitWall(grid, gridWidth, gridHeight, cellSize)) {
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

        if (isSmall) {
            // Draw small split enemy (lighter purple)
            paint.setColor(Color.rgb(200, 100, 255));
            canvas.drawCircle(drawX, drawY, size/2, paint);

            // Draw angry eyes
            paint.setColor(Color.WHITE);
            canvas.drawCircle(drawX - size/4, drawY - size/6, size/5, paint);
            canvas.drawCircle(drawX + size/4, drawY - size/6, size/5, paint);

            paint.setColor(Color.BLACK);
            canvas.drawCircle(drawX - size/4, drawY - size/6, size/10, paint);
            canvas.drawCircle(drawX + size/4, drawY - size/6, size/10, paint);
        } else {
            // Draw large splitter enemy (dark purple)
            paint.setColor(Color.rgb(128, 0, 128)); // Purple
            canvas.drawCircle(drawX, drawY, size/2, paint);

            // Draw "split" lines on body
            paint.setColor(Color.rgb(180, 100, 200));
            paint.setStrokeWidth(2);
            canvas.drawLine(drawX - size/3, drawY - size/3, drawX + size/3, drawY + size/3, paint);
            canvas.drawLine(drawX + size/3, drawY - size/3, drawX - size/3, drawY + size/3, paint);
            paint.setStrokeWidth(1);

            // Draw eyes
            int eyeSpacing = size / 4;
            int eyeSize = size / 5;
            int pupilSize = eyeSize / 2;

            paint.setColor(Color.WHITE);
            canvas.drawCircle(drawX - eyeSpacing, drawY - eyeSpacing/3, eyeSize, paint);
            canvas.drawCircle(drawX + eyeSpacing, drawY - eyeSpacing/3, eyeSize, paint);

            paint.setColor(Color.BLACK);
            canvas.drawCircle(drawX - eyeSpacing, drawY - eyeSpacing/3, pupilSize, paint);
            canvas.drawCircle(drawX + eyeSpacing, drawY - eyeSpacing/3, pupilSize, paint);

            // Draw warning symbol
            paint.setColor(Color.YELLOW);
            paint.setTextSize(size/2);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("!", drawX, drawY + size/6, paint);
        }
    }

    public boolean isSmallVersion() {
        return isSmall;
    }
}
