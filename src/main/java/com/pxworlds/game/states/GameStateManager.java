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
        gameStates.add(new LevelState(this, "test_level"));      // gameState 2

    }

    public void setState(GameStateType state) {
        currentState = state.getIndex();
        gameStates.get(currentState).init(window);
    }

    public void init(Window newWindow) {
        this.window = newWindow;
        gameStates.get(currentState).init(newWindow);
    }

    public void resize() {
        gameStates.get(currentState).resize();
    }

    public void update(double delta) {
        gameStates.get(currentState).update(delta);
    }

    public void render(float alpha) {
        gameStates.get(currentState).render(alpha);
    }

    /**
     * Zeichnet Debug-Visualisierungen für den aktuellen State (delegiert an {@link GameState#renderDebug}).
     * Nur aufrufen, wenn der erweiterte Debug-Modus (F3) aktiv ist.
     *
     * @param alpha Interpolationsfaktor 0.0–1.0
     */
    public void renderDebug(final float alpha) {
        gameStates.get(currentState).renderDebug(alpha);
    }

    /**
     * Zeichnet GUI-Overlays des aktuellen States nach den Debug-Boxen.
     *
     * @param alpha Interpolationsfaktor 0.0–1.0
     */
    public void renderGui(final float alpha) {
        gameStates.get(currentState).renderGui(alpha);
    }

    /**
     * Liefert state-spezifische Debug-Zeilen für das F3-Panel.
     * @return Zeilen die unter dem Separator im Debug-Panel erscheinen
     */
    public String[] getDebugLines() {
        return gameStates.get(currentState).getDebugLines();
    }

    /**
     * Ruft {@link GameState#save()} auf allen registrierten States auf.
     * Wird beim geordneten Beenden des Spiels aufgerufen.
     */
    public void save() {
        for (final GameState state : gameStates) {
            state.save();
        }
    }

}
