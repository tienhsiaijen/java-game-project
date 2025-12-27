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

public class GameWorld {

    private final Pane playfield;
    private final List<GameObject> objects = new ArrayList<>();
    private final List<GameObject> pendingAdd = new ArrayList<>();

    private final Random rng = new Random();

    // Resources: load once
    private final Image bulletSprite = new Image("/img/Bullet.png");
    private final Image enemySprite1 = new Image("/img/enemy.png");
    private final Image enemySprite2 = new Image("/img/enemy2.png");
    private final Image enemySprite3 = new Image("/img/enemyFast.png");

    // Spawn timer
    private double enemySpawnTimer = 0.0;

    // Score
    private int score = 0;

    public GameWorld(Pane playfield) {
        this.playfield = playfield;
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

        // 2) update logic
        for (GameObject o : objects) {
            if (!o.isAlive()) continue;

            o.update(dt);

            if (o instanceof Player player) {
                // Fire bullet if allowed and key is pressed
                if (player.canFire() && player.isFiringPressed()) {
                    Bullet b = player.fire(bulletSprite);
                    if (b != null) spawn(b);
                }

                player.clampTo(getWorldWidth(), getWorldHeight());
            }
        }

        // 3) collision
        handleCollisions();

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
     * Collision handler with "early-out" rules:
     * - If a bullet is killed by a collision, it should not hit anything else in the same frame.
     * - If an object is killed, skip further checks involving it.
     */
    private void handleCollisions() {
        for (int i = 0; i < objects.size(); i++) {
            GameObject a = objects.get(i);
            if (!a.isAlive()) continue;

            for (int j = i + 1; j < objects.size(); j++) {
                GameObject b = objects.get(j);
                if (!b.isAlive()) continue;

                // If 'a' was killed by a previous collision in this inner loop,
                // it must not collide with any more objects this frame.
                if (!a.isAlive()) break;

                if (Collision.aabb(a, b)) {
                    onCollision(a, b);

                    // If either side is killed by this collision, we can avoid extra work.
                    // - If 'a' is dead, it can't collide with anything else -> break inner loop.
                    // - If only 'b' is dead, keep scanning; 'a' might collide with others (e.g., player).
                    if (!a.isAlive()) break;
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
            a.kill(); // bullet disappears on hit
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
