package com.pxworlds.game.states;

import com.pxworlds.game.io.Window;

import java.util.concurrent.TimeUnit;

/**
 * Created by temmiland on 28.09.2017.
 */
public class BrandState extends GameState {

    /** The window. */
    private Window window;

    public BrandState(GameStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void init(Window window) {
        this.window = window;
    }

    @Override
    public void resize() {

    }

    @Override
    public void update(double frameCap)  {

    }

    @Override
    public void render() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        gsm.setState(1);


    }
}
