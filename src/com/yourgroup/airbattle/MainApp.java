package com.yourgroup.airbattle;

import com.yourgroup.airbattle.core.GameLoop;
import com.yourgroup.airbattle.core.GameWorld;
import com.yourgroup.airbattle.input.InputHandler;
import com.yourgroup.airbattle.objects.Player;
import com.yourgroup.airbattle.ui.GameOverMenu;
import com.yourgroup.airbattle.ui.HUD;
import com.yourgroup.airbattle.ui.PauseMenu;
import com.yourgroup.airbattle.ui.StartMenu;
import com.yourgroup.airbattle.util.Assets;
import com.yourgroup.airbattle.util.SoundManager;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import com.yourgroup.airbattle.core.GameConfig;


/**
 * Main JavaFX application entry point for AirBattle.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Create the JavaFX scene graph (root, playfield, overlays)</li>
 *   <li>Initialize menus (Start, Pause, Game Over) and wire callbacks</li>
 *   <li>Start and control the {@link GameLoop}</li>
 *   <li>Orchestrate session lifecycle (new game, pause/resume, game over)</li>
 * </ul>
 * </p>
 *
 * <p>Note: Game logic lives in {@link GameWorld} and entity classes.
 * This class focuses on UI/state orchestration.</p>
 */
public class MainApp extends Application {


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
        root.setPrefSize(GameConfig.WIDTH, GameConfig.HEIGHT);

        playfield = new AnchorPane();
        AnchorPane.setTopAnchor(playfield, 0.0);
        AnchorPane.setBottomAnchor(playfield, 0.0);
        AnchorPane.setLeftAnchor(playfield, 0.0);
        AnchorPane.setRightAnchor(playfield, 0.0);
        root.getChildren().add(playfield);

        setupBackground();

        // Create initial world/loop (player spawns only after Start is pressed).
        world = new GameWorld(playfield);
        loop = createLoop(stage);

        Scene scene = new Scene(root, GameConfig.WIDTH, GameConfig.HEIGHT);
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
                    initNewGame(stage);
                },
                stage::close
        );

        // Show start menu on launch.
        root.getChildren().add(startMenu);
        AnchorPane.setTopAnchor(startMenu, 0.0);
        AnchorPane.setBottomAnchor(startMenu, 0.0);
        AnchorPane.setLeftAnchor(startMenu, 0.0);
        AnchorPane.setRightAnchor(startMenu, 0.0);

        // Play start/menu BGM.
        SoundManager.playStartBgm();

        // ESC toggles pause/resume during gameplay.
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                togglePause(stage);
            }
        });
    }

    /**
     * Starts a brand-new game session:
     * <ul>
     *   <li>Clears the playfield and restores background</li>
     *   <li>Rebuilds world and loop</li>
     *   <li>Spawns the player</li>
     *   <li>Creates and attaches the HUD overlay</li>
     * </ul>
     */
    private void initNewGame(Stage stage) {
    	if (input != null) input.clear();
        // Reset visuals.
        playfield.getChildren().clear();
        setupBackground();

        // Reset loop/world.
        if (loop != null) {
            loop.pauseRunning();
        }
        world = new GameWorld(playfield);
        loop = createLoop(stage);

        // Spawn player (Player uses a spawn callback to create bullets/effects).
        player = new Player(
                430, 480,
                Assets.image("/img/player.png"),
                input,
                world.getBulletSprite(),
                Assets.image("/img/SuperBullet.png"),
                Assets.image("/img/shield.png"),
                world::spawn
        );
        world.spawn(player);

        // Reset HUD.
        if (hud != null) root.getChildren().remove(hud);
        hud = new HUD();
        root.getChildren().add(hud);

        paused = false;
        gameOverShown = false;

        // Play gameplay BGM.
        SoundManager.playFightBgm();

        loop.startRunning();
    }

    /**
     * Creates the main game loop wrapper that also synchronizes HUD and triggers game over.
     *
     * @param stage primary stage used for exit/restart callbacks
     * @return a configured {@link GameLoop} instance
     */
    private GameLoop createLoop(Stage stage) {
        return new GameLoop(world) {
            @Override
            public void handle(long now) {
                super.handle(now);

                // Sync HUD with current session state.
                if (player != null && hud != null) {
                    hud.setHp(player.getHp());
                    hud.setScore(world.getScore());
                }

                // Trigger game-over once when the player dies.
                if (!gameOverShown && player != null && !player.isAlive()) {
                    gameOverShown = true;
                    showGameOver(stage);
                }
            }
        };
    }

    /**
     * Toggles pause/resume state when ESC is pressed.
     *
     * <p>Pause is ignored when the game is not running (start menu) or after game over.</p>
     */
    private void togglePause(Stage stage) {
        if (gameOverShown) return;
        if (loop == null || player == null) return;

        if (!paused && loop.isRunning()) {
            // Pause
            loop.pauseRunning();
            root.getChildren().add(pauseMenu);

            AnchorPane.setTopAnchor(pauseMenu, 0.0);
            AnchorPane.setBottomAnchor(pauseMenu, 0.0);
            AnchorPane.setLeftAnchor(pauseMenu, 0.0);
            AnchorPane.setRightAnchor(pauseMenu, 0.0);

            if (hud != null) hud.setVisible(false);
            paused = true;
        } else if (paused) {
            // Resume
            root.getChildren().remove(pauseMenu);
            loop.startRunning();
            if (hud != null) hud.setVisible(true);
            paused = false;
        }
    }

    /**
     * Ensures the background image is present as the bottom-most node in the playfield.
     *
     * <p>This method is safe to call after {@code playfield.getChildren().clear()}
     * because it re-adds the background node at index 0.</p>
     */
    private void setupBackground() {
        Image bg = Assets.image("/img/background.png");

        if (bgView == null) {
            bgView = new ImageView();
            bgView.setFitWidth(GameConfig.WIDTH);
            bgView.setFitHeight(GameConfig.HEIGHT);
            bgView.setPreserveRatio(false);
            bgView.setLayoutX(0);
            bgView.setLayoutY(0);
        }

        bgView.setImage(bg);

        // Always keep background at the bottom layer.
        if (!playfield.getChildren().contains(bgView)) {
            playfield.getChildren().add(0, bgView);
        } else {
            playfield.getChildren().remove(bgView);
            playfield.getChildren().add(0, bgView);
        }
    }

    /**
     * Displays the game-over overlay and pauses the game loop.
     *
     * <p>The overlay provides restart and exit actions via callbacks.</p>
     */
    private void showGameOver(Stage stage) {
        // Play game-over BGM.
        SoundManager.playGameOverBgm();

        loop.pauseRunning();

        int finalScore = (world == null) ? 0 : world.getScore();

        GameOverMenu gameOverMenu = new GameOverMenu(
                finalScore,
                () -> {
                    // Remove old game-over overlays (defensive).
                    root.getChildren().removeIf(n -> n instanceof GameOverMenu);
                    // Start a new session.
                    initNewGame(stage);
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
