package com.game.supermario;

import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class LevelEditorActivity extends AppCompatActivity implements LevelEditorView.OnObjectPlacedListener {

    private LevelEditorView editorView;
    private Spinner modeSpinner;
    private Spinner levelSpinner;
    private TextView infoPanel;
    private Button undoButton;
    private Button redoButton;
    private Button deleteButton;
    private Button resetViewButton;
    
    private int currentLevelNumber = 1;
    private boolean isDeleteMode = false;

    private String[] modes = {"Platform", "Enemy", "Coin", "Mushroom", "Brick", "Goal"};
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_editor);

        editorView = findViewById(R.id.editorView);
        modeSpinner = findViewById(R.id.modeSpinner);
        levelSpinner = findViewById(R.id.levelSpinner);
        infoPanel = findViewById(R.id.infoPanel);
        undoButton = findViewById(R.id.undoButton);
        redoButton = findViewById(R.id.redoButton);
        deleteButton = findViewById(R.id.deleteButton);
        resetViewButton = findViewById(R.id.resetViewButton);
        
        // Get level number from intent if provided
        int levelToEdit = getIntent().getIntExtra("EDIT_LEVEL", -1);
        if (levelToEdit < 1 || levelToEdit > 30) {
            levelToEdit = 1;
        }
        currentLevelNumber = levelToEdit;

        // Setup level spinner (1-30)
        setupLevelSpinner();

        // Setup mode spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, modes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modeSpinner.setAdapter(adapter);

        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editorView.setCurrentMode(position);
                updateInfoPanel();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Back button
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Save button
        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(v -> saveLevel());

        // Undo button
        undoButton.setOnClickListener(v -> {
            editorView.undo();
            updateInfoPanel();
            updateUndoRedoButtons();
        });

        // Redo button
        redoButton.setOnClickListener(v -> {
            editorView.redo();
            updateInfoPanel();
            updateUndoRedoButtons();
        });

        // Delete button
        deleteButton.setOnClickListener(v -> {
            isDeleteMode = !isDeleteMode;
            if (isDeleteMode) {
                deleteButton.setBackgroundColor(Color.parseColor("#FF5722"));
                Toast.makeText(this, "Tap any object to delete it", Toast.LENGTH_LONG).show();
                editorView.setDeleteMode(true);
            } else {
                deleteButton.setBackgroundColor(Color.parseColor("#F44336"));
                editorView.setDeleteMode(false);
            }
        });

        // Reset view button
        resetViewButton.setOnClickListener(v -> {
            editorView.resetZoomAndPan();
        });
        
        // Setup editor
        editorView.setOnObjectPlacedListener(this);

        // Load the level for editing
        loadLevel(currentLevelNumber);

        updateInfoPanel();
        updateUndoRedoButtons();
    }
    
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_Z && event.isCtrlPressed()) {
            if (event.isShiftPressed()) {
                // Ctrl+Shift+Z = Redo
                if (editorView.canRedo()) {
                    editorView.redo();
                    updateInfoPanel();
                    updateUndoRedoButtons();
                }
            } else {
                // Ctrl+Z = Undo
                if (editorView.canUndo()) {
                    editorView.undo();
                    updateInfoPanel();
                    updateUndoRedoButtons();
                }
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_Y && event.isCtrlPressed()) {
            // Ctrl+Y = Redo
            if (editorView.canRedo()) {
                editorView.redo();
                updateInfoPanel();
                updateUndoRedoButtons();
            }
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_DEL || keyCode == KeyEvent.KEYCODE_FORWARD_DEL) {
            // Delete key - remove selected object
            Toast.makeText(this, "Long press to delete objects", Toast.LENGTH_SHORT).show();
            return true;
        }
        
        return super.onKeyDown(keyCode, event);
    }
    
    private void updateUndoRedoButtons() {
        undoButton.setEnabled(editorView.canUndo());
        redoButton.setEnabled(editorView.canRedo());
        
        // Visual feedback
        undoButton.setAlpha(editorView.canUndo() ? 1.0f : 0.5f);
        redoButton.setAlpha(editorView.canRedo() ? 1.0f : 0.5f);
    }
    
    private void updateInfoPanel() {
        // Get data directly from editor view, not from a field
        LevelData data = editorView.getCurrentData();

        String info = "Objects:\n";
        info += "Platforms: " + data.getPlatforms().size() + "\n";
        info += "Enemies: " + data.getEnemies().size() + "\n";
        info += "Coins: " + data.getCoins().size() + "\n";
        info += "Mushrooms: " + data.getMushrooms().size() + "\n";

        // Show goal status with color
        if (data.getGoal() != null) {
            info += "\n✓ Goal: Placed";
        } else {
            info += "\n✗ Goal: NOT PLACED";
        }

        infoPanel.setText(info);

        // Update undo/redo buttons
        updateUndoRedoButtons();
    }

    private void setupLevelSpinner() {
        // Create array of level names "Level 1" to "Level 30"
        String[] levelNames = new String[30];
        for (int i = 0; i < 30; i++) {
            levelNames[i] = "Level " + (i + 1);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, levelNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        levelSpinner.setAdapter(adapter);

        // Set selection to current level
        levelSpinner.setSelection(currentLevelNumber - 1);

        // Handle level change
        levelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int selectedLevel = position + 1;
                if (selectedLevel != currentLevelNumber) {
                    // Ask for confirmation if there are unsaved changes
                    confirmLevelChange(selectedLevel);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void confirmLevelChange(final int newLevel) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Switch Level")
                .setMessage("Do you want to switch to Level " + newLevel + "? Unsaved changes will be lost.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    currentLevelNumber = newLevel;
                    loadLevel(currentLevelNumber);
                    updateInfoPanel();
                    updateUndoRedoButtons();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Revert spinner to current level
                    levelSpinner.setSelection(currentLevelNumber - 1);
                })
                .show();
    }

    private void loadLevel(int levelNumber) {
        // Try to load custom level first
        String fileName = "custom_level_" + levelNumber + ".json";
        LevelData loadedData = null;

        try {
            FileInputStream fis = openFileInput(fileName);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            fis.close();

            loadedData = LevelData.fromJson(sb.toString());

        } catch (Exception e) {
            // No custom level exists, load default level
            loadedData = loadDefaultLevel(levelNumber);
        }

        // Load data into editor
        if (loadedData != null) {
            editorView.loadData(loadedData);
        }
    }

    private LevelData loadDefaultLevel(int levelNumber) {
        // Initialize LevelCreator if needed
        LevelCreator.initialize(this);

        // Load default level based on number
        com.game.supermario.Level defaultLevel = null;
        switch (levelNumber) {
            case 1:
                defaultLevel = LevelCreator.createLevel1();
                break;
            case 2:
                defaultLevel = LevelCreator.createLevel2();
                break;
            case 3:
                defaultLevel = LevelCreator.createLevel3();
                break;
            // For levels 4-30, return empty level
            default:
                return new LevelData();
        }

        // Convert default level to LevelData
        return convertToLevelData(defaultLevel);
    }

    private LevelData convertToLevelData(com.game.supermario.Level level) {
        LevelData data = new LevelData();
        data.setName(level.getName());
        data.setWidth(level.getWidth());

        // Convert platforms
        for (RectF platform : level.getPlatforms()) {
            data.getPlatforms().add(new LevelData.PlatformData(
                    platform.left, platform.top, platform.right, platform.bottom));
        }

        // Convert enemies - FIXED: Use getType() instead of get()
        for (Enemy enemy : level.getEnemies()) {
            data.getEnemies().add(new LevelData.EnemyData(
                    enemy.getX(), enemy.getY(), enemy.getType(), enemy.getPatrolWidth()));
        }

        // Convert coins
        for (Coin coin : level.getCoins()) {
            data.getCoins().add(new LevelData.CoinData(coin.getX(), coin.getY()));
        }

        // Convert mushrooms
        for (Mushroom mushroom : level.getMushrooms()) {
            data.getMushrooms().add(new LevelData.MushroomData(mushroom.getX(), mushroom.getY()));
        }

        // Convert goal
        RectF goal = level.getGoal();
        if (goal != null) {
            data.setGoal(new LevelData.GoalData(
                    goal.left, goal.top, goal.right, goal.bottom));
        }

        return data;
    }

    private void saveLevel() {
        // Get data DIRECTLY from editorView
        LevelData levelData = editorView.getCurrentData();

        // Check if goal exists
        if (levelData.getGoal() == null) {
            Toast.makeText(this, "Please place a goal!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Set level name
            String levelName = "Level " + currentLevelNumber;
            levelData.setName(levelName);

            // Convert to JSON and save
            String jsonData = levelData.toJson();

            // Save to internal storage with level number
            String fileName = "custom_level_" + currentLevelNumber + ".json";
            FileOutputStream fos = openFileOutput(fileName, Context.MODE_PRIVATE);
            fos.write(jsonData.getBytes());
            fos.close();

            Toast.makeText(this, "Level " + currentLevelNumber + " saved!", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error saving level: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    
    private void updateLevelList() {
        // This would normally update a shared level registry
        // For simplicity, we'll just show a message
        Toast.makeText(this, "Custom levels are now available in level select!", Toast.LENGTH_LONG).show();
    }
    
    @Override
    public void onPlatformPlaced(RectF platform) {
        updateInfoPanel();
        updateUndoRedoButtons();
    }
    
    @Override
    public void onEnemyPlaced(LevelData.EnemyData enemy) {
        updateInfoPanel();
        updateUndoRedoButtons();
    }
    
    @Override
    public void onCoinPlaced(LevelData.CoinData coin) {
        updateInfoPanel();
        updateUndoRedoButtons();
    }
    
    @Override
    public void onMushroomPlaced(LevelData.MushroomData mushroom) {
        updateInfoPanel();
        updateUndoRedoButtons();
    }
    
    @Override
    public void onGoalPlaced(LevelData.GoalData goal) {
        updateInfoPanel();
        updateUndoRedoButtons();
    }
    
    public static class LevelInfo {
        public String name;
        public String fileName;
        
        public LevelInfo(String name, String fileName) {
            this.name = name;
            this.fileName = fileName;
        }
    }
    
    public static ArrayList<LevelInfo> getCustomLevels(Context context) {
        ArrayList<LevelInfo> levels = new ArrayList<>();
        File dir = context.getFilesDir();
        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        
        if (files != null) {
            for (File file : files) {
                try {
                    FileInputStream fis = new FileInputStream(file);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();
                    
                    LevelData level = LevelData.fromJson(sb.toString());
                    levels.add(new LevelInfo(level.getName(), file.getName()));
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        return levels;
    }
}