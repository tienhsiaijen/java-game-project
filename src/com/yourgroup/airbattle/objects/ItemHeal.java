package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;

/**
 * Healing item that restores the player's health.
 *
 * <p>{@code ItemHeal} represents a simple collectible that increases
 * the player's HP upon pickup. This item provides immediate benefit
 * and does not apply any time-based or temporary effects.</p>
 *
 * <p>Design intent:
 * <ul>
 *   <li>Demonstrates a single-responsibility item with an instant effect.</li>
 *   <li>Implements {@link Item#apply(Player)} to encapsulate healing logic.</li>
 *   <li>Avoids conditional logic inside {@link Player} by handling the effect here.</li>
 * </ul>
 * </p>
 */
public class ItemHeal extends Item {

    /**
     * Creates a healing item at the specified position.
     *
     * @param x      initial x-coordinate of the item
     * @param y      initial y-coordinate of the item
     * @param sprite image used to render the item
     */
    public ItemHeal(double x, double y, Image sprite) {
        super(x, y, 40, 40, sprite);
    }

    /**
     * Applies the healing effect to the player.
     *
     * <p>This method is called when the player collides with the item.
     * The player's health is increased by a fixed amount.</p>
     *
     * @param player the player who collected this item
     */
    @Override
    public void apply(Player player) {
        player.heal(1);

        // Optional: play a sound effect to indicate item pickup
        // com.yourgroup.airbattle.util.SoundManager.playItemGet();
    }
}
