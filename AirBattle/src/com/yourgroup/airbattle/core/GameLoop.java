package com.yourgroup.airbattle.core;

import javafx.animation.AnimationTimer;

/**
 * Main game loop using JavaFX AnimationTimer.
 *
 * <p>This loop provides a fixed update entry point driven by JavaFX's pulse.
 * The loop can be started and paused. When paused, the underlying timer is
 * stopped to avoid unnecessary per-frame callbacks.</p>
 */
public class GameLoop extends AnimationTimer {

    private final GameWorld world;

    /** Last frame timestamp in nanoseconds. */
    private long lastNs = -1;

    /** Whether the game should update on each frame. */
    private boolean running = false;

    public GameLoop(GameWorld world) {
        this.world = world;
    }

    @Override
    public void handle(long now) {
        if (!running) {
            return;
        }

        // First frame after (re)start: initialize lastNs.
        if (lastNs < 0) {
            lastNs = now;
            return;
        }

        double dt = (now - lastNs) / 1_000_000_000.0;
        lastNs = now;

        // Prevent huge dt when the window loses focus or the app stalls.
        dt = Math.min(dt, 0.033);

        world.update(dt);
    }

    /**
     * Starts or resumes the loop.
     *
     * <p>Resets the internal time accumulator to avoid a large dt spike on resume.</p>
     */
    public void startRunning() {
        running = true;
        lastNs = -1;
        start(); // Start the underlying AnimationTimer
    }

    /**
     * Pauses the loop.
     *
     * <p>Stops the underlying AnimationTimer to avoid per-frame callbacks while paused.</p>
     */
    public void pauseRunning() {
        running = false;
        stop(); // Stop the underlying AnimationTimer
    }

    public boolean isRunning() {
        return running;
    }
}

