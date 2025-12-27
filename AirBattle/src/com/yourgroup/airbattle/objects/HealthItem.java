package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;

public class HealthItem extends Item {

    private int healAmount = 1;

    public HealthItem(double x, double y, Image sprite) {
        super(x, y, 30, 30, sprite);
    }

    @Override
    public void apply(Player player) {
        player.heal(healAmount);
        kill();
    }
}
