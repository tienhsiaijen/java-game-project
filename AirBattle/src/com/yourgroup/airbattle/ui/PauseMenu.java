package com.yourgroup.airbattle.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class PauseMenu extends VBox {

    public PauseMenu(Runnable onResume, Runnable onExit) {
        setAlignment(Pos.CENTER);
        setSpacing(20);
        setPrefSize(900, 600);
  
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

        Label title = new Label("GAME PAUSED");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 48));

        Button resumeBtn = createButton("RESUME", "#27ae60", onResume);
        Button exitBtn = createButton("EXIT GAME", "#e74c3c", onExit);

        getChildren().addAll(title, resumeBtn, exitBtn);
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

        btn.setOnMousePressed(e -> btn.setScaleX(0.95));
        btn.setOnMouseReleased(e -> {
            btn.setScaleX(1.0);
            action.run();
        });
        
        return btn;
    }
}
