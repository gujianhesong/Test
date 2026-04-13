package com.game.bomberman;

import java.util.Random;

public class LevelManager {
    private int currentLevel;
    private int totalLevels;

    public LevelManager() {
        this.currentLevel = 1;
        this.totalLevels = 10; // 10 levels total
    }

    public void generateLevel(int[][] grid, int level) {
        int height = grid.length;
        int width = grid[0].length;

        // Clear grid
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                grid[y][x] = 0; // Empty
            }
        }

        switch (level) {
            case 1:
                generateLevel1(grid, width, height);
                break;
            case 2:
                generateLevel2(grid, width, height);
                break;
            case 3:
                generateLevel3(grid, width, height);
                break;
            case 4:
                generateLevel4(grid, width, height);
                break;
            case 5:
                generateLevel5(grid, width, height);
                break;
            case 6:
                generateLevel6(grid, width, height);
                break;
            case 7:
                generateLevel7(grid, width, height);
                break;
            case 8:
                generateLevel8(grid, width, height);
                break;
            case 9:
                generateLevel9(grid, width, height);
                break;
            case 10:
                generateLevel10(grid, width, height);
                break;
            default:
                // For levels beyond 10, use procedural generation
                generateRandomLevel(grid, width, height, level);
                break;
        }
    }

    private void generateLevel1(int[][] grid, int width, int height) {
        // Easy level - Open layout with few walls
        createBorders(grid, width, height);
        createFixedWalls(grid, width, height, 2);

        // Fewer breakable walls
        Random rand = new Random(100 + currentLevel);
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (grid[y][x] == 0 && rand.nextDouble() < 0.2) {
                    if (!(x <= 2 && y <= 2)) {
                        grid[y][x] = 2; // Breakable wall
                    }
                }
            }
        }
    }

    private void generateLevel2(int[][] grid, int width, int height) {
        // Medium difficulty - More walls, maze-like
        createBorders(grid, width, height);
        createFixedWalls(grid, width, height, 2);

        // Add horizontal corridors
        for (int y = 3; y < height - 1; y += 4) {
            for (int x = 1; x < width - 1; x++) {
                if (grid[y][x] == 0) {
                    grid[y][x] = 0; // Keep open
                }
            }
        }

        Random rand = new Random(200 + currentLevel);
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (grid[y][x] == 0 && rand.nextDouble() < 0.35) {
                    if (!(x <= 2 && y <= 2)) {
                        grid[y][x] = 2;
                    }
                }
            }
        }
    }

    private void generateLevel3(int[][] grid, int width, int height) {
        // Hard level - Complex maze
        createBorders(grid, width, height);
        createFixedWalls(grid, width, height, 2);

        // Create rooms with passages
        Random rand = new Random(300 + currentLevel);
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (grid[y][x] == 0 && rand.nextDouble() < 0.4) {
                    if (!(x <= 2 && y <= 2)) {
                        grid[y][x] = 2;
                    }
                }
            }
        }

        // Ensure some paths exist
        createPath(grid, width, height, 1, 1, width - 2, height - 2);
    }

    private void generateLevel4(int[][] grid, int width, int height) {
        // Very hard - Dense walls with strategic openings
        createBorders(grid, width, height);
        createFixedWalls(grid, width, height, 2);

        // Create pattern of walls
        for (int y = 2; y < height - 2; y++) {
            for (int x = 2; x < width - 2; x++) {
                if ((x + y) % 3 == 0 && grid[y][x] == 0) {
                    grid[y][x] = 1; // Fixed wall
                }
            }
        }

        Random rand = new Random(400 + currentLevel);
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (grid[y][x] == 0 && rand.nextDouble() < 0.45) {
                    if (!(x <= 2 && y <= 2)) {
                        grid[y][x] = 2;
                    }
                }
            }
        }
    }

    private void generateLevel5(int[][] grid, int width, int height) {
        // Expert level - Arena style with obstacles
        createBorders(grid, width, height);

        // Create arena with pillars
        for (int y = 2; y < height - 1; y += 3) {
            for (int x = 2; x < width - 1; x += 3) {
                if (y < height - 1 && x < width - 1) {
                    grid[y][x] = 1; // Fixed wall pillar
                }
            }
        }

        Random rand = new Random(500 + currentLevel);
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (grid[y][x] == 0 && rand.nextDouble() < 0.3) {
                    if (!(x <= 2 && y <= 2)) {
                        grid[y][x] = 2;
                    }
                }
            }
        }
    }

    private void generateLevel6(int[][] grid, int width, int height) {
        // Cross pattern - walls form a cross
        createBorders(grid, width, height);

        int centerX = width / 2;
        int centerY = height / 2;

        // Horizontal line
        for (int x = 1; x < width - 1; x++) {
            if (Math.abs(x - centerX) > 2) {
                grid[centerY][x] = 1;
            }
        }

        // Vertical line
        for (int y = 1; y < height - 1; y++) {
            if (Math.abs(y - centerY) > 2) {
                grid[y][centerX] = 1;
            }
        }

        Random rand = new Random(600);
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (grid[y][x] == 0 && rand.nextDouble() < 0.35) {
                    if (!(x <= 2 && y <= 2)) {
                        grid[y][x] = 2;
                    }
                }
            }
        }
    }

    private void generateLevel7(int[][] grid, int width, int height) {
        // Checkerboard pattern
        createBorders(grid, width, height);

        for (int y = 2; y < height - 1; y++) {
            for (int x = 2; x < width - 1; x++) {
                if ((x + y) % 2 == 0) {
                    grid[y][x] = 1;
                }
            }
        }

        Random rand = new Random(700);
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (grid[y][x] == 0 && rand.nextDouble() < 0.35) {
                    if (!(x <= 2 && y <= 2)) {
                        grid[y][x] = 2;
                    }
                }
            }
        }
    }

    private void generateLevel8(int[][] grid, int width, int height) {
        // Fortress - heavily fortified center
        createBorders(grid, width, height);

        // Outer ring
        for (int x = 2; x < width - 2; x++) {
            grid[3][x] = 1;
            grid[height - 4][x] = 1;
        }
        for (int y = 3; y < height - 3; y++) {
            grid[y][3] = 1;
            grid[y][width - 4] = 1;
        }

        // Inner fortress
        int centerX = width / 2;
        int centerY = height / 2;
        for (int y = centerY - 2; y <= centerY + 2; y++) {
            for (int x = centerX - 2; x <= centerX + 2; x++) {
                if (y >= 1 && y < height - 1 && x >= 1 && x < width - 1) {
                    grid[y][x] = 1;
                }
            }
        }

        Random rand = new Random(800);
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (grid[y][x] == 0 && rand.nextDouble() < 0.4) {
                    if (!(x <= 2 && y <= 2)) {
                        grid[y][x] = 2;
                    }
                }
            }
        }
    }

    private void generateLevel9(int[][] grid, int width, int height) {
        // Zigzag corridors
        createBorders(grid, width, height);

        for (int y = 2; y < height - 2; y += 3) {
            if ((y / 3) % 2 == 0) {
                for (int x = 2; x < width - 2; x++) {
                    grid[y][x] = 1;
                }
            } else {
                grid[y][1] = 1;
                grid[y][width - 2] = 1;
            }
        }

        Random rand = new Random(900);
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (grid[y][x] == 0 && rand.nextDouble() < 0.4) {
                    if (!(x <= 2 && y <= 2)) {
                        grid[y][x] = 2;
                    }
                }
            }
        }
    }

    private void generateLevel10(int[][] grid, int width, int height) {
        // Final challenge - complex maze with high density
        createBorders(grid, width, height);
        createFixedWalls(grid, width, height, 2);

        // Very dense breakable walls
        Random rand = new Random(1000);
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (grid[y][x] == 0 && rand.nextDouble() < 0.5) {
                    if (!(x <= 2 && y <= 2)) {
                        grid[y][x] = 2;
                    }
                }
            }
        }

        // Ensure path exists
        createPath(grid, width, height, 1, 1, width - 2, height - 2);
    }

    private void generateRandomLevel(int[][] grid, int width, int height, int level) {
        // Procedurally generated levels for level 6+
        createBorders(grid, width, height);
        createFixedWalls(grid, width, height, 2);

        Random rand = new Random(level * 100);
        double wallDensity = Math.min(0.3 + (level - 5) * 0.05, 0.6);

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                if (grid[y][x] == 0 && rand.nextDouble() < wallDensity) {
                    if (!(x <= 2 && y <= 2)) {
                        grid[y][x] = 2;
                    }
                }
            }
        }

        // Ensure path exists
        createPath(grid, width, height, 1, 1, width - 2, height - 2);
    }

    private void createBorders(int[][] grid, int width, int height) {
        for (int x = 0; x < width; x++) {
            grid[0][x] = 1;
            grid[height - 1][x] = 1;
        }
        for (int y = 0; y < height; y++) {
            grid[y][0] = 1;
            grid[y][width - 1] = 1;
        }
    }

    private void createFixedWalls(int[][] grid, int width, int height, int spacing) {
        for (int y = spacing; y < height - spacing; y += spacing * 2) {
            for (int x = spacing; x < width - spacing; x += spacing * 2) {
                grid[y][x] = 1;
            }
        }
    }

    private void createPath(int[][] grid, int width, int height, int startX, int startY, int endX, int endY) {
        // Simple path carving using random walk
        int x = startX;
        int y = startY;
        Random rand = new Random();

        while (x != endX || y != endY) {
            grid[y][x] = 0; // Clear path

            // Move towards goal with some randomness
            if (rand.nextDouble() < 0.6) {
                // Move towards goal
                if (x < endX && (rand.nextBoolean() || y == endY)) {
                    x++;
                } else if (x > endX) {
                    x--;
                } else if (y < endY) {
                    y++;
                } else if (y > endY) {
                    y--;
                }
            } else {
                // Random movement
                int dir = rand.nextInt(4);
                switch (dir) {
                    case 0: if (x < width - 2) x++; break;
                    case 1: if (x > 1) x--; break;
                    case 2: if (y < height - 2) y++; break;
                    case 3: if (y > 1) y--; break;
                }
            }

            // Safety check
            if (x < 1) x = 1;
            if (x >= width - 1) x = width - 2;
            if (y < 1) y = 1;
            if (y >= height - 1) y = height - 2;
        }
        grid[endY][endX] = 0;
    }

    public int getEnemyCount(int level) {
        // Increase enemies with level
        return Math.min(5 + level*2, 20);
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void nextLevel() {
        currentLevel++;
    }

    public void reset() {
        currentLevel = 1;
    }
}
