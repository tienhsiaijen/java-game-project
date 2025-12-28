package com.yourgroup.airbattle.objects;

import com.yourgroup.airbattle.core.ObjectType;
import javafx.scene.image.Image;

/**
 * Abstract base class for all collectible power-up items.
 *
 * <p>{@code Item} represents any object that the player can collect to
 * gain a beneficial effect, such as restoring health or enhancing abilities.
 * Concrete subclasses define the specific effect applied to the player.</p>
 *
 * <p>Shared behavior:
 * <ul>
 *   <li>Falls downward at a constant speed.</li>
 *   <li>Is removed automatically when it leaves the screen.</li>
 *   <li>Uses a unified {@link ObjectType#POWERUP} category for collision handling.</li>
 * </ul>
 * </p>
 *
 * <p>Design intent:
 * <ul>
 *   <li>Encapsulates item behavior in a single hierarchy.</li>
 *   <li>Demonstrates abstraction through the {@link #apply(Player)} method.</li>
 *   <li>Prevents conditional logic inside {@code Player} by delegating effects
 *       to individual item subclasses.</li>
 * </ul>
 * </p>
 */
public abstract class Item extends GameObject {

    /**
     * Vertical falling speed in pixels per second.
     * Subclasses may adjust this value to change item behavior.
     */
    protected double fallSpeed = 80;

    /**
     * Temporary screen height used for off-screen cleanup.
     *
     * <p>This value matches the current fixed window size (900x600).
     * It can be replaced with a dynamic query if the window becomes resizable.</p>
     */
    private static final double DEFAULT_SCREEN_HEIGHT = 600;

    /**
     * Creates a new collectible item.
     *
     * @param x      initial x-coordinate of the item
     * @param y      initial y-coordinate of the item
     * @param width  render width of the item
     * @param height render height of the item
     * @param sprite image used to render the item
     */
    public Item(double x, double y, double width, double height, Image sprite) {
        super(x, y, width, height, sprite);
    }

    /**
     * @return the logical object type used for collision filtering
     */
    @Override
    public ObjectType getType() {
        // Items are treated as POWERUPs for unified collision handling.
        return ObjectType.POWERUP;
    }

    /**
     * Updates the item's position each frame.
     *
     * <p>The item falls downward at a constant speed. Once it moves beyond
     * the visible screen area, it is marked as dead and removed during cleanup.</p>
     *
     * @param dt delta time in seconds since the last frame
     */
    @Override
    public void update(double dt) {
        // Apply downward movement.
        y += fallSpeed * dt;

        // Remove item once it leaves the screen.
        if (y > DEFAULT_SCREEN_HEIGHT + 50) {
            kill();
        }
    }

    /**
     * Applies the item's effect to the player.
     *
     * <p>This method is called when the player collides with the item.
     * Each concrete subclass defines its own effect.</p>
     *
     * @param player the player who collected this item
     */
    public abstract void apply(Player player);
}
