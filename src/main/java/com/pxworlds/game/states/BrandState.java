package com.pxworlds.game.states;

import com.pxworlds.game.io.Window;

/**
 * Created by temmiland on 28.09.2017.
 */
public class BrandState extends GameState {

    /** Anzeigedauer des Splash-Screens in Sekunden. */
    private static final double BRAND_DURATION = 1.0;

    /** Das Spielfenster. */
    private Window window;
    /** Vergangene Zeit seit Eintritt in diesen State. */
    private double elapsed = 0.0;

    public BrandState(GameStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void init(Window newWindow) {
        this.window = newWindow;
    }

    @Override
    public void resize() {

    }

    @Override
    public void update(double delta) {
        elapsed += delta;
        if (elapsed >= BRAND_DURATION) {
            gsm.setState(GameStateType.MENU);
        }
    }

    @Override
    public void render(float alpha) {

    }
}
