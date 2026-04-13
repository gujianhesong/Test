package com.game.bomberman;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Door {
    private float x, y;
    private int size;
    private boolean unlocked;

    public Door(float x, float y, int size) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.unlocked = false;
    }

    public void draw(Canvas canvas, Paint paint, int offsetX, int offsetY) {
        float drawX = x + offsetX;
        float drawY = y + offsetY;

        if (unlocked) {
            // Draw open door (green frame)
            paint.setColor(Color.GREEN);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(3);
            canvas.drawRect(drawX - size/2, drawY - size/2, drawX + size/2, drawY + size/2, paint);

            // Draw "OPEN" text
            paint.setColor(Color.GREEN);
            paint.setTextSize(size/3);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawText("OPEN", drawX, drawY + size/6, paint);
        } else {
            // Draw closed door (brown with lock)
            paint.setColor(Color.rgb(139, 69, 19)); // Brown
            canvas.drawRect(drawX - size/2, drawY - size/2, drawX + size/2, drawY + size/2, paint);

            // Draw door frame
            paint.setColor(Color.rgb(101, 67, 33)); // Darker brown
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            canvas.drawRect(drawX - size/2, drawY - size/2, drawX + size/2, drawY + size/2, paint);

            // Draw lock
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(255, 215, 0)); // Gold
            canvas.drawCircle(drawX, drawY, size/6, paint);

            // Draw keyhole
            paint.setColor(Color.BLACK);
            canvas.drawCircle(drawX, drawY, size/12, paint);
            canvas.drawRect(drawX - size/24, drawY, drawX + size/24, drawY + size/8, paint);
        }

        paint.setStyle(Paint.Style.FILL);
    }

    public Rect getBounds() {
        return new Rect((int)(x - size/2), (int)(y - size/2),
                (int)(x + size/2), (int)(y + size/2));
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isUnlocked() { return unlocked; }
    public void unlock() { this.unlocked = true; }
}
