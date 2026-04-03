package com.game.supermario;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Mushroom {
    
    private float x, y;
    private float width, height;
    private boolean collected;
    private float velocityX;
    private float startX, endX;
    
    public Mushroom(float x, float y) {
        this.x = x;
        this.y = y;
        this.width = 40;
        this.height = 40;
        this.collected = false;
        this.velocityX = 2;
        this.startX = x - 100;
        this.endX = x + 100;
    }
    
    public void update() {
        if (!collected) {
            // Move back and forth
            x += velocityX;
            if (x >= endX || x <= startX) {
                velocityX = -velocityX;
            }
        }
    }
    
    public void collect() {
        collected = true;
    }
    
    public boolean isCollected() {
        return collected;
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

    public void draw(Canvas canvas, Paint paint) {
        if (collected) return;
        
        // Draw mushroom cap (red with white spots)
        paint.setColor(Color.rgb(220, 40, 40)); // Red
        canvas.drawArc(new android.graphics.RectF(x, y, x + width, y + 25), 
                      180, 180, false, paint);
        
        // White spots on cap
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x + 12, y + 12, 5, paint);
        canvas.drawCircle(x + 28, y + 12, 5, paint);
        
        // Draw stem (white/cream color)
        paint.setColor(Color.rgb(255, 250, 220));
        canvas.drawRect(x + 12, y + 22, x + 28, y + 40, paint);
        
        // Eyes
        paint.setColor(Color.BLACK);
        canvas.drawCircle(x + 16, y + 30, 2, paint);
        canvas.drawCircle(x + 24, y + 30, 2, paint);
    }
}
