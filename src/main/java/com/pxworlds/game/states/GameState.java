package com.pxworlds.game.states;

import com.pxworlds.game.io.Window;

/**
 * Created by temmiland on 28.09.2017.
 */
public abstract class GameState {

    /** Der zugehörige Game-State-Manager. */
    protected GameStateManager gsm;
    public abstract void init(Window window);
    public abstract void resize();
    public abstract void update(double delta);

    /**
     * Rendert den aktuellen State.
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

    /**
     * Zeichnet GUI-Overlays (Menüs, Buttons etc.) nach den Debug-Boxen.
     * Wird nach {@link #renderDebug} aufgerufen, damit GUIs immer oben liegen.
     *
     * <p>Standard-Implementierung ist ein No-op.</p>
     *
     * @param alpha Interpolationsfaktor 0.0–1.0
     */
    public void renderGui(final float alpha) {
        // no-op by default
    }

    /**
     * Liefert state-spezifische Zeilen für das F3-Debug-Panel.
     *
     * <p>Standard-Implementierung gibt ein leeres Array zurück.
     * States mit eigenen Debug-Infos überschreiben diese Methode.</p>
     *
     * @return Zeilen die unter dem Separator im Debug-Panel erscheinen
     */
    public String[] getDebugLines() {
        return new String[0];
    }

    /**
     * Persistiert den State-Zustand auf Festplatte (z.B. Welt-Daten).
     * Wird beim geordneten Beenden des Spiels aufgerufen.
     *
     * <p>Standard-Implementierung ist ein No-op — States mit speicherbaren
     * Daten überschreiben diese Methode.</p>
     */
    public void save() {
        // no-op by default
    }

}
