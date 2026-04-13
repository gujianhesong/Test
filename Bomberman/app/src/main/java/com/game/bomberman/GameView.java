package com.game.bomberman;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements Runnable {
    private Thread gameThread;
    private boolean isPlaying;
    private Canvas canvas;
    private SurfaceHolder surfaceHolder;
    private Paint paint;

    // Game objects
    private Player player;
    private GameEngine gameEngine;

    // Screen dimensions
    private int screenWidth;
    private int screenHeight;

    // Cell size for grid
    private static final int CELL_SIZE = 40;

    public GameView(Context context) {
        super(context);

        surfaceHolder = getHolder();
        paint = new Paint();

        // Initialize game engine
        gameEngine = new GameEngine(context);

        // Set focus to receive touch events
        setFocusable(true);
    }

    @Override
    public void run() {
        while (isPlaying) {
            update();
            draw();
            control();
        }
    }

    private void update() {
        gameEngine.update();
    }

    private void draw() {
        if (surfaceHolder.getSurface().isValid()) {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.BLACK);

            // Draw game elements
            gameEngine.draw(canvas, paint);

            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private void control() {
        try {
            Thread.sleep(17); // ~60 FPS
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        isPlaying = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    public void pause() {
        isPlaying = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gameEngine.handleTouch(event);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        setScreenSize(w, h);
    }

    public void setScreenSize(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
        gameEngine.setScreenSize(width, height);
    }
}
