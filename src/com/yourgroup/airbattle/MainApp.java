package com.yourgroup.airbattle;

import com.yourgroup.airbattle.core.GameLoop;
import com.yourgroup.airbattle.core.GameWorld;
import com.yourgroup.airbattle.core.GameState;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
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


	private GameState state = GameState.START_MENU;
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

        // Disable JavaFX default fullscreen exit behavior.
        // ESC is reserved for pause/resume, so we block the default ESC-to-exit-fullscreen shortcut.
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);

        stage.show();

        // Keep the game's logical coordinate system (GameConfig.WIDTH/HEIGHT) stable,
        // and scale the UI to fit the actual window/fullscreen size.
        scene.widthProperty().addListener((obs, oldV, newV) -> updateScale(scene));
        scene.heightProperty().addListener((obs, oldV, newV) -> updateScale(scene));
        updateScale(scene);

        input = new InputHandler(scene);

        pauseMenu = new PauseMenu(
                () -> {
                    root.getChildren().remove(pauseMenu);
                    if (hud != null) hud.setVisible(true);
                    loop.startRunning();
                    state = GameState.RUNNING;
                },
                stage::close
        );

        startMenu = new StartMenu(
                () -> {
                    root.getChildren().remove(startMenu);
                    initNewGame(stage);
                    state = GameState.RUNNING;
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

        // Global key bindings (window-level actions):
        // - ESC: toggle pause/resume during gameplay
        // - P  : toggle fullscreen/windowed mode
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                // Only allow pause toggling during active gameplay states.
                if (state == GameState.RUNNING || state == GameState.PAUSED) {
                    togglePause(stage);
                }
            } else if (e.getCode() == KeyCode.P) {
                // Fullscreen toggle is a window-level concern, so it is handled here (not in InputHandler).
                stage.setFullScreen(!stage.isFullScreen());
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

        state = GameState.RUNNING;
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
                    state = GameState.GAME_OVER;  
                    showGameOver(stage);
                }
            }
        };
    }

    /**
     * Toggle pause/resume when ESC is pressed.
     *
     * <p>State rules:
     * <ul>
     *   <li>Only RUNNING <-> PAUSED is allowed.</li>
     *   <li>Pause/resume is ignored during START_MENU and after GAME_OVER.</li>
     * </ul>
     *
     * <p>UI rules:
     * <ul>
     *   <li>When paused: show PauseMenu overlay and hide HUD.</li>
     *   <li>When resumed: remove PauseMenu overlay and show HUD.</li>
     * </ul>
     *
     * <p>Note: {@code stage} is kept for signature consistency (and future use),
     * but is not required for pause/resume logic.</p>
     */
    private void togglePause(Stage stage) {
        // 0) Do not allow pausing in game-over state or before a session starts.
        //    (gameOverShown is your existing one-shot latch for game over.)
        if (gameOverShown) return;
        if (loop == null || player == null) return;

        // 1) Pause: only allowed when the game is actively running.
        if (state == GameState.RUNNING && loop.isRunning()) {

            // 1.1 Stop the game loop so no updates/movement happen while paused.
            loop.pauseRunning();

            // 1.2 Show pause overlay (full-screen anchored).
            if (!root.getChildren().contains(pauseMenu)) {
                root.getChildren().add(pauseMenu);
                AnchorPane.setTopAnchor(pauseMenu, 0.0);
                AnchorPane.setBottomAnchor(pauseMenu, 0.0);
                AnchorPane.setLeftAnchor(pauseMenu, 0.0);
                AnchorPane.setRightAnchor(pauseMenu, 0.0);
            }

            // 1.3 Hide HUD to keep the pause overlay clean.
            if (hud != null) hud.setVisible(false);

            // 1.4 Update state last to keep state consistent with the actual actions above.
            state = GameState.PAUSED;
            return;
        }

        // 2) Resume: only allowed when the current state is PAUSED.
        if (state == GameState.PAUSED) {

            // 2.1 Remove pause overlay.
            root.getChildren().remove(pauseMenu);

            // 2.2 Restore HUD.
            if (hud != null) hud.setVisible(true);

            // 2.3 Resume the game loop.
            loop.startRunning();

            // 2.4 Update state last.
            state = GameState.RUNNING;
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

            // Bind background size to playfield size for smooth resizing/fullscreen.
            bgView.fitWidthProperty().bind(playfield.widthProperty());
            bgView.fitHeightProperty().bind(playfield.heightProperty());
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

    private void updateScale(Scene scene) {
        double sx = scene.getWidth() / GameConfig.WIDTH;
        double sy = scene.getHeight() / GameConfig.HEIGHT;
        double s = Math.min(sx, sy);

        root.setScaleX(s);
        root.setScaleY(s);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
