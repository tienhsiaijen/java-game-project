package com.yourgroup.airbattle.objects;

import com.yourgroup.airbattle.core.ObjectType;
import javafx.scene.image.Image;

public class Enemy extends GameObject {

    protected double speed = 100;
    protected int hp = 1;

    private static final double SCREEN_H = 600;

    public Enemy(double x, double y, Image sprite) {
        super(x, y, 40, 40, sprite);
    }

    @Override
    public ObjectType getType() {
        return ObjectType.ENEMY;
    }

    @Override
    public void update(double dt) {
        y += speed * dt;
        if (y > SCREEN_H + 50) kill();
    }

    public void damage(int amount) {
        hp -= amount;
        if (hp <= 0) kill();
    }

    public int getHp() {
        return hp;
    }
}
