package com.yourgroup.airbattle.objects;

import com.yourgroup.airbattle.core.ObjectType;
import javafx.scene.image.Image;

/**
 * Visual effect that represents the player's active shield.
 *
 * <p>{@code ShieldEffect} is a non-interactive game object that follows
 * the player while the shield buff is active. It does not participate
 * in collision detection and exists purely for visual feedback.</p>
 *
 * <p>Lifecycle rules:
 * <ul>
 *   <li>The effect follows the player's position every frame.</li>
 *   <li>The effect is automatically destroyed when the shield expires.</li>
 *   <li>The effect is also destroyed if the player dies.</li>
 * </ul>
 * </p>
 *
 * <p>Design intent:
 * <ul>
 *   <li>Separates visual effects from gameplay logic.</li>
 *   <li>Avoids adding rendering responsibilities to {@link Player}.</li>
 *   <li>Uses the same update lifecycle as other {@link GameObject}s.</li>
 * </ul>
 * </p>
 */
public class ShieldEffect extends GameObject {

    /** The player entity this shield effect is attached to. */
    private final Player owner;

    /**
     * Creates a shield visual effect attached to a player.
     *
     * @param owner  the player who owns the shield
     * @param sprite image used to render the shield effect
     */
    public ShieldEffect(Player owner, Image sprite) {
        // Set size slightly larger than the player to visually cover it
        super(owner.getX(), owner.getY(), 80, 80, sprite);
        this.owner = owner;

        // Make the shield semi-transparent for a glowing effect
        view.setOpacity(1.0);
    }

    /**
     * Returns the logical object type.
     *
     * <p>This effect is marked as {@link ObjectType#POWERUP} only to reuse
     * an existing category. It does not participate in collision handling
     * and is ignored by collision logic.</p>
     *
     * @return object type for compatibility with the game world
     */
    @Override
    public ObjectType getType() {
        return ObjectType.POWERUP;
    }

    /**
     * Updates the shield effect each frame.
     *
     * <p>The effect continuously follows the player's position and
     * automatically destroys itself when the shield buff ends or
     * when the player is no longer alive.</p>
     *
     * @param dt delta time in seconds
     */
    @Override
    public void update(double dt) {
        // Follow the player and stay centered
        this.x = owner.getX() - 20;
        this.y = owner.getY() - 20;

        // Destroy the effect when shield expires or player dies
        if (!owner.isAlive() || !owner.isShielded()) {
            kill();
        }
    }
}
