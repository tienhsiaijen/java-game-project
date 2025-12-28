package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;
import com.yourgroup.airbattle.core.ObjectType;

/**
 * Represents a projectile fired by the player.
 *
 * <p>The {@code Bullet} moves vertically upward (and optionally horizontally)
 * and is automatically removed once it leaves the visible screen area or
 * collides with another object.</p>
 *
 * <p>Design notes:
 * <ul>
 * <li>This class extends {@link GameObject}, inheriting position, size,
 * rendering, and life-cycle management.</li>
 * <li>The bullet does not manage collision logic directly; instead,
 * collisions are resolved centrally in {@code GameWorld}.</li>
 * <li>Off-screen cleanup is handled defensively to prevent unnecessary
 * memory usage.</li>
 * </ul>
 * </p>
 */
public class Bullet extends GameObject {

    /**
     * Vertical movement speed of the bullet in pixels per second.
     * A higher value results in faster upward travel.
     */
    private double speedY = 400;

    /**
     * Horizontal movement speed of the bullet in pixels per second.
     * Used for shotgun effects (angled shots).
     */
    private double speedX = 0;

    /**
     * The damage value of this bullet.
     * Standard bullets deal 1 damage; Super bullets deal higher damage.
     */
    private int damage = 1;

    /**
     * Temporary screen height used for off-screen cleanup.
     * Matches the current game window height (900x600).
     */
    private static final double OFFSCREEN_Y_LIMIT = 600;

    /**
     * Creates a standard player bullet moving straight up.
     *
     * @param x      initial x-coordinate of the bullet
     * @param y      initial y-coordinate of the bullet
     * @param sprite image used to render the bullet
     */
    public Bullet(double x, double y, Image sprite) {
        super(x, y, 6, 12, sprite);
    }

    /**
     * Creates a custom bullet with specific size, damage, and trajectory.
     *
     * <p>This constructor is used for special weapon effects like
     * Super Bullets (larger size/damage) or Shotgun (angled trajectory).</p>
     *
     * @param x      initial x-coordinate
     * @param y      initial y-coordinate
     * @param width  render width
     * @param height render height
     * @param sprite image used to render the bullet
     * @param damage damage value applied to enemies
     * @param speedX horizontal speed (negative = left, positive = right)
     */
    public Bullet(double x, double y, double width, double height, Image sprite, int damage, double speedX) {
        super(x, y, width, height, sprite);
        this.damage = damage;
        this.speedX = speedX;
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
     * <p>The bullet travels based on its vertical and horizontal speed.
     * When it moves outside the visible screen bounds (with a small margin),
     * it is marked as dead and removed during the cleanup phase.</p>
     *
     * @param dt delta time in seconds since the last frame
     */
    @Override
    public void update(double dt) {
        // Move the bullet based on its velocity vector.
        y -= speedY * dt;
        x += speedX * dt;

        // Remove the bullet once it leaves the screen bounds (vertical or horizontal).
        if (y + height < -30 || y > OFFSCREEN_Y_LIMIT + 30 || x < -50 || x > 950) {
            kill();
        }
    }

    /**
     * @return the damage value of this bullet
     */
    public int getDamage() {
        return damage;
    }
}