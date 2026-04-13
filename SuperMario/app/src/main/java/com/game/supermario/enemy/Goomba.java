package com.game.supermario.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Goomba extends Enemy {

    public Goomba(float x, float y) {
        super(x, y);
        setMovementType(0);
        setPatrolDistance(300);
    }

    @Override
    public EnemyType getEnemyType() {
        return EnemyType.Goomba;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        if (!active) return;

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

        // Draw movement type indicator
        drawMovementIndicator(canvas, paint);
    }

    private void drawMovementIndicator(Canvas canvas, Paint paint) {
        if (movementType == 0) {
            // Floating - draw small cloud underneath
            paint.setColor(Color.argb(150, 255, 255, 255));
            canvas.drawOval(x + 10, y + height - 5, x + width - 10, y + height + 10, paint);
        } else if (movementType == 2) {
            // Faller - draw red arrow pointing down
            paint.setColor(Color.argb(200, 255, 0, 0));
            canvas.drawLine(x + width / 2, y + height + 5, x + width / 2, y + height + 20, paint);
            canvas.drawLine(x + width / 2 - 5, y + height + 15, x + width / 2, y + height + 20, paint);
            canvas.drawLine(x + width / 2 + 5, y + height + 15, x + width / 2, y + height + 20, paint);
        }
    }
}