package com.yourgroup.airbattle.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

public class HUD extends VBox {

    private static final int DEFAULT_HP = 3;

    private final Label hpLabel;
    private final Label scoreLabel;

    private int currentScore = 0;

    public HUD() {
        setSpacing(10);
        setLayoutX(15);
        setLayoutY(15);

        hpLabel = new Label("HP: " + DEFAULT_HP);
        scoreLabel = new Label("Score: 0");

        hpLabel.setTextFill(Color.LIGHTGREEN);
        scoreLabel.setTextFill(Color.WHITE);

        hpLabel.setFont(Font.font(16));
        scoreLabel.setFont(Font.font(16));

        getChildren().addAll(hpLabel, scoreLabel);
    }

    public void setHp(int hp) {
        hpLabel.setText("HP: " + hp);

        if (hp <= 1) {
            hpLabel.setTextFill(Color.RED);
            hpLabel.setStyle("-fx-font-weight: bold;");
        } else if (hp <= 2) {
            hpLabel.setTextFill(Color.ORANGE);
            hpLabel.setStyle("");
        } else {
            hpLabel.setTextFill(Color.LIGHTGREEN);
            hpLabel.setStyle("");
        }
    }

    public void setScore(int score) {
        this.currentScore = score;
        scoreLabel.setText("Score: " + score);

        if (score > 0 && score % 1000 == 0) {
            animateScoreLabel();
        }
    }

    public void addScore(int points) {
        setScore(this.currentScore + points);
    }

    public int getScore() {
        return currentScore;
    }

    public void reset() {
        setHp(DEFAULT_HP);
        setScore(0);
    }

    private void animateScoreLabel() {
        scoreLabel.setScaleX(1.2);
        scoreLabel.setScaleY(1.2);
        scoreLabel.setTextFill(Color.GOLD);

        new Timeline(
            new KeyFrame(Duration.millis(200), e -> {
                scoreLabel.setScaleX(1.0);
                scoreLabel.setScaleY(1.0);
                scoreLabel.setTextFill(Color.WHITE);
            })
        ).play();
    }
}
