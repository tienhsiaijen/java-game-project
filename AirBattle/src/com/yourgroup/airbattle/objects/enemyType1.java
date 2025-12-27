package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;

public class enemyType1 extends Enemy {

    private static final double SCREEN_H = 600;

    public enemyType1(double x, double y, Image sprite) {
        super(x, y, sprite);
        speed = 200;
        hp = 1;
    }

    @Override
    public void update(double dt) {
        y += speed * dt;
        x += Math.sin(y * 0.05) * 50 * dt;
        if (y > SCREEN_H + 50) kill();
    }
}
