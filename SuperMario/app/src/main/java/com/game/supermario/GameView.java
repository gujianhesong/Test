package com.game.supermario;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable, SurfaceHolder.Callback {
    
    private Thread gameThread;
    private boolean isPlaying;
    private GameEngine gameEngine;
    private Paint paint;
    
    // Touch controls
    private boolean touchLeft, touchRight, touchJump, touchShoot;
    
    // Control buttons
    private float btnLeftX, btnLeftY, btnRightX, btnRightY, btnJumpX, btnJumpY, btnShootX, btnShootY;
    private float btnRadius;
    private int activePointerId = -1;
    private int leftActivePointerId = -1;
    private int rightActivePointerId = -1;
    private int jumpActivePointerId = -1;
    private int shootActivePointerId = -1;
    
    // Zoom/Pinch control
    private float scaleFactor = 1.0f;
    private static final float MIN_SCALE = 0.3f;
    private static final float MAX_SCALE = 2.0f;
    private float oldDist = 1f;
    private boolean isZoomedOut = false;
    
    // Pan control (for moving around when zoomed in/out)
    private float panX = 0f;
    private float panY = 0f;
    private float lastPanX = 0f;
    private float lastPanY = 0f;
    
    // Gesture state - only one active at a time
    private static final int STATE_IDLE = 0;
    private static final int STATE_PANNING = 1;
    private static final int STATE_ZOOMING = 2;
    private int gestureState = STATE_IDLE;
    
    public GameView(Context context) {
        super(context);
        init();
    }
    
    // Constructor for XML inflation (required by Android framework)
    public GameView(Context context, android.util.AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    public void setLevelIndex(int levelIndex) {
        if (gameEngine != null) {
            gameEngine.setLevelIndex(levelIndex);
        }
    }

    public void setRamdonMode(boolean isRamdonMode){
        if (gameEngine != null) {
            gameEngine.setRamdonMode(isRamdonMode);
        }
    }
    
    private void init() {
        getHolder().addCallback(this);
        paint = new Paint();
        paint.setAntiAlias(true);
        gameEngine = new GameEngine(getContext());
        btnRadius = 60;
    }
    
    private void initButtonPositions(int width, int height) {
        if (btnLeftX == 0 && btnLeftY == 0) {
            // Left side: two buttons (left and right movement)
            // Position them at about 15% and 30% of screen width
            btnRadius = Math.min(width, height) * 0.08f; // Responsive size
            
            btnLeftX = width * 0.12f;
            btnLeftY = height - btnRadius - 30;
            
            btnRightX = width * 0.28f;
            btnRightY = height - btnRadius - 30;
            
            // Right side: two buttons (jump and shoot)
            // Jump button at bottom right
            btnJumpX = width * 0.85f;
            btnJumpY = height - btnRadius - 30;
            
            // Shoot button above jump button
            btnShootX = width * 0.90f;
            btnShootY = height - btnRadius * 2.5f - 50;
        }
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
    
    // Quick zoom to fit entire level
    private void quickZoomOut() {
        if (scaleFactor > MIN_SCALE) {
            scaleFactor = MIN_SCALE;
            isZoomedOut = true;
        } else {
            scaleFactor = 1.0f;
            isZoomedOut = false;
        }
        // Reset pan when toggling zoom
        panX = 0f;
        panY = 0f;
    }
    
    // Reset pan to center
    private void resetPan() {
        panX = 0f;
        panY = 0f;
    }
    
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Surface created, can start rendering
    }
    
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Surface size changed
        initButtonPositions(width, height);
    }
    
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed
        pause();
    }
    
    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            sleep();
        }
    }
    
    private void update() {
        gameEngine.setScreenWidth(getWidth());
        gameEngine.update(touchLeft, touchRight, touchJump, touchShoot);
    }
    
    private void draw() {
        if (getHolder().getSurface().isValid()) {
            Canvas canvas = getHolder().lockCanvas();
            
            // Clear screen with sky blue color
            canvas.drawColor(Color.rgb(135, 206, 235));
            
            // Apply zoom and pan transformation
            canvas.save();
            if (scaleFactor != 1.0f || panX != 0f || panY != 0f) {
                // Zoom around center of screen
                float cx = getWidth() / 2f;
                float cy = getHeight() / 2f;
                canvas.scale(scaleFactor, scaleFactor, cx, cy);
                // Apply pan offset
                canvas.translate(panX / scaleFactor, panY / scaleFactor);
            }
            
            // Draw game elements with camera
            gameEngine.draw(canvas, paint, gameEngine.getCameraX());
            
            canvas.restore();
            
            // Draw control buttons (always normal size, not affected by zoom/pan)
            drawControlButtons(canvas, paint);
            
            // Draw HUD (always normal size, not affected by zoom/pan)
            drawHUD(canvas, paint);
            
            // Draw zoom indicator
            drawZoomIndicator(canvas, paint);
            
            getHolder().unlockCanvasAndPost(canvas);
        }
    }
    
    private void drawControlButtons(Canvas canvas, Paint paint) {
        int width = getWidth();
        int height = getHeight();
        initButtonPositions(width, height);
        
        // Button settings
        int normalColor = Color.argb(80, 255, 255, 255); // Semi-transparent white
        int pressedColor = Color.argb(150, 255, 255, 255); // More opaque when pressed
        int borderColor = Color.argb(180, 255, 255, 255);
        
        // Draw left button
        paint.setColor(touchLeft ? pressedColor : normalColor);
        canvas.drawCircle(btnLeftX, btnLeftY, btnRadius, paint);
        paint.setColor(borderColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        canvas.drawCircle(btnLeftX, btnLeftY, btnRadius, paint);
        paint.setStyle(Paint.Style.FILL);
        
        // Draw left arrow
        paint.setColor(Color.WHITE);
        paint.setTextSize(50);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("◄", btnLeftX, btnLeftY + 18, paint);
        
        // Draw right button
        paint.setColor(touchRight ? pressedColor : normalColor);
        canvas.drawCircle(btnRightX, btnRightY, btnRadius, paint);
        paint.setColor(borderColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        canvas.drawCircle(btnRightX, btnRightY, btnRadius, paint);
        paint.setStyle(Paint.Style.FILL);
        
        // Draw right arrow
        paint.setColor(Color.WHITE);
        canvas.drawText("►", btnRightX, btnRightY + 18, paint);
        
        // Draw jump button
        paint.setColor(touchJump ? pressedColor : normalColor);
        canvas.drawCircle(btnJumpX, btnJumpY, btnRadius + 10, paint);
        paint.setColor(borderColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        canvas.drawCircle(btnJumpX, btnJumpY, btnRadius + 10, paint);
        paint.setStyle(Paint.Style.FILL);
        
        // Draw jump text
        paint.setColor(Color.WHITE);
        paint.setTextSize(40);
        canvas.drawText("JUMP", btnJumpX, btnJumpY + 15, paint);
        
        // Draw shoot button (smaller, above jump)
        paint.setColor(touchShoot ? pressedColor : normalColor);
        canvas.drawCircle(btnShootX, btnShootY, btnRadius - 10, paint);
        paint.setColor(borderColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        canvas.drawCircle(btnShootX, btnShootY, btnRadius - 10, paint);
        paint.setStyle(Paint.Style.FILL);
        
        // Draw bullet icon
        paint.setColor(Color.WHITE);
        paint.setTextSize(30);
        canvas.drawText("●", btnShootX, btnShootY + 10, paint);
    }
    
    private void drawZoomIndicator(Canvas canvas, Paint paint) {
        // Draw zoom level indicator in top-left corner
        paint.setColor(Color.argb(150, 0, 0, 0));
        paint.setTextSize(25);
        paint.setTextAlign(Paint.Align.LEFT);
        
        String zoomText = "Zoom: " + (int)(scaleFactor * 100) + "%";
        if (isZoomedOut) {
            zoomText += " (Full Map)";
        }
        canvas.drawText(zoomText, 20, 80, paint);
        
        // Draw hints for controls
        paint.setColor(Color.argb(100, 255, 255, 255));
        paint.setTextSize(18);
        canvas.drawText("Double-tap top-center: Toggle full map", 20, 105, paint);
        
        if (scaleFactor != 1.0f || panX != 0f || panY != 0f) {
            canvas.drawText("Two-finger drag: Pan view | Pinch: Zoom", 20, 125, paint);
            if (panX != 0f || panY != 0f) {
                canvas.drawText("Tap top-center again: Reset view", 20, 145, paint);
            }
        }
    }
    
    private void drawHUD(Canvas canvas, Paint paint) {
        paint.setColor(Color.WHITE);
        paint.setTextSize(35);
        paint.setTextAlign(Paint.Align.RIGHT);
        
        canvas.drawText("Level: " + gameEngine.getCurrentLevel(), getWidth() - 20, 50, paint);
        canvas.drawText("Lives: " + gameEngine.getLives(), getWidth() - 20, 95, paint);
        canvas.drawText("Score: " + gameEngine.getScore(), getWidth() - 20, 140, paint);
        
        // Show power-up status
        paint.setTextAlign(Paint.Align.LEFT);
        String sizeStatus = (scaleFactor <= MIN_SCALE + 0.05f) ? "Full Map View" : "Size: Normal";
        canvas.drawText(sizeStatus, 20, 50, paint);
    }
    
    private void sleep() {
        try {
            Thread.sleep(17); // ~60 FPS
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public void pause() {
        isPlaying = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        int pointerIndex = event.getActionIndex();
        int pointerId = event.getPointerId(pointerIndex);
        
        // Handle pinch zoom and pan with 2 fingers
        if (event.getPointerCount() == 2) {
            switch (action) {
                case MotionEvent.ACTION_POINTER_DOWN:
                    // Initialize both distances
                    oldDist = spacing(event);
                    lastPanX = (event.getX(0) + event.getX(1)) / 2f;
                    lastPanY = (event.getY(0) + event.getY(1)) / 2f;
                    gestureState = STATE_IDLE;
                    break;
                    
                case MotionEvent.ACTION_MOVE:
                    float newDist = spacing(event);
                    float newPanX = (event.getX(0) + event.getX(1)) / 2f;
                    float newPanY = (event.getY(0) + event.getY(1)) / 2f;
                    
                    // Calculate changes from last position
                    float distChange = Math.abs(newDist - oldDist);
                    float panXChange = Math.abs(newPanX - lastPanX);
                    float panYChange = Math.abs(newPanY - lastPanY);
                    float panChange = (float) Math.sqrt(panXChange * panXChange + panYChange * panYChange);
                    
                    // Determine gesture type if not already determined
                    if (gestureState == STATE_IDLE) {
                        if (panChange > 15f && panChange > distChange * 3) {
                            gestureState = STATE_PANNING;
                        } else if (distChange > 10f) {
                            gestureState = STATE_ZOOMING;
                        }
                    }
                    
                    // Execute the determined gesture
                    if (gestureState == STATE_PANNING) {
                        // Apply pan only - don't update oldDist
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
                    
                    return true;
                    
                case MotionEvent.ACTION_POINTER_UP:
                    gestureState = STATE_IDLE;
                    break;
            }
        } else if (event.getPointerCount() == 1) {
            gestureState = STATE_IDLE;
        }
        
        // Handle double tap for quick zoom toggle
        if (action == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            int width = getWidth();
            int height = getHeight();
            
            // Double-tap zone: center top area
            if (x > width * 0.4f && x < width * 0.6f && y < height * 0.2f) {
                quickZoomOut();
            }
        }
        
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                handleTouchDown(event, pointerIndex, pointerId);
                break;
                
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                handleTouchUp(event, pointerId);
                break;
                
            case MotionEvent.ACTION_CANCEL:
                resetAllTouches();
                break;
        }
        
        return true;
    }
    
    private void handleTouchDown(MotionEvent event, int pointerIndex, int pointerId) {
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);
        
        // Check if left button was pressed (circle collision detection)
        float distToLeftBtn = (float) Math.sqrt(
            Math.pow(x - btnLeftX, 2) + Math.pow(y - btnLeftY, 2)
        );
        if (distToLeftBtn <= btnRadius) {
            if (leftActivePointerId == -1 || leftActivePointerId == pointerId) {
                touchLeft = true;
                leftActivePointerId = pointerId;
            }
            return;
        }
        
        // Check if right button was pressed
        float distToRightBtn = (float) Math.sqrt(
            Math.pow(x - btnRightX, 2) + Math.pow(y - btnRightY, 2)
        );
        if (distToRightBtn <= btnRadius) {
            if (rightActivePointerId == -1 || rightActivePointerId == pointerId) {
                touchRight = true;
                rightActivePointerId = pointerId;
            }
            return;
        }
        
        // Check if jump button was pressed
        float distToJumpBtn = (float) Math.sqrt(
            Math.pow(x - btnJumpX, 2) + Math.pow(y - btnJumpY, 2)
        );
        if (distToJumpBtn <= btnRadius + 10) {
            if (jumpActivePointerId == -1 || jumpActivePointerId == pointerId) {
                touchJump = true;
                jumpActivePointerId = pointerId;
            }
            return;
        }
        
        // Check if shoot button was pressed
        float distToShootBtn = (float) Math.sqrt(
            Math.pow(x - btnShootX, 2) + Math.pow(y - btnShootY, 2)
        );
        if (distToShootBtn <= btnRadius - 10) {
            if (shootActivePointerId == -1 || shootActivePointerId == pointerId) {
                touchShoot = true;
                shootActivePointerId = pointerId;
            }
        }
    }
    
    private void handleTouchUp(MotionEvent event, int pointerId) {
        if (pointerId == leftActivePointerId) {
            touchLeft = false;
            leftActivePointerId = -1;
        }
        if (pointerId == rightActivePointerId) {
            touchRight = false;
            rightActivePointerId = -1;
        }
        if (pointerId == jumpActivePointerId) {
            touchJump = false;
            jumpActivePointerId = -1;
        }
        if (pointerId == shootActivePointerId) {
            touchShoot = false;
            shootActivePointerId = -1;
        }
    }
    
    private void resetAllTouches() {
        touchLeft = false;
        touchRight = false;
        touchJump = false;
        touchShoot = false;
        leftActivePointerId = -1;
        rightActivePointerId = -1;
        jumpActivePointerId = -1;
        shootActivePointerId = -1;
    }
}
