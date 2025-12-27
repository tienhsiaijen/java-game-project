package com.yourgroup.airbattle.objects;

import com.yourgroup.airbattle.core.ObjectType;
import com.yourgroup.airbattle.input.InputHandler;
import javafx.scene.image.Image;

import java.util.function.Consumer;

/**
 * Player-controlled aircraft entity.
 *
 * <p>The player is responsible for:
 * <ul>
 *   <li>Movement based on keyboard input</li>
 *   <li>Firing bullets with a cooldown timer</li>
 *   <li>Health (HP) and temporary boost effects</li>
 * </ul>
 * </p>
 *
 * <p>To keep this class decoupled from the world, it uses a callback
 * to spawn bullets (provided by {@code GameWorld}).</p>
 */
public class Player extends GameObject {

    private int hp = 3;

    private double speed = 220; // px/s
    private final double baseSpeed = 220;

    private double fireCooldown = 0.25;
    private final double baseFireCooldown = 0.25;

    private double fireTimer = 0;
    private double fireBoostTimer = 0;
    private double speedBoostTimer = 0;

    private final InputHandler input;

    /** Bullet sprite used when firing. */
    private final Image bulletSprite;

    /** Callback used to spawn bullets into the world. */
    private final Consumer<GameObject> spawner;

    public Player(double x,
                  double y,
                  Image sprite,
                  InputHandler input,
                  Image bulletSprite,
                  Consumer<GameObject> spawner) {
        super(x, y, 40, 40, sprite);
        this.input = input;
        this.bulletSprite = bulletSprite;
        this.spawner = spawner;
    }

    @Override
    public ObjectType getType() {
        return ObjectType.PLAYER;
    }

    // Smaller hitbox to avoid collisions caused by transparent sprite borders.
    @Override
    protected double hitboxInsetX() { return 6; }

    @Override
    protected double hitboxInsetY() { return 6; }

    @Override
    public void update(double dt) {
        // Cooldown timer
        fireTimer = Math.max(0, fireTimer - dt);

        // Expire speed boost
        if (speedBoostTimer > 0) {
            speedBoostTimer -= dt;
            if (speedBoostTimer <= 0) speed = baseSpeed;
        }

        // Expire fire-rate boost
        if (fireBoostTimer > 0) {
            fireBoostTimer -= dt;
            if (fireBoostTimer <= 0) fireCooldown = baseFireCooldown;
        }

        // Movement input
        if (input.left())  moveLeft(dt);
        if (input.right()) moveRight(dt);
        if (input.up())    moveUp(dt);
        if (input.down())  moveDown(dt);

        // Firing input (player owns firing logic now)
        if (input.fire()) {
            Bullet b = fire();
            if (b != null && spawner != null) {
                spawner.accept(b);
            }
        }
    }

    public void clampTo(double maxWidth, double maxHeight) {
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + width > maxWidth) x = maxWidth - width;
        if (y + height > maxHeight) y = maxHeight - height;
    }

    public void moveUp(double dt)    { y -= speed * dt; }
    public void moveDown(double dt)  { y += speed * dt; }
    public void moveLeft(double dt)  { x -= speed * dt; }
    public void moveRight(double dt) { x += speed * dt; }

    public boolean canFire() { return fireTimer <= 0; }

    public Bullet fire() {
        if (!canFire()) return null;
        fireTimer = fireCooldown;
        return new Bullet(x + width / 2 - 3, y - 12, bulletSprite);
    }

    public void heal(int amount) {
        hp = Math.min(hp + amount, 5);
    }

    public void damage() {
        hp--;
        if (hp <= 0) kill();
    }

    public void boostSpeed(double bonus, double duration) {
        speed = baseSpeed + bonus;
        speedBoostTimer = duration;
    }

    public void boostFireRate(double cooldown, double duration) {
        fireCooldown = Math.min(fireCooldown, cooldown);
        fireBoostTimer = duration;
    }

    public int getHp() { return hp; }
}
