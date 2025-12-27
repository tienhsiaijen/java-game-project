package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;
import com.yourgroup.airbattle.core.ObjectType;

public class Bullet extends GameObject {

    private double speed = 400;

    // 临时窗口高度：MainApp 900x600
    private static final double SCREEN_H = 600;

    public Bullet(double x, double y, Image sprite) {
        super(x, y, 6, 12, sprite);
    }

    @Override
    public ObjectType getType() {
        return ObjectType.BULLET_PLAYER;
    }

    @Override
    public void update(double dt) {
        y -= speed * dt;

        // 离屏清理
        if (y + height < -30 || y > SCREEN_H + 30) {
            kill();
        }
    }
}
