package com.game.supermario;

import android.graphics.RectF;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class LevelData {

    private String name;
    private List<PlatformData> platforms;
    private List<EnemyData> enemies;
    private List<CoinData> coins;
    private List<MushroomData> mushrooms;
    private List<BrickData> bricks;
    private GoalData goal;
    private float width;

    public LevelData() {
        platforms = new ArrayList<>();
        enemies = new ArrayList<>();
        coins = new ArrayList<>();
        mushrooms = new ArrayList<>();
        bricks = new ArrayList<>();
        this.name = "Custom Level";
        this.width = 10000;
    }

    public String toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("width", width);

        // Platforms
        JSONArray platformsArray = new JSONArray();
        for (PlatformData platform : platforms) {
            JSONObject pJson = new JSONObject();
            pJson.put("left", platform.left);
            pJson.put("top", platform.top);
            pJson.put("right", platform.right);
            pJson.put("bottom", platform.bottom);
            platformsArray.put(pJson);
        }
        json.put("platforms", platformsArray);

        // Enemies
        JSONArray enemiesArray = new JSONArray();
        for (EnemyData enemy : enemies) {
            JSONObject eJson = new JSONObject();
            eJson.put("x", enemy.x);
            eJson.put("y", enemy.y);
            eJson.put("type", enemy.type);
            eJson.put("patrolWidth", enemy.patrolWidth);
            enemiesArray.put(eJson);
        }
        json.put("enemies", enemiesArray);

        // Coins
        JSONArray coinsArray = new JSONArray();
        for (CoinData coin : coins) {
            JSONObject cJson = new JSONObject();
            cJson.put("x", coin.x);
            cJson.put("y", coin.y);
            coinsArray.put(cJson);
        }
        json.put("coins", coinsArray);

        // Mushrooms
        JSONArray mushroomsArray = new JSONArray();
        for (MushroomData mushroom : mushrooms) {
            JSONObject mJson = new JSONObject();
            mJson.put("x", mushroom.x);
            mJson.put("y", mushroom.y);
            mushroomsArray.put(mJson);
        }
        json.put("mushrooms", mushroomsArray);

        // Bricks
        JSONArray bricksArray = new JSONArray();
        for (BrickData brick : bricks) {
            JSONObject bJson = new JSONObject();
            bJson.put("x", brick.x);
            bJson.put("y", brick.y);
            bJson.put("contentType", brick.contentType);
            bJson.put("isQuestionBlock", brick.isQuestionBlock);
            bricksArray.put(bJson);
        }
        json.put("bricks", bricksArray);

        // Goal
        if (goal != null) {
            JSONObject goalJson = new JSONObject();
            goalJson.put("left", goal.left);
            goalJson.put("top", goal.top);
            goalJson.put("right", goal.right);
            goalJson.put("bottom", goal.bottom);
            json.put("goal", goalJson);
        }

        return json.toString();
    }

    public static LevelData fromJson(String jsonString) throws JSONException {
        JSONObject json = new JSONObject(jsonString);
        LevelData level = new LevelData();

        level.name = json.optString("name", "Custom Level");
        level.width = (float) json.optDouble("width", 10000);

        // Platforms
        JSONArray platformsArray = json.getJSONArray("platforms");
        for (int i = 0; i < platformsArray.length(); i++) {
            JSONObject pJson = platformsArray.getJSONObject(i);
            PlatformData platform = new PlatformData(
                    (float) pJson.getDouble("left"),
                    (float) pJson.getDouble("top"),
                    (float) pJson.getDouble("right"),
                    (float) pJson.getDouble("bottom")
            );
            level.platforms.add(platform);
        }

        // Enemies
        JSONArray enemiesArray = json.getJSONArray("enemies");
        for (int i = 0; i < enemiesArray.length(); i++) {
            JSONObject eJson = enemiesArray.getJSONObject(i);
            EnemyData enemy = new EnemyData(
                    (float) eJson.getDouble("x"),
                    (float) eJson.getDouble("y"),
                    eJson.optInt("type", 0),
                    (float) eJson.optDouble("patrolWidth", 100)
            );
            level.enemies.add(enemy);
        }

        // Coins
        JSONArray coinsArray = json.getJSONArray("coins");
        for (int i = 0; i < coinsArray.length(); i++) {
            JSONObject cJson = coinsArray.getJSONObject(i);
            CoinData coin = new CoinData(
                    (float) cJson.getDouble("x"),
                    (float) cJson.getDouble("y")
            );
            level.coins.add(coin);
        }

        // Mushrooms
        JSONArray mushroomsArray = json.getJSONArray("mushrooms");
        for (int i = 0; i < mushroomsArray.length(); i++) {
            JSONObject mJson = mushroomsArray.getJSONObject(i);
            MushroomData mushroom = new MushroomData(
                    (float) mJson.getDouble("x"),
                    (float) mJson.getDouble("y")
            );
            level.mushrooms.add(mushroom);
        }

        // Bricks
        JSONArray bricksArray = json.getJSONArray("bricks");
        for (int i = 0; i < bricksArray.length(); i++) {
            JSONObject bJson = bricksArray.getJSONObject(i);
            BrickData brick = new BrickData(
                    (float) bJson.getDouble("x"),
                    (float) bJson.getDouble("y"),
                    bJson.optInt("contentType", 0),
                    bJson.optBoolean("isQuestionBlock", false)
            );
            level.bricks.add(brick);
        }

        // Goal
        if (json.has("goal")) {
            JSONObject goalJson = json.getJSONObject("goal");
            level.goal = new GoalData(
                    (float) goalJson.getDouble("left"),
                    (float) goalJson.getDouble("top"),
                    (float) goalJson.getDouble("right"),
                    (float) goalJson.getDouble("bottom")
            );
        }

        return level;
    }

    // Getters and setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public float getWidth() { return width; }
    public void setWidth(float width) { this.width = width; }
    public List<PlatformData> getPlatforms() { return platforms; }
    public List<EnemyData> getEnemies() { return enemies; }
    public List<CoinData> getCoins() { return coins; }
    public List<MushroomData> getMushrooms() { return mushrooms; }
    public List<BrickData> getBricks() { return bricks; }
    public GoalData getGoal() { return goal; }
    public void setGoal(GoalData goal) { this.goal = goal; }

    // Inner classes for data objects
    public static class PlatformData {
        public float left, top, right, bottom;

        public PlatformData(float l, float t, float r, float b) {
            this.left = l;
            this.top = t;
            this.right = r;
            this.bottom = b;
        }

        public RectF toRectF() {
            return new RectF(left, top, right, bottom);
        }
    }

    public static class EnemyData {
        public float x, y;
        public int type;
        public float patrolWidth;

        public EnemyData(float x, float y, int type, float patrolWidth) {
            this.x = x;
            this.y = y;
            this.type = type;
            this.patrolWidth = patrolWidth;
        }

        public Enemy toEnemy() {
            return new Enemy(x, y, patrolWidth, type);
        }
    }

    public static class CoinData {
        public float x, y;

        public CoinData(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public Coin toCoin() {
            Coin coin = new Coin(x, y);
            return coin;
        }
    }

    public static class MushroomData {
        public float x, y;

        public MushroomData(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public Mushroom toMushroom() {
            return new Mushroom(x, y);
        }
    }

    public static class BrickData {
        public float x, y;
        public int contentType; // 0: Coin, 1: Mushroom, 2: Nothing
        public boolean isQuestionBlock;

        public BrickData(float x, float y, int contentType, boolean isQuestionBlock) {
            this.x = x;
            this.y = y;
            this.contentType = contentType;
            this.isQuestionBlock = isQuestionBlock;
        }

        public Brick toBrick() {
            return new Brick(x, y, contentType, isQuestionBlock);
        }
    }

    public static class GoalData {
        public float left, top, right, bottom;

        public GoalData(float l, float t, float r, float b) {
            this.left = l;
            this.top = t;
            this.right = r;
            this.bottom = b;
        }

        public RectF toRectF() {
            return new RectF(left, top, right, bottom);
        }
    }
}