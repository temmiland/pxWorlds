package temmiland.pxworlds.game;

import temmiland.pxworlds.game.world.WorldManager;
import temmiland.rollercoaster.configuration.ConfigurationStorage;
import temmiland.pxworlds.game.assets.Assets;
import temmiland.pxworlds.game.debug.DebugOverlay;
import temmiland.pxworlds.game.io.InputAction;
import temmiland.pxworlds.game.statemanagement.PxGameStateManager;
import temmiland.rollercoaster.game.Game;
import temmiland.rollercoaster.game.debug.DebugHud;
import temmiland.rollercoaster.platform.Window;

import java.io.File;
import java.util.function.BooleanSupplier;

public class PxWorlds extends Game<PxGameStateManager> {

	/** Titel der Anwendung. */
	public static final String TITLE = "pxWorlds";

	/** Feste Logik-Update-Rate in Ticks pro Sekunde.
	 *  Das Rendering läuft unabhängig davon mit der konfigurierten FPS. */
	public static final int TICK_RATE = 20;

	/** Maximale Anzahl an Update-Ticks pro Frame.
	 *  Begrenzt akkumulierte Zeit, um die Spiral-of-Death bei Lag-Spikes zu verhindern. */
	public static final int MAX_UPDATES_PER_FRAME = 10;

	/** Pfad zum Home-Verzeichnis. */
	public static final String HOME_DIRECTORY = System.getProperty("user.home") + File.separator
			+ ".pxworlds" + File.separator;

	/** Pfad zum Konfigurationsverzeichnis. */
	public static final String CONFIG_DIRECTORY = HOME_DIRECTORY + "config" + File.separator;

	/** Pfad zum Welten-Verzeichnis. */
	public static final String WORLD_DIRECTORY = HOME_DIRECTORY + "worlds" + File.separator;

	public PxWorlds(Window cWindow, ConfigurationStorage cConfigurationStorage) {
		super(cWindow, cConfigurationStorage, new PxGameStateManager(cConfigurationStorage));
		WorldManager wm = new WorldManager();
		wm.init();
	}

	@Override
    public void afterWindowCreation(final Window window) {
		window.setTitle(TITLE);
	}

	@Override
	protected void loadAssets() {
		Assets.initAsset();
	}

	@Override
	protected int getTickRate() {
		return TICK_RATE;
	}

	@Override
	protected int getMaxUpdatesPerFrame() {
		return MAX_UPDATES_PER_FRAME;
	}

	@Override
	protected BooleanSupplier getDebugToggle() {
		return () -> window.getInputProcessor().wasPressed(InputAction.DEBUG_TOGGLE.getDefaultKey());
	}

	@Override
	protected DebugHud createDebugHud() {
		return new DebugOverlay();
	}

	@Override
	protected void cleanup() {
		Assets.deleteAsset();
		super.cleanup();
	}
}
