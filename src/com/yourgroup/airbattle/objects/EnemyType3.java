package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;

/**
 * Fast-moving enemy type with low health.
 *
 * <p>{@code EnemyType3} is designed to pressure the player through
 * high movement speed rather than durability. It moves quickly both
 * vertically and horizontally, making it harder to track and hit,
 * despite having only a single health point.</p>
 *
 * <p>Design intent:
 * <ul>
 *   <li>Demonstrates polymorphism by overriding {@link #update(double)}.</li>
 *   <li>Balances difficulty using speed instead of health.</li>
 *   <li>Provides gameplay variety alongside slower or stronger enemy types.</li>
 * </ul>
 * </p>
 */
public class EnemyType3 extends Enemy {

    /**
     * Horizontal movement direction.
     * A value of 1 moves right, and -1 moves left.
     */
    private double dirX = 1;

    /** Fixed screen width used for horizontal boundary checks. */
    private static final double DEFAULT_SCREEN_WIDTH = 900;

    /** Fixed screen height used for off-screen cleanup. */
    private static final double DEFAULT_SCREEN_HEIGHT = 600;

    /**
     * Creates a fast enemy with high speed and minimal health.
     *
     * @param x      initial x-coordinate of the enemy
     * @param y      initial y-coordinate of the enemy
     * @param sprite image used to render the enemy
     */
    public EnemyType3(double x, double y, Image sprite) {
        super(x, y, sprite);

        // Fast descent speed increases reaction difficulty for the player.
        speed = 240;

        // Low HP compensates for the high movement speed.
        hp = 1;
    }

    /**
     * Updates the enemy's position each frame.
     *
     * <p>Movement behavior:
     * <ul>
     *   <li>Rapid downward movement.</li>
     *   <li>Fast horizontal patrol across the screen.</li>
     *   <li>Direction reversal at screen boundaries.</li>
     * </ul>
     * </p>
     *
     * <p>The enemy is removed once it leaves the visible area.</p>
     *
     * @param dt delta time in seconds since the last frame
     */
    @Override
    public void update(double dt) {
        // Vertical movement.
        y += speed * dt;

        // Fast horizontal movement.
        x += dirX * 260 * dt;

        // Reverse direction at screen edges.
        if (x <= 0 || x + width >= DEFAULT_SCREEN_WIDTH) {
            dirX *= -1;
        }

        // Remove enemy once it leaves the screen.
        if (y > DEFAULT_SCREEN_HEIGHT + 50) {
            kill();
        }
    }
}
