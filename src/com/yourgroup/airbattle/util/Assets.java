package com.yourgroup.airbattle.util;

import javafx.scene.image.Image;

import java.io.InputStream;

/**
 * Utility class for loading game assets from the classpath.
 *
 * <p>{@code Assets} centralizes resource loading logic to ensure images
 * are loaded consistently across the application. It also provides
 * fallback behavior to handle differences in project structure or
 * build output directories.</p>
 *
 * <p>Design intent:
 * <ul>
 *   <li>Prevents asset-loading logic from being duplicated throughout the codebase.</li>
 *   <li>Encapsulates classpath access details in a single utility class.</li>
 *   <li>Provides clear failure feedback when an asset cannot be found.</li>
 * </ul>
 * </p>
 */
public final class Assets {

    /** Prevent instantiation of utility class. */
    private Assets() {}

    /**
     * Loads an image from the classpath.
     *
     * <p>The method first attempts to load the image using the provided path
     * (e.g. {@code "/img/example.png"}). If the resource cannot be found and
     * the path starts with {@code "/img/"}, a fallback attempt is made by
     * removing the {@code "/img"} prefix. This improves compatibility with
     * different project layouts and build configurations.</p>
     *
     * @param path classpath-relative path to the image resource
     * @return loaded {@link Image}
     * @throws IllegalArgumentException if the image cannot be found
     */
    public static Image image(String path) {
        // Attempt to load the image using the provided classpath.
        InputStream is = Assets.class.getResourceAsStream(path);

        // Fallback: handle cases where images are located at the classpath root.
        if (is == null && path.startsWith("/img/")) {
            String fallback = path.substring(4); // "/img/example.png" -> "/example.png"
            is = Assets.class.getResourceAsStream(fallback);
        }

        if (is == null) {
            throw new IllegalArgumentException(
                "Image not found on classpath: " + path + " (also tried without /img/)"
            );
        }

        return new Image(is);
    }
}
