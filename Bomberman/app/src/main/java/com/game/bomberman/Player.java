package com.game.bomberman;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Player {
    private float x, y;
    private int maxBombs;
    private int activeBombs;
    private int explosionRange;
    private float speed;
    private int lives;
    private int size;
    private int baseSize = 25;
    private boolean movingUp, movingDown, movingLeft, movingRight;
    private int walkCycle;
    private int gunAmmo; // Number of bullets remaining
    private int lastDirection; // Last direction player was facing (0=up, 1=right, 2=down, 3=left)
    private boolean isInvincible; // Invincibility after respawn
    private int invincibleTimer; // Timer for invincibility
    private boolean isInvisible; // Invisibility power-up
    private int invisibleTimer; // Timer for invisibility
    
    public Player(int x, int y) {
        this.x = x;
        this.y = y;
        this.maxBombs = 1;
        this.activeBombs = 0;
        this.explosionRange = 2;
        this.speed = 2.5f;
        this.lives = 3;
        this.size = baseSize;
        this.movingUp = false;
        this.movingDown = false;
        this.movingLeft = false;
        this.movingRight = false;
        this.walkCycle = 0;
        this.gunAmmo = 0;
        this.lastDirection = 1; // Default facing right
        this.isInvincible = false;
        this.invincibleTimer = 0;
        this.isInvisible = false;
        this.invisibleTimer = 0;
    }

    public void setSize(int cellSize) {
        this.size = (int)(cellSize * 0.7f);
    }

    public void update(int[][] grid, int gridWidth, int gridHeight, int cellSize) {
        // Track last direction for shooting
        if (movingUp) lastDirection = 0;
        else if (movingRight) lastDirection = 1;
        else if (movingDown) lastDirection = 2;
        else if (movingLeft) lastDirection = 3;

        float newX = x;
        float newY = y;

        if (movingLeft) newX -= speed;
        if (movingRight) newX += speed;
        if (movingUp) newY -= speed;
        if (movingDown) newY += speed;

        // Check collision with walls - use smaller collision box for smoother movement
        float collisionSize = size * 0.6f; // Use 60% of visual size for collision

        // Try to move on X axis first
        boolean canMoveX = !checkCollision(newX, y, grid, gridWidth, gridHeight, cellSize, collisionSize);

        if (canMoveX) {
            // Direct movement works
            x = newX;
        } else {
            // Try sliding on Y axis to help corner turning
            float slideOffset = cellSize * 0.15f;
            boolean slidSuccessfully = false;

            // Try sliding in the direction player is moving vertically
            if (movingUp) {
                if (!checkCollision(newX, y - slideOffset, grid, gridWidth, gridHeight, cellSize, collisionSize)) {
                    x = newX;
                    y = y - slideOffset;
                    slidSuccessfully = true;
                }
            } else if (movingDown) {
                if (!checkCollision(newX, y + slideOffset, grid, gridWidth, gridHeight, cellSize, collisionSize)) {
                    x = newX;
                    y = y + slideOffset;
                    slidSuccessfully = true;
                }
            }

            // If no vertical movement or preferred slide failed, try both directions
            if (!slidSuccessfully && !movingUp && !movingDown) {
                if (!checkCollision(newX, y - slideOffset, grid, gridWidth, gridHeight, cellSize, collisionSize)) {
                    x = newX;
                    y = y - slideOffset;
                    slidSuccessfully = true;
                } else if (!checkCollision(newX, y + slideOffset, grid, gridWidth, gridHeight, cellSize, collisionSize)) {
                    x = newX;
                    y = y + slideOffset;
                    slidSuccessfully = true;
                }
            }
        }

        // Try to move on Y axis (with potentially updated x from sliding)
        boolean canMoveY = !checkCollision(x, newY, grid, gridWidth, gridHeight, cellSize, collisionSize);

        if (canMoveY) {
            // Direct movement works
            y = newY;
        } else {
            // Try sliding on X axis to help corner turning
            float slideOffset = cellSize * 0.15f;
            boolean slidSuccessfully = false;

            // Try sliding in the direction player is moving horizontally
            if (movingLeft) {
                if (!checkCollision(x - slideOffset, newY, grid, gridWidth, gridHeight, cellSize, collisionSize)) {
                    y = newY;
                    x = x - slideOffset;
                    slidSuccessfully = true;
                }
            } else if (movingRight) {
                if (!checkCollision(x + slideOffset, newY, grid, gridWidth, gridHeight, cellSize, collisionSize)) {
                    y = newY;
                    x = x + slideOffset;
                    slidSuccessfully = true;
                }
            }

            // If no horizontal movement or preferred slide failed, try both directions
            if (!slidSuccessfully && !movingLeft && !movingRight) {
                if (!checkCollision(x - slideOffset, newY, grid, gridWidth, gridHeight, cellSize, collisionSize)) {
                    y = newY;
                    x = x - slideOffset;
                    slidSuccessfully = true;
                } else if (!checkCollision(x + slideOffset, newY, grid, gridWidth, gridHeight, cellSize, collisionSize)) {
                    y = newY;
                    x = x + slideOffset;
                    slidSuccessfully = true;
                }
            }
        }

        // Keep player within bounds
        x = Math.max(collisionSize/2, Math.min(x, gridWidth * cellSize - collisionSize/2));
        y = Math.max(collisionSize/2, Math.min(y, gridHeight * cellSize - collisionSize/2));
    }

    private boolean checkCollision(float newX, float newY, int[][] grid, int gridWidth, int gridHeight, int cellSize, float collisionSize) {
        // Check all four corners of the player with smaller collision box
        float halfSize = collisionSize / 2;
        int[] checkPoints = {
                (int)((newX - halfSize) / cellSize), (int)((newY - halfSize) / cellSize),
                (int)((newX + halfSize) / cellSize), (int)((newY - halfSize) / cellSize),
                (int)((newX - halfSize) / cellSize), (int)((newY + halfSize) / cellSize),
                (int)((newX + halfSize) / cellSize), (int)((newY + halfSize) / cellSize)
        };

        for (int i = 0; i < checkPoints.length; i += 2) {
            int gridX = checkPoints[i];
            int gridY = checkPoints[i+1];

            if (gridX >= 0 && gridX < gridWidth && gridY >= 0 && gridY < gridHeight) {
                if (grid[gridY][gridX] != 0) { // If not empty
                    return true;
                }
            }
        }

        return false;
    }

    public void draw(Canvas canvas, Paint paint, int offsetX, int offsetY) {
        // If invisible, draw semi-transparent with special effect
        if (isInvisible) {
            // Draw ghostly aura
            paint.setColor(Color.CYAN);
            paint.setAlpha(60);
            canvas.drawCircle(x + offsetX, y + offsetY, size/2 + 8, paint);
            paint.setAlpha(255);

            // Draw semi-transparent player
            paint.setColor(Color.BLUE);
            paint.setAlpha(80);
            canvas.drawCircle(x + offsetX, y + offsetY, size/2, paint);

            paint.setColor(Color.CYAN);
            paint.setAlpha(80);
            canvas.drawCircle(x + offsetX, y + offsetY, size/3, paint);
            paint.setAlpha(255);

            // Draw sparkles around invisible player
            paint.setColor(Color.WHITE);
            for (int i = 0; i < 4; i++) {
                float angle = (walkCycle * 2 + i * 90) * (float)Math.PI / 180;
                float sparkleX = (x + offsetX) + (float)Math.cos(angle) * (size/2 + 5);
                float sparkleY = (y + offsetY) + (float)Math.sin(angle) * (size/2 + 5);
                canvas.drawCircle(sparkleX, sparkleY, 2, paint);
            }
            return;
        }

        // If invincible, blink effect (visible/invisible alternating)
        if (isInvincible) {
            // Blink every 10 frames
            if ((invincibleTimer / 10) % 2 == 0) {
                return; // Don't draw when "invisible"
            }

            // Draw invincibility aura/glow
            paint.setColor(Color.YELLOW);
            paint.setAlpha(80);
            canvas.drawCircle(x + offsetX, y + offsetY, size/2 + 5, paint);
            paint.setAlpha(255);
        }

        // Draw player as blue circle
        paint.setColor(Color.BLUE);
        canvas.drawCircle(x + offsetX, y + offsetY, size/2, paint);

        // Draw inner detail
        paint.setColor(Color.CYAN);
        canvas.drawCircle(x + offsetX, y + offsetY, size/3, paint);
    }

    public Rect getBounds() {
        return new Rect((int)(x - size/2), (int)(y - size/2),
                (int)(x + size/2), (int)(y + size/2));
    }

    // Getters and setters
    public float getX() { return x; }
    public float getY() { return y; }
    public void setMovingLeft(boolean moving) { this.movingLeft = moving; }
    public void setMovingRight(boolean moving) { this.movingRight = moving; }
    public void setMovingUp(boolean moving) { this.movingUp = moving; }
    public void setMovingDown(boolean moving) { this.movingDown = moving; }
    public void setSpeed(float speed) { this.speed = speed; }
    public float getSpeed() { return speed; }

    public boolean canPlaceBomb() {
        return activeBombs < maxBombs;
    }

    public Bullet shoot() {
        if (gunAmmo <= 0) return null;

        // Use lastDirection to determine shooting direction
        int shootDirection = lastDirection;

        if (movingUp) shootDirection = 0;
        else if (movingRight) shootDirection = 1;
        else if (movingDown) shootDirection = 2;
        else if (movingLeft) shootDirection = 3;

        gunAmmo--;

        // Create bullet slightly in front of player
        float bulletX = x;
        float bulletY = y;

        switch (shootDirection) {
            case 0: bulletY -= size; break;
            case 1: bulletX += size; break;
            case 2: bulletY += size; break;
            case 3: bulletX -= size; break;
        }

        return new Bullet(bulletX, bulletY, shootDirection);
    }

    public void addGunAmmo(int amount) {
        this.gunAmmo += amount;
    }

    public int getGunAmmo() {
        return gunAmmo;
    }

    public boolean hasGun() {
        return gunAmmo > 0;
    }

    public void resetPosition(float newX, float newY) {
        this.x = newX;
        this.y = newY;
        this.activeBombs = 0; // Reset active bombs
        this.movingUp = false;
        this.movingDown = false;
        this.movingLeft = false;
        this.movingRight = false;
        // Activate invincibility for 5 seconds (300 frames at 60 FPS)
        this.isInvincible = true;
        this.invincibleTimer = 300;
        // All other stats (maxBombs, explosionRange, speed, gunAmmo) are preserved
    }
    
    public void updateInvincibility() {
        if (isInvincible) {
            invincibleTimer--;
            if (invincibleTimer <= 0) {
                isInvincible = false;
            }
        }
    }
    
    public boolean isInvincible() {
        return isInvincible;
    }
    
    public int getInvincibleTimer() {
        return invincibleTimer;
    }
    
    public void activateInvisibility(int duration) {
        this.isInvisible = true;
        this.invisibleTimer = duration;
        // Also make invincible while invisible
        this.isInvincible = true;
        this.invincibleTimer = Math.max(this.invincibleTimer, duration);
    }
    
    public void updateInvisibility() {
        if (isInvisible) {
            invisibleTimer--;
            if (invisibleTimer <= 0) {
                isInvisible = false;
                // Keep invincibility if it's from respawn
                if (invincibleTimer <= 0) {
                    isInvincible = false;
                }
            }
        }
    }
    
    public boolean isInvisible() {
        return isInvisible;
    }
    
    public int getInvisibleTimer() {
        return invisibleTimer;
    }
}