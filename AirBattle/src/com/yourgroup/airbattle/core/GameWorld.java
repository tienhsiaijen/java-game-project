package com.yourgroup.airbattle.core;

import com.yourgroup.airbattle.objects.Bullet;
import com.yourgroup.airbattle.objects.Enemy;
import com.yourgroup.airbattle.objects.GameObject;
import com.yourgroup.airbattle.objects.Item;
import com.yourgroup.airbattle.objects.Player;
import com.yourgroup.airbattle.util.Collision;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * GameWorld manages all active objects, update loop, collision checks and cleanup.
 */
public class GameWorld {

    private final Pane playfield;
    private final List<GameObject> objects = new ArrayList<>();
    private final List<GameObject> pendingAdd = new ArrayList<>();

    // 子弹图片只加载一次
    private final Image bulletSprite = new Image("/img/Bullet.png");

    public GameWorld(Pane playfield) {
        this.playfield = playfield;
    }

    /** Spawn objects safely (avoid concurrent modification). */
    public void spawn(GameObject obj) {
        pendingAdd.add(obj);
    }

    /** Update all objects for a single frame. */
    public void update(double dt) {
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
                // 接入开火（按空格）
                if (player != null && player.canFire() && playerFirePressed(player)) {
                    Bullet b = player.fire(bulletSprite);
                    if (b != null) spawn(b);
                }

                player.clampTo(
                    playfield.getLayoutBounds().getWidth(),
                    playfield.getLayoutBounds().getHeight()
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

    // 通过类型/输入判断是否开火：避免 GameWorld 直接依赖 InputHandler
    private boolean playerFirePressed(Player player) {
        // Player 内部已经持有 InputHandler；这里用最小侵入方式：
        // 让 Player.update() 不管开火，由 GameWorld 在帧里触发。
        // 但 Player 没暴露 input，因此直接用 ObjectType 不行。
        // 解决：把“是否按下空格”的判断留在 Player（最干净）。
        // 目前你 Player 里没有 isFiringPressed()，所以这里先走最保守方案：
        // ——让玩家只要能开火就自动开火（你不想这样的话，看下面“下一步”我会让你加一个方法）
        return true;
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
            b.kill(); // 你也可以选择不 kill 敌人
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
