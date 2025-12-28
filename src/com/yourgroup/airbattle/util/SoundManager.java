package com.yourgroup.airbattle.util;

import javafx.scene.media.AudioClip;
import java.net.URL;

/**
 * Centralized sound manager for background music (BGM) and sound effects (SFX).
 *
 * <p>{@code SoundManager} is responsible for loading and playing all audio
 * resources used in the game. Background music is managed in a mutually
 * exclusive manner to ensure only one track plays at a time, while sound
 * effects can be triggered independently.</p>
 *
 * <p>Design intent:
 * <ul>
 *   <li>Centralizes all audio-related logic in one utility class.</li>
 *   <li>Preloads audio resources to avoid runtime delays.</li>
 *   <li>Separates background music (BGM) and sound effects (SFX).</li>
 * </ul>
 * </p>
 */
public final class SoundManager {

    /** Background music for the start / menu screen. */
    private static AudioClip startBgm;

    /** Background music played during gameplay. */
    private static AudioClip fightBgm;

    /** Background music played on the game-over screen. */
    private static AudioClip gameOverBgm;

    /** Sound effect played when the player fires a bullet. */
    private static AudioClip shootSound;

    /** Sound effect played when an enemy explodes. */
    private static AudioClip explodeSound;

    /** Currently playing background music (used for mutual exclusion). */
    private static AudioClip currentBgm;

    /**
     * Static initialization block that preloads all audio resources.
     *
     * <p>Audio files are expected to be located on the classpath under
     * the {@code /sound} directory.</p>
     */
    static {
        try {
            startBgm    = load("/sound/GameStartBgm.mp3");
            fightBgm    = load("/sound/FightingBgm.mp3");
            gameOverBgm = load("/sound/GameOverBgm.mp3");

            shootSound  = load("/sound/PlayerShoot.mp3");
            explodeSound = load("/sound/EnemyExplosion.mp3");

        } catch (Exception e) {
            System.err.println("Audio loading failed. Please check sound file paths.");
            e.printStackTrace();
        }
    }

    /** Prevent instantiation of utility class. */
    private SoundManager() {}

    /**
     * Loads an {@link AudioClip} from the classpath.
     *
     * @param path classpath-relative path to the audio file
     * @return loaded {@link AudioClip}, or {@code null} if not found
     */
    private static AudioClip load(String path) {
        URL url = SoundManager.class.getResource(path);
        if (url == null) {
            System.err.println("Sound file not found: " + path);
            return null;
        }
        return new AudioClip(url.toExternalForm());
    }

    // ===== Background Music (BGM) =====

    /** Plays the start/menu background music. */
    public static void playStartBgm() {
        playBgmInternal(startBgm);
    }

    /** Plays the in-game background music. */
    public static void playFightBgm() {
        playBgmInternal(fightBgm);
    }

    /** Plays the game-over background music. */
    public static void playGameOverBgm() {
        playBgmInternal(gameOverBgm);
    }

    /**
     * Core logic for switching background music.
     *
     * <p>Stops any currently playing track before starting a new one.</p>
     *
     * @param newBgm the background music clip to play
     */
    private static void playBgmInternal(AudioClip newBgm) {
        if (currentBgm != null) {
            currentBgm.stop();
        }

        if (newBgm != null) {
            newBgm.setCycleCount(AudioClip.INDEFINITE);
            newBgm.play(0.5); // 50% volume
            currentBgm = newBgm;
        }
    }

    /** Stops any currently playing background music. */
    public static void stopBgm() {
        if (currentBgm != null) {
            currentBgm.stop();
            currentBgm = null;
        }
    }

    // ===== Sound Effects (SFX) =====

    /** Plays the shooting sound effect. */
    public static void playShoot() {
        if (shootSound != null) {
            shootSound.play(0.3);
        }
    }

    /** Plays the enemy explosion sound effect. */
    public static void playExplosion() {
        if (explodeSound != null) {
            explodeSound.play(1.0);
        }
    }
}
