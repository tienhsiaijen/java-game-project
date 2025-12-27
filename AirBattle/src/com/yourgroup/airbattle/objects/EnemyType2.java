package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;

public class EnemyType2 extends Enemy {

    private double dir = 1;

    private static final double SCREEN_W = 900;
    private static final double SCREEN_H = 600;

    public EnemyType2(double x, double y, Image sprite) {
        super(x, y, sprite);

        // 关键：用 setSize 同步 view，否则会出现“视觉没碰到就判定碰到”
        setSize(120, 120);

        // Boss：跟 fast 一样的移动节奏（快）
        speed = 240;
        hp = 10; // 多滴血（你想更肉就改成 15/20）
    }

    // Boss 命中盒再缩一点，避免大图透明边导致提前碰撞
    @Override
    protected double hitboxInsetX() { return 14; }

    @Override
    protected double hitboxInsetY() { return 14; }

    @Override
    public void update(double dt) {
        y += speed * dt;
        x += dir * 260 * dt;

        if (x <= 0 || x + width >= SCREEN_W) {
            dir *= -1;
        }

        if (y > SCREEN_H + 80) kill();
    }
}
