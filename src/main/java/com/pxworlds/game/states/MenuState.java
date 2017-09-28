package com.pxworlds.game.states;

import com.pxworlds.gui.Gui;
import com.pxworlds.io.Window;

/**
 * Created by tompi on 28.09.2017.
 */
public class MenuState extends GameState {

    private Gui gui;
    private Window window;

    public MenuState(GameStateManager gsm) {
        this.gsm = gsm;
    }

    @Override
    public void init(Window window) {
        this.window = window;
        this.gui = new Gui(window);

    }

    @Override
    public void resize() {
        gui.resizeCamera();
    }

    @Override
    public void update(double frame_cap)  {
        gui.update(window.getInput());
    }

    @Override
    public void render() {
        gui.render();
    }
}
