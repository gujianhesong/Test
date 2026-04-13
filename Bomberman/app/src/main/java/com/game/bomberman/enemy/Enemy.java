package com.game.bomberman.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

import com.game.bomberman.Player;

public abstract class Enemy {
    protected float x, y;
    protected float speed;
    protected int size;
    protected int baseSize = 25;

    public Enemy(float x, float y) {
        this.x = x;
        this.y = y;
        this.speed = 1.5f;
        this.size = baseSize;
    }

    public abstract void update(int[][] grid, int gridWidth, int gridHeight, int cellSize, Player player);

    public abstract void draw(Canvas canvas, Paint paint, int offsetX, int offsetY);

    public Rect getBounds() {
        return new Rect((int)(x - size/2), (int)(y - size/2),
                (int)(x + size/2), (int)(y + size/2));
    }

    public void setSize(int cellSize) {
        this.size = (int)(cellSize * 0.65f);
    }

    public float getX() { return x; }
    public float getY() { return y; }

    // Helper method for collision detection
    protected boolean wouldHitWallAtPosition(float testX, float testY, int[][] grid, int gridWidth, int gridHeight, int cellSize) {
        float collisionSize = size;
        float halfSize = collisionSize / 2;

        int[] checkPoints = {
                (int)((testX - halfSize) / cellSize), (int)((testY - halfSize) / cellSize),
                (int)((testX + halfSize) / cellSize), (int)((testY - halfSize) / cellSize),
                (int)((testX - halfSize) / cellSize), (int)((testY + halfSize) / cellSize),
                (int)((testX + halfSize) / cellSize), (int)((testY + halfSize) / cellSize)
        };

        for (int i = 0; i < checkPoints.length; i += 2) {
            int gridX = checkPoints[i];
            int gridY = checkPoints[i+1];

            if (gridX < 0 || gridX >= gridWidth || gridY < 0 || gridY >= gridHeight) {
                return true;
            }

            if (grid[gridY][gridX] != 0) {
                return true;
            }
        }

        return false;
    }
}
