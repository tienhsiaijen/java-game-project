package com.yourgroup.airbattle.core;

import com.yourgroup.airbattle.objects.Bullet;
import com.yourgroup.airbattle.objects.Enemy;
import com.yourgroup.airbattle.objects.GameObject;
import com.yourgroup.airbattle.objects.Item;
import com.yourgroup.airbattle.objects.Player;
import com.yourgroup.airbattle.objects.EnemyType1;
import com.yourgroup.airbattle.objects.EnemyType2;
import com.yourgroup.airbattle.objects.EnemyType3;
import com.yourgroup.airbattle.util.Collision;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * GameWorld is the central container for all game entities and runtime logic.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Own and manage the collection of {@link GameObject} instances.</li>
 *   <li>Advance the simulation each frame via {@link #update(double)}.</li>
 *   <li>Spawn enemies over time and enqueue newly created objects safely.</li>
 *   <li>Perform collision detection and resolve outcomes (damage, score, pickups).</li>
 *   <li>Render objects and remove dead objects from both the scene graph and memory.</li>
 * </ul>
 * </p>
 *
 * <p>Design notes:
 * <ul>
 *   <li>New objects are added through a "pending" list to avoid modifying {@code objects}
 *       while iterating.</li>
 *   <li>Sprites are loaded once and reused to reduce repeated IO and memory overhead.</li>
 *   <li>Collision detection uses AABB checks to keep runtime cost manageable.</li>
 * </ul>
 * </p>
 */
public class GameWorld {

    /** JavaFX container that holds all visible nodes for the gameplay area. */
    private final Pane playfield;

    /** Active game objects currently present in the world. */
    private final List<GameObject> objects = new ArrayList<>();

    /**
     * Objects scheduled to be added at the start of the next update tick.
     * This avoids ConcurrentModificationExceptions and keeps update logic deterministic.
     */
    private final List<GameObject> pendingAdd = new ArrayList<>();

    /** Random generator used for enemy spawning and spawn position selection. */
    private final Random rng = new Random();

    // ===== Sprites (loaded once and reused) =====

    /** Sprite used for player bullets. */
    private final Image bulletSprite = new Image("/img/Bullet.png");

    /** Sprite used for EnemyType1 (basic enemy). */
    private final Image enemySprite1 = new Image("/img/enemy.png");

    /** Sprite used for EnemyType2 (slow / boss-like enemy). */
    private final Image enemySprite2 = new Image("/img/enemy2.png");

    /** Sprite used for EnemyType3 (fast enemy). */
    private final Image enemySprite3 = new Image("/img/enemyFast.png");

    // ===== Enemy spawn timing =====

    /**
     * Countdown timer (seconds) for the next enemy spawn.
     * When it reaches <= 0, an enemy is spawned and the timer is reset.
     */
    private double enemySpawnTimer = 0.0;

    // ===== Score system =====

    /** Current score accumulated by the player. */
    private int score = 0;

    /**
     * Creates a new game world bound to the given playfield.
     *
     * @param playfield JavaFX pane where all game nodes will be attached (must not be null)
     */
    public GameWorld(Pane playfield) {
        this.playfield = playfield;
    }

    /**
     * @return current player score
     */
    public int getScore() {
        return score;
    }

    /**
     * Adds score if the points are positive. This prevents accidental negative scoring
     * from misuse and keeps the scoring logic defensive.
     *
     * @param points points to add (ignored if <= 0)
     */
    private void addScore(int points) {
        if (points > 0) score += points;
    }

    /**
     * Schedules a {@link GameObject} to be added to the world on the next update tick.
     *
     * <p>Why queue instead of adding immediately?
     * Because objects may be spawned while iterating over {@link #objects} during {@link #update(double)}.
     * This approach prevents concurrent modification and keeps the update loop stable.</p>
     *
     * @param obj the object to add (typically Bullet, Enemy, or Item)
     */
    public void spawn(GameObject obj) {
        pendingAdd.add(obj);
    }

    /**
     * Advances the game simulation by one time step.
     *
     * <p>Update pipeline:
     * <ol>
     *   <li>Spawn enemies according to the spawn timer.</li>
     *   <li>Apply all pending spawns to the active object list and scene graph.</li>
     *   <li>Update each alive object (movement, internal timers, AI, etc.).</li>
     *   <li>Handle player-specific actions (firing, bounds clamping).</li>
     *   <li>Detect and resolve collisions (damage, score, pickups).</li>
     *   <li>Render alive objects to sync visuals with their logical state.</li>
     *   <li>Cleanup dead objects from both the JavaFX scene and the list.</li>
     * </ol>
     * </p>
     *
     * @param dt delta time in seconds since the last frame (typically clamped by GameLoop)
     */
    public void update(double dt) {
        // 0) Spawn enemies over time
        spawnEnemies(dt);

        // 1) Apply pending additions (safe point: before we iterate objects)
        if (!pendingAdd.isEmpty()) {
            for (GameObject o : pendingAdd) {
                objects.add(o);
                playfield.getChildren().add(o.getNode());
            }
            pendingAdd.clear();
        }

        // 2) Update logic for all alive objects
        for (GameObject o : objects) {
            if (!o.isAlive()) continue;

            o.update(dt);

            // Player input/actions are handled here to keep the loop centralized.
            if (o instanceof Player player) {

                // Fire bullet when the player can fire and the firing key is pressed.
                if (player.canFire() && player.isFiringPressed()) {
                    Bullet b = player.fire(bulletSprite);
                    if (b != null) spawn(b);
                }

                // Prevent the player from leaving the visible playfield.
                player.clampTo(getWorldWidth(), getWorldHeight());
            }
        }

        // 3) Collision detection and resolution
        handleCollisions();

        // 4) Render (apply logical positions/state to JavaFX nodes)
        for (GameObject o : objects) {
            if (o.isAlive()) o.render();
        }

        // 5) Remove dead objects and detach their nodes
        cleanup();
    }

    /**
     * Spawns a single enemy when the spawn timer expires.
     *
     * <p>Spawn interval is randomized to create more natural pacing.
     * Enemy type is chosen by a weighted roll.</p>
     *
     * @param dt delta time in seconds
     */
    private void spawnEnemies(double dt) {
        enemySpawnTimer -= dt;
        if (enemySpawnTimer > 0) return;

        // Spawn one enemy every ~0.8 to 1.2 seconds.
        enemySpawnTimer = 0.8 + rng.nextDouble() * 0.4;

        double w = getWorldWidth();
        // Defensive fallback in case layout bounds are not ready yet.
        if (w < 100) w = 900;

        // Spawn above the screen so enemies enter from the top.
        double x = rng.nextDouble() * (w - 40);
        double y = -60;

        int roll = rng.nextInt(100);
        GameObject enemy;

        // Weighted enemy distribution:
        // 70% Type1, 25% Type3, 5% Type2.
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
     * @return current width of the world in pixels (layout bounds preferred; falls back to pref width)
     */
    private double getWorldWidth() {
        double w = playfield.getLayoutBounds().getWidth();
        return (w > 0) ? w : playfield.getPrefWidth();
    }

    /**
     * @return current height of the world in pixels (layout bounds preferred; falls back to pref height)
     */
    private double getWorldHeight() {
        double h = playfield.getLayoutBounds().getHeight();
        return (h > 0) ? h : playfield.getPrefHeight();
    }

    /**
     * Performs pairwise collision checks between alive objects using AABB intersection.
     *
     * <p>Complexity is O(n^2) in the number of objects. This is acceptable for small games,
     * but could be optimized later using spatial partitioning (e.g., grid / quadtree) if needed.</p>
     */
    private void handleCollisions() {
        for (int i = 0; i < objects.size(); i++) {
            GameObject a = objects.get(i);
            if (!a.isAlive()) continue;

            for (int j = i + 1; j < objects.size(); j++) {
                GameObject b = objects.get(j);
                if (!b.isAlive()) continue;

                if (Collision.aabb(a, b)) {
                    onCollision(a, b);
                }
            }
        }
    }

    /**
     * Converts an enemy type into its score value.
     *
     * <p>Scoring can be tuned based on difficulty, speed, or HP. Here it is based on enemy class.</p>
     *
     * @param e enemy instance
     * @return score gained for killing this enemy
     */
    private int scoreForEnemy(Enemy e) {
        // Score by enemy type (can also be based on HP/difficulty)
        if (e instanceof EnemyType2) return 1000; // large, slow boss-like enemy
        if (e instanceof EnemyType3) return 150;  // fast enemy (often harder to hit)
        if (e instanceof EnemyType1) return 100;  // basic enemy
        return 100;
    }

    /**
     * Resolves the outcome of a collision between two objects.
     *
     * <p>This method enforces the core game rules:
     * <ul>
     *   <li>Player bullet vs enemy: bullet disappears, enemy takes damage, score awarded on kill.</li>
     *   <li>Player vs enemy: player takes damage, enemy is removed.</li>
     *   <li>Player vs power-up: apply item effect, remove the item.</li>
     * </ul>
     * </p>
     *
     * <p>Note: We check both (a,b) and (b,a) orders to handle collisions symmetrically.</p>
     *
     * @param a first object
     * @param b second object
     */
    private void onCollision(GameObject a, GameObject b) {
        // Player bullet vs enemy
        if (a.getType() == ObjectType.BULLET_PLAYER && b.getType() == ObjectType.ENEMY) {
            a.kill(); // Remove bullet on hit (common shooter rule)
            if (b instanceof Enemy e) {
                e.damage(1);
                // Award score only when the enemy is actually killed.
                if (!e.isAlive()) addScore(scoreForEnemy(e));
            }
            return;
        }
        if (b.getType() == ObjectType.BULLET_PLAYER && a.getType() == ObjectType.ENEMY) {
            b.kill();
            if (a instanceof Enemy e) {
                e.damage(1);
                if (!e.isAlive()) addScore(scoreForEnemy(e));
            }
            return;
        }

        // Player vs enemy
        if (a.getType() == ObjectType.PLAYER && b.getType() == ObjectType.ENEMY) {
            if (a instanceof Player p) p.damage();
            b.kill();
            return;
        }
        if (b.getType() == ObjectType.PLAYER && a.getType() == ObjectType.ENEMY) {
            if (b instanceof Player p) p.damage();
            a.kill();
            return;
        }

        // Player vs power-up
        if (a.getType() == ObjectType.PLAYER && b.getType() == ObjectType.POWERUP) {
            if (a instanceof Player p && b instanceof Item it) it.apply(p);
            b.kill();
            return;
        }
        if (b.getType() == ObjectType.PLAYER && a.getType() == ObjectType.POWERUP) {
            if (b instanceof Player p && a instanceof Item it) it.apply(p);
            a.kill();
        }
    }

    /**
     * Removes dead objects from the world.
     *
     * <p>This method:
     * <ul>
     *   <li>Detaches the object's JavaFX node from the playfield,</li>
     *   <li>Removes the object from the active list to free memory and CPU time.</li>
     * </ul>
     * </p>
     *
     * <p>Using an iterator here avoids issues when removing items during traversal.</p>
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
