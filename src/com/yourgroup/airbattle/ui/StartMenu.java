package com.yourgroup.airbattle.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Start menu shown when the game is first launched.
 *
 * <p>{@code StartMenu} introduces the game to the player and provides
 * basic instructions before gameplay begins. It also allows the player
 * to start a new game or exit the application.</p>
 *
 * <p>Design intent:
 * <ul>
 *   <li>Clearly communicates the game title and theme.</li>
 *   <li>Provides a concise enemy guide and control instructions.</li>
 *   <li>Uses callbacks ({@link Runnable}) to keep UI logic independent
 *       from game state management.</li>
 * </ul>
 * </p>
 */
public class StartMenu extends VBox {

    /**
     * Creates the start menu with start and exit actions.
     *
     * @param onStart action executed when the player starts the game
     * @param onExit  action executed when the player exits the game
     */
    public StartMenu(Runnable onStart, Runnable onExit) {
        setAlignment(Pos.CENTER);
        setSpacing(18);

        // Background styling for the start menu screen.
        setStyle("""
            -fx-background-color: #0f3460;
            -fx-padding: 40;
        """);

        // Main game title.
        Label title = new Label("AIR BATTLE");
        title.setStyle("-fx-font-size: 56px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Subtitle providing a simple call to action.
        Label subtitle = new Label("Press Start to begin");
        subtitle.setStyle("-fx-text-fill: lightblue; -fx-font-size: 16px;");

        // Gameplay tips and enemy descriptions shown before the game starts.
        // Keep descriptions generic to avoid drifting from actual implementation details.
        Label tips = new Label(
        	    "ENEMY GUIDE:\n" +
        	    "• Type 1 (Normal): standard enemy with basic movement.\n" +
        	    "• Type 2 (Boss): larger enemy with higher health.\n" +
        	    "• Type 3 (Fast): fast enemy that is harder to track.\n" +
        	    "Objective: Survive as long as possible and achieve the highest score." +
        	    "Power UP Guide:\n" +
        	    "Heal Pill: A green pill icon, increases 1 HP.\n" +
        	    "Super Bullet: A green star icon, makes bullets bigger and more harmful.\n" +
        	    "Rampage: A blue-white star icon, makes bullets go ballistic.\n" +
        	    "Shotgun: Two green bullets icon, makes each shot have 3 bullets.\n" +
        	    "Body Strengthening: A blue-white flash icon, increases speed and grants a shield.\n\n" +
        	    "Controls: Arrow keys / WASD to move, SPACE to fire."
        	);
        tips.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        tips.setMaxWidth(520);
        tips.setWrapText(true);

        // Container holding the main action buttons.
        VBox buttons = new VBox(12,
                createButton("Start Game", "#e74c3c", onStart),
                createButton("Exit Game", "#7f8c8d", onExit)
        );
        buttons.setAlignment(Pos.CENTER);

        getChildren().addAll(title, subtitle, tips, buttons);
    }

    /**
     * Creates a styled button with a hover highlight effect.
     *
     * <p>Visual feedback is provided on mouse hover to improve usability
     * and indicate interactivity.</p>
     *
     * @param text   text displayed on the button
     * @param color  base background color (hex format)
     * @param action action executed when the button is clicked
     * @return a fully configured {@link Button}
     */
    private Button createButton(String text, String color, Runnable action) {
        Button btn = new Button(text);

        String normalStyle =
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;";

        String hoverStyle =
                "-fx-background-color: " + color + ";" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 6;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(gaussian, rgba(255,255,255,0.5), 10, 0.5, 0, 0);";

        btn.setStyle(normalStyle);

        // Hover effect adds a subtle glow to the button.
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle));

        // Execute the assigned action when clicked (defensive against null).
        btn.setOnAction(e -> { if (action != null) action.run(); });

        return btn;
    }
}

