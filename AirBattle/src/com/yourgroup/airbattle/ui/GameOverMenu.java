package com.yourgroup.airbattle.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class GameOverMenu extends VBox {

    public GameOverMenu(int finalScore, Runnable onRestart, Runnable onExit) {
        setAlignment(Pos.CENTER);
        setSpacing(25);
        setPrefSize(900, 600);

        setStyle("-fx-background-color: rgba(0, 0, 0, 0.75);");

        Label title = new Label("MISSION FAILED");
        title.setTextFill(Color.web("#ff3333"));
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 52));

        Label scoreLabel = new Label("SCORE: " + finalScore);
        scoreLabel.setTextFill(Color.WHITE);
        scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 28));

        Button restartBtn = createButton("PLAY AGAIN", "#27ae60", onRestart);
        Button exitBtn = createButton("QUIT", "#e74c3c", onExit);

        getChildren().addAll(title, scoreLabel, restartBtn, exitBtn);
    }

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

        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(normalStyle));
        btn.setOnAction(e -> action.run());

        return btn;
    }
}
