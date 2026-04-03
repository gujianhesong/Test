package com.game.supermario;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

public class Player {
    
    private float x, y;
    private float velocityX, velocityY;
    private float width, height;
    private boolean isJumping;
    private boolean facingRight;
    private float walkCycle; // Animation cycle for walking
    private float idleCycle; // Animation cycle for idle breathing
    private boolean isBig; // Powered up state
    private boolean canShoot; // Can shoot bullets
    private long lastShotTime; // For shooting cooldown
    private int hitCooldown; // Invincibility after getting hit
    
    // Normal size
    private static final float NORMAL_WIDTH = 60;
    private static final float NORMAL_HEIGHT = 90;
    // Big size
    private static final float BIG_WIDTH = 70;
    private static final float BIG_HEIGHT = 110;
    
    // Physics constants
    private static final float GRAVITY = 0.6f;
    private static final float JUMP_STRENGTH = -18f;
    private static final float MOVE_SPEED = 10f;
    private static final float MAX_VELOCITY = 15f;
    
    public Player(float x, float y) {
        this.x = x;
        this.y = y;
        this.width = NORMAL_WIDTH;
        this.height = NORMAL_HEIGHT;
        this.velocityX = 0;
        this.velocityY = 0;
        this.isJumping = false;
        this.facingRight = true;
        this.walkCycle = 0;
        this.idleCycle = 0;
        this.isBig = false;
        this.canShoot = false;
        this.lastShotTime = 0;
        this.hitCooldown = 0;
    }
    
    public void update(boolean left, boolean right, boolean jump) {
        // Update idle animation always runs
        idleCycle += 0.08f;
        if (idleCycle > Math.PI * 2) {
            idleCycle -= (float)(Math.PI * 2);
        }
        
        // Horizontal movement
        if (left) {
            velocityX = -MOVE_SPEED;
            facingRight = false;
            // Update walk animation
            walkCycle += 0.5f;
            if (walkCycle > Math.PI * 2) {
                walkCycle -= (float)(Math.PI * 2);
            }
        } else if (right) {
            velocityX = MOVE_SPEED;
            facingRight = true;
            // Update walk animation
            walkCycle += 0.5f;
            if (walkCycle > Math.PI * 2) {
                walkCycle -= (float)(Math.PI * 2);
            }
        } else {
            velocityX *= 0.8f; // Friction
        }

        // Jump
        if (jump && !isJumping) {
            velocityY = JUMP_STRENGTH;
            isJumping = true;
        }
        
        // Apply velocity
        x += velocityX;
        y += velocityY;
        
        // Limit horizontal velocity
        if (velocityX > MAX_VELOCITY) velocityX = MAX_VELOCITY;
        if (velocityX < -MAX_VELOCITY) velocityX = -MAX_VELOCITY;
    }
    
    public void applyGravity() {
        velocityY += GRAVITY;
    }
    
    public void landOnPlatform(float platformY) {
        y = platformY - height;
        velocityY = 0;
        isJumping = false;
    }
    
    public void bounce() {
        velocityY = JUMP_STRENGTH / 2;
    }
    
    public void powerUp() {
        if (!isBig) {
            isBig = true;
            width = BIG_WIDTH;
            height = BIG_HEIGHT;
            y -= 20; // Adjust position to stay on ground
        } else {
            canShoot = true;
        }
    }
    
    public boolean hit() {
        if (hitCooldown > 0) {
            return false; // Already invincible
        }
        
        if (isBig) {
            // Shrink instead of dying
            isBig = false;
            canShoot = false;
            width = NORMAL_WIDTH;
            height = NORMAL_HEIGHT;
            hitCooldown = 120; // 2 seconds invincibility at 60 FPS
            return false; // Didn't die
        }
        return true; // Did die
    }
    
    public boolean canShootBullet() {
        return canShoot && System.currentTimeMillis() - lastShotTime > 500; // 500ms cooldown
    }
    
    public Bullet shoot(float x, float y) {
        if (canShootBullet()) {
            lastShotTime = System.currentTimeMillis();
            return new Bullet(x + (facingRight ? width : 0), y + height / 2 - 7, facingRight);
        }
        return null;
    }
    
    public void updateCooldown() {
        if (hitCooldown > 0) {
            hitCooldown--;
        }
    }
    
    public boolean isInvincible() {
        return hitCooldown > 0;
    }
    
    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        this.velocityX = 0;
        this.velocityY = 0;
    }
    
    public boolean collidesWith(RectF rect) {
        RectF playerRect = getBounds();
        return RectF.intersects(playerRect, rect);
    }
    
    public RectF getBounds() {
        return new RectF(x, y, x + width, y + height);
    }
    
    public void draw(Canvas canvas, Paint paint) {
        float w = width;
        float h = height;
        
        // Flash when invincible (after getting hit)
        if (hitCooldown > 0 && (hitCooldown / 5) % 2 == 0) {
            return; // Skip drawing this frame for flashing effect
        }
        
        // Calculate idle breathing animation (always runs)
        float breathOffset = (float)(Math.sin(idleCycle) * 2f); // Body breathes up/down
        float headBob = (float)(Math.sin(idleCycle * 0.5) * 0.8f); // Head bobs slightly
        
        // Calculate leg animation based on walk cycle
        float legOffset1 = 0;
        float legOffset2 = 0;
        
        if (!isJumping && Math.abs(velocityX) > 0.5f) {
            // Walking animation - legs swing back and forth
            legOffset1 = (float)(Math.sin(walkCycle) * 12);
            legOffset2 = (float)(Math.sin(walkCycle + Math.PI) * 12);
        } else if (isJumping) {
            // Jumping pose - legs bent
            legOffset1 = -8;
            legOffset2 = 8;
        } else {
            // Idle stance - slight weight shift
            legOffset1 = (float)(Math.sin(idleCycle * 0.3) * 3);
            legOffset2 = -(float)(Math.sin(idleCycle * 0.3) * 3);
        }
        
        // === DRAW MARIO ===
        
        // Different colors for big/powerup state
        int overallColor = isBig ? Color.rgb(255, 215, 0) : Color.rgb(50, 100, 220); // Gold when big, blue when normal
        int shirtColor = canShoot ? Color.rgb(255, 100, 100) : Color.rgb(220, 40, 40); // Brighter red when can shoot
        
        // 1. Legs with animation (blue/gold overalls)
        paint.setColor(overallColor);
        // Left leg
        canvas.drawRect(x + 12 + legOffset1, y + h - 30 + breathOffset, 
                       x + 28 + legOffset1, y + h - 12 + breathOffset, paint);
        // Right leg
        canvas.drawRect(x + w - 28 + legOffset2, y + h - 30 + breathOffset, 
                       x + w - 12 + legOffset2, y + h - 12 + breathOffset, paint);
        
        // 2. Boots (brown) - bigger boots
        paint.setColor(Color.rgb(139, 90, 43)); // Brown
        // Left boot
        canvas.drawRect(x + 10 + legOffset1, y + h - 12 + breathOffset, 
                       x + 30 + legOffset1, y + h - 2 + breathOffset, paint);
        // Right boot
        canvas.drawRect(x + w - 30 + legOffset2, y + h - 12 + breathOffset, 
                       x + w - 10 + legOffset2, y + h - 2 + breathOffset, paint);
        
        // 3. Body/Overalls - bigger torso
        paint.setColor(overallColor);
        canvas.drawRect(x + 10, y + h - 60 + breathOffset, 
                       x + w - 10, y + h - 30 + breathOffset, paint);
        
        // 4. Arms with animation (red shirt) - thicker arms
        paint.setColor(shirtColor);
        float armSwing;
        if (isJumping) {
            armSwing = -15;
        } else if (Math.abs(velocityX) > 0.5f) {
            armSwing = legOffset1 * 0.7f;
        } else {
            // Idle - arms rest at sides with slight movement
            armSwing = (float)(Math.sin(idleCycle * 0.5) * 2f);
        }
        
        // Left arm
        canvas.drawRect(x + 3 + armSwing, y + h - 55 + breathOffset, 
                       x + 15 + armSwing, y + h - 32 + breathOffset, paint);
        // Right arm  
        canvas.drawRect(x + w - 15 - armSwing, y + h - 55 + breathOffset, 
                       x + w - 3 - armSwing, y + h - 32 + breathOffset, paint);
        
        // 5. Hands (skin color) - bigger hands
        paint.setColor(Color.rgb(255, 220, 180));
        canvas.drawCircle(x + 9 + armSwing, y + h - 30 + breathOffset, 6, paint);
        canvas.drawCircle(x + w - 9 - armSwing, y + h - 30 + breathOffset, 6, paint);
        
        // 6. Head (circle, skin color) - bigger head
        paint.setColor(Color.rgb(255, 220, 180));
        float headSize = isBig ? 26 : 22;
        canvas.drawCircle(x + w / 2, y + 28 + headBob, headSize, paint);
        
        // 7. Hat (red cap) - bigger hat
        paint.setColor(shirtColor);
        // Hat dome
        canvas.drawArc(new android.graphics.RectF(x + 8, y + 5 + headBob, x + w - 8, y + 35 + headBob), 
                      180, 180, false, paint);
        // Hat brim (extends in facing direction) - longer brim
        if (facingRight) {
            canvas.drawRect(x + 32, y + 20 + headBob, x + 55, y + 28 + headBob, paint);
        } else {
            canvas.drawRect(x + 5, y + 20 + headBob, x + 28, y + 28 + headBob, paint);
        }
        
        // 8. Face features - bigger features
        if (facingRight) {
            // Eye - bigger
            paint.setColor(Color.BLACK);
            canvas.drawCircle(x + 42, y + 26 + headBob, 3, paint);
            
            // Nose (small circle) - bigger
            paint.setColor(Color.rgb(255, 200, 180));
            canvas.drawCircle(x + 48, y + 32 + headBob, 5, paint);
            
            // Mustache (simple curve) - thicker
            paint.setColor(Color.rgb(50, 30, 20));
            canvas.drawLine(x + 38, y + 35 + headBob, x + 52, y + 35 + headBob, paint);
            canvas.drawLine(x + 40, y + 37 + headBob, x + 50, y + 37 + headBob, paint);
        } else {
            // Eye (flipped) - bigger
            paint.setColor(Color.BLACK);
            canvas.drawCircle(x + w - 42, y + 26 + headBob, 3, paint);
            
            // Nose - bigger
            paint.setColor(Color.rgb(255, 200, 180));
            canvas.drawCircle(x + w - 48, y + 32 + headBob, 5, paint);
            
            // Mustache
            paint.setColor(Color.rgb(50, 30, 20));
            canvas.drawLine(x + w - 52, y + 35 + headBob, x + w - 38, y + 35 + headBob, paint);
            canvas.drawLine(x + w - 50, y + 37 + headBob, x + w - 40, y + 37 + headBob, paint);
        }
    }
    
    public float getX() {
        return x;
    }
    
    public float getY() {
        return y;
    }
    
    public float getWidth() {
        return width;
    }
    
    public float getHeight() {
        return height;
    }
    
    public float getVelocityY() {
        return velocityY;
    }

    public void setVelocityY(float velocityY) {
        this.velocityY = velocityY;
    }
}
