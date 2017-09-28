package com.pxworlds.game.states;

import com.pxworlds.io.Window;

import java.util.ArrayList;

/**
 * Created by tompi on 28.09.2017.
 */
public class GameStateManager {

    private ArrayList<GameState> gameStates;
    private int currentState;
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

    public void init(Window window) {
        this.window = window;
        gameStates.get(currentState).init(window);
    }

    public void resize() {
        gameStates.get(currentState).resize();
    }

    public void update(double frame_cap)  {
        gameStates.get(currentState).update(frame_cap);
    }

    public void render() {
        gameStates.get(currentState).render();
    }

}
