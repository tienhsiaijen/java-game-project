package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;

/**
 * Super bullet power-up that enhances bullet damage and effectiveness.
 *
 * <p>{@code ItemSuper} grants the player a temporary upgrade that allows
 * bullets to deal increased damage (and/or enhanced properties, depending
 * on player implementation). This item is designed to help defeat
 * high-health enemies more efficiently.</p>
 *
 * <p>Design intent:
 * <ul>
 *   <li>Demonstrates a weapon-enhancing power-up.</li>
 *   <li>Delegates bullet behavior and timing to {@link Player}.</li>
 *   <li>Keeps item logic minimal by only triggering a player state change.</li>
 * </ul>
 * </p>
 */
public class ItemSuper extends Item {

    /**
     * Creates a super bullet power-up item at the specified position.
     *
     * @param x      initial x-coordinate of the item
     * @param y      initial y-coordinate of the item
     * @param sprite image used to render the item
     */
    public ItemSuper(double x, double y, Image sprite) {
        super(x, y, 40, 40, sprite);
    }

    /**
     * Applies the super bullet effect to the player.
     *
     * <p>When collected, the player enters a super bullet mode for a fixed
     * duration, during which bullets deal increased damage compared to
     * standard projectiles.</p>
     *
     * @param player the player who collected this item
     */
    @Override
    public void apply(Player player) {
        // Enable super bullet mode for 10 seconds
        player.activateSuperBullet(10.0);
    }
}
