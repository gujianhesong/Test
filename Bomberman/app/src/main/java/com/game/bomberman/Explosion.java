package com.game.bomberman;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Explosion {
    private float x, y;
    private int timer;
    private static final int DURATION = 30; // 0.5 seconds at 60fps
    private int size = 35;

    public Explosion(float x, float y) {
        this.x = x;
        this.y = y;
        this.timer = 0;
    }

    public void update() {
        timer++;
    }

    public void draw(Canvas canvas, Paint paint, int offsetX, int offsetY) {
        // Calculate alpha based on timer
        int alpha = 255 - (timer * 255 / DURATION);
        if (alpha < 0) alpha = 0;

        // Draw explosion as expanding circle
        float expansion = (float)timer / DURATION * 10;

        paint.setAlpha(alpha);
        paint.setColor(Color.rgb(255, 165, 0));
        canvas.drawCircle(x + offsetX, y + offsetY, size/2 + expansion, paint);

        paint.setColor(Color.YELLOW);
        canvas.drawCircle(x + offsetX, y + offsetY, size/3 + expansion/2, paint);

        paint.setAlpha(255); // Reset alpha
    }

    public Rect getBounds() {
        float expansion = (float)timer / DURATION * 10;
        int expandedSize = size + (int)(expansion * 2);
        return new Rect((int)(x - expandedSize/2), (int)(y - expandedSize/2),
                (int)(x + expandedSize/2), (int)(y + expandedSize/2));
    }

    public boolean isFinished() {
        return timer >= DURATION;
    }
}
