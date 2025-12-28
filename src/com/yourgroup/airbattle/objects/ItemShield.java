package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;

/**
 * Defensive power-up that grants a temporary shield and speed boost.
 *
 * <p>{@code ItemShield} provides a dual-effect buff when collected:
 * the player becomes protected by a shield while also gaining increased
 * movement speed for a limited duration. This item helps the player
 * survive dense enemy waves and reposition quickly.</p>
 *
 * <p>Design intent:
 * <ul>
 *   <li>Demonstrates a composite power-up with multiple simultaneous effects.</li>
 *   <li>Delegates duration and state management to {@link Player}.</li>
 *   <li>Keeps item logic minimal by triggering player state changes only.</li>
 * </ul>
 * </p>
 */
public class ItemShield extends Item {

    /**
     * Creates a shield power-up item at the specified position.
     *
     * @param x      initial x-coordinate of the item
     * @param y      initial y-coordinate of the item
     * @param sprite image used to render the item
     */
    public ItemShield(double x, double y, Image sprite) {
        super(x, y, 40, 40, sprite);
    }

    /**
     * Applies the shield and speed boost effects to the player.
     *
     * <p>When collected:
     * <ul>
     *   <li>A protective shield is activated for a fixed duration.</li>
     *   <li>The player's movement speed is increased for the same duration.</li>
     * </ul>
     * </p>
     *
     * @param player the player who collected this item
     */
    @Override
    public void apply(Player player) {
        // Activate shield protection for 10 seconds
        player.activateShield(10.0);

        // Increase movement speed by 100 units for 10 seconds
        player.boostSpeed(100, 10.0);
    }
}
