package com.game.supermario.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Koopa extends Enemy {

    public Koopa(float x, float y) {
        super(x, y);
        setMovementType(1);
        setPatrolDistance(3000);
    }

    @Override
    public EnemyType getEnemyType() {
        return EnemyType.Koopa;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        if (!active) return;

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
