package com.game.bomberman;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.game.bomberman.enemy.BossEnemy;
import com.game.bomberman.enemy.ChaserEnemy;
import com.game.bomberman.enemy.Enemy;
import com.game.bomberman.enemy.JumperEnemy;
import com.game.bomberman.enemy.RandomEnemy;
import com.game.bomberman.enemy.SpeedsterEnemy;
import com.game.bomberman.enemy.SplitterEnemy;
import com.game.bomberman.enemy.TankEnemy;
import com.game.bomberman.enemy.TeleporterEnemy;

import java.util.ArrayList;
import java.util.List;

public class GameEngine {
    private Context context;
    private int screenWidth;
    private int screenHeight;
    private int offsetX;
    private int offsetY;

    // Control buttons
    private Rect upButton, downButton, leftButton, rightButton, bombButton, gunButton;
    private Rect dpadBounds; // Overall D-pad circular area
    private boolean upPressed, downPressed, leftPressed, rightPressed;

    // Game state
    private boolean gameOver;
    private boolean levelComplete;

    // Grid dimensions
    private static final int GRID_WIDTH = 20;
    private static final int GRID_HEIGHT = 18;
    private static int CELL_SIZE = 40;

    // Game objects
    private Player player;
    private List<Enemy> enemies;
    private List<Bomb> bombs;
    private List<Explosion> explosions;
    private List<PowerUp> powerUps;
    private List<Door> doors;
    private List<Bullet> bullets;
    private int[][] grid; // 0=empty, 1=wall, 2=breakable wall
    
    // Level management
    private LevelManager levelManager;
    
    // Player stats
    private int maxBombs = 3;
    private int bombRange = 2;
    private int lives = 3;
    private int score = 0;
    private int highScore = 0;
    private boolean hasKey = false;

    // Game constants
    private static final int WALL = 1;
    private static final int BREAKABLE_WALL = 2;
    private static final int EMPTY = 0;

    public GameEngine(Context context) {
        this.context = context;
        this.levelManager = new LevelManager();
        initGame();
    }

    private void initGame() {
        // Initialize grid
        grid = new int[GRID_HEIGHT][GRID_WIDTH];
        
        // Generate current level
        levelManager.generateLevel(grid, levelManager.getCurrentLevel());

        // Create player at starting position
        player = new Player(CELL_SIZE * 1 + CELL_SIZE/2, CELL_SIZE * 1 + CELL_SIZE/2);
        player.setSize(CELL_SIZE);

        // Initialize collections
        enemies = new ArrayList<>();
        bombs = new ArrayList<>();
        explosions = new ArrayList<>();
        powerUps = new ArrayList<>();
        doors = new ArrayList<>();
        bullets = new ArrayList<>();
        
        // Reset player stats on first level only
        if (levelManager.getCurrentLevel() == 1) {
            score = 0;
            maxBombs = 3;
            bombRange = 2;
            lives = 3;
        }
        
        // Reset key status for each level
        hasKey = false;

        // Add door for levels 2+
        if (levelManager.getCurrentLevel() > 1) {
            addDoor();
        }

        // Add enemies based on level
        addEnemies();

        gameOver = false;
        levelComplete = false;
    }
    
    private void addDoor() {
        // Place door at bottom-right corner
        int doorX = (GRID_WIDTH - 2) * CELL_SIZE + CELL_SIZE/2;
        int doorY = (GRID_HEIGHT - 2) * CELL_SIZE + CELL_SIZE/2;
        doors.add(new Door(doorX, doorY, CELL_SIZE));
        
        // Mark door position as wall in grid
        grid[GRID_HEIGHT - 2][GRID_WIDTH - 2] = 1;
    }

    private void addEnemies() {
        int enemyCount = levelManager.getEnemyCount(levelManager.getCurrentLevel());
        int currentLevel = levelManager.getCurrentLevel();
        
        // Add enemies at random positions
        for (int i = 0; i < enemyCount; i++) {
            int x, y;
            int attempts = 0;
            do {
                x = (int)(Math.random() * (GRID_WIDTH - 2)) + 1;
                y = (int)(Math.random() * (GRID_HEIGHT - 2)) + 1;
                attempts++;
            } while ((grid[y][x] != 0 || (x <= 3 && y <= 3)) && attempts < 100);

            if (attempts < 100) {
                Enemy enemy;

                // Different enemy types based on level
                double rand = Math.random();

                if (currentLevel >= 9) {
                    // Level 9-10: Mix of all types
                    if (rand < 0.18) {
                        enemy = new ChaserEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2, player);
                    } else if (rand < 0.34) {
                        enemy = new JumperEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    } else if (rand < 0.47) {
                        enemy = new TeleporterEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2, player);
                    } else if (rand < 0.61) {
                        enemy = new SpeedsterEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    } else if (rand < 0.74) {
                        enemy = new SplitterEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    } else if (rand < 0.86) {
                        enemy = new TankEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    } else {
                        enemy = new RandomEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    }
                } else if (currentLevel >= 8) {
                    // Level 8: Introduce tanks
                    if (rand < 0.22) {
                        enemy = new ChaserEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2, player);
                    } else if (rand < 0.40) {
                        enemy = new JumperEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    } else if (rand < 0.54) {
                        enemy = new TeleporterEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2, player);
                    } else if (rand < 0.67) {
                        enemy = new SpeedsterEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    } else if (rand < 0.79) {
                        enemy = new SplitterEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    } else if (rand < 0.89) {
                        enemy = new TankEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    } else {
                        enemy = new RandomEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    }
                } else if (currentLevel >= 7) {
                    // Level 7: No tanks yet
                    if (rand < 0.25) {
                        enemy = new ChaserEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2, player);
                    } else if (rand < 0.43) {
                        enemy = new JumperEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    } else if (rand < 0.56) {
                        enemy = new TeleporterEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2, player);
                    } else if (rand < 0.69) {
                        enemy = new SpeedsterEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    } else if (rand < 0.82) {
                        enemy = new SplitterEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    } else {
                        enemy = new RandomEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    }
                } else if (currentLevel >= 6) {
                    // Level 6: Chasers, Jumpers, and Speedsters
                    if (rand < 0.30) {
                        enemy = new ChaserEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2, player);
                    } else if (rand < 0.55) {
                        enemy = new JumperEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    } else if (rand < 0.70) {
                        enemy = new SpeedsterEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    } else {
                        enemy = new RandomEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    }
                } else if (currentLevel >= 3) {
                    // Level 3-5: Some chasers
                    if (rand < 0.25) {
                        enemy = new ChaserEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2, player);
                    } else {
                        enemy = new RandomEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    }
                } else {
                    // Level 1-2: Only random enemies
                    //enemy = new RandomEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    if (rand < 0.18) {
                        enemy = new ChaserEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2, player);
                    } else if (rand < 0.34) {
                        enemy = new JumperEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    } else if (rand < 0.47) {
                        enemy = new TeleporterEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2, player);
                    } else if (rand < 0.61) {
                        enemy = new SpeedsterEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    } else if (rand < 0.74) {
                        enemy = new SplitterEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    } else if (rand < 0.86) {
                        enemy = new TankEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2);
                    } else {
                        enemy = new BossEnemy(x * CELL_SIZE + CELL_SIZE/2, y * CELL_SIZE + CELL_SIZE/2, player);
                    }
                }

                enemy.setSize(CELL_SIZE);
                enemies.add(enemy);
            }
        }
    }

    public void update() {
        if (gameOver || levelComplete) return;

        // Update player
        player.update(grid, GRID_WIDTH, GRID_HEIGHT, CELL_SIZE);

        // Update player invincibility and invisibility
        player.updateInvincibility();
        player.updateInvisibility();

        // Update enemies
        List<Enemy> newEnemy = new ArrayList(enemies);
        for (Enemy enemy : newEnemy) {
            enemy.update(grid, GRID_WIDTH, GRID_HEIGHT, CELL_SIZE, player);

            // Check if boss can summon
            if (enemy instanceof BossEnemy) {
                BossEnemy boss = (BossEnemy) enemy;
                if (boss.canSummon() && newEnemy.size() < 10) {
                    summonEnemyForBoss(boss);
                    boss.resetSummonTimer();
                }
            }
        }

        // Update bombs
        for (int i = bombs.size() - 1; i >= 0; i--) {
            Bomb bomb = bombs.get(i);
            bomb.update();

            if (bomb.isExploded()) {
                // Create explosion
                createExplosion(bomb.getX(), bomb.getY());
                bombs.remove(i);
            }
        }

        // Update explosions
        for (int i = explosions.size() - 1; i >= 0; i--) {
            Explosion explosion = explosions.get(i);
            explosion.update();

            if (explosion.isFinished()) {
                explosions.remove(i);
            }
        }

        // Update bullets
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            bullet.update();

            // Remove bullets that are out of bounds
            if (bullet.isOutOfBounds(GRID_WIDTH, GRID_HEIGHT, CELL_SIZE)) {
                bullets.remove(i);
            }
        }

        // Check collisions
        checkCollisions();
    }

    private void summonEnemyForBoss(BossEnemy boss) {
        // Find a valid position near the boss
        int attempts = 0;
        float summonX, summonY;

        do {
            // Summon within 3 cells of boss
            int offsetX = (int)(Math.random() * 7) - 3; // -3 to 3
            int offsetY = (int)(Math.random() * 7) - 3;

            int bossGridX = (int)(boss.getX() / CELL_SIZE);
            int bossGridY = (int)(boss.getY() / CELL_SIZE);

            int gridX = bossGridX + offsetX;
            int gridY = bossGridY + offsetY;

            // Ensure within bounds
            gridX = Math.max(1, Math.min(gridX, GRID_WIDTH - 2));
            gridY = Math.max(1, Math.min(gridY, GRID_HEIGHT - 2));

            summonX = gridX * CELL_SIZE + CELL_SIZE/2;
            summonY = gridY * CELL_SIZE + CELL_SIZE/2;

            attempts++;
        } while (grid[(int)(summonY/CELL_SIZE)][(int)(summonX/CELL_SIZE)] != 0 && attempts < 50);

        if (attempts < 50) {
            // Summon random enemy type (excluding Boss)
            Enemy summonedEnemy;
            double rand = Math.random();

            if (rand < 0.25) {
                summonedEnemy = new ChaserEnemy(summonX, summonY, player);
            } else if (rand < 0.45) {
                summonedEnemy = new JumperEnemy(summonX, summonY);
            } else if (rand < 0.60) {
                summonedEnemy = new TeleporterEnemy(summonX, summonY, player);
            } else if (rand < 0.75) {
                summonedEnemy = new SpeedsterEnemy(summonX, summonY);
            } else if (rand < 0.85) {
                summonedEnemy = new SplitterEnemy(summonX, summonY);
            } else {
                summonedEnemy = new RandomEnemy(summonX, summonY);
            }

            summonedEnemy.setSize(CELL_SIZE);
            enemies.add(summonedEnemy);
        }
    }

    private void createExplosion(float x, float y) {
        // Create explosion in cross pattern
        explosions.add(new Explosion(x, y));

        // Check grid positions in all 4 directions
        int gridX = (int)(x / CELL_SIZE);
        int gridY = (int)(y / CELL_SIZE);

        // Center
        destroyBlock(gridX, gridY);

        // Up
        for (int i = 1; i <= 2; i++) {
            if (gridY - i >= 0 && grid[gridY - i][gridX] != WALL) {
                destroyBlock(gridX, gridY - i);
                explosions.add(new Explosion(x, y - i * CELL_SIZE));
                if (grid[gridY - i][gridX] == BREAKABLE_WALL) break;
            } else {
                break;
            }
        }

        // Down
        for (int i = 1; i <= 2; i++) {
            if (gridY + i < GRID_HEIGHT && grid[gridY + i][gridX] != WALL) {
                destroyBlock(gridX, gridY + i);
                explosions.add(new Explosion(x, y + i * CELL_SIZE));
                if (grid[gridY + i][gridX] == BREAKABLE_WALL) break;
            } else {
                break;
            }
        }

        // Left
        for (int i = 1; i <= 2; i++) {
            if (gridX - i >= 0 && grid[gridY][gridX - i] != WALL) {
                destroyBlock(gridX - i, gridY);
                explosions.add(new Explosion(x - i * CELL_SIZE, y));
                if (grid[gridY][gridX - i] == BREAKABLE_WALL) break;
            } else {
                break;
            }
        }

        // Right
        for (int i = 1; i <= 2; i++) {
            if (gridX + i < GRID_WIDTH && grid[gridY][gridX + i] != WALL) {
                destroyBlock(gridX + i, gridY);
                explosions.add(new Explosion(x + i * CELL_SIZE, y));
                if (grid[gridY][gridX + i] == BREAKABLE_WALL) break;
            } else {
                break;
            }
        }
    }

    private void destroyBlock(int gridX, int gridY) {
        if (gridY >= 0 && gridY < GRID_HEIGHT && gridX >= 0 && gridX < GRID_WIDTH) {
            if (grid[gridY][gridX] == BREAKABLE_WALL) {
                grid[gridY][gridX] = EMPTY;
                score += 10; // Points for destroying a wall
                
                // Chance to spawn power-up (12% chance)
                if (Math.random() < 0.9) {
                    spawnPowerUp(gridX, gridY);
                }
            }
        }
    }
    
    private void spawnPowerUp(int gridX, int gridY) {
        float x = gridX * CELL_SIZE + CELL_SIZE/2;
        float y = gridY * CELL_SIZE + CELL_SIZE/2;

        // Random power-up type with different probabilities
        double rand = Math.random();
        PowerUp.Type powerUpType;
        if (rand < 0.25) {
            powerUpType = PowerUp.Type.BOMB_UP;      // 25%
        } else if (rand < 0.45) {
            powerUpType = PowerUp.Type.FIRE_UP;       // 20%
        } else if (rand < 0.60) {
            powerUpType = PowerUp.Type.SPEED_UP;      // 15%
        } else if (rand < 0.72) {
            powerUpType = PowerUp.Type.EXTRA_LIFE;    // 12%
        } else if (rand < 0.82) {
            powerUpType = PowerUp.Type.GUN;           // 10%
        } else if (rand < 0.90) {
            powerUpType = PowerUp.Type.KEY;           // 8%
        } else {
            powerUpType = PowerUp.Type.INVISIBILITY;  // 10%
        }
        
        powerUps.add(new PowerUp(x, y, powerUpType));
    }

    private void checkCollisions() {
        Rect playerRect = player.getBounds();
        // Check player vs enemies - skip if invincible or invisible
        if (!player.isInvincible() && !player.isInvisible()) {
            for (Enemy enemy : enemies) {
                if (Rect.intersects(playerRect, enemy.getBounds())) {
                    lives--;
                    if (lives <= 0) {
                        gameOver = true;
                    } else {
                        // Reset player position, keep all stats
                        float respawnX = CELL_SIZE * 1 + CELL_SIZE/2;
                        float respawnY = CELL_SIZE * 1 + CELL_SIZE/2;
                        player.resetPosition(respawnX, respawnY);
                        player.setSize(CELL_SIZE);
                    }
                    return;
                }
            }
        }

        // Check bullets vs enemies
        for (int i = bullets.size() - 1; i >= 0; i--) {
            Bullet bullet = bullets.get(i);
            boolean bulletHit = false;

            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy enemy = enemies.get(j);

                if (Rect.intersects(bullet.getBounds(), enemy.getBounds())) {
                    // Handle different enemy types
                    if (enemy instanceof SplitterEnemy) {
                        SplitterEnemy splitter = (SplitterEnemy) enemy;
                        if (!splitter.isSmallVersion()) {
                            float ex = enemy.getX();
                            float ey = enemy.getY();

                            SplitterEnemy small1 = new SplitterEnemy(ex - CELL_SIZE/2, ey, true);
                            small1.setSize(CELL_SIZE);
                            enemies.add(small1);

                            SplitterEnemy small2 = new SplitterEnemy(ex + CELL_SIZE/2, ey, true);
                            small2.setSize(CELL_SIZE);
                            enemies.add(small2);

                            score += 50;
                        } else {
                            score += 75;
                        }
                        enemies.remove(j);
                    } else if (enemy instanceof TankEnemy) {
                        TankEnemy tank = (TankEnemy) enemy;
                        tank.takeDamage();

                        if (tank.isDead()) {
                            score += 300;
                            enemies.remove(j);
                        }
                    } else if (enemy instanceof BossEnemy) {
                        BossEnemy boss = (BossEnemy) enemy;
                        boss.takeDamage();

                        if (boss.isDead()) {
                            score += 1000;
                            enemies.remove(j);
                        }
                    } else {
                        score += 100;
                        enemies.remove(j);
                    }

                    bulletHit = true;
                    break;
                }
            }

            if (bulletHit) {
                bullets.remove(i);
            }
        }

        // Check player vs explosions - skip if invincible or invisible
        if (!player.isInvincible() && !player.isInvisible()) {
            for (Explosion explosion : explosions) {
                if (Rect.intersects(playerRect, explosion.getBounds())) {
                    lives--;
                    if (lives <= 0) {
                        gameOver = true;
                    } else {
                        // Reset player position, keep all stats
                        float respawnX = CELL_SIZE * 1 + CELL_SIZE/2;
                        float respawnY = CELL_SIZE * 1 + CELL_SIZE/2;
                        player.resetPosition(respawnX, respawnY);
                        player.setSize(CELL_SIZE);
                    }
                    return;
                }
            }
        }


        // Check player vs power-ups
        for (int i = powerUps.size() - 1; i >= 0; i--) {
            PowerUp powerUp = powerUps.get(i);
            if (Rect.intersects(playerRect, powerUp.getBounds())) {
                applyPowerUp(powerUp.getType());
                powerUps.remove(i);
            }
        }

        // Check player vs doors
        if (!doors.isEmpty()) {
            Door door = doors.get(0);
            if (!door.isUnlocked() && hasKey) {
                if (Rect.intersects(playerRect, door.getBounds())) {
                    // Unlock door and complete level
                    door.unlock();
                    score += 1000; // Bonus for unlocking door
                    levelComplete = true;
                }
            }
        }

        // Check enemies vs explosions
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy enemy = enemies.get(i);
            for (Explosion explosion : explosions) {
                if (Rect.intersects(enemy.getBounds(), explosion.getBounds())) {
                    // Handle different enemy types
                    if (enemy instanceof SplitterEnemy) {
                        SplitterEnemy splitter = (SplitterEnemy) enemy;
                        if (!splitter.isSmallVersion()) {
                            // Split into 2 small enemies
                            float ex = enemy.getX();
                            float ey = enemy.getY();

                            SplitterEnemy small1 = new SplitterEnemy(ex - CELL_SIZE/2, ey, true);
                            small1.setSize(CELL_SIZE);
                            enemies.add(small1);

                            SplitterEnemy small2 = new SplitterEnemy(ex + CELL_SIZE/2, ey, true);
                            small2.setSize(CELL_SIZE);
                            enemies.add(small2);

                            score += 50;
                        } else {
                            score += 75;
                        }
                        enemies.remove(i);
                    } else if (enemy instanceof TankEnemy) {
                        TankEnemy tank = (TankEnemy) enemy;
                        tank.takeDamage();

                        if (tank.isDead()) {
                            // Tank destroyed
                            score += 300; // High points for tough enemy
                            enemies.remove(i);
                        }
                        // If not dead, tank survives with reduced health
                    } else if (enemy instanceof BossEnemy) {
                        BossEnemy boss = (BossEnemy) enemy;
                        boss.takeDamage();

                        if (boss.isDead()) {
                            score += 1000; // Massive points for defeating boss
                            enemies.remove(i);
                        }
                    } else {
                        // Regular enemy dies
                        score += 100;
                        enemies.remove(i);
                    }

                    enemies.remove(i);
                    break;
                }
            }
        }

        // Check win condition (all enemies defeated)
        if (enemies.isEmpty()) {
            levelComplete = true;
        }
    }
    
    private void applyPowerUp(PowerUp.Type type) {
        switch (type) {
            case BOMB_UP:
                maxBombs++;
                score += 50; // Points for power-up
                break;
            case FIRE_UP:
                bombRange++;
                score += 50;
                break;
            case SPEED_UP:
                player.setSpeed(player.getSpeed() + 1f);
                score += 50;
                break;
            case EXTRA_LIFE:
                lives++;
                score += 75;
                break;
            case KEY:
                hasKey = true;
                score += 200;
                break;
            case GUN:
                player.addGunAmmo(10);
                break;
            case INVISIBILITY:
                player.activateInvisibility(600); // 10 seconds at 60 FPS
                break;
        }
    }

    public void draw(Canvas canvas, Paint paint) {
        // Draw background
        canvas.drawColor(Color.rgb(50, 100, 50));

        // Draw grid
        drawGrid(canvas, paint);

        // Draw doors
        for (Door door : doors) {
            door.draw(canvas, paint, offsetX, offsetY);
        }

        // Draw power-ups
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(canvas, paint, offsetX, offsetY);
        }

        // Draw player with offset
        player.draw(canvas, paint, offsetX, offsetY);

        // Draw enemies with offset
        for (Enemy enemy : enemies) {
            enemy.draw(canvas, paint, offsetX, offsetY);
        }

        // Draw bombs with offset
        for (Bomb bomb : bombs) {
            bomb.draw(canvas, paint, offsetX, offsetY);
        }

        // Draw explosions with offset
        for (Explosion explosion : explosions) {
            explosion.draw(canvas, paint, offsetX, offsetY);
        }

        // Draw bullets with offset
        for (Bullet bullet : bullets) {
            bullet.draw(canvas, paint, offsetX, offsetY);
        }

        // Draw UI
        drawUI(canvas, paint);
        
        // Draw control buttons
        drawControls(canvas, paint);
    }

    private void drawControls(Canvas canvas, Paint paint) {
        // Draw D-pad as a single circular control (left side)
        paint.setAlpha(150);
        
        // Draw circular background
        paint.setColor(Color.rgb(40, 80, 160));
        canvas.drawCircle(dpadBounds.centerX(), dpadBounds.centerY(), dpadBounds.width()/2, paint);
        
        // Draw directional dividers
        paint.setColor(Color.rgb(60, 100, 180));
        paint.setStrokeWidth(3);
        
        // Vertical divider
        canvas.drawLine(dpadBounds.centerX(), dpadBounds.top, 
                       dpadBounds.centerX(), dpadBounds.bottom, paint);
        
        // Horizontal divider
        canvas.drawLine(dpadBounds.left, dpadBounds.centerY(), 
                       dpadBounds.right, dpadBounds.centerY(), paint);
        
        // Highlight pressed regions as pie slices
        if (upPressed || downPressed || leftPressed || rightPressed) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.rgb(100, 150, 255));
            
            float centerX = dpadBounds.centerX();
            float centerY = dpadBounds.centerY();
            float radius = dpadBounds.width() / 2;
            
            // Draw highlighted sector based on direction
            if (upPressed) {
                // Up sector (270 to 360 degrees, but Android uses different coordinate system)
                RectF oval = new RectF(centerX - radius, centerY - radius, 
                                      centerX + radius, centerY + radius);
                canvas.drawArc(oval, 180, 180, true, paint); // Top half
            }
            if (downPressed) {
                RectF oval = new RectF(centerX - radius, centerY - radius,
                                      centerX + radius, centerY + radius);
                canvas.drawArc(oval, 0, 180, true, paint); // Bottom half
            }
            if (leftPressed) {
                RectF oval = new RectF(centerX - radius, centerY - radius, 
                                      centerX + radius, centerY + radius);
                canvas.drawArc(oval, 90, 180, true, paint); // Left half
            }
            if (rightPressed) {
                RectF oval = new RectF(centerX - radius, centerY - radius, 
                                      centerX + radius, centerY + radius);
                canvas.drawArc(oval, 270, 180, true, paint); // Right half
            }
        }
        
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1);
        
        // Draw directional arrows
        paint.setColor(Color.WHITE);
        paint.setTextSize(50);
        paint.setTextAlign(Paint.Align.CENTER);
        
        // Up arrow
        canvas.drawText("↑", dpadBounds.centerX(), dpadBounds.centerY() - dpadBounds.height()/4 + 15, paint);
        
        // Down arrow
        canvas.drawText("↓", dpadBounds.centerX(), dpadBounds.centerY() + dpadBounds.height()/4 + 15, paint);
        
        // Left arrow
        canvas.drawText("←", dpadBounds.centerX() - dpadBounds.width()/4, dpadBounds.centerY() + 15, paint);
        
        // Right arrow
        canvas.drawText("→", dpadBounds.centerX() + dpadBounds.width()/4, dpadBounds.centerY() + 15, paint);
        
        // Draw Bomb button (right side)
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(200, 50, 50));
        canvas.drawCircle(bombButton.centerX(), bombButton.centerY(), bombButton.width()/2, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(30);
        canvas.drawText("BOMB", bombButton.centerX(), bombButton.centerY() + 10, paint);

        // Draw Gun button (above bomb button)
        if (player.hasGun()) {
            paint.setColor(Color.rgb(105, 105, 105)); // Dim Gray
            canvas.drawCircle(gunButton.centerX(), gunButton.centerY(), gunButton.width()/2, paint);
            paint.setColor(Color.WHITE);
            paint.setTextSize(25);
            canvas.drawText("GUN", gunButton.centerX(), gunButton.centerY() + 8, paint);

            // Show ammo count
            paint.setTextSize(20);
            paint.setColor(Color.YELLOW);
            canvas.drawText(String.valueOf(player.getGunAmmo()), gunButton.centerX(), gunButton.centerY() + 30, paint);
        } else {
            // Disabled gun button
            paint.setColor(Color.rgb(80, 80, 80));
            canvas.drawCircle(gunButton.centerX(), gunButton.centerY(), gunButton.width()/2, paint);
            paint.setColor(Color.rgb(150, 150, 150));
            paint.setTextSize(25);
            canvas.drawText("GUN", gunButton.centerX(), gunButton.centerY() + 8, paint);
        }

        paint.setAlpha(255);
        paint.setStyle(Paint.Style.FILL);
    }

    private void drawGrid(Canvas canvas, Paint paint) {
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) {
                int left = offsetX + x * CELL_SIZE;
                int top = offsetY + y * CELL_SIZE;
                int right = left + CELL_SIZE;
                int bottom = top + CELL_SIZE;

                switch (grid[y][x]) {
                    case WALL:
                        paint.setColor(Color.GRAY);
                        canvas.drawRect(left, top, right, bottom, paint);
                        paint.setColor(Color.DKGRAY);
                        canvas.drawRect(left+2, top+2, right-2, bottom-2, paint);
                        break;
                    case BREAKABLE_WALL:
                        paint.setColor(Color.rgb(139, 69, 19)); // Brown
                        canvas.drawRect(left, top, right, bottom, paint);
                        paint.setColor(Color.rgb(160, 82, 45)); // Lighter brown
                        canvas.drawRect(left+2, top+2, right-2, bottom-2, paint);
                        break;
                    case EMPTY:
                        // Draw empty cell border
                        paint.setColor(Color.rgb(34, 139, 34)); // Forest green
                        canvas.drawRect(left, top, right, bottom, paint);
                        break;
                }
            }
        }
    }

    private void drawUI(Canvas canvas, Paint paint) {
        paint.setTextSize(30);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.LEFT);

        if (gameOver) {
            paint.setTextSize(50);
            paint.setColor(Color.RED);
            String text = "GAME OVER";
            float textWidth = paint.measureText(text);
            canvas.drawText(text, (screenWidth - textWidth) / 2, screenHeight / 2, paint);

            paint.setTextSize(30);
            paint.setColor(Color.WHITE);
            String scoreText = "Score: " + score;
            float scoreWidth = paint.measureText(scoreText);
            canvas.drawText(scoreText, (screenWidth - scoreWidth) / 2, screenHeight / 2 + 50, paint);
            
            String highScoreText = "High Score: " + highScore;
            float highScoreWidth = paint.measureText(highScoreText);
            canvas.drawText(highScoreText, (screenWidth - highScoreWidth) / 2, screenHeight / 2 + 85, paint);

            paint.setTextSize(25);
            String restartText = "Tap to Restart";
            float restartWidth = paint.measureText(restartText);
            canvas.drawText(restartText, (screenWidth - restartWidth) / 2, screenHeight / 2 + 125, paint);
        } else if (levelComplete) {
            paint.setTextSize(50);
            paint.setColor(Color.GREEN);
            String text = "LEVEL " + levelManager.getCurrentLevel() + " COMPLETE!";
            float textWidth = paint.measureText(text);
            canvas.drawText(text, (screenWidth - textWidth) / 2, screenHeight / 2, paint);

            paint.setTextSize(30);
            paint.setColor(Color.WHITE);
            String scoreText = "Score: " + score;
            float scoreWidth = paint.measureText(scoreText);
            canvas.drawText(scoreText, (screenWidth - scoreWidth) / 2, screenHeight / 2 + 50, paint);
            
            paint.setTextSize(25);
            String nextText = "Tap for Level " + (levelManager.getCurrentLevel() + 1);
            float nextWidth = paint.measureText(nextText);
            canvas.drawText(nextText, (screenWidth - nextWidth) / 2, screenHeight / 2 + 90, paint);
        } else {
            // Draw stats at top of screen
            paint.setTextSize(22);
            paint.setColor(Color.WHITE);
            
            // Left side - Level and Lives
            canvas.drawText("Level: " + levelManager.getCurrentLevel(), 10, 30, paint);
            canvas.drawText("Lives: " + lives, 10, 55, paint);
            
            // Center - Score and High Score
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Score: " + score, screenWidth / 2, 30, paint);
            canvas.drawText("High: " + highScore, screenWidth / 2, 55, paint);
            
            // Right side - Power-ups and Key
            paint.setTextAlign(Paint.Align.RIGHT);
            canvas.drawText("Bombs: " + maxBombs, screenWidth - 10, 30, paint);
            canvas.drawText("Range: " + bombRange, screenWidth - 10, 55, paint);

            // Show gun ammo if player has gun
            if (player.hasGun()) {
                paint.setColor(Color.YELLOW);
                canvas.drawText("Gun: " + player.getGunAmmo(), screenWidth - 10, 80, paint);
            }

            // Show invincibility timer
            if (player.isInvincible() && !player.isInvisible()) {
                paint.setColor(Color.GREEN);
                int secondsLeft = (player.getInvincibleTimer() + 59) / 60; // Round up
                canvas.drawText("Shield: " + secondsLeft + "s", screenWidth - 10, player.hasGun() ? 105 : 80, paint);
            }

            // Show invisibility timer (takes priority over shield)
            if (player.isInvisible()) {
                paint.setColor(Color.CYAN);
                int secondsLeft = (player.getInvisibleTimer() + 59) / 60; // Round up
                canvas.drawText("Stealth: " + secondsLeft + "s", screenWidth - 10, player.hasGun() ? 105 : 80, paint);
            }
            
            // Show key status if level > 1
            if (levelManager.getCurrentLevel() > 1) {
                paint.setColor(hasKey ? Color.rgb(255, 215, 0) : Color.GRAY); // Gold or Gray
                canvas.drawText(hasKey ? "Key: ✓" : "Key: ✗", screenWidth - 10, 80, paint);
            }
            
            paint.setTextAlign(Paint.Align.LEFT);
        }
    }

    public boolean handleTouch(MotionEvent event) {
        if (gameOver || levelComplete) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                initGame(); // Restart game
            }
            return true;
        }

        float x = event.getX();
        float y = event.getY();
        int action = event.getAction();
        int actionMasked = event.getActionMasked();
        
        // Check if touching any control button
        boolean handled = false;
        
        // Check if touch is within D-pad circle
        if (isInCircle(x, y, dpadBounds.centerX(), dpadBounds.centerY(), dpadBounds.width()/2)) {
            handled = true;
            
            if (actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_POINTER_UP) {
                // Finger lifted - check if any other fingers still in D-pad
                int pointerCount = event.getPointerCount();
                boolean stillInDpad = false;
                
                for (int i = 0; i < pointerCount; i++) {
                    if (i != ((android.view.MotionEvent)event).getActionIndex()) {
                        float px = event.getX(i);
                        float py = event.getY(i);
                        
                        if (isInCircle(px, py, dpadBounds.centerX(), dpadBounds.centerY(), dpadBounds.width()/2)) {
                            stillInDpad = true;
                            
                            // Recalculate direction for remaining touch
                            float dx = px - dpadBounds.centerX();
                            float dy = py - dpadBounds.centerY();
                            
                            upPressed = false;
                            downPressed = false;
                            leftPressed = false;
                            rightPressed = false;
                            
                            if (Math.abs(dx) > Math.abs(dy)) {
                                if (dx > 0) rightPressed = true;
                                else leftPressed = true;
                            } else {
                                if (dy > 0) downPressed = true;
                                else upPressed = true;
                            }
                            break;
                        }
                    }
                }
                
                if (!stillInDpad) {
                    // All fingers lifted - stop all movement
                    upPressed = false;
                    downPressed = false;
                    leftPressed = false;
                    rightPressed = false;
                    player.setMovingUp(false);
                    player.setMovingDown(false);
                    player.setMovingLeft(false);
                    player.setMovingRight(false);
                } else {
                    // Update with remaining finger direction
                    player.setMovingUp(upPressed);
                    player.setMovingDown(downPressed);
                    player.setMovingLeft(leftPressed);
                    player.setMovingRight(rightPressed);
                }
            } else {
                // Finger down or moving - determine direction
                float dx = x - dpadBounds.centerX();
                float dy = y - dpadBounds.centerY();
                
                // Reset all directions first
                upPressed = false;
                downPressed = false;
                leftPressed = false;
                rightPressed = false;
                
                // Determine primary direction based on angle
                if (Math.abs(dx) > Math.abs(dy)) {
                    // Horizontal movement
                    if (dx > 0) {
                        rightPressed = true;
                    } else {
                        leftPressed = true;
                    }
                } else {
                    // Vertical movement
                    if (dy > 0) {
                        downPressed = true;
                    } else {
                        upPressed = true;
                    }
                }
                
                player.setMovingUp(upPressed);
                player.setMovingDown(downPressed);
                player.setMovingLeft(leftPressed);
                player.setMovingRight(rightPressed);
            }
        }
        
        // Check bomb button
        if (bombButton.contains((int)x, (int)y)) {
            if (action == MotionEvent.ACTION_DOWN) {
                placeBomb();
            }
            handled = true;
        }

        // Check gun button
        if (gunButton.contains((int)x, (int)y)) {
            if (action == MotionEvent.ACTION_DOWN && player.hasGun()) {
                shoot();
            }
            handled = true;
        }

        return handled;
    }
    
    private boolean isInCircle(float x, float y, float centerX, float centerY, float radius) {
        float dx = x - centerX;
        float dy = y - centerY;
        return (dx * dx + dy * dy) <= (radius * radius);
    }
    
    private void placeBomb() {
        // Limit number of bombs player can place
        if (bombs.size() < maxBombs) {
            bombs.add(new Bomb(player.getX(), player.getY()));
        }
    }

    private void shoot() {
        Bullet bullet = player.shoot();
        if (bullet != null) {
            bullets.add(bullet);
        }
    }

    public void setScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;

        // Calculate cell size to fit screen
        int cellWidth = width / GRID_WIDTH;
        int cellHeight = height / GRID_HEIGHT;
        this.CELL_SIZE = Math.min(cellWidth, cellHeight);

        // Calculate offsets to center the grid
        int gridWidth = GRID_WIDTH * CELL_SIZE;
        int gridHeight = GRID_HEIGHT * CELL_SIZE;
        this.offsetX = (width - gridWidth) / 2;
        this.offsetY = (height - gridHeight) / 2;

        // Initialize control buttons
        initControlButtons();

        // Reinitialize game with new cell size if needed
        if (player != null) {
            initGame();
        }
    }

    private void initControlButtons() {
        int buttonSize = Math.min(screenWidth, screenHeight) / 8;
        
        // Calculate the left blank area (before the grid starts)
        int leftAreaWidth = offsetX;
        int leftAreaCenterX = leftAreaWidth / 2;
        int leftAreaCenterY = screenHeight / 2;

        // D-pad as a single circular control centered in left blank area
        int dpadRadius = (int)(buttonSize * 1.8f);
        dpadBounds = new Rect(
            leftAreaCenterX - dpadRadius,
            leftAreaCenterY - dpadRadius,
            leftAreaCenterX + dpadRadius,
            leftAreaCenterY + dpadRadius
        );
        
        // Define directional regions within the circle (for touch detection)
        // Up region (top quarter)
        upButton = new Rect(
            leftAreaCenterX - dpadRadius/2,
            leftAreaCenterY - dpadRadius,
            leftAreaCenterX + dpadRadius/2,
            leftAreaCenterY
        );
        
        // Down region (bottom quarter)
        downButton = new Rect(
            leftAreaCenterX - dpadRadius/2,
            leftAreaCenterY,
            leftAreaCenterX + dpadRadius/2,
            leftAreaCenterY + dpadRadius
        );
        
        // Left region (left quarter)
        leftButton = new Rect(
            leftAreaCenterX - dpadRadius,
            leftAreaCenterY - dpadRadius/2,
            leftAreaCenterX,
            leftAreaCenterY + dpadRadius/2
        );
        
        // Right region (right quarter)
        rightButton = new Rect(
            leftAreaCenterX,
            leftAreaCenterY - dpadRadius/2,
            leftAreaCenterX + dpadRadius,
            leftAreaCenterY + dpadRadius/2
        );

        // Right side - Bomb button (centered in right blank area)
        int rightAreaStart = offsetX + GRID_WIDTH * CELL_SIZE;
        int rightAreaWidth = screenWidth - rightAreaStart;
        int bombCenterX = rightAreaStart + rightAreaWidth / 2;
        int bombCenterY = screenHeight / 2;
        
        bombButton = new Rect(bombCenterX - buttonSize, bombCenterY - buttonSize,
                              bombCenterX + buttonSize, bombCenterY + buttonSize);

        // Gun button (above bomb button)
        int gunCenterY = bombCenterY - (int)(buttonSize * 2.5f);
        gunButton = new Rect(bombCenterX - buttonSize, gunCenterY - buttonSize,
                bombCenterX + buttonSize, gunCenterY + buttonSize);
    }
}
