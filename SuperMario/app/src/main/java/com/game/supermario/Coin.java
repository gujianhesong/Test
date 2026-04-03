package com.game.supermario;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Coin {

    private float x, y;
    private float width, height;
    private boolean collected;
    private float rotation;

    public Coin(float x, float y) {
        this.x = x;
        this.y = y;
        this.width = 30;
        this.height = 30;
        this.collected = false;
        this.rotation = 0;
    }

    public void update() {
        if (!collected) {
            rotation -= 5; // Counter-clockwise rotation (negative value)
            if (rotation <= -360) {
                rotation = 0;
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

        // Save canvas state before applying transformations
        int saveCount = canvas.save();

        // Apply rotation around the center of the coin
        float centerX = x + width / 2;
        float centerY = y + height / 2;
//        canvas.rotate(rotation, centerX, centerY);

        // Calculate scale for 3D flipping effect
        // Scale X from 1 to 0 and back to 1, then to -1 (flip) and back
        float scaleScale = (float) Math.cos(Math.toRadians(rotation));

        // Draw front or back face based on scale
        boolean isFrontFace = scaleScale >= 0;

        // Apply horizontal scale for 3D flip effect
        canvas.scale(scaleScale, 1, centerX, centerY);

        // Draw coin (yellow circle)
        paint.setColor(Color.rgb(255, 215, 0)); // Gold
        canvas.drawCircle(centerX, centerY, width / 2, paint);

        if (isFrontFace) {
            // Front face - Inner circle for shine effect
            paint.setColor(Color.rgb(255, 255, 150));
            canvas.drawCircle(centerX - 3, centerY - 3, width / 4, paint);

            // Front face - Dollar sign
            paint.setColor(Color.rgb(200, 150, 0));
            paint.setTextSize(20);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("$", centerX, centerY + 7, paint);
        } else {
            // Back face - Different pattern (star or simpler design)
            paint.setColor(Color.rgb(255, 200, 100));
            paint.setTextSize(20);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("★", centerX, centerY + 7, paint);
        }

        // Restore canvas to original state
        canvas.restoreToCount(saveCount);
    }
}