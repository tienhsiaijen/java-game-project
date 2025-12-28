package com.yourgroup.airbattle.core;

public final class GameConfig {
    private GameConfig() {}

    public static final double WIDTH = 900;
    public static final double HEIGHT = 600;

    /** How far off-screen an object must go before being removed. */
    public static final double OFFSCREEN_MARGIN = 80;
    
    // Enemy spawning
    public static final double ENEMY_SPAWN_MIN_INTERVAL = 0.8; // seconds
    public static final double ENEMY_SPAWN_MAX_INTERVAL = 1.2; // seconds

    public static final double ENEMY_SPAWN_Y = -60;            // spawn above screen
    public static final double ENEMY_SPAWN_MARGIN_X = 20;      // keep away from edges

    // Weighted probabilities (0..99 roll)
    public static final int ENEMY_TYPE1_WEIGHT = 70;           // 70%
    public static final int ENEMY_TYPE3_WEIGHT = 25;           // next 25% (70..94)
    public static final int ENEMY_TYPE2_WEIGHT = 5;            // last 5% (95..99)

    // Item drop configuration
    public static final int ITEM_DROP_RATE_PERCENT = 25; // overall drop chance

    // Item weights (relative probability after a drop occurs)
    public static final int ITEM_HEAL_WEIGHT     = 30;
    public static final int ITEM_RAMPAGE_WEIGHT  = 20;
    public static final int ITEM_SUPER_WEIGHT    = 20;
    public static final int ITEM_SHOTGUN_WEIGHT  = 20;
    public static final int ITEM_SHIELD_WEIGHT   = 10;

}
