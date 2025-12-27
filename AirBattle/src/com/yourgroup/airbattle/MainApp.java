package com.yourgroup.airbattle;

import com.yourgroup.airbattle.core.GameLoop;
import com.yourgroup.airbattle.core.GameWorld;
import com.yourgroup.airbattle.input.InputHandler;
import com.yourgroup.airbattle.objects.Player;
import com.yourgroup.airbattle.ui.GameOverMenu;
import com.yourgroup.airbattle.ui.HUD;
import com.yourgroup.airbattle.ui.PauseMenu;
import com.yourgroup.airbattle.ui.StartMenu;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainApp extends Application {

    private static final double WIDTH = 900;
    private static final double HEIGHT = 600;

    private boolean paused = false;
    private boolean gameOverShown = false;

    private AnchorPane root;
    private AnchorPane playfield;

    private InputHandler input;
    private GameWorld world;
    private GameLoop loop;

    private HUD hud;
    private Player player;
    private PauseMenu pauseMenu;
    private StartMenu startMenu;

    private ImageView bgView;

    @Override
    public void start(Stage stage) {

        root = new AnchorPane();
        root.setPrefSize(WIDTH, HEIGHT);

        playfield = new AnchorPane();
        AnchorPane.setTopAnchor(playfield, 0.0);
        AnchorPane.setBottomAnchor(playfield, 0.0);
        AnchorPane.setLeftAnchor(playfield, 0.0);
        AnchorPane.setRightAnchor(playfield, 0.0);
        root.getChildren().add(playfield);

        setupBackground();

        world = new GameWorld(playfield);

        loop = new GameLoop(world) {
            @Override
            public void handle(long now) {
                super.handle(now);

                if (player != null && hud != null) {
                    hud.setHp(player.getHp());
                    hud.setScore(world.getScore()); // ===== 分数同步到 HUD =====
                }

                if (!gameOverShown && player != null && !player.isAlive()) {
                    gameOverShown = true;
                    showGameOver(stage);
                }
            }
        };

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setScene(scene);
        stage.setTitle("AirBattle");
        stage.setResizable(false);
        stage.show();

        input = new InputHandler(scene);

        pauseMenu = new PauseMenu(
            () -> {
                root.getChildren().remove(pauseMenu);
                loop.startRunning();
                if (hud != null) hud.setVisible(true);
                paused = false;
            },
            stage::close
        );

        startMenu = new StartMenu(
            () -> {
                root.getChildren().remove(startMenu);

                player = new Player(
                    430, 480,
                    new Image("/img/player.png"),
                    input
                );

                world.spawn(player);

                hud = new HUD();
                root.getChildren().add(hud);

                gameOverShown = false;
                loop.startRunning();
            },
            stage::close
        );

        root.getChildren().add(startMenu);
        AnchorPane.setTopAnchor(startMenu, 0.0);
        AnchorPane.setBottomAnchor(startMenu, 0.0);
        AnchorPane.setLeftAnchor(startMenu, 0.0);
        AnchorPane.setRightAnchor(startMenu, 0.0);

        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case ESCAPE -> {
                    if (!paused && loop.isRunning() && !gameOverShown) {
                        loop.pauseRunning();
                        root.getChildren().add(pauseMenu);

                        AnchorPane.setTopAnchor(pauseMenu, 0.0);
                        AnchorPane.setBottomAnchor(pauseMenu, 0.0);
                        AnchorPane.setLeftAnchor(pauseMenu, 0.0);
                        AnchorPane.setRightAnchor(pauseMenu, 0.0);

                        if (hud != null) hud.setVisible(false);
                        paused = true;
                    } else if (paused) {
                        root.getChildren().remove(pauseMenu);
                        loop.startRunning();
                        if (hud != null) hud.setVisible(true);
                        paused = false;
                    }
                }
                default -> { }
            }
        });
    }

    private void setupBackground() {
        Image bg = new Image("/img/background.png");
        bgView = new ImageView(bg);
        bgView.setFitWidth(WIDTH);
        bgView.setFitHeight(HEIGHT);
        bgView.setPreserveRatio(false);

        if (!playfield.getChildren().contains(bgView)) {
            playfield.getChildren().add(bgView);
        }
    }

    private void showGameOver(Stage stage) {
        loop.pauseRunning();

        int finalScore = (hud == null) ? 0 : hud.getScore();

        GameOverMenu gameOverMenu = new GameOverMenu(
            finalScore,
            () -> {
                root.getChildren().removeIf(n -> n instanceof GameOverMenu);

                playfield.getChildren().clear();
                setupBackground();

                world = new GameWorld(playfield);

                loop = new GameLoop(world) {
                    @Override
                    public void handle(long now) {
                        super.handle(now);

                        if (player != null && hud != null) {
                            hud.setHp(player.getHp());
                            hud.setScore(world.getScore());
                        }

                        if (!gameOverShown && player != null && !player.isAlive()) {
                            gameOverShown = true;
                            showGameOver(stage);
                        }
                    }
                };

                player = new Player(
                    430, 480,
                    new Image("/img/player.png"),
                    input
                );
                world.spawn(player);

                if (hud != null) root.getChildren().remove(hud);
                hud = new HUD();
                root.getChildren().add(hud);

                paused = false;
                gameOverShown = false;
                loop.startRunning();
            },
            stage::close
        );

        if (hud != null) hud.setVisible(false);
        root.getChildren().add(gameOverMenu);

        AnchorPane.setTopAnchor(gameOverMenu, 0.0);
        AnchorPane.setBottomAnchor(gameOverMenu, 0.0);
        AnchorPane.setLeftAnchor(gameOverMenu, 0.0);
        AnchorPane.setRightAnchor(gameOverMenu, 0.0);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
