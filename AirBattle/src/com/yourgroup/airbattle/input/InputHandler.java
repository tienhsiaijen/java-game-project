package com.yourgroup.airbattle.input;

import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.HashSet;
import java.util.Set;

public class InputHandler {

    private final Set<KeyCode> pressedKeys = new HashSet<>();

    public InputHandler(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> pressedKeys.add(e.getCode()));
        scene.addEventHandler(KeyEvent.KEY_RELEASED, e -> pressedKeys.remove(e.getCode()));
    }

    public boolean isPressed(KeyCode key) {
        return pressedKeys.contains(key);
    }

    public boolean up() {
        return isPressed(KeyCode.W) || isPressed(KeyCode.UP);
    }

    public boolean down() {
        return isPressed(KeyCode.S) || isPressed(KeyCode.DOWN);
    }

    public boolean left() {
        return isPressed(KeyCode.A) || isPressed(KeyCode.LEFT);
    }

    public boolean right() {
        return isPressed(KeyCode.D) || isPressed(KeyCode.RIGHT);
    }

    public boolean fire() {
        return isPressed(KeyCode.SPACE);
    }
}
