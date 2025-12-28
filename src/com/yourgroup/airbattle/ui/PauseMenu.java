package com.yourgroup.airbattle.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Pause menu overlay displayed when the game is temporarily halted.
 *
 * <p>{@code PauseMenu} provides the player with options to resume gameplay
 * or exit the game. It is rendered as a semi-transparent overlay on top
 * of the active game scene, clearly indicating the paused state.</p>
 *
 * <p>Design intent:
 * <ul>
 *   <li>Separates pause-state UI from game logic and rendering.</li>
 *   <li>Uses callbacks ({@link Runnable}) to keep the menu independent
 *       from game state management.</li>
 *   <li>Provides clear visual feedback that gameplay is suspended.</li>
 * </ul>
 * </p>
 */
public class PauseMenu extends VBox {

    private static final double UI_WIDTH = 900;
    private static final double UI_HEIGHT = 600;

    private static final double MENU_SPACING = 20;

    private static final double BUTTON_WIDTH = 220;
    private static final double BUTTON_HEIGHT = 50;

    /**
     * Creates a pause menu with resume and exit actions.
     *
     * @param onResume action executed when the player chooses to resume the game
     * @param onExit   action executed when the player chooses to exit the game
     */
    public PauseMenu(Runnable onResume, Runnable onExit) {
        setAlignment(Pos.CENTER);
        setSpacing(MENU_SPACING);
        setPrefSize(UI_WIDTH, UI_HEIGHT);

        // Semi-transparent background to dim the game scene underneath.
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        // Title indicating the paused state.
        Label title = new Label("GAME PAUSED");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 48));

        // Action buttons.
        Button resumeBtn = createButton("RESUME", "#27ae60", onResume);
        Button exitBtn = createButton("EXIT GAME", "#e74c3c", onExit);

        getChildren().addAll(title, resumeBtn, exitBtn);
    }

    /**
     * Creates a styled button with hover effects and press feedback.
     *
     * <p>Styling is applied using inline CSS to keep the component self-contained.</p>
     *
     * @param text   text displayed on the button
     * @param color  base background color (hex format)
     * @param action action executed when the button is activated
     * @return a fully configured {@link Button}
     */
    private Button createButton(String text, String color, Runnable action) {
        Button btn = new Button(text);
        btn.setPrefWidth(BUTTON_WIDTH);
        btn.setPrefHeight(BUTTON_HEIGHT);

        String normalStyle =
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 18;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 5;" +
                "-fx-cursor: hand;";

        String hoverStyle =
                "-fx-background-color: derive(" + color + ", 20%);" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 18;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 5;" +
                "-fx-cursor: hand;";

        btn.setStyle(normalStyle);

        // Hover effects.
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle));

        // Press feedback (visual only).
        btn.setOnMousePressed(e -> {
            btn.setScaleX(0.95);
            btn.setScaleY(0.95);
        });
        btn.setOnMouseReleased(e -> {
            btn.setScaleX(1.0);
            btn.setScaleY(1.0);
        });

        // Action execution (supports mouse + keyboard activation).
        btn.setOnAction(e -> { if (action != null) action.run(); });

        return btn;
    }
}
