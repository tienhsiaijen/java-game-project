package com.yourgroup.airbattle.input;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * InputHandler manages keyboard input for the game.
 *
 * <p>This class tracks the current pressed/released state of keys using
 * JavaFX key events, allowing the game logic to query input states
 * in a frame-independent manner.</p>
 *
 * <p>Design rationale:
 * <ul>
 *   <li>Key states are stored in a {@link Set}, enabling support for multiple
 *       simultaneous key presses (e.g., moving diagonally while firing).</li>
 *   <li>Input handling is decoupled from game logic, improving modularity
 *       and testability.</li>
 *   <li>Key bindings are centralized, making future remapping easier.</li>
 * </ul>
 * </p>
 */
public class InputHandler {

    /**
     * Set of keys that are currently being held down.
     * Keys are added on KEY_PRESSED and removed on KEY_RELEASED events.
     */
    private final Set<KeyCode> pressedKeys = new HashSet<>();

    /**
     * Creates an InputHandler and attaches key listeners to the given scene.
     *
     * @param scene the JavaFX scene that receives keyboard input
     */
    public InputHandler(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED,
                e -> pressedKeys.add(e.getCode()));

        scene.addEventHandler(KeyEvent.KEY_RELEASED,
                e -> pressedKeys.remove(e.getCode()));
    }

    /**
     * Checks whether a specific key is currently pressed.
     *
     * @param key the {@link KeyCode} to check
     * @return true if the key is currently held down; false otherwise
     */
    public boolean isPressed(KeyCode key) {
        return pressedKeys.contains(key);
    }

    /**
     * @return true if the "move up" action is active (W or UP arrow)
     */
    public boolean up() {
        return isPressed(KeyCode.W) || isPressed(KeyCode.UP);
    }

    /**
     * @return true if the "move down" action is active (S or DOWN arrow)
     */
    public boolean down() {
        return isPressed(KeyCode.S) || isPressed(KeyCode.DOWN);
    }

    /**
     * @return true if the "move left" action is active (A or LEFT arrow)
     */
    public boolean left() {
        return isPressed(KeyCode.A) || isPressed(KeyCode.LEFT);
    }

    /**
     * @return true if the "move right" action is active (D or RIGHT arrow)
     */
    public boolean right() {
        return isPressed(KeyCode.D) || isPressed(KeyCode.RIGHT);
    }

    /**
     * @return true if the "fire weapon" action is active (SPACE key)
     */
    public boolean fire() {
        return isPressed(KeyCode.SPACE);
    }
    public void clear() {
        pressedKeys.clear();
    }
}
