package com.yourgroup.airbattle.core;

/**
 * Defines logical categories for all game objects.
 *
 * <p>This enum is primarily used for collision filtering and rule resolution,
 * allowing the game to determine how two objects should interact when they
 * intersect.</p>
 *
 * <p>Using {@code ObjectType} instead of {@code instanceof} checks everywhere
 * helps to:
 * <ul>
 *   <li>Centralize collision rules in one place,</li>
 *   <li>Improve readability of collision-handling code,</li>
 *   <li>Make it easier to extend the game with new object types.</li>
 * </ul>
 * </p>
 *
 * <p>Typical usage:
 * <pre>
 * if (a.getType() == ObjectType.PLAYER && b.getType() == ObjectType.ENEMY) {
 *     // resolve player-enemy collision
 * }
 * </pre>
 * </p>
 */
public enum ObjectType {

    /** The player-controlled character. */
    PLAYER,

    /** Any hostile enemy unit controlled by the game. */
    ENEMY,

    /** Bullet fired by the player. */
    BULLET_PLAYER,

    /** Bullet fired by an enemy. */
    BULLET_ENEMY,

    /** Collectible item that provides a temporary or permanent effect. */
    POWERUP,
    /** 
     * Non-interactive visual or status effect that follows or decorates a game object.
     * Effects are not collectible and do not participate in collision handling.
     */
    EFFECT
}
