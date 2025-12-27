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

/**
 * Main JavaFX application entry point for AirBattle.
 *
 * <p>This class is responsible for:
 * <ul>
 *   <li>Creating and wiring together the scene graph (root, playfield, background)</li>
 *   <li>Initializing core game components (InputHandler, GameWorld, GameLoop)</li>
 *   <li>Managing high-level UI overlays (Start menu, Pause menu, Game Over screen)</li>
 *   <li>Handling top-level state transitions (start, pause/resume, restart, exit)</li>
 * </ul>
 * </p>
 *
 * <p>Architecture overview:
 * <ul>
 *   <li>{@link GameWorld} owns and updates all game objects.</li>
 *   <li>{@link GameLoop} drives the frame updates and delegates to the world.</li>
 *   <li>UI components ({@link HUD}, menus) are layered on top of the playfield.</li>
 * </ul>
 * </p>
 *
 * <p>Note: This class focuses on orchestration rather than entity logic.
 * Most gameplay behavior is implemented in the world and object classes.</p>
 */
public class MainApp extends Application {

    /** Fixed window width (pixels). */
    private static final double WIDTH = 900;

    /** Fixed window height (pixels). */
    private static final double HEIGHT = 600;

    /** Indicates whether the game is currently paused. */
    private boolean paused = false;

    /** Prevents showing multiple Game Over menus for the same death event. */
    private boolean gameOverShown = false;

    /** Root container for the entire scene (playfield + overlays). */
    private AnchorPane root;

    /** Playfield container where game objects are rendered. */
    private AnchorPane playfield;

    /** Keyboard input handler bound to the JavaFX scene. */
    private InputHandler input;

    /** Game world containing all active entities and collision logic. */
    private GameWorld world;

    /** Main game loop driving updates and coordinating with the world. */
    private GameLoop loop;

    /** On-screen HUD displaying HP and score. */
    private HUD hud;

    /** Player entity (spawned when the game starts). */
    private Player player;

    /** Pause menu overlay shown when the player presses ESC during gameplay. */
    private PauseMenu pauseMenu;

    /** Start menu overlay shown on launch. */
    private StartMenu startMenu;

    /** Background image view displayed behind all gameplay entities. */
    private ImageView bgView;

    /**
     * JavaFX lifecycle method called when the application starts.
     *
     * <p>This method sets up the stage, scene graph, game components,
     * and overlay menus. Gameplay begins only after the player presses
     * "Start Game" on the start menu.</p>
     *
     * @param stage primary JavaFX stage
     */
    @Override
    public void start(Stage stage) {

        // Root container for all nodes.
        root = new AnchorPane();
        root.setPrefSize(WIDTH, HEIGHT);

        // Playfield occupies the full window and contains background + game entities.
        playfield = new AnchorPane();
        AnchorPane.setTopAnchor(playfield, 0.0);
        AnchorPane.setBottomAnchor(playfield, 0.0);
        AnchorPane.setLeftAnchor(playfield, 0.0);
        AnchorPane.setRightAnchor(playfield, 0.0);
        root.getChildren().add(playfield);

        // Background is part of the playfield so it stays behind gameplay objects.
        setupBackground();

        // Initialize the game world (spawning objects happens later after Start).
        world = new GameWorld(playfield);

        /**
         * Create a game loop that updates the world and also performs
         * top-level orchestration tasks per frame:
         * <ul>
         *   <li>Sync player HP/score to the HUD</li>
         *   <li>Detect player death and trigger the Game Over screen</li>
         * </ul>
         */
        loop = new GameLoop(world) {
            @Override
            public void handle(long now) {
                super.handle(now);

                // Keep the HUD in sync with the current player/world state.
                if (player != null && hud != null) {
                    hud.setHp(player.getHp());
                    hud.setScore(world.getScore());
                }

                // Show Game Over menu once when the player dies.
                if (!gameOverShown && player != null && !player.isAlive()) {
                    gameOverShown = true;
                    showGameOver(stage);
                }
            }
        };

        // Create and show the JavaFX window.
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        stage.setScene(scene);
        stage.setTitle("AirBattle");
        stage.setResizable(false);
        stage.show();

        // Input handler listens for key press/release events on this scene.
        input = new InputHandler(scene);

        /**
         * Pause menu:
         * - Resume removes the overlay and resumes the game loop.
         * - Exit closes the window.
         */
        pauseMenu = new PauseMenu(
            () -> {
                root.getChildren().remove(pauseMenu);
                loop.startRunning();
                if (hud != null) hud.setVisible(true);
                paused = false;
            },
            stage::close
        );

        /**
         * Start menu:
         * - Start creates/spawns the player, shows HUD, and starts the game loop.
         * - Exit closes the window.
         */
        startMenu = new StartMenu(
            () -> {
                root.getChildren().remove(startMenu);

                // Spawn the player at a fixed starting position.
                player = new Player(
                    430, 480,
                    new Image("/img/player.png"),
                    input
                );
                world.spawn(player);

                // Create and display the HUD overlay.
                hud = new HUD();
                root.getChildren().add(hud);

                gameOverShown = false;
                loop.startRunning();
            },
            stage::close
        );

        // Show the start menu overlay by default.
        root.getChildren().add(startMenu);
        AnchorPane.setTopAnchor(startMenu, 0.0);
        AnchorPane.setBottomAnchor(startMenu, 0.0);
        AnchorPane.setLeftAnchor(startMenu, 0.0);
        AnchorPane.setRightAnchor(startMenu, 0.0);

        /**
         * Global key handling (Java 8/11 compatible):
         * ESC toggles pause/resume during active gameplay.
         */
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            switch (e.getCode()) {
                case ESCAPE:
                    // Only allow pausing during gameplay (not on game over screen).
                    if (!paused && loop.isRunning() && !gameOverShown) {
                        // Pause the game loop and show pause overlay.
                        loop.pauseRunning();
                        root.getChildren().add(pauseMenu);

                        AnchorPane.setTopAnchor(pauseMenu, 0.0);
                        AnchorPane.setBottomAnchor(pauseMenu, 0.0);
                        AnchorPane.setLeftAnchor(pauseMenu, 0.0);
                        AnchorPane.setRightAnchor(pauseMenu, 0.0);

                        // Hide HUD while paused to keep the UI clean.
                        if (hud != null) hud.setVisible(false);
                        paused = true;
                    } else if (paused) {
                        // Resume gameplay.
                        root.getChildren().remove(pauseMenu);
                        loop.startRunning();
                        if (hud != null) hud.setVisible(true);
                        paused = false;
                    }
                    break;

                default:
                    break;
            }
        });
    }

    /**
     * Loads and displays the background image.
     *
     * <p>The background is inserted into the playfield first so that all
     * game entities render above it.</p>
     */
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

    /**
     * Displays the Game Over overlay and pauses gameplay.
     *
     * <p>This method:
     * <ul>
     *   <li>Pauses the current game loop</li>
     *   <li>Captures the final score</li>
     *   <li>Shows the {@link GameOverMenu} with restart/exit actions</li>
     * </ul>
     * </p>
     *
     * @param stage primary stage (used for exiting the application)
     */
    private void showGameOver(Stage stage) {
        loop.pauseRunning();

        // Capture the final score from the HUD (fallback to 0 if HUD is not available).
        int finalScore = (hud == null) ? 0 : hud.getScore();

        GameOverMenu gameOverMenu = new GameOverMenu(
            finalScore,
            () -> {
                // Restart: remove existing GameOverMenu overlays.
                root.getChildren().removeIf(n -> n instanceof GameOverMenu);

                // Clear the playfield and recreate the background.
                playfield.getChildren().clear();
                setupBackground();

                // Recreate a fresh world for a clean restart.
                world = new GameWorld(playfield);

                // Recreate the loop and reattach orchestration logic.
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

                // Respawn player.
                player = new Player(
                    430, 480,
                    new Image("/img/player.png"),
                    input
                );
                world.spawn(player);

                // Reset HUD.
                if (hud != null) root.getChildren().remove(hud);
                hud = new HUD();
                root.getChildren().add(hud);

                // Reset top-level flags and resume gameplay.
                paused = false;
                gameOverShown = false;
                loop.startRunning();
            },
            stage::close
        );

        // Hide HUD while the Game Over menu is shown.
        if (hud != null) hud.setVisible(false);

        // Add and anchor the overlay to cover the full window.
        root.getChildren().add(gameOverMenu);
        AnchorPane.setTopAnchor(gameOverMenu, 0.0);
        AnchorPane.setBottomAnchor(gameOverMenu, 0.0);
        AnchorPane.setLeftAnchor(gameOverMenu, 0.0);
        AnchorPane.setRightAnchor(gameOverMenu, 0.0);
    }

    /**
     * Standard Java entry point.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
