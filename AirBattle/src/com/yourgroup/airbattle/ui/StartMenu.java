package com.yourgroup.airbattle.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class StartMenu extends VBox {

    public StartMenu(Runnable onStart, Runnable onExit) {
        setAlignment(Pos.CENTER);
        setSpacing(18);
        setStyle("""
            -fx-background-color: #0f3460;
            -fx-padding: 40;
        """);

        Label title = new Label("AIR BATTLE");
        title.setStyle("-fx-font-size: 56px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Press Start to begin");
        subtitle.setStyle("-fx-text-fill: lightblue; -fx-font-size: 16px;");

        Label tips = new Label(
            "ENEMY GUIDE:\n" +
            "• Type 1 (Normal): 1 HP, medium speed, moves in a wavy path.\n" +
            "• Type 3 (Fast): VERY fast, 1 HP, bounces left-right while diving.\n" +
            "• Type 2 (Boss): Same speed pattern as Fast, BIG body, many HP.\n\n" +
            "Controls: Arrow keys / WASD to move, SPACE to fire."
        );
        tips.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        tips.setMaxWidth(520);
        tips.setWrapText(true);

        VBox buttons = new VBox(12,
                createButton("Start Game", "#e74c3c", onStart),
                createButton("Exit Game", "#7f8c8d", onExit)
        );
        buttons.setAlignment(Pos.CENTER);

        getChildren().addAll(title, subtitle, tips, buttons);
    }

    private Button createButton(String text, String color, Runnable action) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;");

        btn.setOnMouseEntered(e ->
            btn.setStyle("-fx-background-color: " + color +
                "; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, rgba(255,255,255,0.5), 10, 0.5, 0, 0);")
        );

        btn.setOnMouseExited(e ->
            btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white;")
        );

        btn.setOnAction(e -> action.run());
        return btn;
    }
}
