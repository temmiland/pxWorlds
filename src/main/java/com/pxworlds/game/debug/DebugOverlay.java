package com.pxworlds.game.debug;

import com.pxworlds.Constants;
import com.pxworlds.game.io.Window;

import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL20.glUseProgram;

/**
 * HUD-Overlay für Debug-Informationen.
 *
 * <p>Zeigt Versionsnummer und FPS immer oben links an.
 * F3 schaltet eine erweiterte Ansicht mit TPS, Alpha-Wert und
 * State-spezifischen Zeilen (z.B. Chunk-Stats) ein und aus.</p>
 *
 * <p>Alle Texte werden mit einem Drop-Shadow gerendert.</p>
 */
public class DebugOverlay {

    /** Skalierungsfaktor des Debug-Fonts für Lesbarkeit auf modernen Bildschirmen. */
    private static final float TEXT_SCALE = 2.0f;
    /** Linker und oberer Rand in STBEasyFont-Koordinateneinheiten (vor TEXT_SCALE). */
    private static final float MARGIN = 4.0f;
    /** Vertikaler Zeilenabstand in STBEasyFont-Koordinateneinheiten (vor TEXT_SCALE). */
    private static final float LINE_HEIGHT = 10.0f;
    /** OpenGL-Programm-ID 0 — löst jeden aktiven Shader. */
    private static final int NO_SHADER = 0;
    /** Trennlinie zwischen eingebauten Zeilen und state-spezifischen Zeilen. */
    private static final String SEPARATOR = "---";
    /** Hinweis im kompakten Modus. */
    private static final String F3_HINT = "[F3]";
    /** Überschrift im Detailmodus. */
    private static final String F3_HEADER = "[F3 Debug]";

    /** Ob die erweiterte Debug-Ansicht aktuell sichtbar ist. */
    private boolean showDetailed = false;
    /** Zuletzt gemessene Frames pro Sekunde. */
    private int currentFps = 0;

    /** Schaltet die erweiterte Debug-Ansicht ein oder aus. */
    public void toggleDetailed() {
        showDetailed = !showDetailed;
    }

    /** @return {@code true} wenn die erweiterte Debug-Ansicht (F3) aktiv ist */
    public boolean isShowDetailed() {
        return showDetailed;
    }

    /**
     * Aktualisiert den angezeigten FPS-Wert.
     * @param fps die gemessenen Frames pro Sekunde
     */
    public void updateFps(final int fps) {
        currentFps = fps;
    }

    /**
     * Zeichnet das Debug-Overlay über dem aktuellen Frame.
     *
     * @param window     das Spielfenster
     * @param alpha      der aktuelle Render-Interpolationsfaktor (0.0–1.0)
     * @param extraLines state-spezifische Debug-Zeilen (darf leer, aber nicht null sein)
     */
    public void render(final Window window, final float alpha, final String[] extraLines) {
        glUseProgram(NO_SHADER);
        glDisable(GL_TEXTURE_2D);

        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, window.getWidth(), window.getHeight(), 0, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glScalef(TEXT_SCALE, TEXT_SCALE, 1.0f);

        // --- Always-visible: version + FPS ---
        DebugRenderer.drawStringShadowed(
                "pxWorlds v" + Constants.VERSION + "  " + currentFps + " fps",
                MARGIN, MARGIN);

        float lineY = MARGIN + LINE_HEIGHT;

        if (showDetailed) {
            DebugRenderer.drawStringShadowed(F3_HEADER, MARGIN, lineY);
            lineY += LINE_HEIGHT;
            DebugRenderer.drawStringShadowed("FPS:   " + currentFps,                       MARGIN, lineY); lineY += LINE_HEIGHT;
            DebugRenderer.drawStringShadowed("TPS:   " + Constants.TICK_RATE + " (fixed)", MARGIN, lineY); lineY += LINE_HEIGHT;
            DebugRenderer.drawStringShadowed("Alpha: " + String.format("%.3f", alpha),     MARGIN, lineY); lineY += LINE_HEIGHT;

            // State-specific extra lines (e.g. chunk stats from LevelState)
            if (extraLines.length > 0) {
                DebugRenderer.drawStringShadowed(SEPARATOR, MARGIN, lineY);
                lineY += LINE_HEIGHT;
                for (final String line : extraLines) {
                    DebugRenderer.drawStringShadowed(line, MARGIN, lineY);
                    lineY += LINE_HEIGHT;
                }
            }
        } else {
            DebugRenderer.drawStringShadowed(F3_HINT, MARGIN, lineY);
        }

        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glEnable(GL_TEXTURE_2D);
    }
}
