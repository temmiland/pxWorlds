package com.pxworlds.game.states;

import com.pxworlds.game.io.Window;

/**
 * Created by tompi on 28.09.2017.
 */
public abstract class GameState {

    protected GameStateManager gsm;
    public abstract void init(Window window);
    public abstract void resize();
    public abstract void update(double frame_cap) ;
    public abstract void render();

}