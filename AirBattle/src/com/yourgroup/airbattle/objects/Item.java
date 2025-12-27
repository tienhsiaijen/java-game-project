package com.yourgroup.airbattle.objects;

import com.yourgroup.airbattle.core.ObjectType;
import javafx.scene.image.Image;

public abstract class Item extends GameObject {

    protected double fallSpeed = 80;

    // 临时使用固定窗口大小（MainApp: 900x600）
    private static final double SCREEN_H = 600;

    public Item(double x, double y, double width, double height, Image sprite) {
        super(x, y, width, height, sprite);
    }

    @Override
    public ObjectType getType() {
        // ObjectType 里没有 ITEM，统一用 POWERUP
        return ObjectType.POWERUP;
    }

    @Override
    public void update(double dt) {
        y += fallSpeed * dt;

        // 超出屏幕后移除
        if (y > SCREEN_H + 50) {
            kill();
        }
    }

    public abstract void apply(Player player);
}
