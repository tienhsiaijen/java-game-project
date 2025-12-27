package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;

public class enemyType3 extends Enemy {

    private double dir = 1;

    private static final double SCREEN_W = 900;
    private static final double SCREEN_H = 600;

    public enemyType3(double x, double y, Image sprite) {
        super(x, y, sprite);
        speed = 120;
        hp = 2;
    }

    @Override
    public void update(double dt) {
        y += speed * dt;
        x += dir * 150 * dt;

        if (x <= 0 || x + width >= SCREEN_W) {
            dir *= -1;
        }

        if (y > SCREEN_H + 50) kill();
    }
}
