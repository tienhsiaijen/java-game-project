package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;

/**
 * Boss-type enemy with large size, high health, and horizontal patrol behavior.
 *
 * <p>{@code EnemyType2} represents a high-difficulty enemy designed to
 * challenge the player through increased durability and screen-wide movement.
 * Unlike basic enemies, it occupies more space and requires multiple hits
 * to defeat.</p>
 *
 * <p>Design highlights:
 * <ul>
 *   <li>Demonstrates inheritance by extending {@link Enemy}.</li>
 *   <li>Overrides hitbox configuration to improve collision accuracy.</li>
 *   <li>Implements distinct movement logic to differentiate boss behavior.</li>
 * </ul>
 * </p>
 */
public class EnemyType2 extends Enemy {

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
     * Creates a boss-type enemy with increased size, speed, and health.
     *
     * @param x      initial x-coordinate of the enemy
     * @param y      initial y-coordinate of the enemy
     * @param sprite image used to render the enemy
     */
    public EnemyType2(double x, double y, Image sprite) {
        super(x, y, sprite);

     // Synchronize logical size with visual size.
     // This ensures collision detection matches the visible sprite.
     setSize(120, 120);


        // Boss enemies move faster and have significantly more health.
        speed = 240;
        hp = 10;
    }

    /**
     * Reduces the horizontal hitbox size to compensate for transparent
     * padding in the sprite image.
     *
     * @return horizontal hitbox inset in pixels
     */
    @Override
    protected double hitboxInsetX() {
        return 14;
    }

    /**
     * Reduces the vertical hitbox size to compensate for transparent
     * padding in the sprite image.
     *
     * @return vertical hitbox inset in pixels
     */
    @Override
    protected double hitboxInsetY() {
        return 14;
    }

    /**
     * Updates the boss enemy's position each frame.
     *
     * <p>Movement behavior:
     * <ul>
     *   <li>Moves downward at a constant speed.</li>
     *   <li>Patrols horizontally across the screen.</li>
     *   <li>Reverses direction upon hitting the left or right screen boundary.</li>
     * </ul>
     * </p>
     *
     * <p>The enemy is removed once it leaves the visible area.</p>
     *
     * @param dt delta time in seconds since the last frame
     */
    @Override
    public void update(double dt) {
        // Vertical descent.
        y += speed * dt;

        // Horizontal patrol movement.
        x += dirX * 260 * dt;

        // Reverse direction when reaching screen edges.
        if (x <= 0 || x + width >= DEFAULT_SCREEN_WIDTH) {
            dirX *= -1;
        }

        // Remove enemy once it leaves the screen.
        if (y > DEFAULT_SCREEN_HEIGHT + 80) {
            kill();
        }
    }
}
