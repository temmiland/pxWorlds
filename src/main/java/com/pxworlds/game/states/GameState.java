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

    /**
     * Zeichnet Debug-Visualisierungen (z.B. Kollisions-AABBs) über das Spielbild.
     * Wird nur aufgerufen, wenn der erweiterte Debug-Modus (F3) aktiv ist.
     *
     * <p>Standard-Implementierung ist ein No-op — States die Debug-Rendering
     * unterstützen, überschreiben diese Methode.</p>
     *
     * @param alpha Interpolationsfaktor 0.0–1.0
     */
    public void renderDebug(final float alpha) {
        // no-op by default
    }

}
