package com.game.supermario;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class LevelEditorView extends View {
    
    private Paint paint;
    private Paint gridPaint;
    private Paint textPaint;
    
    private List<RectF> platforms;
    private List<LevelData.EnemyData> enemies;
    private List<LevelData.CoinData> coins;
    private List<LevelData.MushroomData> mushrooms;
    private List<LevelData.BrickData> bricks;
    private LevelData.GoalData goal;
    
    // Undo/Redo stacks
    private Stack<LevelState> undoStack;
    private Stack<LevelState> redoStack;
    private static final int MAX_HISTORY_SIZE = 50;

    // Level boundaries
    private float levelWidth = 10000f;
    private float levelHeight = 800f;
    
    private int currentMode = 0; // 0=Platform, 1=Enemy, 2=Coin, 3=Mushroom, 4=Goal
    private int selectedEnemyType = 0; // 0: Goomba, 1: Koopa
    private boolean isPlacing = false;
    private boolean isDeleteMode = false;
    private RectF currentPlacement;
    
    private float scrollOffset = 0;
    private float lastTouchX;
    
    // Zoom and Pan support
    private float scaleFactor = 1.0f;
    private float panX = 0f;
    private float panY = 0f;
    private float oldDist = 1f;
    private float lastPanX = 0f;
    private float lastPanY = 0f;
    
    private static final float MIN_SCALE = 0.3f;
    private static final float MAX_SCALE = 2.0f;
    private boolean isZoomedOut = false;
    
    // Gesture state
    private static final int STATE_IDLE = 0;
    private static final int STATE_PANNING = 1;
    private static final int STATE_ZOOMING = 2;
    private int gestureState = STATE_IDLE;

    // Flag to track if we just released from a multi-touch gesture
    private boolean justReleasedFromGesture = false;
    
    private OnObjectPlacedListener listener;

    public interface OnObjectPlacedListener {
        void onPlatformPlaced(RectF platform);
        void onEnemyPlaced(LevelData.EnemyData enemy);
        void onCoinPlaced(LevelData.CoinData coin);
        void onMushroomPlaced(LevelData.MushroomData mushroom);
        void onGoalPlaced(LevelData.GoalData goal);
    }

    public LevelEditorView(Context context) {
        super(context);
        init();
    }

    public LevelEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        platforms = new ArrayList<>();
        enemies = new ArrayList<>();
        coins = new ArrayList<>();
        mushrooms = new ArrayList<>();
        bricks = new ArrayList<>();
        
        undoStack = new Stack<>();
        redoStack = new Stack<>();
        
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        gridPaint = new Paint();
        gridPaint.setColor(Color.GRAY);
        gridPaint.setStrokeWidth(1);
        
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(40);
    }

    public void setOnObjectPlacedListener(OnObjectPlacedListener listener) {
        this.listener = listener;
    }

    public void setCurrentMode(int mode) {
        this.currentMode = mode;
        invalidate();
    }

    public void setSelectedEnemyType(int type) {
        this.selectedEnemyType = type;
    }

    public int getSelectedEnemyType() {
        return selectedEnemyType;
    }

    public void setDeleteMode(boolean enabled) {
        this.isDeleteMode = enabled;
        invalidate();
    }
    
    public boolean isDeleteMode() {
        return isDeleteMode;
    }
    
    public void loadData(LevelData levelData) {
        platforms.clear();
        enemies.clear();
        coins.clear();
        mushrooms.clear();
        bricks.clear();

        for (LevelData.PlatformData p : levelData.getPlatforms()) {
            platforms.add(p.toRectF());
        }
        
        for (LevelData.EnemyData e : levelData.getEnemies()) {
            enemies.add(e);
        }
        
        for (LevelData.CoinData c : levelData.getCoins()) {
            coins.add(c);
        }
        
        for (LevelData.MushroomData m : levelData.getMushrooms()) {
            mushrooms.add(m);
        }

        for (LevelData.BrickData b : levelData.getBricks()) {
            bricks.add(b);
        }
        
        goal = levelData.getGoal();

        // Update level dimensions from loaded data
        if (levelData.getWidth() > 0) {
            this.levelWidth = levelData.getWidth();
        }
        
        // Clear history when loading new data
        clearHistory();
        
        invalidate();
    }

    public void setLevelDimensions(float width, float height) {
        this.levelWidth = width;
        this.levelHeight = height;
        invalidate();
    }
    
    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
        saveState(); // Save initial state
    }
    
    private void saveState() {
        LevelState state = new LevelState();
        
        // Deep copy platforms
        for (RectF platform : platforms) {
            state.platforms.add(new RectF(platform));
        }
        
        // Deep copy enemies
        for (LevelData.EnemyData enemy : enemies) {
            state.enemies.add(new LevelData.EnemyData(enemy.x, enemy.y, enemy.type, enemy.movementType, enemy.patrolWidth));
        }
        
        // Deep copy coins
        for (LevelData.CoinData coin : coins) {
            state.coins.add(new LevelData.CoinData(coin.x, coin.y));
        }
        
        // Deep copy mushrooms
        for (LevelData.MushroomData mushroom : mushrooms) {
            state.mushrooms.add(new LevelData.MushroomData(mushroom.x, mushroom.y));
        }

        // Deep Copy bricks
        for (LevelData.BrickData brick : bricks) {
            state.bricks.add(new LevelData.BrickData(brick.x, brick.y, brick.contentType, brick.isQuestionBlock));
        }
        
        // Copy goal
        if (goal != null) {
            state.goal = new LevelData.GoalData(goal.left, goal.top, goal.right, goal.bottom);
        }
        
        undoStack.push(state);
        
        // Limit stack size
        if (undoStack.size() > MAX_HISTORY_SIZE) {
            undoStack.removeElementAt(0);
        }
        
        // Clear redo stack on new action
        redoStack.clear();
    }
    
    public void undo() {
        if (undoStack.size() > 1) { // Keep at least one state
            // Move current state to redo stack
            redoStack.push(undoStack.pop());
            
            // Restore previous state
            restoreState(undoStack.peek());
        }
    }
    
    public void redo() {
        if (!redoStack.isEmpty()) {
            // Move state from redo to undo
            LevelState state = redoStack.pop();
            undoStack.push(state);
            
            // Restore the state
            restoreState(state);
        }
    }
    
    private void restoreState(LevelState state) {
        platforms.clear();
        enemies.clear();
        coins.clear();
        mushrooms.clear();
        bricks.clear();

        for (RectF platform : state.platforms) {
            platforms.add(new RectF(platform));
        }
        
        for (LevelData.EnemyData enemy : state.enemies) {
            enemies.add(new LevelData.EnemyData(enemy.x, enemy.y, enemy.type, enemy.movementType, enemy.patrolWidth));
        }
        
        for (LevelData.CoinData coin : state.coins) {
            coins.add(new LevelData.CoinData(coin.x, coin.y));
        }
        
        for (LevelData.MushroomData mushroom : state.mushrooms) {
            mushrooms.add(new LevelData.MushroomData(mushroom.x, mushroom.y));
        }

        for (LevelData.BrickData brick : state.bricks) {
            bricks.add(new LevelData.BrickData(brick.x, brick.y, brick.contentType, brick.isQuestionBlock));
        }
        
        goal = state.goal != null ? 
            new LevelData.GoalData(state.goal.left, state.goal.top, state.goal.right, state.goal.bottom) : null;
        
        invalidate();
    }
    
    public boolean canUndo() {
        return undoStack.size() > 1;
    }
    
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
    
    public LevelData getCurrentData() {
        LevelData data = new LevelData();

        for (RectF platform : platforms) {
            data.getPlatforms().add(new LevelData.PlatformData(
                    platform.left, platform.top, platform.right, platform.bottom));
        }

        data.getEnemies().addAll(enemies);
        data.getCoins().addAll(coins);
        data.getMushrooms().addAll(mushrooms);
        data.getBricks().addAll(bricks);

        if (goal != null) {
            data.setGoal(goal);
        }

        return data;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        canvas.save();
        
        // Apply zoom and pan transformations
        if (scaleFactor != 1.0f || panX != 0f || panY != 0f) {
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            canvas.scale(scaleFactor, scaleFactor, cx, cy);
            canvas.translate(panX / scaleFactor, panY / scaleFactor);
        }
        
        canvas.translate(-scrollOffset, 0);

        // Draw background
        canvas.drawColor(Color.rgb(135, 206, 235));

        // Draw grid
        drawGrid(canvas);

        // Draw level boundaries
        drawLevelBoundaries(canvas);

        // Draw ground line reference
        paint.setColor(Color.GREEN);
        paint.setStrokeWidth(2);
        canvas.drawLine(0, 700, 10000, 700, paint);

        // Draw platforms
        paint.setColor(Color.rgb(139, 69, 19)); // Brown
        for (RectF platform : platforms) {
            canvas.drawRect(platform, paint);
            // Grass top
            paint.setColor(Color.GREEN);
            canvas.drawRect(platform.left, platform.top, platform.right, platform.top + 10, paint);
            paint.setColor(Color.rgb(139, 69, 19));
        }

        // Draw enemies
        for (LevelData.EnemyData enemy : enemies) {
            // Draw based on type
            if (enemy.type == 0) {
                // Goomba (brown)
                paint.setColor(Color.rgb(139, 69, 19));
            } else if (enemy.type == 1) {
                // Koopa (green)
                paint.setColor(Color.GREEN);
            } else {
                // Bouncing Ball (red/orange)
                paint.setColor(Color.rgb(255, 100, 50));
            }
            
            if (enemy.type == 2) {
                // Bouncing ball - draw as circle
                canvas.drawCircle(enemy.x + 25, enemy.y + 25, 25, paint);
                // Highlight
                paint.setColor(Color.rgb(255, 200, 150));
                canvas.drawCircle(enemy.x + 17, enemy.y + 17, 8, paint);
            } else {
                // Other enemies - draw as circle with eyes
                canvas.drawCircle(enemy.x, enemy.y, 25, paint);
                // Eyes
                paint.setColor(Color.WHITE);
                canvas.drawCircle(enemy.x - 8, enemy.y - 5, 8, paint);
                canvas.drawCircle(enemy.x + 8, enemy.y - 5, 8, paint);
                paint.setColor(Color.BLACK);
                canvas.drawCircle(enemy.x - 8, enemy.y - 5, 4, paint);
                canvas.drawCircle(enemy.x + 8, enemy.y - 5, 4, paint);
            }
        }

        // Draw coins
        paint.setColor(Color.YELLOW);
        for (LevelData.CoinData coin : coins) {
            canvas.drawCircle(coin.x, coin.y, 20, paint);
            paint.setColor(Color.rgb(255, 215, 0));
            canvas.drawCircle(coin.x, coin.y, 15, paint);
        }

        // Draw mushrooms
        for (LevelData.MushroomData mushroom : mushrooms) {
            // Cap
            paint.setColor(Color.RED);
            canvas.drawCircle(mushroom.x, mushroom.y, 20, paint);
            // Stem
            paint.setColor(Color.WHITE);
            canvas.drawRect(mushroom.x - 10, mushroom.y, mushroom.x + 10, mushroom.y + 20, paint);
        }

        // Draw bricks
        for (LevelData.BrickData brick : bricks) {
            if (brick.isQuestionBlock) {
                // Question block (golden)
                paint.setColor(Color.rgb(255, 165, 0));
                canvas.drawRect(brick.x, brick.y, brick.x + 40, brick.y + 40, paint);
                paint.setColor(Color.rgb(200, 100, 0));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(3);
                canvas.drawRect(brick.x, brick.y, brick.x + 40, brick.y + 40, paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(1);

                // Content indicator
                String contentSymbol = "?";
                if (brick.contentType == 0) {
                    contentSymbol = "●"; // Coin
                } else if (brick.contentType == 1) {
                    contentSymbol = "★"; // Mushroom
                }
                paint.setColor(Color.WHITE);
                paint.setTextSize(28);
                paint.setTextAlign(Paint.Align.CENTER);
                canvas.drawText(contentSymbol, brick.x + 20, brick.y + 28, paint);
            } else {
                // Regular brick (brown/red)
                paint.setColor(Color.rgb(178, 34, 34));
                canvas.drawRect(brick.x, brick.y, brick.x + 40, brick.y + 40, paint);
                // Brick pattern lines
                paint.setColor(Color.rgb(139, 69, 19));
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(2);
                canvas.drawLine(brick.x, brick.y + 20, brick.x + 40, brick.y + 20, paint);
                canvas.drawLine(brick.x + 20, brick.y, brick.x + 20, brick.y + 20, paint);
                paint.setStyle(Paint.Style.FILL);
                paint.setStrokeWidth(1);
            }
        }

        // Draw goal
        if (goal != null) {
            paint.setColor(Color.rgb(255, 215, 0));
            canvas.drawRect(goal.toRectF(), paint);
            paint.setColor(Color.BLACK);
            canvas.drawText("GOAL", goal.left + 10, goal.top + 30, textPaint);
        }

        // Draw current placement preview
        if (isPlacing && currentPlacement != null) {
            paint.setColor(Color.argb(128, 255, 255, 255));
            if (currentMode == 0 || currentMode == 5) { // Platform or Goal
                canvas.drawRect(currentPlacement, paint);
            } else if (currentMode == 1) { // Enemy
                // Show enemy type in preview
                if (selectedEnemyType == 0) {
                    paint.setColor(Color.argb(180, 139, 69, 19)); // Brown for Goomba
                } else if (selectedEnemyType == 1) {
                    paint.setColor(Color.argb(180, 0, 255, 0)); // Green for Koopa
                } else {
                    paint.setColor(Color.argb(180, 255, 100, 50)); // Red for BouncingBall
                }
                
                if (selectedEnemyType == 2) {
                    // Bouncing ball preview - circle
                    canvas.drawCircle(currentPlacement.centerX(), currentPlacement.centerY(), 25, paint);
                    // Label
                    paint.setColor(Color.WHITE);
                    paint.setTextSize(20);
                    paint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText("B", currentPlacement.centerX(), currentPlacement.centerY() + 7, paint);
                    paint.setTextAlign(Paint.Align.LEFT);
                } else {
                    // Other enemies - circle with label
                    canvas.drawCircle(currentPlacement.centerX(), currentPlacement.centerY(), 25, paint);
                    paint.setColor(Color.WHITE);
                    paint.setTextSize(20);
                    paint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText(selectedEnemyType == 0 ? "G" : "K", 
                        currentPlacement.centerX(), currentPlacement.centerY() + 7, paint);
                    paint.setTextAlign(Paint.Align.LEFT);
                }
            } else if (currentMode == 2) { // Coin
                canvas.drawCircle(currentPlacement.centerX(), currentPlacement.centerY(), 20, paint);
            } else if (currentMode == 3) { // Mushroom
                canvas.drawCircle(currentPlacement.centerX(), currentPlacement.centerY(), 20, paint);
            } else if (currentMode == 4) { // Brick
                // Preview shows random type
                boolean willBeQuestion = Math.random() < 0.5;
                if (willBeQuestion) {
                    paint.setColor(Color.argb(180, 255, 165, 0));
                    canvas.drawRect(currentPlacement, paint);
                    paint.setColor(Color.WHITE);
                    paint.setTextSize(28);
                    paint.setTextAlign(Paint.Align.CENTER);
                    canvas.drawText("?", currentPlacement.centerX(), currentPlacement.centerY() + 10, paint);
                } else {
                    paint.setColor(Color.argb(180, 178, 34, 34));
                    canvas.drawRect(currentPlacement, paint);
                }
                paint.setTextAlign(Paint.Align.LEFT);
            }
        }

        // Draw mode indicator
        String modeText = "Mode: " + getModeName(currentMode);
        if (isDeleteMode) {
            modeText += " [DELETE MODE]";
        }
        paint.setColor(Color.BLACK);
        canvas.drawText(modeText, 10 + scrollOffset, 50, textPaint);

        // Draw gesture status
        // Draw gesture status with improved visibility
        if (gestureState == STATE_PANNING) {
            paint.setColor(Color.argb(255, 76, 175, 80)); // Green for panning
            paint.setFakeBoldText(true);
            canvas.drawText("↔ PANNING", 10 + scrollOffset, 85, textPaint);
            paint.setFakeBoldText(false);
        } else if (gestureState == STATE_ZOOMING) {
            paint.setColor(Color.argb(255, 255, 87, 34)); // Orange for zooming
            paint.setFakeBoldText(true);
            canvas.drawText("⤢ ZOOMING", 10 + scrollOffset, 85, textPaint);
            paint.setFakeBoldText(false);
        } else {
            // Show readiness state
            paint.setColor(Color.argb(150, 100, 100, 100));
            canvas.drawText("Ready", 10 + scrollOffset, 85, textPaint);
        }

        // Draw zoom indicator
        String zoomText = "Zoom: " + (int)(scaleFactor * 100) + "%";
        if (isZoomedOut) {
            zoomText += " (Full View)";
        }
        canvas.drawText(zoomText, 10 + scrollOffset, 120, textPaint);

        // Draw gesture hint
        paint.setTextSize(20);
        paint.setColor(Color.argb(150, 0, 0, 0));
        if (gestureState == STATE_IDLE) {
            if (scaleFactor != 1.0f || panX != 0f || panY != 0f) {
                canvas.drawText("Two-finger drag: Pan | Pinch: Zoom | 1 finger: Edit", 10 + scrollOffset, 150, paint);
            } else {
                canvas.drawText("Pinch to zoom | Two-finger drag to pan | 1 finger: Edit", 10 + scrollOffset, 150, paint);
            }
        } else {
            canvas.drawText("Release to resume editing", 10 + scrollOffset, 150, paint);
        }

        canvas.restore();
    }

    private String getModeName(int mode) {
        switch (mode) {
            case 0: return "Platform";
            case 1: return "Enemy";
            case 2: return "Coin";
            case 3: return "Mushroom";
            case 4: return "Brick";
            case 5: return "Goal";
            default: return "Unknown";
        }
    }

    private void drawGrid(Canvas canvas) {
        gridPaint.setColor(Color.argb(50, 200, 200, 200));

        // Vertical lines every 100 pixels
        for (float x = 0; x < 10000; x += 100) {
            canvas.drawLine(x, 0, x, 2000, gridPaint);
        }

        // Horizontal lines every 100 pixels
        for (float y = 0; y < 2000; y += 100) {
            canvas.drawLine(0, y, 10000, y, gridPaint);
        }
    }

    private void drawLevelBoundaries(Canvas canvas) {
        // Draw boundary rectangle
        paint.setColor(Color.argb(100, 255, 0, 0)); // Semi-transparent red
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);

        // Draw the main level boundary
        RectF levelBounds = new RectF(0, 0, levelWidth, levelHeight);
        canvas.drawRect(levelBounds, paint);

        // Draw corner markers
        float markerSize = 50f;
        float strokeWidth = 8f;

        paint.setStrokeWidth(strokeWidth);
        paint.setColor(Color.argb(200, 255, 0, 0)); // More opaque red

        // Top-left corner
        canvas.drawLine(0, 0, markerSize, 0, paint);
        canvas.drawLine(0, 0, 0, markerSize, paint);

        // Top-right corner
        canvas.drawLine(levelWidth - markerSize, 0, levelWidth, 0, paint);
        canvas.drawLine(levelWidth, 0, levelWidth, markerSize, paint);

        // Bottom-left corner
        canvas.drawLine(0, levelHeight - markerSize, 0, levelHeight, paint);
        canvas.drawLine(0, levelHeight, markerSize, levelHeight, paint);

        // Bottom-right corner
        canvas.drawLine(levelWidth - markerSize, levelHeight, levelWidth, levelHeight, paint);
        canvas.drawLine(levelWidth, levelHeight - markerSize, levelWidth, levelHeight, paint);

        // Reset paint style
        paint.setStyle(Paint.Style.FILL);
        paint.setStrokeWidth(1);

        // Draw dimension labels
        paint.setColor(Color.argb(200, 255, 0, 0));
        paint.setTextSize(30);
        paint.setTextAlign(Paint.Align.CENTER);

        // Width label at top
        String widthLabel = "Level Width: " + (int)levelWidth + "px";
        canvas.drawText(widthLabel, levelWidth / 2, 40, paint);

        // Height label on right side
        paint.setTextSize(25);
        canvas.rotate(90, levelWidth + 30, levelHeight / 2);
        canvas.drawText("Height: " + (int)levelHeight + "px", levelWidth + 30, levelHeight / 2, paint);
        canvas.rotate(-90, levelWidth + 30, levelHeight / 2);

        // Draw X-axis markers
        paint.setTextSize(20);
        for (float x = 0; x <= levelWidth; x += 500) {
            canvas.drawLine(x, 0, x, 10, paint);
            canvas.drawText("" + (int)x, x, 25, paint);
        }

        // Draw Y-axis markers
        paint.setTextAlign(Paint.Align.RIGHT);
        for (float y = 0; y <= levelHeight; y += 200) {
            canvas.drawLine(0, y, 10, y, paint);
            canvas.drawText("" + (int)y, 25, y + 7, paint);
        }
        paint.setTextAlign(Paint.Align.LEFT);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        int pointerCount = event.getPointerCount();

        // Handle pinch zoom and pan with 2 fingers
        if (pointerCount >= 2) {
            switch (action) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    // Initialize both distances
                    oldDist = spacing(event);
                    lastPanX = (event.getX(0) + event.getX(1)) / 2f;
                    lastPanY = (event.getY(0) + event.getY(1)) / 2f;
                    gestureState = STATE_IDLE;
                    justReleasedFromGesture = false;

                    // Cancel any ongoing placement when starting two-finger gesture
                    isPlacing = false;
                    currentPlacement = null;
                    invalidate();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    float newDist = spacing(event);
                    float newPanX = (event.getX(0) + event.getX(1)) / 2f;
                    float newPanY = (event.getY(0) + event.getY(1)) / 2f;

                    // Calculate changes from last position
                    float distChange = Math.abs(newDist - oldDist);
                    float panXChange = Math.abs(newPanX - lastPanX);
                    float panYChange = Math.abs(newPanY - lastPanY);
                    float panChange = (float) Math.sqrt(panXChange * panXChange + panYChange * panYChange);

                    // IMPROVED: Determine gesture type with better sensitivity
                    if (gestureState == STATE_IDLE) {
                        // Use a much lower threshold for pan detection
                        // and require more significant distance change for zoom
                        if (panChange > 10f) {
                            // Strong preference for panning
                            gestureState = STATE_PANNING;
                        } else if (distChange > 15f) {
                            // Require more significant change for zoom
                            gestureState = STATE_ZOOMING;
                        }
                    }

                    // Execute the determined gesture
                    if (gestureState == STATE_PANNING) {
                        // Apply pan only - don't update oldDist to avoid zoom interference
                        float deltaX = newPanX - lastPanX;
                        float deltaY = newPanY - lastPanY;

                        panX += deltaX * scaleFactor;
                        panY += deltaY * scaleFactor;

                        lastPanX = newPanX;
                        lastPanY = newPanY;
                    } else if (gestureState == STATE_ZOOMING) {
                        // Apply zoom only
                        handleZoom(event);

                        // Update pan center to avoid jump
                        lastPanX = newPanX;
                        lastPanY = newPanY;
                    }

                    invalidate();
                    return true;

                case MotionEvent.ACTION_POINTER_UP:
                    // First finger lifted - set flag to block subsequent single-finger events
                    gestureState = STATE_IDLE;
                    justReleasedFromGesture = true;
                    invalidate();
                    return true;
            }
        }

        // Single finger handling
        if (pointerCount == 1) {
            // IMPORTANT: If we just released from a gesture, ignore this ACTION_UP
            if (justReleasedFromGesture && action == MotionEvent.ACTION_UP) {
                justReleasedFromGesture = false;
                return true; // Consume the event, don't process placement
            }

            // Reset the flag for normal single-finger operations
            if (action == MotionEvent.ACTION_DOWN) {
                justReleasedFromGesture = false;
            }

            // Get raw touch coordinates
            float x = event.getX();
            float y = event.getY();

            // FIXED: Properly transform screen coordinates to canvas coordinates
            if (scaleFactor != 1.0f || panX != 0f || panY != 0f) {
                // When zoomed/panned, we need to inverse the transformation
                // Screen point -> Canvas point
                float cx = getWidth() / 2f;
                float cy = getHeight() / 2f;

                // Step 1: Translate by negative pan
                x = x - panX;
                y = y - panY;

                // Step 2: Scale back (inverse of zoom)
                x = cx + (x - cx) / scaleFactor;
                y = cy + (y - cy) / scaleFactor;

                // Step 3: Add scroll offset
                x = x + scrollOffset;
            } else {
                // No zoom/pan, just add scroll offset
                x = x + scrollOffset;
            }

            // If in delete mode, handle deletion on ACTION_UP
            if (isDeleteMode && event.getAction() == MotionEvent.ACTION_UP) {
                removeObjectAt(x, y);
                return true;
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastTouchX = x;

                    // Only allow placement if NOT in zoom/pan gesture
                    if (!isDeleteMode && (currentMode == 0 || currentMode == 4 || currentMode == 5)) {
                        // Start placing rectangle (Platform, Brick, or Goal)
                        currentPlacement = new RectF(x, y, x, y);
                        isPlacing = true;
                    } else if (!isDeleteMode) {
                        // Place single object
                        placeObject(x, y);
                    }
                    invalidate();
                    return true;

                case MotionEvent.ACTION_MOVE:
                    if (isPlacing && currentPlacement != null) {
                        currentPlacement.right = x;
                        currentPlacement.bottom = y;
                        invalidate();
                    } else if (gestureState == STATE_IDLE) {
                        // Single finger scroll when not zoomed
                        float delta = x - lastTouchX;
                        scrollOffset -= delta;
                        if (scrollOffset < 0) scrollOffset = 0;
                        lastTouchX = x;
                        invalidate();
                    }
                    return true;

                case MotionEvent.ACTION_UP:
                    // Only process if we were actually placing something
                    if (isPlacing && currentPlacement != null) {
                        // Normalize rect (ensure left < right, top < bottom)
                        normalizeRect(currentPlacement);
                        finishPlacement(currentPlacement);
                        isPlacing = false;
                        currentPlacement = null;
                        invalidate();
                    }
                    return true;
            }
        }

        return super.onTouchEvent(event);
    }
    
    // Pinch zoom helper methods
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }
    
    private void handleZoom(MotionEvent event) {
        float newDist = spacing(event);
        if (newDist > 10f) {
            float delta = newDist - oldDist;

            // Add deadzone to prevent accidental micro-zooms
            if (Math.abs(delta) < 3f) {
                // Too small, ignore
                return;
            }

            scaleFactor += delta * 0.005f;

            // Clamp scale factor
            if (scaleFactor < MIN_SCALE) {
                scaleFactor = MIN_SCALE;
            } else if (scaleFactor > MAX_SCALE) {
                scaleFactor = MAX_SCALE;
            }

            // Check if fully zoomed out
            isZoomedOut = (scaleFactor <= MIN_SCALE + 0.05f);
        }
        oldDist = newDist;
    }
    
    // Reset zoom and pan
    public void resetZoomAndPan() {
        scaleFactor = 1.0f;
        panX = 0f;
        panY = 0f;
        isZoomedOut = false;
        invalidate();
    }
    
    public float getScaleFactor() {
        return scaleFactor;
    }
    
    public boolean isZoomedOut() {
        return isZoomedOut;
    }
    
    private void normalizeRect(RectF rect) {
        float temp;
        if (rect.left > rect.right) {
            temp = rect.left;
            rect.left = rect.right;
            rect.right = temp;
        }
        if (rect.top > rect.bottom) {
            temp = rect.top;
            rect.top = rect.bottom;
            rect.bottom = temp;
        }
    }

    private void placeObject(float x, float y) {
        if (listener == null) return;
        
        switch (currentMode) {
            case 1: // Enemy
                int movementType = 0;
                int patrolWidth = 200;
                switch (selectedEnemyType){
                    case 0:{
                        movementType = 0;
                        patrolWidth = 200;
                        break;
                    }
                    case 1:{
                        movementType = 1;
                        patrolWidth = 3000;
                        break;
                    }
                }
                LevelData.EnemyData enemy = new LevelData.EnemyData(x, y, selectedEnemyType, movementType, patrolWidth);
                enemies.add(enemy);
                listener.onEnemyPlaced(enemy);
                saveState();
                break;
            case 2: // Coin
                LevelData.CoinData coin = new LevelData.CoinData(x, y);
                coins.add(coin);
                listener.onCoinPlaced(coin);
                saveState();
                break;
            case 3: // Mushroom
                LevelData.MushroomData mushroom = new LevelData.MushroomData(x, y);
                mushrooms.add(mushroom);
                listener.onMushroomPlaced(mushroom);
                saveState();
                break;
            case 4: // Brick - For bricks, we use finishPlacement instead
                // Bricks are placed by dragging, not clicking
                break;
        }
        invalidate();
    }
    
    private void finishPlacement(RectF rect) {
        if (listener == null) return;
        
        switch (currentMode) {
            case 0: // Platform
                platforms.add(rect);
                listener.onPlatformPlaced(rect);
                break;
            case 4: // Brick - Place a brick at this position
                // 50% question block, 50% regular brick
                boolean isQuestionBlock = Math.random() < 0.5;
                // Content: only for question blocks
                int contentType = isQuestionBlock ? (int)(Math.random() * 2) : 2; // 2 = nothing

                LevelData.BrickData brick = new LevelData.BrickData(
                        rect.left, rect.top, contentType, isQuestionBlock);
                bricks.add(brick);

                listener.onPlatformPlaced(rect);
                break;

            case 5: // Goal
                goal = new LevelData.GoalData(rect.left, rect.top, rect.right, rect.bottom);
                listener.onGoalPlaced(goal);
                break;
        }

        // Save state after placement
        saveState();

        // Force redraw
        invalidate();
    }
    
    public void removeObjectAt(float x, float y) {
        RectF touchRect = new RectF(x - 10, y - 10, x + 10, y + 10);
        
        boolean removed = false;
        
        // Check platforms
        for (int i = platforms.size() - 1; i >= 0; i--) {
            if (RectF.intersects(touchRect, platforms.get(i))) {
                platforms.remove(i);
                removed = true;
                break;
            }
        }
        
        // Check enemies
        if (!removed) {
            for (int i = enemies.size() - 1; i >= 0; i--) {
                LevelData.EnemyData enemy = enemies.get(i);
                if (x > enemy.x - 30 && x < enemy.x + 30 && 
                    y > enemy.y - 30 && y < enemy.y + 30) {
                    enemies.remove(i);
                    removed = true;
                    break;
                }
            }
        }
        
        // Check coins
        if (!removed) {
            for (int i = coins.size() - 1; i >= 0; i--) {
                LevelData.CoinData coin = coins.get(i);
                if (x > coin.x - 25 && x < coin.x + 25 && 
                    y > coin.y - 25 && y < coin.y + 25) {
                    coins.remove(i);
                    removed = true;
                    break;
                }
            }
        }
        
        // Check mushrooms
        if (!removed) {
            for (int i = mushrooms.size() - 1; i >= 0; i--) {
                LevelData.MushroomData mushroom = mushrooms.get(i);
                if (x > mushroom.x - 25 && x < mushroom.x + 25 && 
                    y > mushroom.y - 25 && y < mushroom.y + 25) {
                    mushrooms.remove(i);
                    removed = true;
                    break;
                }
            }
        }

        // Check bricks
        if (!removed) {
            for (int i = bricks.size() - 1; i >= 0; i--) {
                LevelData.BrickData brick = bricks.get(i);
                if (x > brick.x - 25 && x < brick.x + 25 &&
                        y > brick.y - 25 && y < brick.y + 25) {
                    bricks.remove(i);
                    removed = true;
                    break;
                }
            }
        }
        
        // Check goal
        if (!removed && goal != null && goal.toRectF().contains(x, y)) {
            goal = null;
            removed = true;
        }
        
        if (removed) {
            saveState();
            invalidate();
            
            // Notify listener to update UI
            if (listener != null) {
                listener.onPlatformPlaced(null); // Just to trigger UI update
            }
        }
    }
    
    // Inner class for state storage
    private static class LevelState {
        List<RectF> platforms = new ArrayList<>();
        List<LevelData.EnemyData> enemies = new ArrayList<>();
        List<LevelData.CoinData> coins = new ArrayList<>();
        List<LevelData.MushroomData> mushrooms = new ArrayList<>();
        List<LevelData.BrickData> bricks = new ArrayList<>();
        LevelData.GoalData goal;
    }
}