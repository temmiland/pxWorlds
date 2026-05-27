package com.pxworlds.game.states;

import com.pxworlds.game.io.Window;

/**
 * Created by temmiland on 28.09.2017.
 */
public abstract class GameState {

    /** The game state manager. */
    protected GameStateManager gsm;
    public abstract void init(Window window);
    public abstract void resize();
    public abstract void update(double delta);

    /**
     * Renders the current state.
     * @param alpha Interpolationsfaktor 0.0–1.0 zwischen letztem und nächstem Tick.
     */
    public abstract void render(float alpha);

}
