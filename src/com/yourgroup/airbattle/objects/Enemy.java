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

    /* =========================
     * Defaults / Tuning Constants
     * ========================= */

    /** Default render width of a basic enemy (pixels). */
    private static final double DEFAULT_ENEMY_WIDTH = 40;

    /** Default render height of a basic enemy (pixels). */
    private static final double DEFAULT_ENEMY_HEIGHT = 40;

    /** Default downward speed for a basic enemy (pixels/second). */
    protected static final double DEFAULT_SPEED = 100;

    /** Default health points for a basic enemy. */
    protected static final int DEFAULT_HP = 1;

    /**
     * Downward movement speed in pixels per second.
     * Subclasses may override this value to represent faster or slower enemies.
     */
    protected double speed = DEFAULT_SPEED;

    /**
     * Current health points of the enemy.
     * When HP reaches zero or below, the enemy is destroyed.
     */
    protected int hp = DEFAULT_HP;

    /**
     * Creates a new enemy at the specified position.
     *
     * @param x      initial x-coordinate of the enemy
     * @param y      initial y-coordinate of the enemy
     * @param sprite image used to render the enemy
     */
    public Enemy(double x, double y, Image sprite) {
        // Use named constants rather than raw numbers to avoid magic values.
        super(x, y, DEFAULT_ENEMY_WIDTH, DEFAULT_ENEMY_HEIGHT, sprite);
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
     * beyond the visible screen area (plus an off-screen margin),
     * it is marked as dead and will be removed during the cleanup phase.</p>
     *
     * @param dt delta time in seconds since the last frame
     */
    @Override
    public void update(double dt) {
        // 1) Movement: enemies fall downward at their configured speed.
        y += speed * dt;

        // 2) Off-screen cleanup (B3 unified):
        //    Use the shared helper in GameObject instead of repeating boundary math
        //    or directly referencing GameConfig here.
        //
        //    Benefit:
        //    - One consistent rule across Bullet / Enemy / Item.
        //    - No hardcoded screen sizes or margins.
        killIfOffscreen();
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
    /**
     * Score awarded when this enemy is destroyed.
     * Subclasses override to provide different score values.
     *
     * @return score points for killing this enemy
     */
    public int scoreValue() {
        return 100; // default for basic enemies
    }
    
    /**
     * Multiplies the enemy's movement speed by a factor.
     *
     * @param factor multiplier (e.g., 1.2 means +20%)
     */
    public void multiplySpeed(double factor) {
        this.speed *= factor;
    }

    /**
     * Buffs enemy HP by a factor.
     *
     * <p>HP is kept at least its original integer value.</p>
     *
     * @param factor multiplier (e.g., 1.5 means +50%)
     */
    public void buffHp(double factor) {
        this.hp = (int) Math.max(this.hp, this.hp * factor);
    }
}
