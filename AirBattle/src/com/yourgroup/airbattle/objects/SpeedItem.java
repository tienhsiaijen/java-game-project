package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;

/**
 * Collectible item that temporarily increases the player's movement speed.
 *
 * <p>{@code SpeedItem} grants a time-limited speed boost, allowing the
 * player to move faster and dodge enemies more effectively. After being
 * collected, the item is immediately removed from the game world.</p>
 *
 * <p>Design intent:
 * <ul>
 *   <li>Encapsulates movement-related power-up logic inside the item.</li>
 *   <li>Applies a temporary effect via {@link Player#boostSpeed(double, double)}.</li>
 *   <li>Demonstrates polymorphism through the {@link Item#apply(Player)} method.</li>
 * </ul>
 * </p>
 */
public class SpeedItem extends Item {

    /**
     * Additional movement speed (in pixels per second) granted by this item.
     */
    private double speedBonus = 100;

    /**
     * Duration (in seconds) for which the speed boost remains active.
     */
    private double duration = 5.0;

    /**
     * Creates a speed-boost power-up item.
     *
     * @param x      initial x-coordinate of the item
     * @param y      initial y-coordinate of the item
     * @param sprite image used to render the item
     */
    public SpeedItem(double x, double y, Image sprite) {
        super(x, y, 30, 30, sprite);
    }

    /**
     * Applies the speed boost effect to the player.
     *
     * <p>The player's movement speed is increased for a limited duration.
     * The item is destroyed immediately after being collected to ensure
     * single-use behavior.</p>
     *
     * @param player the player who collected this item
     */
    @Override
    public void apply(Player player) {
        player.boostSpeed(speedBonus, duration);
        kill();
    }
}
