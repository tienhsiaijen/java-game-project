package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;

public class SpeedItem extends Item {

    private double speedBonus = 100;
    private double duration = 5.0;

    public SpeedItem(double x, double y, Image sprite) {
        super(x, y, 30, 30, sprite);
    }

    @Override
    public void apply(Player player) {
        player.boostSpeed(speedBonus, duration);
        kill();
    }
}
