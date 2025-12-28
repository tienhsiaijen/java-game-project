package com.yourgroup.airbattle.objects;

import com.yourgroup.airbattle.core.ObjectType;
import com.yourgroup.airbattle.input.InputHandler;
import javafx.scene.image.Image;

import java.util.function.Consumer;

/**
 * Player-controlled aircraft entity.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Movement based on keyboard input</li>
 *   <li>Firing bullets with cooldown and power-up modifiers</li>
 *   <li>Health (HP) and temporary boost effects</li>
 * </ul>
 * </p>
 *
 * <p>Decoupling note:
 * This class does not directly add bullets/effects into the world. It uses a
 * {@link Consumer} callback (provided by GameWorld) to spawn new {@link GameObject}s.</p>
 */
public class Player extends GameObject {

    /** Current health points. When it reaches 0, the player is killed. */
    private int currentHp = 3;

    /** Current movement speed in pixels per second. */
    private double moveSpeed = 220;

    /** Base movement speed used to restore default after speed boost expires. */
    private final double baseMoveSpeed = 220;

    /**
     * Fire cooldown used by the legacy/simple fire-rate boost.
     * (The effective cooldown is computed in {@link #fireWeapon()}.)
     */
    private double fireCooldown = 0.25;

    /** Default fire cooldown used to restore after fire-boost expires. */
    private final double baseFireCooldown = 0.25;

    /** Countdown timer until the player can fire again. */
    private double fireCooldownTimer = 0;

    // --- Buff State Timers (seconds) ---
    private double fireBoostTimer = 0;
    private double speedBoostTimer = 0;

    /** Whether the defensive shield is active (negates damage while true). */
    private boolean isShielded = false;
    private double shieldTimer = 0;

    /** Whether auto-fire (rampage) mode is active. */
    private boolean isAutoFire = false;
    private double autoFireTimer = 0;

    /** Whether super bullets (higher damage/size) are active. */
    private boolean isSuperBullet = false;
    private double superBulletTimer = 0;

    /** Whether shotgun mode (multi-bullet spread) is active. */
    private boolean isShotgun = false;
    private double shotgunTimer = 0;
    // -------------------------

    /** Keyboard input abstraction (tracks key state). */
    private final InputHandler input;

    /** Sprite used for normal bullets. */
    private final Image bulletSprite;

    /** Sprite used for super bullets. */
    private final Image superBulletSprite;

    /** Sprite used for the shield visual effect. */
    private final Image shieldSprite;

    /**
     * Callback used to spawn objects into the world (typically GameWorld::spawn).
     */
    private final Consumer<GameObject> spawner;

    /**
     * Creates the player object.
     *
     * <p>This constructor receives all required images so the player can switch
     * visuals based on power-up state (normal bullets vs super bullets, shield effect).</p>
     *
     * @param x                 initial x-coordinate (top-left)
     * @param y                 initial y-coordinate (top-left)
     * @param sprite            player sprite image
     * @param input             input handler used for movement and firing
     * @param bulletSprite      sprite for normal bullets
     * @param superBulletSprite sprite for super bullets (used when buff is active)
     * @param shieldSprite      sprite for the shield visual effect
     * @param spawner           callback to spawn bullets/effects into the game world
     */
    public Player(double x,
                  double y,
                  Image sprite,
                  InputHandler input,
                  Image bulletSprite,
                  Image superBulletSprite,
                  Image shieldSprite,
                  Consumer<GameObject> spawner) {
        super(x, y, 40, 40, sprite);
        this.input = input;
        this.bulletSprite = bulletSprite;
        this.superBulletSprite = superBulletSprite;
        this.shieldSprite = shieldSprite;
        this.spawner = spawner;
    }

    @Override
    public ObjectType getType() {
        return ObjectType.PLAYER;
    }

    /**
     * Uses a smaller hitbox to avoid collisions caused by transparent sprite borders.
     */
    @Override
    protected double hitboxInsetX() { return 6; }

    @Override
    protected double hitboxInsetY() { return 6; }

    /**
     * Per-frame update for player logic.
     *
     * @param dt delta time in seconds
     */
    @Override
    public void update(double dt) {
        // Update firing cooldown timer
        fireCooldownTimer = Math.max(0, fireCooldownTimer - dt);

        // --- Handle Buff Expiration ---

        // Expire speed boost
        if (speedBoostTimer > 0) {
            speedBoostTimer -= dt;
            if (speedBoostTimer <= 0) moveSpeed = baseMoveSpeed;
        }

        // Expire fire-rate boost (legacy/simple boost)
        if (fireBoostTimer > 0) {
            fireBoostTimer -= dt;
            if (fireBoostTimer <= 0) fireCooldown = baseFireCooldown;
        }

        // Expire shield
        if (isShielded) {
            shieldTimer -= dt;
            if (shieldTimer <= 0) isShielded = false;
        }

        // Expire auto-fire
        if (isAutoFire) {
            autoFireTimer -= dt;
            if (autoFireTimer <= 0) isAutoFire = false;
        }

        // Expire super bullet
        if (isSuperBullet) {
            superBulletTimer -= dt;
            if (superBulletTimer <= 0) isSuperBullet = false;
        }

        // Expire shotgun
        if (isShotgun) {
            shotgunTimer -= dt;
            if (shotgunTimer <= 0) isShotgun = false;
        }

        // --- Movement Input ---
        if (input.left())  moveLeft(dt);
        if (input.right()) moveRight(dt);
        if (input.up())    moveUp(dt);
        if (input.down())  moveDown(dt);

        // --- Firing Logic (supports multiple active effects) ---

     // Fire only when SPACE is pressed.
     // AutoFire affects fire rate / bullet attributes, but does NOT shoot by itself.
     if (input.fire()) {
         if (canFire()) fireWeapon();
     }

    }

    /**
     * Handles firing behavior, including Shotgun and Super Bullet effects.
     *
     * <p>This method:
     * <ul>
     *   <li>Computes the effective cooldown based on active buffs.</li>
     *   <li>Selects bullet sprite/size/damage based on super-bullet state.</li>
     *   <li>Spawns one or more {@link Bullet} objects via {@link #spawner}.</li>
     * </ul>
     * </p>
     */
    private void fireWeapon() {
        // 1) Determine effective fire rate.
        double actualCooldown = isAutoFire ? 0.1 : 0.25;

        // If a simple fire-boost is active, choose the faster cooldown.
        if (fireBoostTimer > 0) actualCooldown = Math.min(actualCooldown, fireCooldown);

        fireCooldownTimer = actualCooldown;

        // 2) Play shooting sound.
        com.yourgroup.airbattle.util.SoundManager.playShoot();

        // 3) Determine bullet attributes (Super Bullet vs Normal).
        // Auto-fire also uses the super bullet sprite for clearer feedback.
        boolean useSuper = isSuperBullet || isAutoFire;

        int damage = useSuper ? 5 : 1;
        double bulletWidth = useSuper ? 20 : 6;
        double bulletHeight = useSuper ? 60 : 12;

        Image sprite = useSuper ? superBulletSprite : bulletSprite;

        // 4) Spawn center bullet (straight up).
        Bullet center = new Bullet(x + width / 2 - bulletWidth / 2, y - 20,
                bulletWidth, bulletHeight, sprite, damage, 0);
        spawner.accept(center);

        // 5) Spawn additional bullets in shotgun mode (spread).
        if (isShotgun) {
            Bullet left = new Bullet(x + width / 2 - bulletWidth / 2, y - 20,
                    bulletWidth, bulletHeight, sprite, damage, -100);
            Bullet right = new Bullet(x + width / 2 - bulletWidth / 2, y - 20,
                    bulletWidth, bulletHeight, sprite, damage, 100);
            spawner.accept(left);
            spawner.accept(right);
        }
    }

    /**
     * Clamps the player inside the playfield bounds.
     */
    public void clampTo(double maxWidth, double maxHeight) {
        if (x < 0) x = 0;
        if (y < 0) y = 0;
        if (x + width > maxWidth) x = maxWidth - width;
        if (y + height > maxHeight) y = maxHeight - height;
    }

    public void moveUp(double dt)    { y -= moveSpeed * dt; }
    public void moveDown(double dt)  { y += moveSpeed * dt; }
    public void moveLeft(double dt)  { x -= moveSpeed * dt; }
    public void moveRight(double dt) { x += moveSpeed * dt; }

    /** @return true if the player is allowed to fire (cooldown complete). */
    public boolean canFire() { return fireCooldownTimer <= 0; }

    /**
     * Restores player health by a given amount, capped at a maximum value.
     *
     * @param amount health points to restore
     */
    public void heal(int amount) {
        currentHp = Math.min(currentHp + amount, 5);
    }

    /**
     * Applies one unit of damage unless shield is active.
     * If HP reaches 0, the player is killed.
     */
    public void damage() {
        if (isShielded) return;
        currentHp--;
        if (currentHp <= 0) kill();
    }

    // --- Buff Activation Methods ---

    /**
     * Activates a temporary speed boost.
     *
     * @param bonus    speed increase added on top of base speed
     * @param duration duration in seconds
     */
    public void boostSpeed(double bonus, double duration) {
        moveSpeed = baseMoveSpeed + bonus;
        speedBoostTimer = duration;
    }

    /**
     * Activates an invincibility shield for a limited duration.
     * Also spawns a visual shield effect that follows the player.
     *
     * @param duration duration in seconds
     */
    public void activateShield(double duration) {
        isShielded = true;
        shieldTimer = duration;

        // Spawn a shield visual effect attached to the player (if resources are available).
        if (spawner != null && shieldSprite != null) {
            spawner.accept(new ShieldEffect(this, shieldSprite));
        }
    }

    /**
     * Activates rampage mode (automatic rapid fire).
     *
     * @param duration duration in seconds
     */
    public void activateAutoFire(double duration) {
        isAutoFire = true;
        autoFireTimer = duration;
    }

    /**
     * Activates super bullet mode (higher damage and larger bullets).
     *
     * @param duration duration in seconds
     */
    public void activateSuperBullet(double duration) {
        isSuperBullet = true;
        superBulletTimer = duration;
    }

    /**
     * Activates shotgun mode (fires multiple bullets per shot).
     *
     * @param duration duration in seconds
     */
    public void activateShotgun(double duration) {
        isShotgun = true;
        shotgunTimer = duration;
    }

    /** @return true if shield protection is currently active. */
    public boolean isShielded() {
        return isShielded;
    }

    /** @return current HP. Method name kept for compatibility with UI/HUD code. */
    public int getHp() { return currentHp; }
}
