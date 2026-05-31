package temmiland.pxworlds.game.statemanagement;

import temmiland.pxworlds.game.statemanagement.states.LevelGameState;
import temmiland.pxworlds.game.statemanagement.states.WorldSelectGameState;
import temmiland.rollercoaster.configuration.ConfigurationStorage;
import temmiland.rollercoaster.game.state.GameStateDescriptor;
import temmiland.rollercoaster.game.state.GameStateManager;

import java.util.List;

public class PxGameStateManager extends GameStateManager<PxGameStateManager> {

    /** Configuration storage shared with all game states. */
    private final ConfigurationStorage configurationStorage;

    public PxGameStateManager(final ConfigurationStorage cConfigurationStorage) {
        this.configurationStorage = cConfigurationStorage;
    }

    /** Returns the configuration storage accessible to all game states. */
    public ConfigurationStorage getConfigurationStorage() {
        return configurationStorage;
    }

    @Override
    protected List<GameStateDescriptor<PxGameStateManager>> getStates() {
        return PxGameStates.ALL;
    }

    @Override
    protected GameStateDescriptor<PxGameStateManager> getInitialState() {
        return PxGameStates.BRAND;
    }

    public void setLevelState(final String worldName) {
        ((LevelGameState) getState(PxGameStates.LEVEL)).setWorldName(worldName);
        setCurrentState(PxGameStates.LEVEL);
    }

    public void setMigrationErrorState(final String message) {
        ((WorldSelectGameState) getState(PxGameStates.WORLD_SELECT))
                .setMigrationError(message);
        setCurrentState(PxGameStates.WORLD_SELECT);
    }
}
