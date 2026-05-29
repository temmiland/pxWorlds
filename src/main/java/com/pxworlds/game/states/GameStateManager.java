package com.pxworlds.game.states;

import com.pxworlds.game.io.Window;

import java.util.ArrayList;

/**
 * Created by temmiland on 28.09.2017.
 */
public class GameStateManager {

    /** Liste aller registrierten Game-States. */
    private ArrayList<GameState> gameStates;
    /** Index des aktuell aktiven States. */
    private int currentState;
    /** Das Spielfenster. */
    private Window window;

    public GameStateManager() {
        gameStates = new ArrayList<>();

        // Setzt den BrandState als erster State nach dem Erstellen
        currentState = 0;

        gameStates.add(new BrandState(this));           // 0 – BRAND
        gameStates.add(new MenuState(this));            // 1 – MENU
        gameStates.add(new LevelState(this, null));     // 2 – LEVEL (Welt wird via startLevel gesetzt)
        gameStates.add(new WorldSelectState(this));     // 3 – WORLD_SELECT
        gameStates.add(new NewWorldState(this));        // 4 – NEW_WORLD

    }

    public void setState(GameStateType state) {
        currentState = state.getIndex();
        gameStates.get(currentState).init(window);
    }

    /**
     * Setzt die Zielwelt und wechselt in den {@link LevelState}.
     * Ersetzt den bestehenden LevelState durch eine frische Instanz mit dem neuen Weltnamen,
     * sodass alte OpenGL-Ressourcen korrekt freigegeben werden.
     *
     * @param worldName Weltname ohne {@code .pxwl}-Endung
     */
    public void startLevel(final String worldName) {
        gameStates.set(GameStateType.LEVEL.getIndex(), new LevelState(this, worldName));
        setState(GameStateType.LEVEL);
    }

    public void showMigrationError(final String message) {
        ((WorldSelectState) gameStates.get(GameStateType.WORLD_SELECT.getIndex()))
                .setMigrationError(message);
        setState(GameStateType.WORLD_SELECT);
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
