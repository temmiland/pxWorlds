package land.temmi.pxworlds.statemanagement;

import land.temmi.pxworlds.statemanagement.states.*;
import land.temmi.rollercoaster.game.state.GameStateDescriptor;

import java.util.List;

/**
 * Registry of all pxWorlds game state descriptors.
 *
 * <p>Each descriptor pairs a state key with its factory. {@link #ALL} is the
 * complete list registered with the {@link PxGameStateManager}.</p>
 */
public final class PxGameStates {

    /** Branding/splash screen shown at startup. */
    public static final GameStateDescriptor<PxGameStateManager> BRAND
            = new GameStateDescriptor<>("BRAND",        BrandGameState::new);

    /** Main menu screen. */
    public static final GameStateDescriptor<PxGameStateManager> MENU
            = new GameStateDescriptor<>("MENU",         MainMenuGameState::new);

    /** In-world gameplay screen. */
    public static final GameStateDescriptor<PxGameStateManager> LEVEL
            = new GameStateDescriptor<>("LEVEL",        LevelGameState::new);

    /** World selection screen. */
    public static final GameStateDescriptor<PxGameStateManager> WORLD_SELECT
            = new GameStateDescriptor<>("WORLD_SELECT", WorldSelectGameState::new);

    /** New-world creation screen. */
    public static final GameStateDescriptor<PxGameStateManager> WORLD_NEW
            = new GameStateDescriptor<>("WORLD_NEW",    NewWorldGameState::new);

    /** Options/settings screen. */
    public static final GameStateDescriptor<PxGameStateManager> OPTIONS
            = new GameStateDescriptor<>("OPTIONS",      OptionsGameState::new);

    /** All registered game states, in registration order. */
    public static final List<GameStateDescriptor<PxGameStateManager>> ALL =
            List.of(BRAND, MENU, LEVEL, WORLD_SELECT, WORLD_NEW, OPTIONS);

    private PxGameStates() {}
}
