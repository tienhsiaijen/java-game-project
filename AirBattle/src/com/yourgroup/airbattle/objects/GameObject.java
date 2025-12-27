package com.yourgroup.airbattle.objects;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.yourgroup.airbattle.core.ObjectType;


/**
 * Base class for all game entities in AirBattle.
 * <p>
 * This is the core OOP parent class. All entities such as Player, Enemy, Bullet,
 * and Powerup should extend this class.
 * </p>
 */
public abstract class GameObject {

    /** World position (top-left). */
    protected double x;
    protected double y;

    /** Render size. */
    protected double width;
    protected double height;

    /** Alive flag for cleanup. */
    protected boolean alive = true;

    /** JavaFX node used for rendering (default: ImageView). */
    protected final ImageView view = new ImageView();

    /**
     * Construct a game object with sprite.
     *
     * @param x      initial x
     * @param y      initial y
     * @param width  render width
     * @param height render height
     * @param sprite image sprite (can be null for placeholder)
     */
    public GameObject(double x, double y, double width, double height, Image sprite) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        view.setFitWidth(width);
        view.setFitHeight(height);

        if (sprite != null) {
            view.setImage(sprite);
        }
        relocateView();
    }

    /**
     * @return object type for collision rules
     */
    public abstract ObjectType getType();

    /**
     * Update per frame.
     *
     * @param dt delta time in seconds
     */
    public abstract void update(double dt);


    /**
     * Render step (default: sync node position).
     * Subclasses may override for animation effects.
     */
    public void render() {
        relocateView();
    }

    /** @return JavaFX Node to add into the scene graph */
    public Node getNode() {
        return view;
    }

    /** Mark this object as dead for cleanup. */
    public void kill() {
        this.alive = false;
    }

    /** @return true if this object should stay in world */
    public boolean isAlive() {
        return alive;
    }

    /** Sync ImageView position with x,y. */
    	protected void relocateView() {
    	    view.setTranslateX(x);
    	    view.setTranslateY(y);
    	}
    

    // --- Getters for collision (AABB) ---

    public double left()   { return x; }
    public double right()  { return x + width; }
    public double top()    { return y; }
    public double bottom() { return y + height; }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}
