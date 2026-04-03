# Super Mario Android Game

A Super Mario-style platformer game for Android with 3 levels.

## Features

- **3 Unique Levels:**
  1. Level 1: Green Hills - Easy introduction level
  2. Level 2: Mountain Peak - Medium difficulty with vertical climbing
  3. Level 3: Castle Challenge - Hard level with precise jumps

- **Gameplay:**
  - Touch controls (left/middle/right side of screen)
  - Jump on enemies to defeat them
  - Avoid or jump over enemies
  - Reach the golden goal to complete each level
  - 3 lives system

- **Enemies:**
  - Goomba-like enemies (brown, walk back and forth)
  - Koopa-like enemies (green turtles, patrol platforms)

## Controls

- **Left side of screen:** Move left
- **Middle of screen:** Move right  
- **Right side of screen:** Jump

## How to Build

1. Open the project in Android Studio
2. Sync Gradle files
3. Build and run on an Android device or emulator

## Project Structure

```
SuperMario/
├── app/
│   ├── src/main/
│   │   ├── java/com/game/supermario/
│   │   │   ├── MainActivity.java      # Main activity
│   │   │   ├── GameView.java          # SurfaceView for rendering
│   │   │   ├── GameEngine.java        # Game logic and state
│   │   │   ├── Player.java            # Player character
│   │   │   ├── Enemy.java             # Enemy characters
│   │   │   ├── Level.java             # Level data structure
│   │   │   └── LevelCreator.java      # 3 level designs
│   │   ├── res/
│   │   │   ├── values/
│   │   │   │   ├── strings.xml
│   │   │   │   └── themes.xml
│   │   │   └── layout/
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── build.gradle
└── settings.gradle
```

## Requirements

- Android SDK 24+
- Android Studio Arctic Fox or newer
