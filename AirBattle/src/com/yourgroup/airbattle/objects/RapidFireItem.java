package com.yourgroup.airbattle.objects;

import javafx.scene.image.Image;

/**
 * Collectible item that temporarily increases the player's firing rate.
 *
 * <p>{@code RapidFireItem} reduces the delay between consecutive shots
 * for a limited duration, allowing the player to fire bullets more
 * frequently and increase damage output.</p>
 *
 * <p>Design intent:
 * <ul>
 *   <li>Encapsulates fire-rate modification logic inside the item itself.</li>
 *   <li>Uses {@link Player#boostFireRate(double, double)} to apply a
 *       time-limited effect.</li>
 *   <li>Demonstrates polymorphism by providing a concrete implementation
 *       of {@link Item#apply(Player)}.</li>
 * </ul>
 * </p>
 */
public class RapidFireItem extends Item {

    /**
     * New firing cooldown (in seconds) applied while the effect is active.
     * A smaller value results in faster shooting.
     */
    private double newCooldown = 0.1;

    /**
     * Duration (in seconds) for which the rapid-fire effect remains active.
     */
    private double duration = 5.0;

    /**
     * Creates a rapid-fire power-up item.
     *
     * @param x      initial x-coordinate of the item
     * @param y      initial y-coordinate of the item
     * @param sprite image used to render the item
     */
    public RapidFireItem(double x, double y, Image sprite) {
        super(x, y, 30, 30, sprite);
    }

    /**
     * Applies the rapid-fire effect to the player.
     *
     * <p>The player's firing cooldown is temporarily reduced, allowing
     * faster shooting. The item is destroyed immediately after being
     * collected to ensure it can only be used once.</p>
     *
     * @param player the player who collected this item
     */
    @Override
    public void apply(Player player) {
        player.boostFireRate(newCooldown, duration);
        kill();
    }
}
