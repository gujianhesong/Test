package com.game.supermario;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Brick {

    private float x, y;
    private float width, height;
    private boolean destroyed;
    private int contentType; // 0: Coin, 1: Mushroom, 2: Nothing (just break)
    private boolean isQuestionBlock; // true = question block, false = regular brick
    private float bounceOffset; // For bounce animation
    private boolean bouncing;

    // Break animation
    private boolean breaking;
    private float breakFrame;
    private java.util.List<BrickParticle> particles;

    public Brick(float x, float y, int contentType, boolean isQuestionBlock) {
        this.x = x;
        this.y = y;
        this.width = 40;
        this.height = 40;
        this.destroyed = false;
        this.contentType = contentType;
        this.isQuestionBlock = isQuestionBlock;
        this.bounceOffset = 0;
        this.bouncing = false;
        this.breaking = false;
        this.breakFrame = 0;
        this.particles = new java.util.ArrayList<>();
    }

    public void hit() {
        if (destroyed || bouncing || breaking) return;

        if (isQuestionBlock) {
            // Question block just bounces
            bouncing = true;

            // If it contains something, release it
            if (contentType >= 0) {
                destroyed = true;
            }
        } else {
            // Regular brick breaks
            breaking = true;
            createParticles();
        }
    }

    private void createParticles() {
        // Create brick debris particles
        particles.add(new BrickParticle(x, y, BrickPiece.TOP_LEFT));
        particles.add(new BrickParticle(x + 20, y, BrickPiece.TOP_RIGHT));
        particles.add(new BrickParticle(x, y + 20, BrickPiece.BOTTOM_LEFT));
        particles.add(new BrickParticle(x + 20, y + 20, BrickPiece.BOTTOM_RIGHT));
    }

    public void update() {
        if (bouncing) {
            // Bounce animation for question block
            bounceOffset -= 8;
            if (bounceOffset <= -20) {
                bounceOffset = 0;
                bouncing = false;

                // If regular brick with content, destroy it after bounce
                if (!isQuestionBlock && contentType >= 0) {
                    destroyed = true;
                }
            }
        }
        if (breaking) {
            // Update break animation
            breakFrame += 0.5f;
            // Update particles
            for (BrickParticle particle : particles) {
                particle.update();
            }

            // Animation complete after all particles fall
            if (breakFrame > 20) {
                destroyed = true;
            }
        }
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public boolean isBouncing() {
        return bouncing;
    }

    public boolean isQuestionBlock() {
        return isQuestionBlock;
    }

    public int getContentType() {
        return contentType;
    }

    public RectF getBounds() {
        return new RectF(x, y + bounceOffset, x + width, y + height + bounceOffset);
    }

    public void draw(Canvas canvas, Paint paint) {
        // Don't draw if completely destroyed and not in breaking animation
        if (destroyed && !breaking && particles.isEmpty()) return;

        if (breaking) {
            // Draw breaking animation
            drawBreakingBrick(canvas, paint);
        } else if (isQuestionBlock) {
            drawQuestionBlock(canvas, paint);
        } else {
            drawRegularBrick(canvas, paint);
        }
    }

    private void drawBreakingBrick(Canvas canvas, Paint paint) {
        // Draw particles instead of the whole brick
        for (BrickParticle particle : particles) {
            particle.draw(canvas, paint);
        }
    }

    private void drawQuestionBlock(Canvas canvas, Paint paint) {
        // Block body (golden/orange)
        paint.setColor(Color.rgb(255, 165, 0));
        canvas.drawRect(x, y + bounceOffset, x + width, y + height + bounceOffset, paint);

        // Border
        paint.setColor(Color.rgb(200, 100, 0));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        canvas.drawRect(x, y + bounceOffset, x + width, y + height + bounceOffset, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // Question mark
        paint.setColor(Color.WHITE);
        paint.setTextSize(28);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("?", x + width / 2, y + height / 2 + bounceOffset + 10, paint);

        // Corner rivets
        paint.setColor(Color.rgb(150, 75, 0));
        float rivetSize = 4;
        canvas.drawCircle(x + 5, y + bounceOffset + 5, rivetSize, paint);
        canvas.drawCircle(x + width - 5, y + bounceOffset + 5, rivetSize, paint);
        canvas.drawCircle(x + 5, y + height + bounceOffset - 5, rivetSize, paint);
        canvas.drawCircle(x + width - 5, y + height + bounceOffset - 5, rivetSize, paint);
    }

    private void drawRegularBrick(Canvas canvas, Paint paint) {
        // Brick pattern (brown/red)
        paint.setColor(Color.rgb(178, 34, 34));
        canvas.drawRect(x, y + bounceOffset, x + width, y + height + bounceOffset, paint);

        // Brick lines
        paint.setColor(Color.rgb(139, 69, 19));
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(2);

        // Horizontal line in middle
        canvas.drawLine(x, y + height / 2 + bounceOffset, x + width, y + height / 2 + bounceOffset, paint);

        // Vertical lines (offset for brick pattern)
        canvas.drawLine(x + width / 2, y + bounceOffset, x + width / 2, y + height / 2 + bounceOffset, paint);
        canvas.drawLine(x + width / 4, y + height / 2 + bounceOffset, x + width / 4, y + height + bounceOffset, paint);
        canvas.drawLine(x + width * 3 / 4, y + height / 2 + bounceOffset, x + width * 3 / 4, y + height + bounceOffset, paint);

        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);
    }

    // Inner class for brick pieces
    public enum BrickPiece {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    // Inner class for brick particles
    private static class BrickParticle {
        float x, y;
        float velocityX, velocityY;
        BrickPiece piece;

        public BrickParticle(float x, float y, BrickPiece piece) {
            this.x = x;
            this.y = y;
            this.piece = piece;

            // Initial velocities based on piece position
            switch (piece) {
                case TOP_LEFT:
                    this.velocityX = -3;
                    this.velocityY = -8;
                    break;
                case TOP_RIGHT:
                    this.velocityX = 3;
                    this.velocityY = -8;
                    break;
                case BOTTOM_LEFT:
                    this.velocityX = -3;
                    this.velocityY = -6;
                    break;
                case BOTTOM_RIGHT:
                    this.velocityX = 3;
                    this.velocityY = -6;
                    break;
                default:
                    this.velocityX = 0;
                    this.velocityY = -7;
            }
        }

        public void update() {
            // Apply gravity
            velocityY += 0.5f;

            // Move particle
            x += velocityX;
            y += velocityY;
        }

        public void draw(Canvas canvas, Paint paint) {
            paint.setColor(Color.rgb(178, 34, 34)); // Brick red

            // Draw piece based on type
            switch (piece) {
                case TOP_LEFT:
                    canvas.drawRect(x, y, x + 20, y + 20, paint);
                    break;
                case TOP_RIGHT:
                    canvas.drawRect(x, y, x + 20, y + 20, paint);
                    break;
                case BOTTOM_LEFT:
                    canvas.drawRect(x, y, x + 20, y + 20, paint);
                    break;
                case BOTTOM_RIGHT:
                    canvas.drawRect(x, y, x + 20, y + 20, paint);
                    break;
            }

            // Add detail lines
            paint.setColor(Color.rgb(139, 69, 19));
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            canvas.drawLine(x, y + 10, x + 20, y + 10, paint);
            paint.setStyle(Paint.Style.FILL);
            paint.setStrokeWidth(1);
        }
    }
}