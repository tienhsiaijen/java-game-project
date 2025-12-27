package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;
import com.yourgroup.airbattle.core.ObjectType;

/**
 * Represents a projectile fired by the player.
 *
 * <p>The {@code Bullet} moves vertically upward at a constant speed and is
 * automatically removed once it leaves the visible screen area or collides
 * with another object.</p>
 *
 * <p>Design notes:
 * <ul>
 *   <li>This class extends {@link GameObject}, inheriting position, size,
 *       rendering, and life-cycle management.</li>
 *   <li>The bullet does not manage collision logic directly; instead,
 *       collisions are resolved centrally in {@code GameWorld}.</li>
 *   <li>Off-screen cleanup is handled defensively to prevent unnecessary
 *       memory usage.</li>
 * </ul>
 * </p>
 */
public class Bullet extends GameObject {

    /**
     * Vertical movement speed of the bullet in pixels per second.
     * A higher value results in faster projectile travel.
     */
    private double speed = 400;

    /**
     * Temporary screen height used for off-screen cleanup.
     *
     * <p>This value matches the current game window height (900x600).
     * It can be replaced by a dynamic world height query in future
     * iterations to improve flexibility.</p>
     */
    private static final double SCREEN_H = 600;

    /**
     * Creates a new player bullet at the specified position.
     *
     * @param x      initial x-coordinate of the bullet
     * @param y      initial y-coordinate of the bullet
     * @param sprite image used to render the bullet
     */
    public Bullet(double x, double y, Image sprite) {
        super(x, y, 6, 12, sprite);
    }

    /**
     * @return the logical object type used for collision filtering
     */
    @Override
    public ObjectType getType() {
        return ObjectType.BULLET_PLAYER;
    }

    /**
     * Updates the bullet's position each frame.
     *
     * <p>The bullet travels upward along the y-axis. When it moves outside
     * the visible screen bounds (with a small margin), it is marked as dead
     * and removed during the cleanup phase.</p>
     *
     * @param dt delta time in seconds since the last frame
     */
    @Override
    public void update(double dt) {
        // Move the bullet upward at a constant speed.
        y -= speed * dt;

        // Remove the bullet once it leaves the screen bounds.
        if (y + height < -30 || y > SCREEN_H + 30) {
            kill();
        }
    }
}
