package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;

/**
 * Shotgun power-up that enables multi-directional firing.
 *
 * <p>{@code ItemShotgun} temporarily allows the player to fire multiple
 * bullets in a spread pattern, increasing damage output at close range.
 * This item is designed to handle clusters of enemies more effectively.</p>
 *
 * <p>Design intent:
 * <ul>
 *   <li>Demonstrates a weapon-modifying power-up.</li>
 *   <li>Delegates firing logic and timing to {@link Player}.</li>
 *   <li>Avoids embedding weapon behavior directly inside the item.</li>
 * </ul>
 * </p>
 */
public class ItemShotgun extends Item {

    /**
     * Creates a shotgun power-up item at the specified position.
     *
     * @param x      initial x-coordinate of the item
     * @param y      initial y-coordinate of the item
     * @param sprite image used to render the item
     */
    public ItemShotgun(double x, double y, Image sprite) {
        super(x, y, 40, 40, sprite);
    }

    /**
     * Applies the shotgun firing mode to the player.
     *
     * <p>When collected, the player enters a shotgun mode for a fixed duration,
     * during which each firing action produces multiple angled bullets.</p>
     *
     * @param player the player who collected this item
     */
    @Override
    public void apply(Player player) {
        // Enable shotgun firing mode for 10 seconds
        player.activateShotgun(10.0);
    }
}
