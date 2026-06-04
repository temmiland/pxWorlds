package land.temmi.pxworlds;

import land.temmi.pxworlds.world.WorldManager;
import land.temmi.rollercoaster.config.ConfigurationStorage;
import land.temmi.pxworlds.debug.DebugOverlay;
import land.temmi.pxworlds.io.InputAction;
import land.temmi.pxworlds.statemanagement.PxGameStateManager;
import land.temmi.rollercoaster.game.Game;
import land.temmi.rollercoaster.debug.DebugHud;
import land.temmi.rollercoaster.platform.Window;

import java.io.File;
import java.util.function.BooleanSupplier;

/**
 * Main game class of pxWorlds, built on top of the Rollercoaster
 * {@link Game} framework.
 *
 * <p>It wires up the fixed-rate game loop, the {@link PxGameStateManager},
 * asset loading and the debug overlay, and defines the file system layout used
 * for configuration and world storage.</p>
 */
public class PxWorlds extends Game<PxGameStateManager> {

	/** Application title. */
	public static final String TITLE = "pxWorlds";

	/**
	 * Fixed logic update rate in ticks per second. Rendering runs independently
	 * at the configured FPS.
	 */
	public static final int TICK_RATE = 20;

	/**
	 * Maximum number of update ticks per frame. Caps accumulated time to prevent
	 * the spiral of death during lag spikes.
	 */
	public static final int MAX_UPDATES_PER_FRAME = 10;

	/** Path to the home directory. */
	public static final String HOME_DIRECTORY = System.getProperty("user.home") + File.separator
			+ ".pxworlds" + File.separator;

	/** Path to the configuration directory. */
	public static final String CONFIG_DIRECTORY = HOME_DIRECTORY + "config" + File.separator;

	/** Path to the worlds directory. */
	public static final String WORLD_DIRECTORY = HOME_DIRECTORY + "worlds" + File.separator;

	/**
	 * Creates the game and initializes the world manager.
	 *
	 * @param window               the platform window
	 * @param configurationStorage the persistent configuration storage
	 */
	public PxWorlds(Window window, ConfigurationStorage configurationStorage) {
		super(window, configurationStorage, new PxGameStateManager(configurationStorage));
		WorldManager wm = new WorldManager();
		wm.init();
	}

	/**
	 * @return the window title
	 */
	@Override
	public String getTitle() {
		return TITLE;
	}

	/**
	 * @return the fixed logic update rate in ticks per second
	 */
	@Override
	protected int getTickRate() {
		return TICK_RATE;
	}

	/**
	 * @return the maximum number of update ticks per frame
	 */
	@Override
	protected int getMaxUpdatesPerFrame() {
		return MAX_UPDATES_PER_FRAME;
	}

	/**
	 * @return a supplier that reports whether the debug toggle key was pressed
	 */
	@Override
	protected BooleanSupplier isDebugTogglePressed() {
		return () -> window.getInputProcessor().wasPressed(InputAction.DEBUG_TOGGLE.getDefaultKey());
	}

	/**
	 * @return the debug HUD overlay used by the game
	 */
	@Override
	protected DebugHud createDebugHud() {
		return new DebugOverlay();
	}

	@Override
	protected void onInitRenderer() {
	}

	@Override
	protected void onLoadAssets() {
	}

	@Override
	protected void onInitGameState() {
	}

	@Override
	protected void onCleanup() {
	}

}
