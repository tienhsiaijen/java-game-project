package com.yourgroup.airbattle.objects;

import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import com.yourgroup.airbattle.core.ObjectType;

/**
 * Base class for all game entities in AirBattle.
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

    public GameObject(double x, double y, double width, double height, Image sprite) {
        this.x = x;
        this.y = y;
        setSize(width, height);

        if (sprite != null) {
            view.setImage(sprite);
        }
        relocateView();
    }

    public abstract ObjectType getType();
    public abstract void update(double dt);

    public void render() {
        relocateView();
    }

    public Node getNode() {
        return view;
    }

    public void kill() {
        this.alive = false;
    }

    public boolean isAlive() {
        return alive;
    }

    protected void relocateView() {
        view.setTranslateX(x);
        view.setTranslateY(y);
    }

    /** IMPORTANT: always use this if you change size after construction */
    protected void setSize(double w, double h) {
        this.width = w;
        this.height = h;
        view.setFitWidth(w);
        view.setFitHeight(h);
    }

    // ===== Collision box (hitbox) =====
    // Default: no inset. Subclasses can override to make hitbox smaller than sprite.
    protected double hitboxInsetX() { return 0; }
    protected double hitboxInsetY() { return 0; }

    public double hitLeft()   { return x + hitboxInsetX(); }
    public double hitRight()  { return x + width - hitboxInsetX(); }
    public double hitTop()    { return y + hitboxInsetY(); }
    public double hitBottom() { return y + height - hitboxInsetY(); }

    // Legacy getters (still useful)
    public double left()   { return x; }
    public double right()  { return x + width; }
    public double top()    { return y; }
    public double bottom() { return y + height; }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}
