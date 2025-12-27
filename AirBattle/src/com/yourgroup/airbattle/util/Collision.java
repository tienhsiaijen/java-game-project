package com.yourgroup.airbattle.util;

import com.yourgroup.airbattle.objects.GameObject;

/**
 * Collision utilities for AirBattle.
 * Uses Axis-Aligned Bounding Box (AABB) with hitbox insets.
 */
public final class Collision {

    private Collision() {}

    public static boolean aabb(GameObject a, GameObject b) {
        return a.hitRight()  > b.hitLeft()
            && a.hitLeft()   < b.hitRight()
            && a.hitBottom() > b.hitTop()
            && a.hitTop()    < b.hitBottom();
    }
}
