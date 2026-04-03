package com.game.supermario;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    
    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        gameView = findViewById(R.id.gameView);

        // Get level index from intent (default to 0)
        int levelIndex = getIntent().getIntExtra("LEVEL_INDEX", 0);

        gameView.setLevelIndex(levelIndex);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        gameView.pause();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        gameView.resume();
    }
}
