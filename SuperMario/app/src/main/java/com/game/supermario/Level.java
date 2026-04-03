package com.game.supermario;

import android.graphics.RectF;
import java.util.List;

public class Level {
    
    private List<RectF> platforms;
    private List<Enemy> enemies;
    private RectF goal;
    private String name;
    private List<Coin> coins;
    private List<Mushroom> mushrooms;
    private List<Brick> bricks;
    private float width;
    
    public Level(String name, List<RectF> platforms, List<Enemy> enemies, RectF goal, List<Coin> coins, List<Mushroom> mushrooms, List<Brick> bricks, float width) {
        this.name = name;
        this.platforms = platforms;
        this.enemies = enemies;
        this.goal = goal;
        this.coins = coins;
        this.mushrooms = mushrooms;
        this.bricks = bricks;
        this.width = width;
    }
    
    public List<RectF> getPlatforms() {
        return platforms;
    }
    
    public List<Enemy> getEnemies() {
        return enemies;
    }
    
    public RectF getGoal() {
        return goal;
    }
    
    public String getName() {
        return name;
    }
    
    public List<Coin> getCoins() {
        return coins;
    }
    
    public List<Mushroom> getMushrooms() {
        return mushrooms;
    }

    public List<Brick> getBricks() {
        return bricks;
    }
    
    public float getWidth() {
        return width;
    }
}
