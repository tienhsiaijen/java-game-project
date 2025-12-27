package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;

public class enemyType2 extends Enemy {

    public enemyType2(double x, double y, Image sprite) {
        super(x, y, sprite);
        width = 120;
        height = 120;
        speed = 40;
        hp = 20;
    }

    @Override
    public void update(double dt) {
        y += speed * dt;
        x += Math.sin(y * 0.01) * 100 * dt;
    }
}
