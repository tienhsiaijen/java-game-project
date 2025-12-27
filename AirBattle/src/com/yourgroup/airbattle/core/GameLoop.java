package com.yourgroup.airbattle.core;

import javafx.animation.AnimationTimer;

/**
 * Main game loop using JavaFX AnimationTimer.
 */
public class GameLoop extends AnimationTimer {

    private final GameWorld world;
    private long lastNs = -1;
    private boolean running = false;

    public GameLoop(GameWorld world) {
        this.world = world;
    }

    @Override
    public void handle(long now) {
        if (!running) return;

        if (lastNs < 0) {
            lastNs = now;
            return;
        }

        double dt = (now - lastNs) / 1_000_000_000.0;
        lastNs = now;

        // prevent huge dt when window loses focus
        dt = Math.min(dt, 0.033);

        world.update(dt);
        
    }

    public void startRunning() {
        running = true;
        lastNs = -1;
        start();
    }

    public void pauseRunning() {
        running = false;
    }

    public boolean isRunning() {
        return running;
    }
}
