package temmiland.pxworlds.game.statemanagement.states;
import temmiland.pxworlds.game.statemanagement.PxGameStateManager;
import temmiland.rollercoaster.game.state.GameState;
import temmiland.pxworlds.game.statemanagement.PxGameStates;
import temmiland.rollercoaster.platform.Window;

public class BrandGameState extends GameState<PxGameStateManager> {

    private static final double BRAND_DURATION = 1.0;

    private double elapsed;

    public BrandGameState(final PxGameStateManager gsm) {
        super(gsm);
    }

    @Override
    protected void onInit(final Window window) {
        elapsed = 0.0;
    }

    @Override
    protected void onUpdate(final double delta) {
        elapsed += delta;
        if (elapsed >= BRAND_DURATION) {
            gsm.setCurrentState(PxGameStates.MENU);
        }
    }

    @Override
    protected void onRender(final float alpha) {}

    @Override
    protected boolean showDebugMenu() { return false; }
}
