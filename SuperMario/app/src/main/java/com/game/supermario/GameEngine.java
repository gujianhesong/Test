package com.game.supermario;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.widget.Toast;

import com.game.supermario.enemy.Enemy;

public class GameEngine {
    
    private Context context;
    private Player player;
    private Level currentLevel;
    private int currentLevelIndex;
    private int lives;
    private int score;
    private boolean gameOver;
    private boolean victory;
    
    // Camera for scrolling
    private float cameraX;
    private float screenWidth;

    private boolean isRamdonMode = false;
    
    // Bullets list
    private java.util.List<Bullet> bullets;
    
    public GameEngine(Context context) {
        this.context = context;
        lives = 3;
        currentLevelIndex = 0;
        score = 0;
        gameOver = false;
        victory = false;
        cameraX = 0;
        bullets = new java.util.ArrayList<>();
        loadLevel(0);
    }
    
    public void setScreenWidth(float width) {
        this.screenWidth = width;
    }
    
    public float getCameraX() {
        return cameraX;
    }

    public void setRamdonMode(boolean isRamdonMode){
        this.isRamdonMode = isRamdonMode;
    }
    
    private void loadLevel(int levelIndex) {
        // Initialize LevelCreator if needed
        LevelCreator.initialize(context);
        currentLevelIndex = levelIndex;
        if (isRamdonMode) {
            //随机生成关卡模式
            currentLevel = LevelCreator.generateRandomLevel(levelIndex);
        } else {
            //正常关卡模式
            currentLevel = LevelCreator.loadCustomLevel(levelIndex);
            if (currentLevel == null) {
                switch (levelIndex) {
                    case 0:
                        currentLevel = LevelCreator.createLevel1();
                        break;
                    case 1:
                        currentLevel = LevelCreator.createLevel2();
                        break;
                    case 2:
                        currentLevel = LevelCreator.createLevel3();
                        break;
                    default:
                        Toast.makeText(context, "没有这个关卡，你可以长按去创建", Toast.LENGTH_SHORT).show();
                        ((Activity)context).finish();
                        return;
                }
            }
        }

        // Reset player position for new level - spawn on the ground
        // Move spawn point away from first enemy (at x=150)
        if (player == null) {
            player = new Player(50, 550); // Spawn at left edge, away from enemies
        } else {
            player.setPosition(50, 550);
        }
    }
    
    public void update(boolean left, boolean right, boolean jump, boolean shoot) {
        if (gameOver || victory) {
            // Tap to restart
            if (jump) {
                resetGame();
            }
            return;
        }
        
        player.update(left, right, jump);
        player.updateCooldown();
        
        // Apply gravity
        player.applyGravity();
        
        // Handle shooting
        if (shoot) {
            shootBullet();
        }
        
        // Update camera to follow player
        updateCamera();
        
        // Update enemies
        updateEnemies();
        
        // Update coins
        updateCoins();
        
        // Update mushrooms
        updateMushrooms();

        // Update bricks
        updateBricks();
        
        // Update bullets
        updateBullets();
        
        // Check platform collisions
        checkPlatformCollisions();

        // Check brick collisions
        checkBrickCollisions();
        
        // Check enemy collisions
        checkEnemyCollisions();
        
        // Check coin collisions
        checkCoinCollisions();
        
        // Check mushroom collisions
        checkMushroomCollisions();
        
        // Check bullet-enemy collisions
        checkBulletCollisions();
        
        // Check if player fell off the world
        if (player.getY() > 1000) {
            playerDie();
        }
        
        // Check if player reached the goal
        if (checkGoalReached()) {
            nextLevel();
        }
    }
    
    private void shootBullet() {
        Bullet bullet = player.shoot(player.getX(), player.getY());
        if (bullet != null) {
            bullets.add(bullet);
        }
    }
    
    private void updateBullets() {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update();
            
            // Remove bullets that go off screen
            if (bullet.getX() < cameraX - 100 || bullet.getX() > cameraX + screenWidth + 100) {
                bullet.deactivate();
                bullets.remove(i);
            }
        }
    }
    
    private void checkBulletCollisions() {
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            if (!bullet.isActive()) continue;
            
            for (Enemy enemy : currentLevel.getEnemies()) {
                if (enemy.isActive() && bullet.isActive() && 
                    RectF.intersects(bullet.getBounds(), enemy.getBounds())) {
                    enemy.defeat();
                    bullet.deactivate();
                    bullets.remove(i);
                    score += 200; // Bonus for shooting enemy
                    break;
                }
            }
        }
    }
    
    private void updateMushrooms() {
        for (Mushroom mushroom : currentLevel.getMushrooms()) {
            mushroom.update();
        }
    }

    private void updateBricks() {
        for (Brick brick : currentLevel.getBricks()) {
            brick.update();
        }
    }
    
    private void checkMushroomCollisions() {
        for (Mushroom mushroom : currentLevel.getMushrooms()) {
            if (!mushroom.isCollected() && player.collidesWith(mushroom.getBounds())) {
                mushroom.collect();
                player.powerUp();
                score += 500;
            }
        }
    }
    
    private void updateCamera() {
        // Camera follows player, keeping player at about 1/3 of screen from left
        float targetCameraX = player.getX() - screenWidth / 3;
        
        // Clamp camera to level bounds
        float maxCameraX = currentLevel.getWidth() - screenWidth;
        if (targetCameraX < 0) {
            cameraX = 0;
        } else if (targetCameraX > maxCameraX) {
            cameraX = maxCameraX;
        } else {
            cameraX = targetCameraX;
        }
    }
    
    private void updateEnemies() {
        for (Enemy enemy : currentLevel.getEnemies()) {
            enemy.update(currentLevel.getPlatforms());
        }
    }
    
    private void updateCoins() {
        for (Coin coin : currentLevel.getCoins()) {
            coin.update();
        }
    }
    
    private void checkCoinCollisions() {
        for (Coin coin : currentLevel.getCoins()) {
            if (!coin.isCollected() && player.collidesWith(coin.getBounds())) {
                coin.collect();
                score += 100;
            }
        }
    }
    
    private void checkPlatformCollisions() {
        for (RectF platform : currentLevel.getPlatforms()) {
            if (player.collidesWith(platform)) {
                // Simple collision resolution - land on top
                // Check if player is above the platform and falling down
                float playerBottom = player.getY() + player.getHeight();
                float playerTop = player.getY();
                float platformSurface = platform.top;
                
                // Player is landing on platform (was above, now colliding)
                if (player.getVelocityY() >= 0 && 
                    playerBottom >= platformSurface && 
                    playerBottom <= platformSurface + 30 &&
                    playerTop < platformSurface) {
                    player.landOnPlatform(platformSurface);
                }
            }
        }
    }

    private void checkBrickCollisions() {
        for (Brick brick : currentLevel.getBricks()) {
            if (!brick.isDestroyed() && !brick.isBouncing() && player.collidesWith(brick.getBounds())) {
                float playerBottom = player.getY() + player.getHeight();
                float playerTop = player.getY();
                float brickTop = brick.getBounds().top;
                float brickBottom = brick.getBounds().bottom;

                // Check if player is landing ON TOP of brick
                if (player.getVelocityY() >= 0 && // Falling down
                        playerBottom >= brickTop &&
                        playerBottom <= brickTop + 20 && // Close to brick top
                        playerTop < brickTop && // Player was above brick
                        player.getX() + player.getWidth() > brick.getBounds().left + 5 &&
                        player.getX() < brick.getBounds().right - 5) {

                    // Land on brick - don't break it!
                    player.landOnPlatform(brickTop);

                } else if (player.getVelocityY() < 0 && // Moving upward (jumping)
                        playerTop < brickBottom &&   // Player top near brick bottom
                        playerTop > brickBottom - 15 && // Very close to brick bottom
                        playerBottom > brickBottom && // Player is below brick
                        player.getX() + player.getWidth() > brick.getBounds().left + 5 &&
                        player.getX() < brick.getBounds().right - 5) {

                    // Hit brick from below!
                    android.util.Log.d("GameEngine", "Brick hit from below! QuestionBlock=" + brick.isQuestionBlock());

                    // Hit the brick!
                    brick.hit();

                    // Stop player's upward movement
                    if (player.getVelocityY() < 0) {
                        player.setVelocityY(0);
                    }

                    // Handle brick content or break effect
                    if (brick.isQuestionBlock()) {
                        // Question block - spawn content
                        if (brick.isDestroyed() && brick.getContentType() >= 0) {
                            if (brick.getContentType() == 0) {
                                // Spawn coin
                                Coin coin = new Coin(brick.getBounds().centerX() - 15, brick.getBounds().top - 30);
                                currentLevel.getCoins().add(coin);
                                score += 100;
                                android.util.Log.d("GameEngine", "Coin spawned!");
                            } else if (brick.getContentType() == 1) {
                                // Spawn mushroom
                                Mushroom mushroom = new Mushroom(brick.getBounds().centerX() - 20, brick.getBounds().top - 40);
                                currentLevel.getMushrooms().add(mushroom);
                                android.util.Log.d("GameEngine", "Mushroom spawned!");
                            }
                        }
                    } else {
                        // Regular brick - just breaks, no content
                        android.util.Log.d("GameEngine", "Regular brick breaking!");
                        score += 50;
                    }
                }
            }
        }
    }

    private void checkEnemyCollisions() {
        for (Enemy enemy : currentLevel.getEnemies()) {
            if (enemy.isActive() && player.collidesWith(enemy.getBounds())) {
                // Check if jumping on enemy (from above)
                if (player.getVelocityY() > 0 && 
                    player.getY() + player.getHeight() < enemy.getY() + enemy.getHeight() / 2) {
                    // Jumped on top - defeat enemy
                    enemy.defeat();
                    player.bounce();
                    score += 100;
                } else {
                    // Hit by enemy - use player's hit method
                    // Returns true if died, false if just shrunk (when big)
                    if (player.hit()) {
                        // Player is not big enough to survive, so die
                        playerDie();
                    } else {
                        // Player was big and shrunk instead of dying
                        // Give some invincibility frames
                        // Enemy is defeated when player gets hit
                        enemy.defeat();
                    }
                }
            }
        }
    }
    
    private boolean checkGoalReached() {
        RectF goal = currentLevel.getGoal();
        return player.collidesWith(goal);
    }
    
    private void playerDie() {
        lives--;
        if (lives <= 0) {
            gameOver = true;
        } else {
            // Respawn player on the ground
            player.setPosition(100, 630);
        }
    }
    
    private void nextLevel() {
        if (currentLevelIndex < 29) {
            loadLevel(currentLevelIndex + 1);
        } else {
            victory = true;
        }
    }
    
    private void resetGame() {
        lives = 3;
        score = 0;
        gameOver = false;
        victory = false;
        loadLevel(0);
    }
    
    public void setLevelIndex(int levelIndex) {
        loadLevel(levelIndex);
    }
    
    public void draw(Canvas canvas, Paint paint, float cameraX) {
        if (gameOver) {
            drawGameOver(canvas, paint);
            return;
        }
        
        if (victory) {
            drawVictory(canvas, paint);
            return;
        }
        
        // Save canvas state
        canvas.save();
        // Apply camera translation
        canvas.translate(-cameraX, 0);
        
        // Draw platforms
        paint.setColor(Color.rgb(139, 69, 19)); // Brown
        for (RectF platform : currentLevel.getPlatforms()) {
            canvas.drawRect(platform, paint);
            
            // Add grass on top
            paint.setColor(Color.rgb(34, 139, 34)); // Green
            canvas.drawRect(platform.left, platform.top, 
                          platform.right, platform.top + 10, paint);
            paint.setColor(Color.rgb(139, 69, 19));
        }
        
        // Draw coins
        for (Coin coin : currentLevel.getCoins()) {
            coin.draw(canvas, paint);
        }
        
        // Draw mushrooms
        for (Mushroom mushroom : currentLevel.getMushrooms()) {
            mushroom.draw(canvas, paint);
        }

        // Draw bricks
        for (Brick brick : currentLevel.getBricks()) {
            brick.draw(canvas, paint);
        }
        
        // Draw bullets
        for (Bullet bullet : bullets) {
            bullet.draw(canvas, paint);
        }
        
        // Draw goal
        paint.setColor(Color.rgb(255, 215, 0)); // Gold color
        RectF goal = currentLevel.getGoal();
        canvas.drawRect(goal, paint);
        paint.setColor(Color.BLACK);
        canvas.drawText("GOAL", goal.left + 10, goal.top + 40, paint);
        
        // Draw enemies
        for (Enemy enemy : currentLevel.getEnemies()) {
            if (enemy.isActive()) {
                enemy.draw(canvas, paint);
            }
        }
        
        // Draw player
        player.draw(canvas, paint);
        
        // Restore canvas state
        canvas.restore();
    }
    
    private void drawGameOver(Canvas canvas, Paint paint) {
        paint.setColor(Color.RED);
        paint.setTextSize(80);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("GAME OVER", canvas.getWidth() / 2, canvas.getHeight() / 2, paint);
        paint.setTextSize(40);
        canvas.drawText("Tap to restart", canvas.getWidth() / 2, canvas.getHeight() / 2 + 60, paint);
    }
    
    private void drawVictory(Canvas canvas, Paint paint) {
        paint.setColor(Color.GREEN);
        paint.setTextSize(80);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("VICTORY!", canvas.getWidth() / 2, canvas.getHeight() / 2, paint);
        paint.setTextSize(40);
        canvas.drawText("All levels completed!", canvas.getWidth() / 2, canvas.getHeight() / 2 + 60, paint);
        paint.setTextSize(50);
        canvas.drawText("Final Score: " + score, canvas.getWidth() / 2, canvas.getHeight() / 2 + 120, paint);
    }
    
    public int getCurrentLevel() {
        return currentLevelIndex + 1;
    }
    
    public int getLives() {
        return lives;
    }
    
    public int getScore() {
        return score;
    }
    
    public float getPlayerY() {
        return player != null ? player.getY() : 0;
    }
}
