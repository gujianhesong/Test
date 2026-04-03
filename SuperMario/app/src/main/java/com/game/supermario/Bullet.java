package com.game.supermario;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Bullet {
    
    private float x, y;
    private float width, height;
    private float velocityX;
    private boolean active;
    private boolean facingRight;
    
    public Bullet(float x, float y, boolean facingRight) {
        this.x = x;
        this.y = y;
        this.width = 20;
        this.height = 15;
        this.velocityX = facingRight ? 15 : -15;
        this.active = true;
        this.facingRight = facingRight;
    }
    
    public void update() {
        if (active) {
            x += velocityX;
        }
    }
    
    public void deactivate() {
        active = false;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public RectF getBounds() {
        return new RectF(x, y, x + width, y + height);
    }
    
    public void draw(Canvas canvas, Paint paint) {
        if (!active) return;
        
        // Draw bullet (yellow/orange fireball-like)
        paint.setColor(Color.rgb(255, 200, 0)); // Yellow-orange
        canvas.drawOval(x, y, x + width, y + height, paint);
        
        // Inner glow
        paint.setColor(Color.rgb(255, 255, 200));
        canvas.drawOval(x + 4, y + 3, x + width - 4, y + height - 3, paint);
        
        // Trail effect
        paint.setColor(Color.rgb(255, 150, 0));
        if (facingRight) {
            canvas.drawRect(x - 10, y + 5, x, y + height - 5, paint);
        } else {
            canvas.drawRect(x + width, y + 5, x + width + 10, y + height - 5, paint);
        }
    }
}
