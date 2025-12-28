package com.yourgroup.airbattle.core;

import javafx.animation.AnimationTimer;

/**
 * Runs the per-frame update loop using JavaFX {@link AnimationTimer}.
 *
 * <p>This class is intentionally lightweight: it only calculates delta time (dt)
 * and delegates the actual game logic update to {@link GameWorld#update(double)}.
 *
 * <p>Start/Pause behavior is controlled externally (e.g., by MainApp via GameState),
 * while this loop maintains a simple {@code running} flag to ignore frames when paused.
 */
public class GameLoop extends AnimationTimer {

    /** The game world that owns and updates all game entities each frame. */
    private final GameWorld world;

    /** The timestamp (ns) of the previous frame, used for computing delta time. */
    private long lastNs = -1;

    /** Whether the loop should process frames (true) or ignore them (false). */
    private boolean running = false;

    /**
     * Creates a game loop bound to the given {@link GameWorld}.
     *
     * @param world the game world to update every frame; must not be null
     */
    public GameLoop(GameWorld world) {
        this.world = world;
    }

    /**
     * Called by JavaFX every frame while the AnimationTimer is started.
     * Computes delta time and updates the game world when the loop is running.
     *
     * @param now current timestamp in nanoseconds provided by JavaFX
     */
    @Override
    public void handle(long now) {
        // When paused, ignore frame callbacks without stopping the timer.
        if (!running) return;

        // First frame after starting/resuming: initialize time base and skip update.
        if (lastNs < 0) {
            lastNs = now;
            return;
        }

        // Convert nanoseconds to seconds for frame-based movement/logic updates.
        double dt = (now - lastNs) / 1_000_000_000.0;
        lastNs = now;

        // Defensive clamp: avoid huge dt values due to window switching or lag spikes.
        // This prevents objects from "teleporting" and keeps physics/collisions stable.
        if (dt > 0.05) dt = 0.05;

        world.update(dt);
    }

    /**
     * Starts (or resumes) processing frames.
     * Resets the previous timestamp so dt is re-initialized on the next frame.
     */
    public void startRunning() {
        running = true;
        lastNs = -1;
        start();
    }

    /**
     * Pauses processing frames without stopping the underlying AnimationTimer.
     * The timer keeps firing, but {@link #handle(long)} returns immediately.
     */
    public void pauseRunning() {
        running = false;
    }

    /**
     * @return true if the game loop is currently processing updates; false otherwise
     */
    public boolean isRunning() {
        return running;
    }
}

