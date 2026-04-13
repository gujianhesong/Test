package com.game.supermario;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Level selection screen for Super Mario game
 */
public class LevelSelectActivity extends AppCompatActivity {
    private boolean isRamdonMode = false;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_select);

        // Setup level editor button
        Button editorButton = findViewById(R.id.editorButton);
        editorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openLevelEditor();
            }
        });

        Button ramdonModeButton = findViewById(R.id.ramdonModeButton);
        if (isRamdonMode) {
            ramdonModeButton.setText("RAMDON MODE");
        } else {
            ramdonModeButton.setText("NORMAL MODE");
        }
        ramdonModeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRamdonMode = !isRamdonMode;
                if (isRamdonMode) {
                    ramdonModeButton.setText("RAMDON MODE");
                    editorButton.setVisibility(View.GONE);
                } else {
                    ramdonModeButton.setText("NORMAL MODE");
                    editorButton.setVisibility(View.VISIBLE);
                }
            }
        });
        
        // Setup all 30 level buttons
        setupLevelButton(R.id.level1Button, 0);
        setupLevelButton(R.id.level2Button, 1);
        setupLevelButton(R.id.level3Button, 2);
        setupLevelButton(R.id.level4Button, 3);
        setupLevelButton(R.id.level5Button, 4);
        setupLevelButton(R.id.level6Button, 5);
        setupLevelButton(R.id.level7Button, 6);
        setupLevelButton(R.id.level8Button, 7);
        setupLevelButton(R.id.level9Button, 8);
        setupLevelButton(R.id.level10Button, 9);
        setupLevelButton(R.id.level11Button, 10);
        setupLevelButton(R.id.level12Button, 11);
        setupLevelButton(R.id.level13Button, 12);
        setupLevelButton(R.id.level14Button, 13);
        setupLevelButton(R.id.level15Button, 14);
        setupLevelButton(R.id.level16Button, 15);
        setupLevelButton(R.id.level17Button, 16);
        setupLevelButton(R.id.level18Button, 17);
        setupLevelButton(R.id.level19Button, 18);
        setupLevelButton(R.id.level20Button, 19);
        setupLevelButton(R.id.level21Button, 20);
        setupLevelButton(R.id.level22Button, 21);
        setupLevelButton(R.id.level23Button, 22);
        setupLevelButton(R.id.level24Button, 23);
        setupLevelButton(R.id.level25Button, 24);
        setupLevelButton(R.id.level26Button, 25);
        setupLevelButton(R.id.level27Button, 26);
        setupLevelButton(R.id.level28Button, 27);
        setupLevelButton(R.id.level29Button, 28);
        setupLevelButton(R.id.level30Button, 29);
        
        // Back button
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });
    }

    private void setupLevelButton(int buttonId, final int levelIndex) {
        Button button = findViewById(buttonId);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGame(isRamdonMode, levelIndex);
            }
        });

        // Add long click to edit level
        button.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                editLevel(levelIndex);
                return true;
            }
        });
    }

    private void startGame(boolean isRamdonMode, int levelIndex) {
        Intent intent = new Intent(LevelSelectActivity.this, MainActivity.class);
        intent.putExtra("LEVEL_INDEX", levelIndex);
        intent.putExtra("RAMDON_MODE", isRamdonMode);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void openLevelEditor() {
        // Open editor without specific level (defaults to level 1)
        Intent intent = new Intent(LevelSelectActivity.this, LevelEditorActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void editLevel(int levelIndex) {
        // Open editor with specific level number (1-based)
        int levelNumber = levelIndex + 1;
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Edit Level " + levelNumber)
                .setMessage("Do you want to edit Level " + levelNumber + "?")
                .setPositiveButton("Edit", (dialog, which) -> {
                    Intent intent = new Intent(LevelSelectActivity.this, LevelEditorActivity.class);
                    intent.putExtra("EDIT_LEVEL", levelNumber);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
