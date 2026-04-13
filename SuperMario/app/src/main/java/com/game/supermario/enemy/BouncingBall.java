package com.game.supermario.enemy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class BouncingBall extends Enemy {

    private float bounceVelocity;
    private boolean isOnGround;
    private static final float BOUNCE_STRENGTH = -15f;
    private static final float GRAVITY = 0.5f;

    public BouncingBall(float x, float y) {
        super(x, y);
        this.bounceVelocity = 0;
        this.isOnGround = false;
        setPatrolDistance(200);
    }

    @Override
    public void update(java.util.List<android.graphics.RectF> platforms) {
        if (!active) return;

        // Always apply horizontal movement
        x += velocityX;

        // Check patrol bounds
        if (x >= endX || x <= startX) {
            velocityX = -velocityX;
        }

        // Apply gravity to vertical movement
        bounceVelocity += GRAVITY;
        y += bounceVelocity;

        // Check collision with platforms
        boolean onGround = false;
        android.graphics.RectF enemyBounds = getBounds();

        for (android.graphics.RectF platform : platforms) {
            if (android.graphics.RectF.intersects(enemyBounds, platform) &&
                    enemyBounds.bottom >= platform.top &&
                    enemyBounds.bottom <= platform.top + 20 &&
                    bounceVelocity >= 0) { // Only bounce when falling

                // Land on platform
                y = platform.top - height;
                bounceVelocity = BOUNCE_STRENGTH; // Bounce!
                onGround = true;
                break;
            }
        }

        // If fell too far, despawn
        if (y > originalY + 500) {
            active = false;
        }
    }

    @Override
    public EnemyType getEnemyType() {
        return EnemyType.BouncingBall;
    }

    @Override
    public void draw(Canvas canvas, Paint paint) {
        if (!active) return;

        // Draw ball body (red/orange gradient effect)
        paint.setColor(Color.rgb(255, 100, 50));
        canvas.drawCircle(x + width / 2, y + height / 2, width / 2, paint);

        // Highlight (top-left)
        paint.setColor(Color.rgb(255, 200, 150));
        canvas.drawCircle(x + width / 2 - 8, y + height / 2 - 8, 8, paint);

        // Shadow/bottom
        paint.setColor(Color.rgb(200, 50, 0));
        canvas.drawArc(
                x + 5, y + height - 15,
                x + width - 5, y + height + 5,
                0, 180, true, paint
        );

        // Eyes (cute bouncing ball face)
        paint.setColor(Color.WHITE);
        canvas.drawCircle(x + 18, y + 20, 6, paint);
        canvas.drawCircle(x + 32, y + 20, 6, paint);

        paint.setColor(Color.BLACK);
        canvas.drawCircle(x + 18, y + 20, 3, paint);
        canvas.drawCircle(x + 32, y + 20, 3, paint);

        // Smile
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);
        canvas.drawArc(x + 15, y + 25, x + 35, y + 35, 30, 120, false, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // Draw movement indicator if needed
        if (movementType == 2) {
            // Faller variant - red arrow
            paint.setColor(Color.argb(200, 255, 0, 0));
            canvas.drawLine(x + width / 2, y + height + 5, x + width / 2, y + height + 20, paint);
            canvas.drawLine(x + width / 2 - 5, y + height + 15, x + width / 2, y + height + 20, paint);
            canvas.drawLine(x + width / 2 + 5, y + height + 15, x + width / 2, y + height + 20, paint);
        }
    }
}
