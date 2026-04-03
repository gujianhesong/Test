package com.game.supermario;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Enemy {
    
    private float x, y;
    private float width, height;
    private float velocityX;
    private float startX, endX;
    private boolean active;
    private int type; // 0: Goomba-like, 1: Koopa-like
    
    public Enemy(float x, float y, float patrolDistance, int type) {
        this.x = x;
        this.y = y;
        this.width = 50;
        this.height = 50;
        this.startX = x;
        this.endX = x + patrolDistance;
        this.velocityX = 2;
        this.active = true;
        this.type = type;
    }
    
    public void update() {
        if (!active) return;
        
        x += velocityX;
        
        // Patrol back and forth
        if (x >= endX || x <= startX) {
            velocityX = -velocityX;
        }
    }
    
    public void defeat() {
        active = false;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public RectF getBounds() {
        return new RectF(x, y, x + width, y + height);
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public float getWidth() {
        return width;
    }
    
    public float getHeight() {
        return height;
    }

    public int getType() {
        return type;
    }

    public float getPatrolWidth() {
        return endX - startX;
    }
    
    public void draw(Canvas canvas, Paint paint) {
        if (!active) return;
        
        if (type == 0) {
            // Goomba-like enemy (brown mushroom)
            drawGoomba(canvas, paint);
        } else {
            // Koopa-like enemy (green turtle)
            drawKoopa(canvas, paint);
        }
    }
    
    private void drawGoomba(Canvas canvas, Paint paint) {
        // Body (brown)
        paint.setColor(Color.rgb(139, 69, 19));
        canvas.drawOval(x, y, x + width, y + height, paint);
        
        // Eyes
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x + 15, y + 20, 8, paint);
        canvas.drawCircle(x + 35, y + 20, 8, paint);
        
        paint.setColor(Color.BLACK);
        canvas.drawCircle(x + 15, y + 20, 4, paint);
        canvas.drawCircle(x + 35, y + 20, 4, paint);
        
        // Eyebrows (angry)
        paint.setColor(Color.BLACK);
        canvas.drawLine(x + 8, y + 12, x + 20, y + 18, paint);
        canvas.drawLine(x + 42, y + 12, x + 30, y + 18, paint);
    }
    
    private void drawKoopa(Canvas canvas, Paint paint) {
        // Shell (green)
        paint.setColor(Color.GREEN);
        canvas.drawOval(x + 5, y + 20, x + width - 5, y + height, paint);
        
        // Head (light green)
        paint.setColor(Color.rgb(144, 238, 144));
        canvas.drawCircle(x + width / 2, y + 15, 15, paint);
        
        // Eyes
        paint.setColor(Color.BLACK);
        canvas.drawCircle(x + width / 2 + 5, y + 12, 3, paint);
        
        // Feet
        paint.setColor(Color.rgb(144, 238, 144));
        canvas.drawOval(x + 5, y + height - 10, x + 20, y + height, paint);
        canvas.drawOval(x + width - 20, y + height - 10, x + width - 5, y + height, paint);
    }
}
