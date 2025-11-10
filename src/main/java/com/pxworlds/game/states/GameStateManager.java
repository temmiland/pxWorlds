package com.pxworlds.game.states;

import com.pxworlds.game.io.Window;

import java.util.ArrayList;

/**
 * Created by temmiland on 28.09.2017.
 */
public class GameStateManager {

    /** The list of game states. */
    private ArrayList<GameState> gameStates;
    /** The index of the current state. */
    private int currentState;
    /** The window. */
    private Window window;

    public GameStateManager() {
        gameStates = new ArrayList<>();

        // Setzt den BrandState als erster State nach dem Erstellen
        currentState = 0;

        gameStates.add(new BrandState(this)); // gameState 0
        gameStates.add(new MenuState(this));  // gameState 1
        gameStates.add(new LevelState(this));      // gameState 2

    }

    public void setState(int state) {
        currentState = state;
        gameStates.get(currentState).init(window);
    }

    public void init(Window newWindow) {
        this.window = newWindow;
        gameStates.get(currentState).init(newWindow);
    }

    public void resize() {
        gameStates.get(currentState).resize();
    }

    public void update(double frameCap)  {
        gameStates.get(currentState).update(frameCap);
    }

    public void render() {
        gameStates.get(currentState).render();
    }

}
