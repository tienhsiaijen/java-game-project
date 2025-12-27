package com.yourgroup.airbattle.core;

import com.yourgroup.airbattle.objects.Enemy;
import com.yourgroup.airbattle.objects.EnemyType1;
import com.yourgroup.airbattle.objects.EnemyType2;
import com.yourgroup.airbattle.objects.EnemyType3;
import com.yourgroup.airbattle.objects.GameObject;
import com.yourgroup.airbattle.objects.Item;
import com.yourgroup.airbattle.objects.Player;
import com.yourgroup.airbattle.util.Collision;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameWorld {

    private final Pane playfield;
    private final List<GameObject> objects = new ArrayList<>();
    private final List<GameObject> pendingAdd = new ArrayList<>();

    private final Random rng = new Random();

    // Resources: load once (Player uses bullet sprite)
    private final Image bulletSprite = new Image("/img/Bullet.png");
    private final Image enemySprite1 = new Image("/img/enemy.png");
    private final Image enemySprite2 = new Image("/img/enemy2.png");
    private final Image enemySprite3 = new Image("/img/enemyFast.png");

    private double enemySpawnTimer = 0.0;

    private int score = 0;

    public GameWorld(Pane playfield) {
        this.playfield = playfield;
    }

    public Image getBulletSprite() {
        return bulletSprite;
    }

    public int getScore() {
        return score;
    }

    private void addScore(int points) {
        if (points > 0) score += points;
    }

    public void spawn(GameObject obj) {
        pendingAdd.add(obj);
    }

    public void update(double dt) {
        // 0) spawn enemies
        spawnEnemies(dt);

        // 1) apply pending adds
        if (!pendingAdd.isEmpty()) {
            for (GameObject o : pendingAdd) {
                objects.add(o);
                playfield.getChildren().add(o.getNode());
            }
            pendingAdd.clear();
        }

        // 2) update objects
        for (GameObject o : objects) {
            if (!o.isAlive()) continue;

            o.update(dt);

            // Keep player inside world bounds
            if (o instanceof Player p) {
                p.clampTo(getWorldWidth(), getWorldHeight());
            }
        }

        // 3) collisions (optimized)
        handleCollisionsOptimized();

        // 4) render
        for (GameObject o : objects) {
            if (o.isAlive()) o.render();
        }

        // 5) cleanup
        cleanup();
    }

    private void spawnEnemies(double dt) {
        enemySpawnTimer -= dt;
        if (enemySpawnTimer > 0) return;

        // spawn every 0.8 ~ 1.2 sec
        enemySpawnTimer = 0.8 + rng.nextDouble() * 0.4;

        double w = getWorldWidth();
        if (w < 100) w = 900;

        double x = rng.nextDouble() * (w - 40);
        double y = -60;

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

    private double getWorldWidth() {
        double w = playfield.getLayoutBounds().getWidth();
        return (w > 0) ? w : playfield.getPrefWidth();
    }

    private double getWorldHeight() {
        double h = playfield.getLayoutBounds().getHeight();
        return (h > 0) ? h : playfield.getPrefHeight();
    }

    /**
     * Optimized collision detection:
     * - Instead of checking every pair (O(n^2)), we bucket objects by type
     *   and only test pairs that can actually collide under our rules:
     *   1) Player bullets vs Enemies
     *   2) Player vs Enemies
     *   3) Player vs Power-ups
     */
    private void handleCollisionsOptimized() {
        // Buckets
        List<GameObject> bullets = new ArrayList<>();
        List<GameObject> enemies = new ArrayList<>();
        List<GameObject> powerups = new ArrayList<>();
        Player player = null;

        // 1) Build buckets (single pass)
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
                    // Assume one player; if more than one, extend this to a list
                    if (o instanceof Player p) player = p;
                    break;
                default:
                    break;
            }
        }

        // 2) Bullet vs Enemy (each bullet should hit at most ONE enemy per frame)
        for (GameObject bullet : bullets) {
            if (!bullet.isAlive()) continue;

            for (GameObject enemyObj : enemies) {
                if (!enemyObj.isAlive()) continue;

                if (Collision.aabb(bullet, enemyObj)) {
                    onCollision(bullet, enemyObj);

                    // If bullet is killed on hit, it must not hit any other enemy this frame.
                    if (!bullet.isAlive()) break;
                }
            }
        }

        // 3) Player vs Enemy
        if (player != null && player.isAlive()) {
            for (GameObject enemyObj : enemies) {
                if (!enemyObj.isAlive()) continue;

                if (Collision.aabb(player, enemyObj)) {
                    onCollision(player, enemyObj);

                    // If player dies, no need to continue checking this frame.
                    if (!player.isAlive()) break;
                }
            }
        }

        // 4) Player vs Power-up
        if (player != null && player.isAlive() && !powerups.isEmpty()) {
            for (GameObject pu : powerups) {
                if (!pu.isAlive()) continue;

                if (Collision.aabb(player, pu)) {
                    onCollision(player, pu);
                }
            }
        }
    }

    private int scoreForEnemy(Enemy e) {
        if (e instanceof EnemyType2) return 1000; // boss
        if (e instanceof EnemyType3) return 150;  // fast
        if (e instanceof EnemyType1) return 100;  // normal
        return 100;
    }

    private void onCollision(GameObject a, GameObject b) {
        // Bullet (player) vs Enemy
        if (a.getType() == ObjectType.BULLET_PLAYER && b.getType() == ObjectType.ENEMY) {
            a.kill();
            if (b instanceof Enemy e) {
                e.damage(1);
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

        // Player vs Enemy
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

        // Player vs Power-up
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
