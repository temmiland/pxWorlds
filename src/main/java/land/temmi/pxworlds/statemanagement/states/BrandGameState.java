package land.temmi.pxworlds.statemanagement.states;
import land.temmi.pxworlds.statemanagement.PxGameStateManager;
import land.temmi.rollercoaster.game.state.GameState;
import land.temmi.pxworlds.statemanagement.PxGameStates;
import land.temmi.rollercoaster.platform.Window;

/**
 * Branding/splash screen shown at startup.
 *
 * <p>Displays for {@link #BRAND_DURATION} seconds and then automatically
 * transitions to the main menu.</p>
 */
public class BrandGameState extends GameState<PxGameStateManager> {

    /** Duration the branding screen is shown, in seconds. */
    private static final double BRAND_DURATION = 1.0;

    /** Time elapsed since the screen was shown, in seconds. */
    private double elapsed;

    /**
     * Creates the branding state.
     *
     * @param gsm the game state manager
     */
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
    protected boolean onDebugOverlayEnabled() {
        return false;
    }
}
