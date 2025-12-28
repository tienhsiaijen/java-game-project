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
     * <p>The enemy is automatically removed once it leaves the screen
     * (with an off-screen margin).</p>
     *
     * @param dt delta time in seconds since the last frame
     */
    @Override
    public void update(double dt) {
        // 1) Vertical movement.
        y += speed * dt;

        // 2) Horizontal oscillation for more dynamic movement.
        x += Math.sin(y * 0.05) * 50 * dt;

        // 3) Off-screen cleanup (B3 unified):
        //    Delegate boundary checking to the shared helper in GameObject.
        //    This avoids duplicating screen-size logic and keeps removal
        //    behavior consistent across all game objects.
        killIfOffscreen();
    }
}

