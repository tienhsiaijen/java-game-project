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
    /** Enemy sprite (Type 1: normal). */
    private final Image enemyType1Sprite = com.yourgroup.airbattle.util.Assets.image("/img/enemy.png");
    /** Enemy sprite (Fast). */
    private final Image enemyFastSprite  = com.yourgroup.airbattle.util.Assets.image("/img/enemyFast.png");
    /** Enemy sprite (Boss). */
    private final Image enemyBossSprite  = com.yourgroup.airbattle.util.Assets.image("/img/enemyBoss.png");


    // --- Power-up item sprites (match exact filenames) ---
    private final Image itemHealImg    = com.yourgroup.airbattle.util.Assets.image("/img/item_heal.png");
    private final Image itemRampageImg = com.yourgroup.airbattle.util.Assets.image("/img/item_GoBallistic.png");     // Rampage
    private final Image itemSuperImg   = com.yourgroup.airbattle.util.Assets.image("/img/item_SuperBullet.png");     // Super bullet
    private final Image itemShotgunImg = com.yourgroup.airbattle.util.Assets.image("/img/item_MoreBullet.png");      // Shotgun
    private final Image itemShieldImg  = com.yourgroup.airbattle.util.Assets.image("/img/item_SpeedAndShield.png");  // Speed + shield

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

    /** @return current score */
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
     * {@link #update(double)} to avoid concurrent modification during iteration.</p>
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
     * <p>Drop chance and item weights are configured in {@link GameConfig} so gameplay balance
     * can be tuned without changing core game logic.</p>
     */
    private void trySpawnItem(double x, double y) {
        // Overall drop chance check (configured in GameConfig).
        if (rng.nextInt(100) >= GameConfig.ITEM_DROP_RATE_PERCENT) {
            return;
        }

        int totalWeight =
                GameConfig.ITEM_HEAL_WEIGHT
              + GameConfig.ITEM_RAMPAGE_WEIGHT
              + GameConfig.ITEM_SUPER_WEIGHT
              + GameConfig.ITEM_SHOTGUN_WEIGHT
              + GameConfig.ITEM_SHIELD_WEIGHT;

        int roll = rng.nextInt(totalWeight);

        if (roll < GameConfig.ITEM_HEAL_WEIGHT) {
            spawn(new ItemHeal(x, y, itemHealImg));

        } else if (roll < GameConfig.ITEM_HEAL_WEIGHT + GameConfig.ITEM_RAMPAGE_WEIGHT) {
            spawn(new ItemRampage(x, y, itemRampageImg));

        } else if (roll < GameConfig.ITEM_HEAL_WEIGHT
                         + GameConfig.ITEM_RAMPAGE_WEIGHT
                         + GameConfig.ITEM_SUPER_WEIGHT) {
            spawn(new ItemSuper(x, y, itemSuperImg));

        } else if (roll < GameConfig.ITEM_HEAL_WEIGHT
                         + GameConfig.ITEM_RAMPAGE_WEIGHT
                         + GameConfig.ITEM_SUPER_WEIGHT
                         + GameConfig.ITEM_SHOTGUN_WEIGHT) {
            spawn(new ItemShotgun(x, y, itemShotgunImg));

        } else {
            spawn(new ItemShield(x, y, itemShieldImg));
        }
    }

    /**
     * Spawns enemies using a countdown timer and a difficulty scaling model.
     *
     * <p>Difficulty design:
     * <ul>
     *   <li>Difficulty level increases with score (1 level per 500 points), capped at 25.</li>
     *   <li>Higher difficulty reduces spawn interval (more enemies) down to a safe minimum.</li>
     *   <li>Higher difficulty increases the chance of stronger enemy types (Fast/Boss).</li>
     *   <li>Enemy speed and HP are scaled by difficulty (+10% per level).</li>
     * </ul>
     *
     * <p>Enemy type meaning (must stay consistent with menus and sprites):
     * <ul>
     *   <li>Type 1: Normal</li>
     *   <li>Type 2: Fast</li>
     *   <li>Type 3: Boss</li>
     * </ul>
     *
     * @param dt delta time in seconds since last frame
     */
    private void spawnEnemies(double dt) {
        // Countdown timer; when it reaches 0, spawn one enemy and reset.
        enemySpawnTimer -= dt;
        if (enemySpawnTimer > 0) return;

        // Difficulty level: level up every 500 points, capped to keep gameplay reasonable.
        int difficultyLevel = score / 500;
        if (difficultyLevel > 25) difficultyLevel = 25;

        // Spawn frequency scaling: base interval decreases with difficulty, clamped to a minimum.
        double baseInterval = 0.8 - (difficultyLevel * 0.04);
        if (baseInterval < 0.2) baseInterval = 0.2;

        // Add jitter so spawns are not perfectly periodic.
        enemySpawnTimer = baseInterval + rng.nextDouble() * 0.3;

        // Fallback if layout bounds are not ready yet.
        double width = playfield.getWidth();
        if (width <= 0) width = GameConfig.WIDTH;

        // Spawn just above the visible area so enemies enter from the top.
        double margin = GameConfig.ENEMY_SPAWN_MARGIN_X;
        double minX = margin;
        double maxX = Math.max(minX, width - margin);
        double x = minX + rng.nextDouble() * (maxX - minX);
        double y = GameConfig.ENEMY_SPAWN_Y;

     // Type probabilities (0..99):
     // Base chances: Boss 10%, Fast 25%. Each difficulty level adds +1% to each.
     int bossChance = 10 + difficultyLevel; // Type 3 (Boss)
     int fastChance = 25 + difficultyLevel; // Type 2 (Fast)

     int roll = rng.nextInt(100); // 0..99
     Enemy enemy;

     if (roll < bossChance) {
         // Boss -> EnemyType3
         enemy = new EnemyType3(x, y, enemyBossSprite);

     } else if (roll < bossChance + fastChance) {
         // Fast -> EnemyType2
         enemy = new EnemyType2(x, y, enemyFastSprite);

     } else {
         // Normal -> EnemyType1
         enemy = new EnemyType1(x, y, enemyType1Sprite);
     }

        // Scale enemy stats by difficulty (+10% per level).
        // Note: Enemy must provide multiplySpeed(double) and buffHp(double).
        double multiplier = 1.0 + (difficultyLevel * 0.1);
        enemy.multiplySpeed(multiplier);
        enemy.buffHp(multiplier);

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
     * to reduce unnecessary pair checks compared to checking every object with every other object.</p>
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
                case BULLET_PLAYER:
                    bullets.add(o);
                    break;

                case ENEMY:
                    enemies.add(o);
                    break;

                case POWERUP:
                    powerups.add(o);
                    break;

                case PLAYER:
                    if (o instanceof Player p) {
                        player = p;
                    }
                    break;

                case EFFECT:
                    // Visual/status effects do not participate in collision handling.
                    break;
            }
        }

        // Bullet vs Enemy
        for (GameObject bullet : bullets) {
            if (!bullet.isAlive()) continue;
            for (GameObject enemyObj : enemies) {
                if (!enemyObj.isAlive()) continue;

                if (Collision.aabb(bullet, enemyObj)) {
                    handleBulletHitEnemy((Bullet) bullet, (Enemy) enemyObj);

                    // Once the bullet is consumed, stop checking it against other enemies.
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
                    // Destruction feedback is provided via sound; this project keeps effects lightweight.
                }
            }
        }

        // Player vs Power-up
        if (player != null && player.isAlive() && !powerups.isEmpty()) {
            for (GameObject pu : powerups) {
                if (!pu.isAlive()) continue;

                if (Collision.aabb(player, pu)) {
                    // Apply the power-up effect to the player.
                    if (pu instanceof Item it) {
                        it.apply(player);
                        SoundManager.playItemGet();
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
     * @param bullet the bullet that hit the enemy
     * @param enemy  the enemy being hit
     */
    private void handleBulletHitEnemy(Bullet bullet, Enemy enemy) {
        enemy.damage(bullet.getDamage());
        bullet.kill();

        if (!enemy.isAlive()) {
            addScore(enemy.scoreValue());
            SoundManager.playExplosion();

            // Item drop at enemy death position.
            trySpawnItem(enemy.getX(), enemy.getY());
        }
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
