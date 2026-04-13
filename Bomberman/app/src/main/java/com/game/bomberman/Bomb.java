package com.game.bomberman;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Bomb {
    private float x, y;
    private int timer;
    private static final int EXPLOSION_TIME = 180; // 3 seconds at 60fps
    private boolean exploded;

    public Bomb(float x, float y) {
        this.x = x;
        this.y = y;
        this.timer = 0;
        this.exploded = false;
    }

    public void update() {
        timer++;
        if (timer >= EXPLOSION_TIME) {
            exploded = true;
        }
    }

    public void draw(Canvas canvas, Paint paint, int offsetX, int offsetY) {
        // Draw bomb as black circle with fuse
        paint.setColor(Color.BLACK);
        canvas.drawCircle(x + offsetX, y + offsetY, 15, paint);

        // Draw fuse
        paint.setColor(Color.YELLOW);
        canvas.drawLine(x + offsetX, y + offsetY-15, x + offsetX+5, y + offsetY-20, paint);

        // Draw pulsing effect as timer increases
        float pulse = (float)Math.sin(timer * 0.1) * 3;
        paint.setColor(Color.RED);
        canvas.drawCircle(x + offsetX, y + offsetY, 10 + pulse, paint);
    }

    public float getX() { return x; }
    public float getY() { return y; }
    public boolean isExploded() { return exploded; }
}
