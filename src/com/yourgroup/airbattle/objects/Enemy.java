package com.yourgroup.airbattle.objects;

import com.yourgroup.airbattle.core.ObjectType;
import javafx.scene.image.Image;

/**
 * Base class for all enemy entities in the game.
 *
 * <p>This class defines shared behavior and attributes for enemies,
 * including movement, health management, and off-screen cleanup.
 * Specific enemy variants extend this class to customize speed,
 * health, size, or behavior.</p>
 *
 * <p>Design intent:
 * <ul>
 *   <li>Encapsulates common enemy logic to avoid duplication.</li>
 *   <li>Supports polymorphism through subclassing (e.g. different enemy types).</li>
 *   <li>Allows game rules to treat all enemies uniformly via {@link ObjectType#ENEMY}.</li>
 * </ul>
 * </p>
 */
public class Enemy extends GameObject {

    /**
     * Downward movement speed in pixels per second.
     * Subclasses may override this value to represent faster or slower enemies.
     */
    protected double speed = 100;

    /**
     * Current health points of the enemy.
     * When HP reaches zero or below, the enemy is destroyed.
     */
    protected int hp = 1;

    /**
     * Temporary screen height used to determine when the enemy
     * has moved completely off-screen.
     */
    private static final double DEFAULT_DEFAULT_SCREEN_HEIGHTEIGHT = 600;

    /**
     * Creates a new enemy at the specified position.
     *
     * @param x      initial x-coordinate of the enemy
     * @param y      initial y-coordinate of the enemy
     * @param sprite image used to render the enemy
     */
    public Enemy(double x, double y, Image sprite) {
        super(x, y, 40, 40, sprite);
    }

    /**
     * @return the logical object type used for collision filtering
     */
    @Override
    public ObjectType getType() {
        return ObjectType.ENEMY;
    }

    /**
     * Updates the enemy's position each frame.
     *
     * <p>The enemy moves downward at a constant speed. If it travels
     * beyond the visible screen area, it is marked as dead and will be
     * removed during the cleanup phase.</p>
     *
     * @param dt delta time in seconds since the last frame
     */
    @Override
    public void update(double dt) {
        // Move the enemy downward.
        y += speed * dt;

        // Remove enemy once it leaves the screen.
        if (y > DEFAULT_DEFAULT_SCREEN_HEIGHTEIGHT + 50) {
            kill();
        }
    }

    /**
     * Applies damage to the enemy.
     *
     * <p>This method reduces the enemy's HP and automatically destroys
     * the enemy when HP reaches zero or below.</p>
     *
     * @param amount amount of damage to apply
     */
    public void damage(int amount) {
        hp -= amount;
        if (hp <= 0) {
            kill();
        }
    }

    /**
     * @return current health points of the enemy
     */
    public int getHp() {
        return hp;
    }
}
