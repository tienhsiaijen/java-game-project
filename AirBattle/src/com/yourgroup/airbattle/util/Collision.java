package com.yourgroup.airbattle.util;

import com.yourgroup.airbattle.objects.GameObject;

/**
 * Collision utilities for AirBattle.
 * Uses Axis-Aligned Bounding Box (AABB).
 */
public final class Collision {

    private Collision() {}

    /**
     * AABB collision check.
     *
     * @param a object A
     * @param b object B
     * @return true if rectangles overlap
     */
    public static boolean aabb(GameObject a, GameObject b) {
        return a.right()  > b.left()
            && a.left()   < b.right()
            && a.bottom() > b.top()
            && a.top()    < b.bottom();
    }
}
