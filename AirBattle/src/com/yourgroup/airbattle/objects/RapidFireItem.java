package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;

public class RapidFireItem extends Item {

    private double newCooldown = 0.1;
    private double duration = 5.0;

    public RapidFireItem(double x, double y, Image sprite) {
        super(x, y, 30, 30, sprite);
    }

    @Override
    public void apply(Player player) {
        player.boostFireRate(newCooldown, duration);
        kill();
    }
}
