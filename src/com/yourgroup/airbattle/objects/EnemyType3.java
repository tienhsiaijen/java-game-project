package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;
import com.yourgroup.airbattle.core.GameConfig;


/**
 * Boss-type enemy with large size, high health, and horizontal patrol behavior.
 *
 * <p>{@code EnemyType3} represents a high-difficulty enemy designed to
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
public class EnemyType3 extends Enemy {

    /**
     * Horizontal movement direction.
     * A value of 1 moves right, and -1 moves left.
     */
    private double dirX = 1;

    /**
     * Creates a boss-type enemy with increased size, speed, and health.
     *
     * @param x      initial x-coordinate of the enemy
     * @param y      initial y-coordinate of the enemy
     * @param sprite image used to render the enemy
     */
    public EnemyType3(double x, double y, Image sprite) {
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
        // 1) Vertical descent.
        y += speed * dt;

        // 2) Horizontal patrol movement.
        x += dirX * 260 * dt;

        // 3) Horizontal boundary handling:
        //    Keep the boss within the visible screen bounds.
        //    This is NOT off-screen cleanup; it is the boss movement rule.
        if (x <= 0 || x + width >= GameConfig.WIDTH) {
            dirX *= -1;
        }

        // 4) Off-screen cleanup (B3 unified):
        //    Delegate boundary checking to the shared helper in GameObject.
        //    This avoids duplicating screen-size logic and keeps removal behavior
        //    consistent across all object types.
        killIfOffscreen();
    }
    @Override
    public int scoreValue() {
        return 1000;
    }
}
