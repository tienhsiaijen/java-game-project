package com.yourgroup.airbattle.objects;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.yourgroup.airbattle.core.ObjectType;

/**
 * Abstract base class for all in-game entities in AirBattle.
 *
 * <p>{@code GameObject} encapsulates shared state and behavior such as:
 * <ul>
 *   <li>World position (x, y) and render size (width, height)</li>
 *   <li>Life-cycle control (alive/dead) for cleanup</li>
 *   <li>JavaFX rendering via an {@link ImageView}</li>
 *   <li>Hitbox boundaries used by collision detection</li>
 * </ul>
 * </p>
 *
 * <p>Subclasses (e.g., {@code Player}, {@code Enemy}, {@code Bullet}, {@code Item})
 * implement:
 * <ul>
 *   <li>{@link #getType()} to support rule-based collision handling</li>
 *   <li>{@link #update(double)} to define per-frame logic (movement, cooldowns, AI)</li>
 * </ul>
 * </p>
 *
 * <p>Design notes:
 * <ul>
 *   <li>Rendering is separated from logic: {@link #update(double)} modifies state,
 *       {@link #render()} applies state to the JavaFX node.</li>
 *   <li>Hitbox inset hooks allow sprites with transparent padding to have more accurate collisions.</li>
 * </ul>
 * </p>
 */
public abstract class GameObject {

    /** World position (top-left corner) in pixels. */
    protected double x;
    protected double y;

    /** Render size in pixels. */
    protected double width;
    protected double height;

    /**
     * Life-cycle flag.
     * When false, the object will be removed from the world during cleanup.
     */
    protected boolean alive = true;

    /**
     * JavaFX node used for rendering (default: {@link ImageView}).
     * The world will add/remove this node to/from the playfield.
     */
    protected final ImageView view = new ImageView();

    /**
     * Constructs a new game object with position, size, and an optional sprite.
     *
     * <p>The sprite is assigned to the internal {@link ImageView}, and the view
     * is positioned immediately using {@link #relocateView()}.</p>
     *
     * @param x      initial x-coordinate (top-left) in world space
     * @param y      initial y-coordinate (top-left) in world space
     * @param width  initial render width in pixels
     * @param height initial render height in pixels
     * @param sprite sprite image for rendering (can be null for non-image objects)
     */
    public GameObject(double x, double y, double width, double height, Image sprite) {
        this.x = x;
        this.y = y;
        setSize(width, height);

        if (sprite != null) {
            view.setImage(sprite);
        }
        relocateView();
    }

    /**
     * Returns the logical type of this object.
     *
     * <p>This is used for collision filtering and rule resolution inside the game world.</p>
     *
     * @return the {@link ObjectType} category of this object
     */
    public abstract ObjectType getType();

    /**
     * Updates the object's internal logic for one frame.
     *
     * <p>Subclasses should update position, timers, AI, cooldowns, etc. here.
     * Rendering should be handled separately via {@link #render()}.</p>
     *
     * @param dt delta time in seconds since the last frame
     */
    public abstract void update(double dt);

    /**
     * Applies the current logical state (x, y, etc.) to the JavaFX node.
     *
     * <p>By default, this simply relocates the view. Subclasses may override
     * if they need additional visual updates (e.g., rotation, animation).</p>
     */
    public void render() {
        relocateView();
    }

    /**
     * @return the JavaFX node representing this object in the scene graph
     */
    public Node getNode() {
        return view;
    }

    /**
     * Marks this object as dead. The world will remove it during cleanup.
     */
    public void kill() {
        this.alive = false;
    }

    /**
     * @return true if the object is active; false if it should be removed
     */
    public boolean isAlive() {
        return alive;
    }

    /**
     * Moves the JavaFX view to match the object's world coordinates.
     * Uses translate properties to avoid layout interference.
     */
    protected void relocateView() {
        view.setTranslateX(x);
        view.setTranslateY(y);
    }

    /**
     * Sets the render size of this object and synchronizes it with the {@link ImageView}.
     *
     * <p>Important: always use this method if you change size after construction.
     * Updating {@code width}/{@code height} without updating the view can cause
     * a mismatch between visuals and collision boundaries.</p>
     *
     * @param w new width in pixels
     * @param h new height in pixels
     */
    protected void setSize(double width, double height) {
        this.width = width;
        this.height = height;
        view.setFitWidth(width);
        view.setFitHeight(height);
    }

    // ===== Hitbox / collision boundaries =====

    /**
     * Horizontal inset (in pixels) applied to the hitbox on both left and right sides.
     *
     * <p>Default is 0 (hitbox matches sprite bounds). Subclasses may override
     * to shrink the hitbox if the sprite contains transparent padding.</p>
     *
     * @return horizontal inset in pixels
     */
    protected double hitboxInsetX() { return 0; }

    /**
     * Vertical inset (in pixels) applied to the hitbox on both top and bottom sides.
     *
     * @return vertical inset in pixels
     */
    protected double hitboxInsetY() { return 0; }

    /**
     * @return left boundary of the hitbox in world coordinates
     */
    public double hitLeft()   { return x + hitboxInsetX(); }

    /**
     * @return right boundary of the hitbox in world coordinates
     */
    public double hitRight()  { return x + width - hitboxInsetX(); }

    /**
     * @return top boundary of the hitbox in world coordinates
     */
    public double hitTop()    { return y + hitboxInsetY(); }

    /**
     * @return bottom boundary of the hitbox in world coordinates
     */
    public double hitBottom() { return y + height - hitboxInsetY(); }

    // ===== Legacy bounds (sprite bounds) =====

    /** @return left boundary of the sprite bounds in world coordinates */
    public double left()   { return x; }

    /** @return right boundary of the sprite bounds in world coordinates */
    public double right()  { return x + width; }

    /** @return top boundary of the sprite bounds in world coordinates */
    public double top()    { return y; }

    /** @return bottom boundary of the sprite bounds in world coordinates */
    public double bottom() { return y + height; }

    /** @return current x-coordinate (top-left) */
    public double getX() { return x; }

    /** @return current y-coordinate (top-left) */
    public double getY() { return y; }

    /** @return render width in pixels */
    public double getWidth() { return width; }

    /** @return render height in pixels */
    public double getHeight() { return height; }
}
