package com.yourgroup.airbattle.core;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

// Ensure all Item subclasses are visible
import com.yourgroup.airbattle.objects.Bullet;
import com.yourgroup.airbattle.objects.Enemy;
import com.yourgroup.airbattle.objects.EnemyType1;
import com.yourgroup.airbattle.objects.EnemyType2;
import com.yourgroup.airbattle.objects.EnemyType3;
import com.yourgroup.airbattle.objects.GameObject;
import com.yourgroup.airbattle.objects.Item;
import com.yourgroup.airbattle.objects.ItemHeal;
import com.yourgroup.airbattle.objects.ItemRampage;
import com.yourgroup.airbattle.objects.ItemShield;
import com.yourgroup.airbattle.objects.ItemShotgun;
import com.yourgroup.airbattle.objects.ItemSuper;
import com.yourgroup.airbattle.objects.Player;
import com.yourgroup.airbattle.util.Collision;
import com.yourgroup.airbattle.util.SoundManager;

import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

/**
 * The central runtime container that owns all game entities and updates them each frame.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Manage lifecycle of {@link GameObject} instances (spawn, update, render, cleanup).</li>
 *   <li>Spawn enemies and (optionally) power-up items.</li>
 *   <li>Handle collision detection and apply game rules (damage, scoring, item pickup).</li>
 * </ul>
 *
 * <p>Usage:
 * <ul>
 *   <li>Created once with a JavaFX {@link Pane} (the playfield).</li>
 *   <li>{@link #update(double)} is called every frame by {@link GameLoop}.</li>
 * </ul>
 */
public class GameWorld {

    /** The JavaFX container where all game nodes (sprites) are rendered. */
    private final Pane playfield;

    /** Active objects currently in the world (player, enemies, bullets, items, etc.). */
    private final List<GameObject> objects = new ArrayList<>();

    /**
     * Newly spawned objects are queued here and added at a safe point
     * to avoid modifying {@link #objects} while iterating.
     */
    private final List<GameObject> pendingAdd = new ArrayList<>();

    /** Random source for spawn timing, positions, and item drops. */
    private final Random rng = new Random();

    // --- Shared sprites (loaded once) ---
    /** Sprite used by player bullets. */
    private final Image bulletSprite = com.yourgroup.airbattle.util.Assets.image("/img/Bullet.png");
    /** Default enemy sprite (type 1). */
    private final Image enemySprite1 = com.yourgroup.airbattle.util.Assets.image("/img/enemy.png");
    /** Enemy sprite (type 2). */
    private final Image enemySprite2 = com.yourgroup.airbattle.util.Assets.image("/img/enemy2.png");
    /** Enemy sprite (fast/type 3). */
    private final Image enemySprite3 = com.yourgroup.airbattle.util.Assets.image("/img/enemyFast.png");

    // --- Power-up item sprites (match exact filenames) ---
    private final Image itemHealImg    = com.yourgroup.airbattle.util.Assets.image("/img/item_heal.png");
    private final Image itemRampageImg = com.yourgroup.airbattle.util.Assets.image("/img/item_GoBallistic.png"); // Rampage
    private final Image itemSuperImg   = com.yourgroup.airbattle.util.Assets.image("/img/item_SuperBullet.png"); // Super bullet
    private final Image itemShotgunImg = com.yourgroup.airbattle.util.Assets.image("/img/item_MoreBullet.png");  // More bullets
    private final Image itemShieldImg  = com.yourgroup.airbattle.util.Assets.image("/img/item_SpeedAndShield.png"); // Speed + shield

    /** Countdown timer (seconds) to control enemy spawn rate. */
    private double enemySpawnTimer = 0.0;

    /** Player score accumulated during the game session. */
    private int score = 0;

    /**
     * Creates a new {@link GameWorld} bound to a JavaFX playfield pane.
     *
     * @param playfield the Pane used for rendering; game objects add their nodes into it
     */
    public GameWorld(Pane playfield) {
        this.playfield = playfield;
    }

    /**
     * @return the shared sprite used by player bullets (so other classes can reuse it)
     */
    public Image getBulletSprite() {
        return bulletSprite;
    }

    /**
     * @return current score
     */
    public int getScore() {
        return score;
    }

    /**
     * Adds points to the score (ignores non-positive values defensively).
     *
     * @param points points to add; must be > 0 to take effect
     */
    private void addScore(int points) {
        if (points > 0) score += points;
    }

    /**
     * Requests a {@link GameObject} to be spawned into the world.
     *
     * <p>The object is not added immediately; it is queued and inserted at the start of
     * {@link #update(double)} to avoid concurrent modification during iteration.
     *
     * @param obj game object to add (enemy, bullet, item, etc.)
     */
    public void spawn(GameObject obj) {
        pendingAdd.add(obj);
    }

    /**
     * Main per-frame update entry point called by {@link GameLoop}.
     *
     * <p>Frame order (important for predictable behavior):
     * <ol>
     *   <li>Spawn enemies</li>
     *   <li>Add pending objects into the scene graph</li>
     *   <li>Update all alive objects</li>
     *   <li>Resolve collisions (bullets vs enemies, player vs enemies/items)</li>
     *   <li>Render all alive objects</li>
     *   <li>Cleanup dead objects (remove nodes and list entries)</li>
     * </ol>
     *
     * @param dt delta time in seconds since last frame
     */
    public void update(double dt) {
        spawnEnemies(dt);

        // Safely add newly spawned objects and their JavaFX nodes
        if (!pendingAdd.isEmpty()) {
            for (GameObject o : pendingAdd) {
                objects.add(o);
                playfield.getChildren().add(o.getNode());
            }
            pendingAdd.clear();
        }

        // Update logic for each object
        for (GameObject o : objects) {
            if (!o.isAlive()) continue;
            o.update(dt);

            // Keep player inside the visible playfield
            if (o instanceof Player p) {
                p.clampTo(getWorldWidth(), getWorldHeight());
            }
        }

        handleCollisionsOptimized();

        // Render sprites (position/rotation/etc.) after logic and collisions
        for (GameObject o : objects) {
            if (o.isAlive()) o.render();
        }

        cleanup();
    }

    /**
     * Attempts to spawn a random item at the given location when an enemy is defeated.
     *
     * <p>Drop chance is controlled by {@link #ITEM_DROP_RATE_PERCENT} so gameplay balance
     * can be tuned without touching core logic.</p>
     *
     * @param x spawn x position (typically enemy's x at death)
     * @param y spawn y position (typically enemy's y at death)
     */
    private static final int ITEM_DROP_RATE_PERCENT = 20;

    private void trySpawnItem(double x, double y) {
        // Drop chance (percentage)
        if (rng.nextInt(100) < ITEM_DROP_RATE_PERCENT) {
            int itemType = rng.nextInt(5); // 0..4 choose one item type
            switch (itemType) {
                case 0 -> spawn(new ItemHeal(x, y, itemHealImg));
                case 1 -> spawn(new ItemRampage(x, y, itemRampageImg));
                case 2 -> spawn(new ItemSuper(x, y, itemSuperImg));
                case 3 -> spawn(new ItemShotgun(x, y, itemShotgunImg));
                case 4 -> spawn(new ItemShield(x, y, itemShieldImg));
                default -> { /* no-op */ }
            }
        }
    }


    /**
     * Spawns enemies based on a countdown timer.
     *
     * <p>Spawn behavior:
     * <ul>
     *   <li>Timer is reduced by dt each frame.</li>
     *   <li>When timer reaches 0, a new enemy is spawned above the screen and the timer is reset.</li>
     *   <li>Enemy type is determined by a random roll (weighted probabilities).</li>
     * </ul>
     *
     * @param dt delta time in seconds since last frame
     */
    private void spawnEnemies(double dt) {
        enemySpawnTimer -= dt;
        if (enemySpawnTimer > 0) return;

        // Reset timer to a random interval (controls spawn rate)
        enemySpawnTimer = 0.8 + rng.nextDouble() * 0.4;

        // Determine a safe spawn width; fallback if layout bounds are not ready yet
        double width = getWorldWidth();
        if (width < 100) width = 900;

        // Spawn just above the visible area so enemies enter from the top
        double x = rng.nextDouble() * (width - 40);
        double y = -60;

        // Weighted enemy selection
        int roll = rng.nextInt(100);
        GameObject enemy;
        if (roll < 70) {
            enemy = new EnemyType1(x, y, enemySprite1);
        } else if (roll < 95) {
            enemy = new EnemyType3(x, y, enemySprite3);
        } else {
            enemy = new EnemyType2(x, y, enemySprite2);
        }

        spawn(enemy);
    }

    /**
     * @return current playfield width based on layout bounds; falls back to pref width if needed
     */
    private double getWorldWidth() {
        double width = playfield.getLayoutBounds().getWidth();
        return (width > 0) ? width : playfield.getPrefWidth();
    }

    /**
     * @return current playfield height based on layout bounds; falls back to pref height if needed
     */
    private double getWorldHeight() {
        double h = playfield.getLayoutBounds().getHeight();
        return (h > 0) ? h : playfield.getPrefHeight();
    }

    /**
     * Collision resolution step.
     *
     * <p>This method groups objects by type first (bullets/enemies/powerups/player)
     * to reduce unnecessary pair checks compared to checking every object with every other object.
     *
     * <p>Rules implemented:
     * <ul>
     *   <li>Player bullet hits enemy: enemy takes damage, bullet is removed; if enemy dies, score increases and item may drop.</li>
     *   <li>Player collides with enemy: player takes damage, enemy is destroyed.</li>
     *   <li>Player picks up power-up: item effect is applied, item is removed.</li>
     * </ul>
     */
    private void handleCollisionsOptimized() {
        List<GameObject> bullets = new ArrayList<>();
        List<GameObject> enemies = new ArrayList<>();
        List<GameObject> powerups = new ArrayList<>();
        Player player = null;

        // Partition objects by their declared type (fast filtering)
        for (GameObject o : objects) {
            if (!o.isAlive()) continue;
            switch (o.getType()) {
                case BULLET_PLAYER: bullets.add(o); break;
                case ENEMY:         enemies.add(o); break;
                case POWERUP:       powerups.add(o); break;
                case PLAYER:        if (o instanceof Player p) player = p; break;
                default: break;
            }
        }

        // Bullet vs Enemy
        for (GameObject bullet : bullets) {
            if (!bullet.isAlive()) continue;
            for (GameObject enemyObj : enemies) {
                if (!enemyObj.isAlive()) continue;

                if (Collision.aabb(bullet, enemyObj)) {
                    handleBulletHitEnemy((Bullet) bullet, (Enemy) enemyObj);

                    // Once the bullet is consumed, stop checking it against other enemies
                    if (!bullet.isAlive()) break;
                }
            }
        }

        // Player vs Enemy
        if (player != null && player.isAlive()) {
            for (GameObject enemyObj : enemies) {
                if (!enemyObj.isAlive()) continue;

                if (Collision.aabb(player, enemyObj)) {
                    player.damage();
                    enemyObj.kill();
                    SoundManager.playExplosion();
                    // Explosion visual removed because Explosion class is not used in this codebase
                }
            }
        }

        // Player vs Power-up
        if (player != null && player.isAlive() && !powerups.isEmpty()) {
            for (GameObject pu : powerups) {
                if (!pu.isAlive()) continue;

                if (Collision.aabb(player, pu)) {
                    // Apply the power-up effect to the player
                    if (pu instanceof Item it) {
                        it.apply(player);
                        // Optional: SoundManager.playItemGet();
                        pu.kill();
                    }
                }
            }
        }
    }

    /**
     * Handles the event when a player bullet hits an enemy.
     *
     * <p>Game rules:
     * <ul>
     *   <li>Enemy takes damage equal to bullet damage.</li>
     *   <li>Bullet is removed immediately (single-hit bullet).</li>
     *   <li>If enemy dies, score is awarded and an item may drop.</li>
     * </ul>
     *
     * @param b the bullet that hit the enemy
     * @param e the enemy being hit
     */
    private void handleBulletHitEnemy(Bullet bullet, Enemy enemy) {
        enemy.damage(bullet.getDamage());
        bullet.kill();

        if (!enemy.isAlive()) {
            addScore(scoreForEnemy(enemy));
            SoundManager.playExplosion();

            // Item drop at enemy death position
            trySpawnItem(enemy.getX(), enemy.getY());
        }
    }

    /**
     * Calculates score reward based on enemy subtype.
     *
     * @param e enemy instance
     * @return points to award when the enemy is destroyed
     */
    private int scoreForEnemy(Enemy e) {
        if (e instanceof EnemyType2) return 1000;
        if (e instanceof EnemyType3) return 150;
        return 100;
    }

    /**
     * Removes dead objects from both the JavaFX scene graph and the world list.
     * This prevents invisible nodes and memory growth over time.
     */
    private void cleanup() {
        Iterator<GameObject> it = objects.iterator();
        while (it.hasNext()) {
            GameObject o = it.next();
            if (!o.isAlive()) {
                playfield.getChildren().remove(o.getNode());
                it.remove();
            }
        }
    }
}
