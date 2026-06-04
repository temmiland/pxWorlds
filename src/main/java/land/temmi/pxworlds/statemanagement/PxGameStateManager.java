package land.temmi.pxworlds.statemanagement;

import land.temmi.pxworlds.statemanagement.states.LevelGameState;
import land.temmi.pxworlds.statemanagement.states.WorldSelectGameState;
import land.temmi.rollercoaster.config.ConfigurationStorage;
import land.temmi.rollercoaster.game.state.GameStateDescriptor;
import land.temmi.rollercoaster.game.state.GameStateManager;

import java.util.List;

/**
 * pxWorlds-specific {@link GameStateManager}.
 *
 * <p>Holds the shared {@link ConfigurationStorage}, tracks which screen the
 * options menu should return to, and provides convenience transitions into the
 * level and world-select states.</p>
 */
public class PxGameStateManager extends GameStateManager<PxGameStateManager> {

    /** Configuration storage shared with all game states. */
    private final ConfigurationStorage configurationStorage;
    /** State the options screen returns to when left (main menu or pause screen). */
    private GameStateDescriptor<PxGameStateManager> optionsReturnState = PxGameStates.MENU;

    /**
     * Creates the state manager.
     *
     * @param configurationStorage the configuration storage shared with all states
     */
    public PxGameStateManager(final ConfigurationStorage configurationStorage) {
        this.configurationStorage = configurationStorage;
    }

    /**
     * Returns the configuration storage accessible to all game states.
     *
     * @return the configuration storage
     */
    public ConfigurationStorage getConfigurationStorage() {
        return configurationStorage;
    }

    /**
     * Sets the state the options screen returns to when left.
     *
     * @param key the return-state descriptor (e.g. main menu or level)
     */
    public void setOptionsReturnState(final GameStateDescriptor<PxGameStateManager> key) {
        this.optionsReturnState = key;
    }

    /**
     * Returns the state the options screen returns to when left.
     *
     * @return the return-state descriptor
     */
    public GameStateDescriptor<PxGameStateManager> getOptionsReturnState() {
        return optionsReturnState;
    }

    @Override
    protected List<GameStateDescriptor<PxGameStateManager>> getStates() {
        return PxGameStates.ALL;
    }

    @Override
    protected GameStateDescriptor<PxGameStateManager> getInitialState() {
        return PxGameStates.BRAND;
    }

    /**
     * Enters the level state for the given world.
     *
     * @param worldName the name of the world to load
     */
    public void setLevelState(final String worldName) {
        ((LevelGameState) getState(PxGameStates.LEVEL)).setWorldName(worldName);
        setCurrentState(PxGameStates.LEVEL);
    }

    /**
     * Switches to the world-select state and shows a migration error message.
     *
     * @param message the migration error message to display
     */
    public void setMigrationErrorState(final String message) {
        ((WorldSelectGameState) getState(PxGameStates.WORLD_SELECT))
                .setMigrationError(message);
        setCurrentState(PxGameStates.WORLD_SELECT);
    }
}
