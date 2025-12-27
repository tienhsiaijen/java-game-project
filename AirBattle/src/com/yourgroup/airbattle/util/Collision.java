package com.yourgroup.airbattle.util;

import com.yourgroup.airbattle.objects.GameObject;

/**
 * Collision utility class for AirBattle.
 *
 * <p>This class provides static methods for collision detection between
 * {@link GameObject} instances. It currently implements Axis-Aligned
 * Bounding Box (AABB) collision checks using each object's hitbox
 * boundaries.</p>
 *
 * <p>Design intent:
 * <ul>
 *   <li>Centralizes collision logic to avoid duplication across the codebase.</li>
 *   <li>Uses hitbox insets defined in {@link GameObject} to improve collision accuracy
 *       when sprites contain transparent padding.</li>
 *   <li>Provides a lightweight and efficient collision check suitable for
 *       real-time gameplay.</li>
 * </ul>
 * </p>
 */
public final class Collision {

    /**
     * Private constructor to prevent instantiation.
     *
     * <p>This class is intended to be used as a static utility.</p>
     */
    private Collision() {}

    /**
     * Performs an Axis-Aligned Bounding Box (AABB) collision test
     * between two game objects.
     *
     * <p>The method checks whether the hitbox of object {@code a}
     * overlaps with the hitbox of object {@code b} in both the
     * horizontal and vertical axes.</p>
     *
     * <p>This collision test is symmetric:
     * {@code aabb(a, b)} produces the same result as {@code aabb(b, a)}.</p>
     *
     * @param a the first game object
     * @param b the second game object
     * @return true if the two objects' hitboxes intersect; false otherwise
     */
    public static boolean aabb(GameObject a, GameObject b) {
        return a.hitRight()  > b.hitLeft()
            && a.hitLeft()   < b.hitRight()
            && a.hitBottom() > b.hitTop()
            && a.hitTop()    < b.hitBottom();
    }
}
