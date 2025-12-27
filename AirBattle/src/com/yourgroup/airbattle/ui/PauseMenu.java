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

    /**
     * Creates a pause menu with resume and exit actions.
     *
     * @param onResume action executed when the player chooses to resume the game
     * @param onExit   action executed when the player chooses to exit the game
     */
    public PauseMenu(Runnable onResume, Runnable onExit) {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setPrefSize(900, 600);

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
     * Creates a styled button with hover and press effects.
     *
     * <p>Buttons visually respond to user interaction through color changes
     * and slight scaling, improving user experience and feedback.</p>
     *
     * @param text   text displayed on the button
     * @param color  base background color (hex format)
     * @param action action executed when the button is activated
     * @return a fully configured {@link Button}
     */
    private Button createButton(String text, String color, Runnable action) {
        Button btn = new Button(text);
        btn.setPrefWidth(220);
        btn.setPrefHeight(50);

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

        // Press feedback and action execution.
        btn.setOnMousePressed(e -> btn.setScaleX(0.95));
        btn.setOnMouseReleased(e -> {
            btn.setScaleX(1.0);
            action.run();
        });

        return btn;
    }
}
