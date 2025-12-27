package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;

/**
 * Collectible item that restores the player's health.
 *
 * <p>{@code HealthItem} is a single-use power-up. When the player collides
 * with this item, it immediately restores a fixed amount of health and
 * is then removed from the game world.</p>
 *
 * <p>Design intent:
 * <ul>
 *   <li>Encapsulates health restoration logic inside the item itself.</li>
 *   <li>Demonstrates polymorphism by overriding {@link #apply(Player)}.</li>
 *   <li>Keeps player logic simple by delegating item effects to item classes.</li>
 * </ul>
 * </p>
 */
public class HealthItem extends Item {

    /**
     * Amount of health restored when the item is collected.
     * This value can be tuned to adjust game difficulty.
     */
    private int healAmount = 1;

    /**
     * Creates a health item at the specified position.
     *
     * @param x      initial x-coordinate of the item
     * @param y      initial y-coordinate of the item
     * @param sprite image used to render the item
     */
    public HealthItem(double x, double y, Image sprite) {
        super(x, y, 30, 30, sprite);
    }

    /**
     * Applies the item's effect to the player.
     *
     * <p>The player is healed by {@link #healAmount} and the item is
     * immediately destroyed to ensure it can only be used once.</p>
     *
     * @param player the player who collected this item
     */
    @Override
    public void apply(Player player) {
        player.heal(healAmount);
        kill();
    }
}
