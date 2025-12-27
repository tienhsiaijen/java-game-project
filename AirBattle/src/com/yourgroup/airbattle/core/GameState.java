package com.yourgroup.airbattle.core;

/**
 * Represents the high-level state of the game.
 *
 * <p>This enum is used to control the overall flow of the application,
 * including which screen is displayed and how user input and updates
 * are processed.</p>
 *
 * <p>Typical state transitions:
 * <ul>
 *   <li>{@link #MENU} → {@link #RUNNING} : Player starts the game</li>
 *   <li>{@link #RUNNING} → {@link #PAUSED} : Player pauses the game</li>
 *   <li>{@link #PAUSED} → {@link #RUNNING} : Player resumes the game</li>
 *   <li>{@link #RUNNING} → {@link #GAME_OVER} : Win or lose condition reached</li>
 * </ul>
 * </p>
 *
 * <p>Using an enum for game states improves readability, prevents
 * invalid states, and simplifies state-based logic in the game loop
 * and UI controllers.</p>
 */
public enum GameState {

    /** Initial state shown when the application launches. */
    MENU,

    /** Active gameplay state where the game world is updated each frame. */
    RUNNING,

    /** Temporary halt state where the game world is frozen but not reset. */
    PAUSED,

    /** Terminal state reached when a win or lose condition is met. */
    GAME_OVER
}
