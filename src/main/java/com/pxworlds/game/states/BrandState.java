package com.pxworlds.game.states;

import com.pxworlds.game.io.Window;

import java.util.concurrent.TimeUnit;

/**
 * Created by tompi on 28.09.2017.
 */
public class BrandState extends GameState {

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
    public void update(double frame_cap)  {

    }

    @Override
    public void render() {
        System.out.println("IAM IN BRANDSTATE!!!");
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        gsm.setState(1);


    }
}
