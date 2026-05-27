package com.pxworlds.game.states;

import com.pxworlds.game.io.Window;

/**
 * Created by temmiland on 28.09.2017.
 */
public class BrandState extends GameState {

    /** The duration of the brand screen in seconds. */
    private static final double BRAND_DURATION = 1.0;

    /** The window. */
    private Window window;
    /** Accumulated time since the state was entered. */
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
            gsm.setState(1);
        }
    }

    @Override
    public void render(float alpha) {

    }
}
