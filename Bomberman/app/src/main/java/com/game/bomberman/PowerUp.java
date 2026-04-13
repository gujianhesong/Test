package com.game.bomberman;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;

public class PowerUp {
    public enum Type {
        BOMB_UP,    // Increase bomb capacity
        FIRE_UP,    // Increase explosion range
        SPEED_UP,   // Increase movement speed
        EXTRA_LIFE, // Extra life
        KEY,        // Key to unlock door
        GUN,        // Gun with 10 bullets
        INVISIBILITY // Invisibility for 10 seconds
    }
    
    private float x, y;
    private Type type;
    private int size;
    private int baseSize = 20;
    
    public PowerUp(float x, float y, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.size = baseSize;
    }

    public void draw(Canvas canvas, Paint paint, int offsetX, int offsetY) {
        float drawX = x + offsetX;
        float drawY = y + offsetY;
        
        switch (type) {
            case BOMB_UP:
                paint.setColor(Color.BLACK);
                canvas.drawCircle(drawX, drawY, size/2, paint);
                paint.setColor(Color.RED);
                canvas.drawCircle(drawX - 3, drawY - 3, 3, paint);
                break;
            case FIRE_UP:
                paint.setColor(Color.rgb(255, 69, 0));
                canvas.drawRect(drawX - size/2, drawY - size/2, drawX + size/2, drawY + size/2, paint);
                paint.setColor(Color.YELLOW);
                canvas.drawRect(drawX - size/4, drawY - size/4, drawX + size/4, drawY + size/4, paint);
                break;
            case SPEED_UP:
                paint.setColor(Color.BLUE);
                canvas.drawCircle(drawX, drawY, size/2, paint);
                paint.setColor(Color.WHITE);
                paint.setTextSize(size/2);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("S", drawX, drawY + size/6, paint);
                break;
            case EXTRA_LIFE:
                paint.setColor(Color.RED);
                canvas.drawCircle(drawX - size/4, drawY, size/3, paint);
                canvas.drawCircle(drawX + size/4, drawY, size/3, paint);
                break;
            case KEY:
                paint.setColor(Color.rgb(255, 215, 0));
                canvas.drawCircle(drawX, drawY - size/4, size/3, paint);
                paint.setStrokeWidth(3);
                canvas.drawLine(drawX, drawY, drawX, drawY + size/2, paint);
                canvas.drawLine(drawX, drawY + size/3, drawX + size/4, drawY + size/3, paint);
                paint.setStrokeWidth(1);
                break;
            case GUN:
                // Draw detailed gun icon
                float gunX = drawX;
                float gunY = drawY;
                float scale = 1.5f; // Scale up by 50%
                
                // Gun barrel (longer and more prominent)
                paint.setColor(Color.rgb(80, 80, 80)); // Dark gray
                canvas.drawRect(gunX - size/2 * scale, gunY - size/6 * scale, 
                               gunX + size/3 * scale, gunY + size/12 * scale, paint);
                
                // Gun body/handle (wider)
                paint.setColor(Color.rgb(105, 105, 105)); // Dim Gray
                canvas.drawRect(gunX - size/3 * scale, gunY, 
                               gunX + size/6 * scale, gunY + size/2 * scale, paint);
                
                // Barrel tip (darker)
                paint.setColor(Color.rgb(50, 50, 50));
                canvas.drawRect(gunX + size/3 * scale, gunY - size/8 * scale, 
                               gunX + size/2 * scale, gunY + size/8 * scale, paint);
                
                // Trigger area
                paint.setColor(Color.rgb(60, 60, 60));
                canvas.drawCircle(gunX - size/8 * scale, gunY + size/4 * scale, size/10 * scale, paint);
                
                // Highlight on top of barrel
                paint.setColor(Color.rgb(180, 180, 180)); // Light gray highlight
                canvas.drawRect(gunX - size/2 * scale, gunY - size/6 * scale, 
                               gunX + size/3 * scale, gunY - size/12 * scale, paint);
                
                // Ammo badge (larger and more visible)
                paint.setColor(Color.YELLOW);
                canvas.drawCircle(gunX, gunY - size/2 * scale, size/3 * scale, paint);
                
                // Badge border
                paint.setColor(Color.rgb(255, 165, 0)); // Orange
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2);
                canvas.drawCircle(gunX, gunY - size/2 * scale, size/3 * scale, paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(1);
                
                // Ammo count text
                paint.setColor(Color.BLACK);
                paint.setTextSize(size/2 * scale);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setFakeBoldText(true);
                canvas.drawText("10", gunX, gunY - size/2 * scale + size/6 * scale, paint);
                paint.setFakeBoldText(false);
                break;
            case INVISIBILITY:
                // Draw invisibility cloak/ghost icon
                float cloakX = drawX;
                float cloakY = drawY;
                
                // Cloak body (semi-transparent purple)
                paint.setColor(Color.rgb(138, 43, 226)); // BlueViolet
                paint.setAlpha(180);
                
                // Draw cloak shape (triangle with rounded bottom)
                Path cloakPath = new Path();
                cloakPath.moveTo(cloakX, cloakY - size/2); // Top point
                cloakPath.lineTo(cloakX + size/2, cloakY + size/3); // Bottom right
                cloakPath.quadTo(cloakX, cloakY + size/2, cloakX - size/2, cloakY + size/3); // Rounded bottom
                cloakPath.close();
                canvas.drawPath(cloakPath, paint);
                
                paint.setAlpha(255);
                
                // Sparkle effects around cloak
                paint.setColor(Color.WHITE);
                float[] sparkleOffsets = {-size/3, 0, size/3};
                for (float offset : sparkleOffsets) {
                    canvas.drawCircle(cloakX + offset, cloakY - size/4, size/8, paint);
                }
                
                // "Ghost" eyes
                paint.setColor(Color.WHITE);
                canvas.drawCircle(cloakX - size/6, cloakY - size/6, size/8, paint);
                canvas.drawCircle(cloakX + size/6, cloakY - size/6, size/8, paint);
                
                paint.setColor(Color.BLACK);
                canvas.drawCircle(cloakX - size/6, cloakY - size/6, size/16, paint);
                canvas.drawCircle(cloakX + size/6, cloakY - size/6, size/16, paint);
                
                // Magic sparkles
                paint.setColor(Color.CYAN);
                for (int i = 0; i < 3; i++) {
                    float angle = (i * 120) * (float)Math.PI / 180;
                    float sparkleX = cloakX + (float)Math.cos(angle) * size/2;
                    float sparkleY = cloakY + (float)Math.sin(angle) * size/2;
                    canvas.drawCircle(sparkleX, sparkleY, 2, paint);
                }
                break;
        }
    }

    public Rect getBounds() {
        return new Rect((int)(x - size/2), (int)(y - size/2),
                (int)(x + size/2), (int)(y + size/2));
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public Type getType() { return type; }
}
