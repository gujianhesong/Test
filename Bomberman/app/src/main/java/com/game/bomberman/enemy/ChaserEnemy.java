package com.game.bomberman.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.game.bomberman.Player;

import java.util.Random;

public class ChaserEnemy extends Enemy {
    private int direction;
    private Random random;
    private Player targetPlayer;
    private boolean isStuck;
    private int stuckTimer;
    private int lastX, lastY;
    private int stuckCheckTimer;
    private boolean isChasing = false;

    public ChaserEnemy(float x, float y, Player player) {
        super(x, y);
        this.targetPlayer = player;
        this.random = new Random();
        this.direction = random.nextInt(4);
        this.speed = 2.0f; // Faster than random enemies
        this.isStuck = false;
        this.stuckTimer = 0;
        this.lastX = (int)x;
        this.lastY = (int)y;
        this.stuckCheckTimer = 0;
    }

    @Override
    public void update(int[][] grid, int gridWidth, int gridHeight, int cellSize, Player player) {
        if (targetPlayer == null) return;

        // If player is invisible, switch to random movement
        if (player.isInvisible()) {
            updateRandomMovement(grid, gridWidth, gridHeight, cellSize);
            return;
        }

        // Check if stuck (position hasn't changed for a while)
        stuckCheckTimer++;
        if (stuckCheckTimer >= 10) {
            int currentX = (int)x;
            int currentY = (int)y;

            if (!isStuck && stuckTimer >= 0) {
                // Only check for stuck when not already in random mode
                if (currentX == lastX && currentY == lastY) {
                    stuckTimer++;
                    if (stuckTimer > 5) {
                        isStuck = true;
                        stuckTimer = 0; // Reset timer for recovery countdown
                    }
                } else {
                    stuckTimer = 0;
                }
            }

            lastX = currentX;
            lastY = currentY;
            stuckCheckTimer = 0;
        }

        // If in stuck or recovery mode
        if (isStuck) {
            updateRandomMovement(grid, gridWidth, gridHeight, cellSize);

            // Count frames in random mode (60 FPS * 5 seconds = 300 frames)
            stuckTimer++;

            // After 300 frames (~5 seconds), try to resume chasing
            if (stuckTimer >= 300) {
                isStuck = false;
                stuckTimer = 0;
            }
            return;
        }

        // Normal chasing behavior
        updateChaseBehavior(grid, gridWidth, gridHeight, cellSize);
    }

    private void updateChaseBehavior(int[][] grid, int gridWidth, int gridHeight, int cellSize) {
        isChasing = true;

        // Calculate direction to player
        float dx = targetPlayer.getX() - x;
        float dy = targetPlayer.getY() - y;

        // Determine primary direction (only move on one axis at a time)
        int moveDirection;
        if (Math.abs(dx) > Math.abs(dy)) {
            moveDirection = (dx > 0) ? 1 : 3; // right or left
        } else {
            moveDirection = (dy > 0) ? 2 : 0; // down or up
        }

        // Try to move in the primary direction
        float newX = x;
        float newY = y;

        switch (moveDirection) {
            case 0: newY -= speed; break; // up
            case 1: newX += speed; break; // right
            case 2: newY += speed; break; // down
            case 3: newX -= speed; break; // left
        }

        if (!wouldHitWallAtPosition(newX, newY, grid, gridWidth, gridHeight, cellSize)) {
            x = newX;
            y = newY;
            direction = moveDirection;
            return;
        }

        // If blocked on primary axis, try perpendicular axis
        int perpDirection1, perpDirection2;
        if (moveDirection == 0 || moveDirection == 2) {
            perpDirection1 = 1; // right
            perpDirection2 = 3; // left
        } else {
            perpDirection1 = 0; // up
            perpDirection2 = 2; // down
        }

        // Try first perpendicular direction
        newX = x;
        newY = y;
        switch (perpDirection1) {
            case 0: newY -= speed; break;
            case 1: newX += speed; break;
            case 2: newY += speed; break;
            case 3: newX -= speed; break;
        }

        if (!wouldHitWallAtPosition(newX, newY, grid, gridWidth, gridHeight, cellSize)) {
            x = newX;
            y = newY;
            direction = perpDirection1;
            return;
        }

        // Try second perpendicular direction
        newX = x;
        newY = y;
        switch (perpDirection2) {
            case 0: newY -= speed; break;
            case 1: newX += speed; break;
            case 2: newY += speed; break;
            case 3: newX -= speed; break;
        }

        if (!wouldHitWallAtPosition(newX, newY, grid, gridWidth, gridHeight, cellSize)) {
            x = newX;
            y = newY;
            direction = perpDirection2;
            return;
        }

        // If all directions blocked, try opposite of primary direction
        int oppositeDirection = (moveDirection + 2) % 4;
        newX = x;
        newY = y;
        switch (oppositeDirection) {
            case 0: newY -= speed; break;
            case 1: newX += speed; break;
            case 2: newY += speed; break;
            case 3: newX -= speed; break;
        }

        if (!wouldHitWallAtPosition(newX, newY, grid, gridWidth, gridHeight, cellSize)) {
            x = newX;
            y = newY;
            direction = oppositeDirection;
            return;
        }

        // Completely stuck - mark as stuck for next frame
        stuckTimer++;
        if (stuckTimer > 5) {
            isStuck = true;
        }
    }

    private void updateRandomMovement(int[][] grid, int gridWidth, int gridHeight, int cellSize) {
        isChasing = false;

        // Change direction periodically
        if (random.nextInt(20) == 0) {
            direction = random.nextInt(4);
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
        } else {
            // Hit wall, change direction immediately
            direction = random.nextInt(4);
        }
    }

    @Override
    public void draw(Canvas canvas, Paint paint, int offsetX, int offsetY) {
        float drawX = x + offsetX;
        float drawY = y + offsetY;

        // Draw chaser enemy (red with angry eyes)
        paint.setColor(Color.RED);
        canvas.drawCircle(drawX, drawY, size/2, paint);

        // Draw angry eyebrows
        if(isChasing){
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(3);
            canvas.drawLine(drawX - size/3, drawY - size/3, drawX - size/6, drawY - size/4, paint);
            canvas.drawLine(drawX + size/3, drawY - size/3, drawX + size/6, drawY - size/4, paint);

            // Draw eyes
            paint.setColor(Color.WHITE);
            canvas.drawCircle(drawX - size/5, drawY - size/8, size/6, paint);
            canvas.drawCircle(drawX + size/5, drawY - size/8, size/6, paint);
        }

        // Draw pupils (looking at player)
        paint.setColor(Color.BLACK);
        float pupilOffsetX = 0, pupilOffsetY = 0;
        if (targetPlayer != null) {
            float dx = targetPlayer.getX() - x;
            float dy = targetPlayer.getY() - y;
            float dist = (float)Math.sqrt(dx*dx + dy*dy);
            if (dist > 0) {
                pupilOffsetX = (dx / dist) * 2;
                pupilOffsetY = (dy / dist) * 2;
            }
        }
        canvas.drawCircle(drawX - size/5 + pupilOffsetX, drawY - size/8 + pupilOffsetY, size/10, paint);
        canvas.drawCircle(drawX + size/5 + pupilOffsetX, drawY - size/8 + pupilOffsetY, size/10, paint);

        paint.setStrokeWidth(1);
    }
}
