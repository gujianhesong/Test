package com.game.bomberman.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.game.bomberman.Player;

import java.util.Random;

public class BossEnemy extends Enemy {
    private int direction;
    private Random random;
    private int changeDirectionTimer;
    private int health;
    private int maxHealth;
    private int summonTimer;
    private static final int SUMMON_INTERVAL = 600; // 10 seconds at 60 FPS
    private float animationOffset;
    private Player targetPlayer;

    public BossEnemy(float x, float y, Player player) {
        super(x, y);
        this.targetPlayer = player;
        this.random = new Random();
        this.direction = random.nextInt(4);
        this.changeDirectionTimer = 0;
        this.speed = 0.8f; // Very slow but tough
        this.maxHealth = 5;
        this.health = maxHealth;
        this.summonTimer = 0;
        this.animationOffset = 0;

        // Make boss bigger
        this.baseSize = 40;
        this.size = baseSize;
    }

    @Override
    public void update(int[][] grid, int gridWidth, int gridHeight, int cellSize, Player player) {
        changeDirectionTimer++;
        animationOffset += 0.05f;

        if (changeDirectionTimer > 150 || wouldHitWall(grid, gridWidth, gridHeight, cellSize)) {
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

        // Update summon timer
        summonTimer++;
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

        // Pulsing effect
        float pulse = (float)Math.sin(animationOffset) * 3;

        // Draw boss body (dark red/purple gradient effect)
        paint.setColor(Color.rgb(139, 0, 0)); // Dark Red
        canvas.drawCircle(drawX, drawY, size/2 + pulse/2, paint);

        // Draw armor plates
        paint.setColor(Color.rgb(80, 0, 0)); // Darker red
        paint.setStrokeWidth(3);
        canvas.drawCircle(drawX, drawY, size/2 + pulse/2, paint);
        paint.setStrokeWidth(1);

        // Draw crown/horns on top
        paint.setColor(Color.rgb(255, 215, 0)); // Gold
        float hornSpacing = size/3;
        // Left horn
        canvas.drawLine(drawX - hornSpacing, drawY - size/2, drawX - hornSpacing - 5, drawY - size/2 - 10, paint);
        // Right horn
        canvas.drawLine(drawX + hornSpacing, drawY - size/2, drawX + hornSpacing + 5, drawY - size/2 - 10, paint);
        // Center spike
        canvas.drawLine(drawX, drawY - size/2, drawX, drawY - size/2 - 12, paint);

        // Draw damage cracks based on health
        if (health < maxHealth) {
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(3);

            int cracks = maxHealth - health;
            for (int i = 0; i < cracks; i++) {
                float angle = (i * 72 + animationOffset * 20) * (float)Math.PI / 180;
                float startX = drawX + (float)Math.cos(angle) * size/4;
                float startY = drawY + (float)Math.sin(angle) * size/4;
                float endX = drawX + (float)Math.cos(angle) * size/2;
                float endY = drawY + (float)Math.sin(angle) * size/2;
                canvas.drawLine(startX, startY, endX, endY, paint);
            }

            paint.setStrokeWidth(1);
        }

        // Draw menacing eyes
        int eyeSpacing = size / 3;
        int eyeSize = size / 5;

        // Eye glow effect
        paint.setColor(Color.RED);
        paint.setAlpha(100);
        canvas.drawCircle(drawX - eyeSpacing, drawY - size/6, eyeSize + 2, paint);
        canvas.drawCircle(drawX + eyeSpacing, drawY - size/6, eyeSize + 2, paint);
        paint.setAlpha(255);

        // Eyes
        paint.setColor(Color.WHITE);
        canvas.drawCircle(drawX - eyeSpacing, drawY - size/6, eyeSize, paint);
        canvas.drawCircle(drawX + eyeSpacing, drawY - size/6, eyeSize, paint);

        // Pupils (follow player)
        paint.setColor(Color.RED);
        float pupilOffsetX = 0, pupilOffsetY = 0;
        if (targetPlayer != null) {
            float dx = targetPlayer.getX() - x;
            float dy = targetPlayer.getY() - y;
            float dist = (float)Math.sqrt(dx*dx + dy*dy);
            if (dist > 0) {
                pupilOffsetX = (dx / dist) * 3;
                pupilOffsetY = (dy / dist) * 3;
            }
        }
        canvas.drawCircle(drawX - eyeSpacing + pupilOffsetX, drawY - size/6 + pupilOffsetY, eyeSize/2, paint);
        canvas.drawCircle(drawX + eyeSpacing + pupilOffsetX, drawY - size/6 + pupilOffsetY, eyeSize/2, paint);

        // Draw angry mouth
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(3);
        canvas.drawArc(drawX - size/4, drawY, drawX + size/4, drawY + size/3, 0, 180, false, paint);
        paint.setStrokeWidth(1);

        // Draw health bar above boss
        int barWidth = size + 10;
        int barHeight = 6;
        int barX = (int)(drawX - barWidth/2);
        int barY = (int)(drawY - size/2 - 20);

        // Background (dark red)
        paint.setColor(Color.rgb(80, 0, 0));
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, paint);

        // Health (bright red to green gradient)
        float healthPercent = (float)health / maxHealth;
        if (healthPercent > 0.6) {
            paint.setColor(Color.GREEN);
        } else if (healthPercent > 0.3) {
            paint.setColor(Color.YELLOW);
        } else {
            paint.setColor(Color.RED);
        }
        canvas.drawRect(barX, barY, barX + (int)(barWidth * healthPercent), barY + barHeight, paint);

        // Border
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawRect(barX, barY, barX + barWidth, barY + barHeight, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // Draw "BOSS" label
        paint.setColor(Color.rgb(255, 215, 0)); // Gold
        paint.setTextSize(size/3);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("BOSS", drawX, barY - 5, paint);

        // Draw summon timer indicator (small dots around boss)
        if (summonTimer > SUMMON_INTERVAL - 120) { // Show warning in last 2 seconds
            int dots = (summonTimer - (SUMMON_INTERVAL - 120)) / 20;
            paint.setColor(Color.YELLOW);
            for (int i = 0; i < dots && i < 6; i++) {
                float angle = (i * 60 + animationOffset * 30) * (float)Math.PI / 180;
                float dotX = drawX + (float)Math.cos(angle) * (size/2 + 8);
                float dotY = drawY + (float)Math.sin(angle) * (size/2 + 8);
                canvas.drawCircle(dotX, dotY, 3, paint);
            }
        }
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

    public boolean canSummon() {
        return summonTimer >= SUMMON_INTERVAL;
    }

    public void resetSummonTimer() {
        summonTimer = 0;
    }

    public int getSummonTimer() {
        return summonTimer;
    }
}
