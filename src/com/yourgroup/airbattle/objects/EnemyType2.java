package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;
import com.yourgroup.airbattle.core.GameConfig;

/**
 * Fast-moving enemy type with low health.
 *
 * <p>{@code EnemyType2} is designed to pressure the player through
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
public class EnemyType2 extends Enemy {

    /**
     * Horizontal movement direction.
     * A value of 1 moves right, and -1 moves left.
     */
    private double dirX = 1;

    /**
     * Creates a fast enemy with high speed and minimal health.
     *
     * @param x      initial x-coordinate of the enemy
     * @param y      initial y-coordinate of the enemy
     * @param sprite image used to render the enemy
     */
    public EnemyType2(double x, double y, Image sprite) {
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
     * <p>The enemy is removed once it leaves the visible area
     * (with an off-screen margin).</p>
     *
     * @param dt delta time in seconds since the last frame
     */
    @Override
    public void update(double dt) {
        // 1) Vertical movement.
        y += speed * dt;

        // 2) Fast horizontal movement.
        x += dirX * 260 * dt;

        // 3) Reverse direction at screen edges.
        //    This is movement logic, NOT off-screen cleanup.
        if (x <= 0 || x + width >= GameConfig.WIDTH) {
            dirX *= -1;
        }

        // 4) Off-screen cleanup (B3 unified):
        //    Delegate boundary checking to the shared helper in GameObject.
        //    This keeps off-screen removal logic consistent across
        //    Bullet / Enemy / Item and avoids direct screen-size checks here.
        killIfOffscreen();
    }
    @Override
    public int scoreValue() {
        return 150;
    }
}
