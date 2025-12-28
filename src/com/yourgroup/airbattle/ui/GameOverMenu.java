package com.yourgroup.airbattle.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * Game-over overlay menu displayed when the player loses the game.
 *
 * <p>{@code GameOverMenu} is a self-contained UI component responsible for
 * presenting the final result of a game session and offering the player
 * options to restart or exit. It is displayed as a semi-transparent overlay
 * on top of the gameplay area.</p>
 *
 * <p>Design intent:
 * <ul>
 *   <li>Separates UI concerns from game logic and world updates.</li>
 *   <li>Uses callbacks ({@link Runnable}) to keep the menu decoupled from
 *       scene and state management logic.</li>
 *   <li>Provides clear visual feedback for the GAME_OVER state.</li>
 * </ul>
 * </p>
 */
public class GameOverMenu extends VBox {

    // ===== Layout constants (avoid magic numbers) =====
    private static final double UI_WIDTH = 900;
    private static final double UI_HEIGHT = 600;

    private static final double MENU_SPACING = 25;

    private static final double TITLE_FONT_SIZE = 52;
    private static final double SCORE_FONT_SIZE = 28;

    private static final double BUTTON_WIDTH = 220;
    private static final double BUTTON_HEIGHT = 50;

    /**
     * Creates a game-over menu with the final score and action callbacks.
     *
     * @param finalScore the player's score at the end of the game
     * @param onRestart  action executed when the player chooses to restart
     * @param onExit     action executed when the player chooses to quit
     */
    public GameOverMenu(int finalScore, Runnable onRestart, Runnable onExit) {
        setAlignment(Pos.CENTER);
        setSpacing(MENU_SPACING);
        setPrefSize(UI_WIDTH, UI_HEIGHT);

        // Semi-transparent dark background overlay.
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.75);");

        // Game over title label.
        Label title = new Label("YOU LOST");
        title.setTextFill(Color.web("#ff3333"));
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, TITLE_FONT_SIZE));

        // Display the final score.
        Label scoreLabel = new Label("SCORE: " + finalScore);
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, SCORE_FONT_SIZE));

        // Action buttons.
        Button restartBtn = createButton("PLAY AGAIN", "#27ae60", onRestart);
        Button exitBtn = createButton("QUIT", "#e74c3c", onExit);

        getChildren().addAll(title, scoreLabel, restartBtn, exitBtn);
    }

    /**
     * Creates a styled button with hover effects and an associated action.
     *
     * <p>Styling is applied directly using inline CSS for simplicity and to
     * keep the component self-contained.</p>
     *
     * @param text   label displayed on the button
     * @param color  base background color (hex format)
     * @param action action executed when the button is clicked
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

        // Click action (defensive against null callbacks).
        btn.setOnAction(e -> { if (action != null) action.run(); });

        return btn;
    }
}

