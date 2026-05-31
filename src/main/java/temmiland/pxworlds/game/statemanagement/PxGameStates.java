package temmiland.pxworlds.game.statemanagement;

import temmiland.pxworlds.game.statemanagement.states.*;
import temmiland.rollercoaster.game.state.GameStateDescriptor;

import java.util.List;

public final class PxGameStates {

    public static final GameStateDescriptor<PxGameStateManager> BRAND
            = new GameStateDescriptor<>("BRAND",        BrandGameState::new);

    public static final GameStateDescriptor<PxGameStateManager> MENU
            = new GameStateDescriptor<>("MENU",         MainMenuGameState::new);

    public static final GameStateDescriptor<PxGameStateManager> LEVEL
            = new GameStateDescriptor<>("LEVEL",        LevelGameState::new);

    public static final GameStateDescriptor<PxGameStateManager> WORLD_SELECT
            = new GameStateDescriptor<>("WORLD_SELECT", WorldSelectGameState::new);

    public static final GameStateDescriptor<PxGameStateManager> WORLD_NEW
            = new GameStateDescriptor<>("WORLD_NEW",    NewWorldGameState::new);

    public static final GameStateDescriptor<PxGameStateManager> OPTIONS
            = new GameStateDescriptor<>("OPTIONS",      OptionsGameState::new);

    public static final List<GameStateDescriptor<PxGameStateManager>> ALL =
            List.of(BRAND, MENU, LEVEL, WORLD_SELECT, WORLD_NEW, OPTIONS);

    private PxGameStates() {}
}
