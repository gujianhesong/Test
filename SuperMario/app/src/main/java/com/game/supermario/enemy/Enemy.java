package com.game.supermario.enemy;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.List;

public abstract class Enemy {
    public enum EnemyType{
        Goomba,
        Koopa,
        BouncingBall,
        ;

        public static EnemyType valueToType(int value){
            EnemyType[] values = EnemyType.values();
            for(EnemyType item : values){
                if(item.ordinal() == value){
                    return item;
                }
            }
            return Goomba;
        }
    }

    public static Enemy createEnemyWithType(float x, float y, Enemy.EnemyType enemyType){
        switch (enemyType){
            case Goomba:
                return new Goomba(x, y);
            case Koopa:
                return new Koopa(x, y);
            case BouncingBall:
                return new BouncingBall(x, y);
        }
        return new Goomba(x, y);
    }

    protected float x, y;
    protected float width, height;
    protected float velocityX;
    protected float startX, endX;
    protected boolean active;
    protected float patrolDistance = 200;
    protected int type; // Enemy type
    protected int movementType; // 0: Float, 1: Ground Walker, 2: Faller
    protected float originalY;

    public Enemy(float x, float y) {
        this.x = x;
        this.y = y;
        this.width = 50;
        this.height = 50;
        this.startX = x - patrolDistance;
        this.endX = x + patrolDistance;
        this.velocityX = 2;
        this.active = true;
        this.originalY = y;
    }

    public void update(List<RectF> platforms) {
        if (!active) return;

        if (movementType == 0) {
            updateFloating();
        } else if (movementType == 1) {
            updateGroundWalker(platforms);
        } else if (movementType == 2) {
            updateFaller(platforms);
        }
    }

    private void updateFloating() {
        x += velocityX;
        if (x >= endX || x <= startX) {
            velocityX = -velocityX;
        }
    }

    private void updateGroundWalker(List<RectF> platforms) {
        y += 5;

        boolean onGround = false;
        RectF enemyBounds = getBounds();

        for (RectF platform : platforms) {
            if (RectF.intersects(enemyBounds, platform) &&
                    enemyBounds.bottom >= platform.top &&
                    enemyBounds.bottom <= platform.top + 20 &&
                    y + height <= platform.top + 10) {
                y = platform.top - height;
                onGround = true;
                break;
            }
        }

        if (onGround) {
            x += velocityX;

            if (x >= endX || x <= startX) {
                velocityX = -velocityX;
            }

            RectF currentPlatform = null;
            for (RectF platform : platforms) {
                if (RectF.intersects(enemyBounds, platform) &&
                        Math.abs(platform.top - (y + height)) < 5) {
                    currentPlatform = platform;
                    break;
                }
            }

            if (currentPlatform != null) {
                float rightEdge = currentPlatform.right - width - 5;
                float leftEdge = currentPlatform.left + 5;

                if (x <= leftEdge || x >= rightEdge) {
                    velocityX = -velocityX;
                }
            }
        }
    }

    private void updateFaller(List<RectF> platforms) {
        y += 6;

        boolean onGround = false;
        RectF enemyBounds = getBounds();

        for (RectF platform : platforms) {
            if (RectF.intersects(enemyBounds, platform) &&
                    enemyBounds.bottom >= platform.top &&
                    enemyBounds.bottom <= platform.top + 20 &&
                    y + height <= platform.top + 10) {
                y = platform.top - height;
                onGround = true;
                break;
            }
        }

        if (onGround) {
            x += velocityX;
            if (x >= endX || x <= startX) {
                velocityX = -velocityX;
            }
        } else {
            if (y > originalY + 500) {
                active = false;
            }
        }
    }

    public void defeat() {
        active = false;
    }

    public boolean isActive() {
        return active;
    }

    public RectF getBounds() {
        return new RectF(x, y, x + width, y + height);
    }

    public abstract EnemyType getEnemyType();

    public abstract void draw(Canvas canvas, Paint paint);

    public float getPatrolDistance(){
        return patrolDistance;
    }

    public void setPatrolDistance(float patrolDistance){
        this.patrolDistance = patrolDistance;
        this.startX = x - patrolDistance;
        this.endX = x + patrolDistance;
    }

    public int getMovementType() {
        return movementType;
    }

    public void setMovementType(int movementType) {
        this.movementType = movementType;
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

    public float getPatrolWidth() {
        return endX - startX;
    }
}