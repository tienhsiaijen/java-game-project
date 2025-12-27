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

    // Resources: load once (used to pass into Player from MainApp)
    private final Image bulletSprite = new Image("/img/Bullet.png");
    private final Image enemySprite1 = new Image("/img/enemy.png");
    private final Image enemySprite2 = new Image("/img/enemy2.png");
    private final Image enemySprite3 = new Image("/img/enemyFast.png");

    private double enemySpawnTimer = 0.0;

    private int score = 0;

    public GameWorld(Pane playfield) {
        this.playfield = playfield;
    }

    /** Expose bullet sprite so MainApp can build Player cleanly. */
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
        spawnEnemies(dt);

        // Apply pending adds
        if (!pendingAdd.isEmpty()) {
            for (GameObject o : pendingAdd) {
                objects.add(o);
                playfield.getChildren().add(o.getNode());
            }
            pendingAdd.clear();
        }

        // Update objects (no special "player fire" logic here anymore)
        for (GameObject o : objects) {
            if (!o.isAlive()) continue;

            o.update(dt);

            // Keep player inside bounds (world is the owner of the playfield size)
            if (o instanceof Player p) {
                p.clampTo(getWorldWidth(), getWorldHeight());
            }
        }

        handleCollisions();

        for (GameObject o : objects) {
            if (o.isAlive()) o.render();
        }

        cleanup();
    }

    private void spawnEnemies(double dt) {
        enemySpawnTimer -= dt;
        if (enemySpawnTimer > 0) return;

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

    // Same collision fix you already applied: bullet can't multi-hit in one frame
    private void handleCollisions() {
        for (int i = 0; i < objects.size(); i++) {
            GameObject a = objects.get(i);
            if (!a.isAlive()) continue;

            for (int j = i + 1; j < objects.size(); j++) {
                GameObject b = objects.get(j);
                if (!b.isAlive()) continue;

                if (!a.isAlive()) break;

                if (Collision.aabb(a, b)) {
                    onCollision(a, b);
                    if (!a.isAlive()) break;
                }
            }
        }
    }

    private int scoreForEnemy(Enemy e) {
        if (e instanceof EnemyType2) return 1000;
        if (e instanceof EnemyType3) return 150;
        if (e instanceof EnemyType1) return 100;
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
