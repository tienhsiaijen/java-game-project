package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;

/**
 * Rampage item that enables temporary automatic firing.
 *
 * <p>{@code ItemRampage} grants the player a short burst of increased
 * offensive capability by enabling automatic firing for a limited
 * duration. This item is designed to help the player handle
 * high-pressure situations with many enemies on screen.</p>
 *
 * <p>Design intent:
 * <ul>
 *   <li>Demonstrates a time-based power-up effect.</li>
 *   <li>Delegates effect duration management to {@link Player}.</li>
 *   <li>Keeps item logic simple by triggering state changes rather than
 *       managing timers internally.</li>
 * </ul>
 * </p>
 */
public class ItemRampage extends Item {

    /**
     * Creates a rampage power-up item at the specified position.
     *
     * @param x      initial x-coordinate of the item
     * @param y      initial y-coordinate of the item
     * @param sprite image used to render the item
     */
    public ItemRampage(double x, double y, Image sprite) {
        super(x, y, 40, 40, sprite);
    }

    /**
     * Applies the rampage effect to the player.
     *
     * <p>When collected, the player enters an auto-fire state for
     * a fixed duration. During this period, bullets are fired
     * automatically without requiring manual input.</p>
     *
     * @param player the player who collected this item
     */
    @Override
    public void apply(Player player) {
        // Enable automatic firing for 10 seconds
        player.activateAutoFire(10.0);
    }
}
