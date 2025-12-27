package com.yourgroup.airbattle.core;

import javafx.animation.AnimationTimer;

/**
 * Main game loop using JavaFX AnimationTimer.
 * The actual RUN/PAUSE is controlled by MainApp via GameState.
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

        // Defensive clamp: avoid huge dt when switching windows / lag spikes
        if (dt > 0.05) dt = 0.05;

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
