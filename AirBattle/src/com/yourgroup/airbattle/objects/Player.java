package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;
import com.yourgroup.airbattle.input.InputHandler;
import com.yourgroup.airbattle.core.ObjectType;

/**
 * Player-controlled aircraft entity.
 *
 * <p>{@code Player} handles:
 * <ul>
 *   <li>Movement based on keyboard input (via {@link InputHandler})</li>
 *   <li>Health (HP) and damage/healing rules</li>
 *   <li>Shooting bullets with a cooldown timer</li>
 *   <li>Temporary power-up effects (speed boost / fire-rate boost)</li>
 * </ul>
 * </p>
 *
 * <p>Design notes:
 * <ul>
 *   <li>Input is injected through the constructor to keep the player decoupled
 *       from JavaFX event handling (better modularity/testability).</li>
 *   <li>Combat timing is frame-rate independent by using {@code dt} timers.</li>
 *   <li>Hitbox is slightly inset to avoid unfair collisions caused by sprite padding.</li>
 * </ul>
 * </p>
 */
public class Player extends GameObject {

    /** Current health points of the player. Reaching 0 triggers death. */
    private int hp = 3;

    /** Current movement speed in pixels per second (may be boosted temporarily). */
    private double speed = 220; // px/s

    /**
     * Current firing cooldown in seconds (time between shots).
     * May be reduced temporarily by a fire-rate boost.
     */
    private double fireCooldown = 0.25;

    /** Remaining time (seconds) before the next shot is allowed. */
    private double fireTimer = 0;

    /** Remaining duration (seconds) of the fire-rate boost effect. */
    private double fireBoostTimer = 0;

    /** Default cooldown value used to restore normal fire rate when boost ends. */
    private final double baseFireCooldown = 0.25;

    /** Default movement speed used to restore normal speed when boost ends. */
    private final double baseSpeed = 220;

    /** Remaining duration (seconds) of the speed boost effect. */
    private double speedBoostTimer = 0;

    /** Input provider used to query movement and firing actions. */
    private final InputHandler input;

    /**
     * Creates a player entity at the specified position.
     *
     * @param x      initial x-coordinate of the player (top-left)
     * @param y      initial y-coordinate of the player (top-left)
     * @param sprite image used to render the player
     * @param input  input handler used to query player controls
     */
    public Player(double x, double y, Image sprite, InputHandler input) {
        super(x, y, 40, 40, sprite);
        this.input = input;
    }

    /**
     * @return the logical object type used for collision filtering
     */
    @Override
    public ObjectType getType() {
        return ObjectType.PLAYER;
    }

    /**
     * Shrinks the horizontal hitbox to better match the visible aircraft shape.
     * This reduces "unfair" collisions caused by transparent sprite borders.
     *
     * @return horizontal inset in pixels
     */
    @Override
    protected double hitboxInsetX() { return 6; }

    /**
     * Shrinks the vertical hitbox to better match the visible aircraft shape.
     *
     * @return vertical inset in pixels
     */
    @Override
    protected double hitboxInsetY() { return 6; }

    /**
     * Updates player logic each frame.
     *
     * <p>This method:
     * <ul>
     *   <li>Updates the firing cooldown timer</li>
     *   <li>Applies and expires temporary boost effects</li>
     *   <li>Moves the player based on current input state</li>
     * </ul>
     * </p>
     *
     * @param dt delta time in seconds since the last frame
     */
    @Override
    public void update(double dt) {
        // Cooldown timer for shooting (counts down to 0).
        fireTimer = Math.max(0, fireTimer - dt);

        // Speed boost duration management.
        if (speedBoostTimer > 0) {
            speedBoostTimer -= dt;
            if (speedBoostTimer <= 0) {
                speed = baseSpeed;
            }
        }

        // Fire-rate boost duration management.
        if (fireBoostTimer > 0) {
            fireBoostTimer -= dt;
            if (fireBoostTimer <= 0) {
                fireCooldown = baseFireCooldown;
            }
        }

        // Movement input handling (supports multiple simultaneous keys).
        if (input.left())  moveLeft(dt);
        if (input.right()) moveRight(dt);
        if (input.up())    moveUp(dt);
        if (input.down())  moveDown(dt);
    }

    /**
     * Clamps the player position so it cannot leave the playfield.
     *
     * @param maxWidth  width of the playable area in pixels
     * @param maxHeight height of the playable area in pixels
     */
    public void clampTo(double maxWidth, double maxHeight) {
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + width > maxWidth) x = maxWidth - width;
        if (y + height > maxHeight) y = maxHeight - height;
    }

    /** Moves the player upward based on current speed. */
    public void moveUp(double dt)    { y -= speed * dt; }

    /** Moves the player downward based on current speed. */
    public void moveDown(double dt)  { y += speed * dt; }

    /** Moves the player left based on current speed. */
    public void moveLeft(double dt)  { x -= speed * dt; }

    /** Moves the player right based on current speed. */
    public void moveRight(double dt) { x += speed * dt; }

    /**
     * @return true if the player is allowed to fire (cooldown expired)
     */
    public boolean canFire() { return fireTimer <= 0; }

    /**
     * Restores player health by a given amount.
     *
     * <p>HP is capped at a fixed maximum to prevent unlimited healing.</p>
     *
     * @param amount amount of HP to restore
     */
    public void heal(int amount) {
        hp = Math.min(hp + amount, 5); // max HP = 5
    }

    /**
     * Creates a bullet if firing is allowed and starts the cooldown timer.
     *
     * <p>The bullet spawns at the horizontal center of the player and slightly
     * above the player sprite.</p>
     *
     * @param bulletSprite sprite used to render the bullet
     * @return a new {@link Bullet} instance, or null if the player cannot fire yet
     */
    public Bullet fire(Image bulletSprite) {
        if (!canFire()) return null;

        // Reset cooldown after firing.
        fireTimer = fireCooldown;

        // Spawn bullet from the player's center.
        return new Bullet(x + width / 2 - 3, y - 12, bulletSprite);
    }

    /**
     * Applies one unit of damage to the player.
     *
     * <p>If HP reaches zero, the player is marked as dead and will be removed
     * during cleanup. Game state transitions (e.g., GAME_OVER) are handled by
     * higher-level world/state logic.</p>
     */
    public void damage() {
        hp--;
        if (hp <= 0) kill();
    }

    /**
     * Temporarily increases player movement speed.
     *
     * @param bonus    additional speed added on top of {@link #baseSpeed}
     * @param duration duration of the boost in seconds
     */
    public void boostSpeed(double bonus, double duration) {
        speed = baseSpeed + bonus;
        speedBoostTimer = duration;
    }

    /**
     * Temporarily improves the firing rate by lowering the cooldown.
     *
     * <p>The cooldown is only reduced (never increased) to avoid accidentally
     * weakening the player when multiple boosts are applied.</p>
     *
     * @param cooldown desired cooldown value in seconds
     * @param duration duration of the boost in seconds
     */
    public void boostFireRate(double cooldown, double duration) {
        fireCooldown = Math.min(fireCooldown, cooldown);
        fireBoostTimer = duration;
    }

    /**
     * @return current health points of the player
     */
    public int getHp() { return hp; }

    /**
     * @return true if the firing control is currently pressed
     */
    public boolean isFiringPressed() {
        return input.fire();
    }
}

