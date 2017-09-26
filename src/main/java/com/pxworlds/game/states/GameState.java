package com.pxworlds.game.states;

import java.awt.*;

/**
 * Created by tompi on 22.09.2017.
 */
public abstract class GameState {

    protected GameStateManager gsm;
    public abstract void init();
    public abstract void update();
    public abstract void draw(Graphics2D g);
    public abstract void keyPressed(int k);
    public abstract void keyReleased(int k);

}
