package com.yourgroup.airbattle.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * Heads-Up Display (HUD) shown during gameplay.
 *
 * <p>{@code HUD} is a lightweight UI component responsible for displaying
 * real-time player information such as health points (HP) and score.
 * It is positioned on top of the game view and updated continuously
 * as the game state changes.</p>
 *
 * <p>Design intent:
 * <ul>
 *   <li>Separates presentation logic from game logic.</li>
 *   <li>Provides immediate visual feedback for critical states
 *       (e.g., low HP, score milestones).</li>
 *   <li>Keeps UI updates simple and efficient for per-frame usage.</li>
 * </ul>
 * </p>
 */
public class HUD extends VBox {

    /** Default starting health value displayed when the game begins or resets. */
    private static final int DEFAULT_HP = 3;

    /** Label displaying the player's current health points. */
    private final Label hpLabel;

    /** Label displaying the player's current score. */
    private final Label scoreLabel;

    /** Cached score value to support incremental updates. */
    private int currentScore = 0;

    /**
     * Creates a HUD instance and initializes its visual layout.
     *
     * <p>The HUD is positioned in the top-left corner of the screen and
     * uses simple text styling for clarity and readability.</p>
     */
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

    /**
     * Updates the displayed health points.
     *
     * <p>The text color changes dynamically to indicate danger levels:
     * <ul>
     *   <li>Green: safe</li>
     *   <li>Orange: warning</li>
     *   <li>Red: critical</li>
     * </ul>
     * </p>
     *
     * @param hp current health points of the player
     */
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

    /**
     * Sets the player's score and updates the display.
     *
     * <p>A brief animation is triggered whenever the score reaches
     * a multiple of 1000, providing positive feedback to the player.</p>
     *
     * @param score new total score value
     */
    public void setScore(int score) {
        this.currentScore = score;
        scoreLabel.setText("Score: " + score);

        if (score > 0 && score % 1000 == 0) {
            animateScoreLabel();
        }
    }

    /**
     * Increases the current score by a given amount.
     *
     * @param points number of points to add
     */
    public void addScore(int points) {
        setScore(this.currentScore + points);
    }

    /**
     * @return the current score value
     */
    public int getScore() {
        return currentScore;
    }

    /**
     * Resets the HUD to its initial state.
     *
     * <p>Used when restarting the game.</p>
     */
    public void reset() {
        setHp(DEFAULT_HP);
        setScore(0);
    }

    /**
     * Plays a short visual animation on the score label.
     *
     * <p>The label briefly enlarges and changes color to highlight
     * milestone achievements.</p>
     */
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
