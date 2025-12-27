package com.yourgroup.airbattle.core;

import com.yourgroup.airbattle.objects.Bullet;
import com.yourgroup.airbattle.objects.Enemy;
import com.yourgroup.airbattle.objects.GameObject;
import com.yourgroup.airbattle.objects.Item;
import com.yourgroup.airbattle.objects.Player;
import com.yourgroup.airbattle.objects.enemyType1;
import com.yourgroup.airbattle.objects.enemyType2;
import com.yourgroup.airbattle.objects.enemyType3;
import com.yourgroup.airbattle.util.Collision;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * GameWorld manages all active objects, update loop, collision checks and cleanup.
 */
public class GameWorld {

    private final Pane playfield;
    private final List<GameObject> objects = new ArrayList<>();
    private final List<GameObject> pendingAdd = new ArrayList<>();

    private final Random rng = new Random();

    // 资源：只加载一次
    private final Image bulletSprite = new Image("/img/Bullet.png");
    private final Image enemySprite1 = new Image("/img/enemy.png");
    private final Image enemySprite2 = new Image("/img/enemy2.png");
    private final Image enemySprite3 = new Image("/img/enemyFast.png");

    // 刷怪计时
    private double enemySpawnTimer = 0.0;

    public GameWorld(Pane playfield) {
        this.playfield = playfield;
    }

    /** Spawn objects safely (avoid concurrent modification). */
    public void spawn(GameObject obj) {
        pendingAdd.add(obj);
    }

    /** Update all objects for a single frame. */
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

        // 2) update
        for (GameObject o : objects) {
            if (!o.isAlive()) continue;

            o.update(dt);

            if (o instanceof Player player) {
                // 空格开火：Player 已经有 isFiringPressed()
                if (player.canFire() && player.isFiringPressed()) {
                    Bullet b = player.fire(bulletSprite);
                    if (b != null) spawn(b);
                }

                player.clampTo(
                    getWorldWidth(),
                    getWorldHeight()
                );
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

        // 下一波刷怪间隔：0.8 ~ 1.2 秒
        enemySpawnTimer = 0.8 + rng.nextDouble() * 0.4;

        double w = getWorldWidth();
        // 避免 w 还没布局出来导致 0
        if (w < 100) w = 900;

        double x = rng.nextDouble() * (w - 40);
        double y = -60;

        // 概率：70% type1, 25% type3, 5% type2
        int roll = rng.nextInt(100);
        GameObject enemy;

        if (roll < 70) {
            enemy = new enemyType1(x, y, enemySprite1);
        } else if (roll < 95) {
            enemy = new enemyType3(x, y, enemySprite3);
        } else {
            enemy = new enemyType2(x, y, enemySprite2);
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

    private void onCollision(GameObject a, GameObject b) {
        // 子弹 vs 敌人
        if (a.getType() == ObjectType.BULLET_PLAYER && b.getType() == ObjectType.ENEMY) {
            a.kill();
            if (b instanceof Enemy e) e.damage(1);
            return;
        }
        if (b.getType() == ObjectType.BULLET_PLAYER && a.getType() == ObjectType.ENEMY) {
            b.kill();
            if (a instanceof Enemy e) e.damage(1);
            return;
        }

        // 玩家 vs 敌人
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

        // 玩家 vs 道具
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
