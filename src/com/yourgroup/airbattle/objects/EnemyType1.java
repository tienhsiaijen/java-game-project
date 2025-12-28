package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;

/**
 * Basic enemy type with simple movement behavior.
 *
 * <p>{@code EnemyType1} represents a standard enemy that moves downward
 * while slightly oscillating horizontally. This creates a predictable
 * but less static movement pattern compared to a straight-line descent.</p>
 *
 * <p>Design intent:
 * <ul>
 *   <li>Demonstrates polymorphism by overriding {@link #update(double)}.</li>
 *   <li>Provides a baseline difficulty enemy for early gameplay.</li>
 *   <li>Extends {@link Enemy} to reuse common movement and health logic.</li>
 * </ul>
 * </p>
 */
public class EnemyType1 extends Enemy {

    /**
     * Temporary screen height used for off-screen cleanup.
     * Matches the current fixed game window height.
     */
    private static final double DEFAULT_SCREEN_HEIGHT = 600;

    /**
     * Creates a basic enemy with moderate speed and low health.
     *
     * @param x      initial x-coordinate of the enemy
     * @param y      initial y-coordinate of the enemy
     * @param sprite image used to render the enemy
     */
    public EnemyType1(double x, double y, Image sprite) {
        super(x, y, sprite);
        speed = 200;
        hp = 1;
    }

    /**
     * Updates the enemy's position and movement pattern each frame.
     *
     * <p>Movement behavior:
     * <ul>
     *   <li>Moves downward at a constant speed.</li>
     *   <li>Applies a sinusoidal horizontal offset based on the current y-position,
     *       creating a gentle side-to-side motion.</li>
     * </ul>
     * </p>
     *
     * <p>The enemy is automatically removed once it leaves the screen.</p>
     *
     * @param dt delta time in seconds since the last frame
     */
    @Override
    public void update(double dt) {
        // Vertical movement.
        y += speed * dt;

        // Horizontal oscillation for more dynamic movement.
        x += Math.sin(y * 0.05) * 50 * dt;

        // Remove enemy once it leaves the visible area.
        if (y > DEFAULT_SCREEN_HEIGHT + 50) {
            kill();
        }
    }
}
