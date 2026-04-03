package com.game.supermario;

import android.content.Context;
import android.graphics.RectF;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LevelCreator {

    private static Context context;

    public static void initialize(Context ctx) {
        context = ctx;
    }

    // Load custom level from file
    public static Level loadCustomLevel(int levelIndex) {
        String fileName = "custom_level_" + (levelIndex + 1) + ".json";
        return loadCustomLevel(fileName);
    }
    
    public static Level loadCustomLevel(String fileName) {
        if (context == null) {
            throw new IllegalStateException("LevelCreator not initialized");
        }

        try {
            FileInputStream fis = context.openFileInput(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            fis.close();

            LevelData levelData = LevelData.fromJson(sb.toString());
            return convertToLevel(levelData);

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Level convertToLevel(LevelData data) {
        List<RectF> platforms = new ArrayList<>();
        List<Enemy> enemies = new ArrayList<>();
        List<Coin> coins = new ArrayList<>();
        List<Mushroom> mushrooms = new ArrayList<>();
        List<Brick> bricks = new ArrayList<>();

        for (LevelData.PlatformData p : data.getPlatforms()) {
            platforms.add(p.toRectF());
        }

        for (LevelData.EnemyData e : data.getEnemies()) {
            enemies.add(e.toEnemy());
        }

        for (LevelData.CoinData c : data.getCoins()) {
            coins.add(c.toCoin());
        }

        for (LevelData.MushroomData m : data.getMushrooms()) {
            mushrooms.add(m.toMushroom());
        }

        for (LevelData.BrickData b : data.getBricks()) {
            bricks.add(b.toBrick());
        }

        RectF goal = data.getGoal() != null ? data.getGoal().toRectF() : new RectF(0, 0, 50, 50);

        return new Level(data.getName(), platforms, enemies, goal, coins, mushrooms, bricks, data.getWidth());
    }
    
    // Level 1: Easy - Introduction level with basic platforms and few enemies
    public static Level createLevel1() {
        List<RectF> platforms = new ArrayList<>();
        List<Enemy> enemies = new ArrayList<>();
        List<Coin> coins = new ArrayList<>();
        List<Mushroom> mushrooms = new ArrayList<>();
        List<Brick> bricks = new ArrayList<>();

        // Ground platform - extended
        platforms.add(new RectF(0, 700, 600, 750));
        
        // Gap then more ground
        platforms.add(new RectF(700, 700, 1200, 750));
        
        // Floating platforms - lower section
        platforms.add(new RectF(200, 600, 400, 620));
        platforms.add(new RectF(500, 550, 650, 570));
        
        // Higher platforms
        platforms.add(new RectF(750, 500, 950, 520));
        platforms.add(new RectF(1000, 450, 1200, 470));
        
        // More ground sections
        platforms.add(new RectF(1300, 700, 1700, 750));
        platforms.add(new RectF(1800, 700, 2200, 750));
        
        // High floating platforms
        platforms.add(new RectF(1400, 550, 1600, 570));
        platforms.add(new RectF(1650, 450, 1850, 470));
        platforms.add(new RectF(1900, 350, 2100, 370));
        
        // Final platform with goal
        platforms.add(new RectF(2300, 700, 2600, 750));
        
        // Enemies - patrol different areas
        enemies.add(new Enemy(250, 650, 150, 0));  // Goomba on first ground (moved away from spawn)
        enemies.add(new Enemy(800, 650, 250, 0));  // Goomba on second ground
        enemies.add(new Enemy(1400, 650, 200, 1)); // Koopa on third ground
        enemies.add(new Enemy(1900, 650, 200, 0)); // Goomba on fourth ground
        
        // Coins - placed along the path
        coins.add(new Coin(250, 560));   // First floating platform
        coins.add(new Coin(320, 560));
        coins.add(new Coin(550, 510));   // Second floating
        coins.add(new Coin(800, 460));   // High platform
        coins.add(new Coin(900, 460));
        coins.add(new Coin(1050, 410));  // Higher platform
        coins.add(new Coin(1450, 510));  // Over third ground
        coins.add(new Coin(1500, 510));
        coins.add(new Coin(1700, 410));  // High platform
        coins.add(new Coin(1950, 310));  // Very high platform
        coins.add(new Coin(2000, 310));
        coins.add(new Coin(2050, 310));
        
        // Mushrooms - hidden power-ups
        mushrooms.add(new Mushroom(400, 660));  // On first floating platform
        mushrooms.add(new Mushroom(1500, 660)); // On third ground section
        
        // Goal at the end
        RectF goal = new RectF(2450, 640, 2500, 700);
        
        return new Level("Green Hills", platforms, enemies, goal, coins, mushrooms, bricks, 2600);
    }
    
    // Level 2: Medium - Mountain climbing with vertical gameplay
    public static Level createLevel2() {
        List<RectF> platforms = new ArrayList<>();
        List<Enemy> enemies = new ArrayList<>();
        List<Coin> coins = new ArrayList<>();
        List<Mushroom> mushrooms = new ArrayList<>();
        List<Brick> bricks = new ArrayList<>();
        
        // Starting area
        platforms.add(new RectF(0, 700, 500, 750));
        
        // Staircase up
        platforms.add(new RectF(550, 650, 700, 670));
        platforms.add(new RectF(750, 600, 900, 620));
        platforms.add(new RectF(950, 550, 1100, 570));
        
        // First high platform
        platforms.add(new RectF(1150, 500, 1450, 520));
        
        // Ground section with enemies
        platforms.add(new RectF(1500, 700, 2000, 750));
        
        // Upper platforms
        platforms.add(new RectF(1600, 550, 1800, 570));
        platforms.add(new RectF(1850, 450, 2050, 470));
        
        // High mountain section
        platforms.add(new RectF(2100, 350, 2400, 370));
        platforms.add(new RectF(2450, 300, 2650, 320));
        platforms.add(new RectF(2700, 250, 2900, 270));
        
        // Final peak
        platforms.add(new RectF(2950, 200, 3200, 250));
        
        // Enemies
        enemies.add(new Enemy(200, 650, 150, 0));   // Start area
        enemies.add(new Enemy(1200, 450, 150, 1));  // High platform
        enemies.add(new Enemy(1600, 650, 200, 0));  // Ground section
        enemies.add(new Enemy(1800, 650, 150, 1));  // Ground section
        enemies.add(new Enemy(2150, 300, 150, 0));  // Mountain top
        enemies.add(new Enemy(2500, 250, 100, 1));  // Upper peak
        
        // Coins
        coins.add(new Coin(600, 610));   // Staircase
        coins.add(new Coin(800, 560));
        coins.add(new Coin(1000, 510));
        coins.add(new Coin(1200, 460));  // High platform
        coins.add(new Coin(1300, 460));
        coins.add(new Coin(1400, 460));
        coins.add(new Coin(1650, 510));  // Upper platforms
        coins.add(new Coin(1700, 510));
        coins.add(new Coin(1900, 410));
        coins.add(new Coin(2200, 310));  // Mountain section
        coins.add(new Coin(2300, 310));
        coins.add(new Coin(2500, 260));
        coins.add(new Coin(2600, 260));
        coins.add(new Coin(2800, 210));
        
        // Mushrooms
        mushrooms.add(new Mushroom(1250, 460));  // On high platform
        mushrooms.add(new Mushroom(2250, 310));  // On mountain section
        
        // Goal at the peak
        RectF goal = new RectF(3050, 140, 3100, 200);
        
        return new Level("Mountain Peak", platforms, enemies, goal, coins, mushrooms, bricks, 3200);
    }
    
    // Level 3: Hard - Challenging level with many enemies and precise jumps
    public static Level createLevel3() {
        List<RectF> platforms = new ArrayList<>();
        List<Enemy> enemies = new ArrayList<>();
        List<Coin> coins = new ArrayList<>();
        List<Mushroom> mushrooms = new ArrayList<>();
        List<Brick> bricks = new ArrayList<>();
        
        // Small starting platform
        platforms.add(new RectF(0, 700, 200, 750));
        
        // Series of small floating platforms (hard jumps)
        platforms.add(new RectF(250, 680, 350, 700));
        platforms.add(new RectF(400, 640, 500, 660));
        platforms.add(new RectF(550, 600, 650, 620));
        platforms.add(new RectF(700, 550, 800, 570));
        
        // Enemy platform
        platforms.add(new RectF(900, 500, 1100, 520));
        
        // Narrow platforms
        platforms.add(new RectF(1200, 450, 1300, 470));
        platforms.add(new RectF(1400, 400, 1500, 420));
        platforms.add(new RectF(1600, 350, 1700, 370));
        
        // Long dangerous platform with multiple enemies
        platforms.add(new RectF(1800, 350, 2200, 370));
        
        // Final challenge - vertical climb
        platforms.add(new RectF(2300, 300, 2400, 320));
        platforms.add(new RectF(2200, 250, 2300, 270));
        platforms.add(new RectF(2350, 200, 2450, 220));
        platforms.add(new RectF(2500, 150, 2600, 170));
        
        // Goal platform
        platforms.add(new RectF(2700, 150, 2900, 200));
        
        // Many enemies
        enemies.add(new Enemy(950, 450, 100, 0));   // Goomba on middle platform
        enemies.add(new Enemy(1850, 330, 150, 1));  // Koopa on long platform
        enemies.add(new Enemy(2000, 330, 100, 0));  // Goomba on long platform
        enemies.add(new Enemy(2100, 330, 80, 1));   // Koopa on long platform
        
        // Coins
        coins.add(new Coin(270, 640));  // On first floating platform
        coins.add(new Coin(420, 600));  // On second
        coins.add(new Coin(570, 560));  // On third
        coins.add(new Coin(720, 510));  // On fourth
        coins.add(new Coin(950, 460));  // On enemy platform
        coins.add(new Coin(1220, 410)); // On narrow platforms
        coins.add(new Coin(1420, 360));
        coins.add(new Coin(1620, 310));
        coins.add(new Coin(1900, 310)); // On long dangerous platform
        coins.add(new Coin(2050, 310));
        coins.add(new Coin(2320, 260)); // On final climb
        coins.add(new Coin(2370, 160));
        
        // Mushrooms
        mushrooms.add(new Mushroom(1150, 480));  // On enemy platform
        mushrooms.add(new Mushroom(2250, 310));  // On long dangerous platform
        
        // Goal at the very end
        RectF goal = new RectF(2800, 90, 2850, 150);
        
        return new Level("Castle Challenge", platforms, enemies, goal, coins, mushrooms, bricks, 2900);
    }
}
