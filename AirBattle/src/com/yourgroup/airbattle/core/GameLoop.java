package com.yourgroup.airbattle.core;

import javafx.animation.AnimationTimer;

/**
 * GameLoop is the central timing driver of the game.
 *
 * <p>This class uses {@link AnimationTimer} to execute the main update cycle
 * once per JavaFX frame. On each frame, it computes a delta time (dt) in seconds
 * and delegates the update step to {@link GameWorld#update(double)}.</p>
 *
 * <p>Design notes:
 * <ul>
 *   <li>The loop can be started/paused without destroying the timer instance.</li>
 *   <li>dt is clamped to avoid a large physics jump when the application loses focus.</li>
 *   <li>Keeping timing logic in one place improves maintainability and testing.</li>
 * </ul>
 * </p>
 */
public class GameLoop extends AnimationTimer {

    /** The game world that contains all entities, state, and update logic. */
    private final GameWorld world;

    /**
     * Timestamp (in nanoseconds) of the previous rendered frame.
     * A value of -1 indicates that no frame has been processed yet.
     */
    private long lastNs = -1;

    /**
     * Whether the update step should be executed.
     * When false, {@link #handle(long)} returns immediately (paused state).
     */
    private boolean running = false;

    /**
     * Creates a new GameLoop to drive updates for the given {@link GameWorld}.
     *
     * @param world the game world to update each frame (must not be null)
     */
    public GameLoop(GameWorld world) {
        this.world = world;
    }

    /**
     * Called by JavaFX every frame while this {@link AnimationTimer} is active.
     *
     * <p>Responsibilities:
     * <ol>
     *   <li>Skip updating if the loop is currently paused.</li>
     *   <li>Compute the time step (dt) in seconds since the last frame.</li>
     *   <li>Clamp dt to prevent large jumps after focus loss or frame stalls.</li>
     *   <li>Advance the game simulation by calling {@code world.update(dt)}.</li>
     * </ol>
     * </p>
     *
     * @param now current timestamp in nanoseconds provided by JavaFX
     */
    @Override
    public void handle(long now) {
        // If paused, do not advance the simulation.
        if (!running) return;

        // First frame after (re)start: initialize timing without updating the world.
        if (lastNs < 0) {
            lastNs = now;
            return;
        }

        // Convert nanoseconds delta to seconds.
        double dt = (now - lastNs) / 1_000_000_000.0;
        lastNs = now;

        // Prevent huge dt when the window loses focus or the frame rate stalls.
        // 0.033s ≈ 30 FPS time step upper bound.
        dt = Math.min(dt, 0.033);

        // Advance the game world by one time step.
        world.update(dt);
    }

    /**
     * Starts (or resumes) the game loop.
     *
     * <p>This resets the timing accumulator so that the next frame will not
     * produce an unusually large dt.</p>
     */
    public void startRunning() {
        running = true;
        lastNs = -1;
        start();
    }

    /**
     * Pauses the game loop without stopping the underlying {@link AnimationTimer}.
     *
     * <p>JavaFX will continue to call {@link #handle(long)}, but updates will be skipped
     * because {@link #running} is false.</p>
     */
    public void pauseRunning() {
        running = false;
    }

    /**
     * @return true if the game loop is currently advancing the simulation; false if paused
     */
    public boolean isRunning() {
        return running;
    }
}
