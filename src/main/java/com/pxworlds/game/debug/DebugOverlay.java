package com.pxworlds.game.debug;

import com.pxworlds.Constants;
import com.pxworlds.game.io.Window;

import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
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

    /** Scale factor applied to the debug font for readability on modern screens. */
    private static final float TEXT_SCALE = 2.0f;
    /** Left and top margin in STBEasyFont coordinate units (before TEXT_SCALE). */
    private static final float MARGIN = 4.0f;
    /** Vertical line spacing in STBEasyFont coordinate units (before TEXT_SCALE). */
    private static final float LINE_HEIGHT = 10.0f;
    /** OpenGL program ID 0 — unbinds any active shader. */
    private static final int NO_SHADER = 0;
    /** Separator drawn between built-in lines and state-specific extra lines. */
    private static final String SEPARATOR = "---";
    /** Hint shown in compact mode. */
    private static final String F3_HINT = "[F3]";
    /** Header shown in detailed mode. */
    private static final String F3_HEADER = "[F3 Debug]";

    /** Whether the detailed debug view is currently visible. */
    private boolean showDetailed = false;
    /** Most recently recorded frames per second. */
    private int currentFps = 0;

    /** Toggles the detailed debug view on or off. */
    public void toggleDetailed() {
        showDetailed = !showDetailed;
    }

    /** @return {@code true} wenn die erweiterte Debug-Ansicht (F3) aktiv ist */
    public boolean isShowDetailed() {
        return showDetailed;
    }

    /**
     * Updates the displayed FPS value.
     * @param fps The measured frames per second.
     */
    public void updateFps(final int fps) {
        currentFps = fps;
    }

    /**
     * Renders the debug overlay on top of the current frame.
     *
     * @param window     The game window.
     * @param alpha      The current render-interpolation factor (0.0–1.0).
     * @param extraLines State-specific debug lines (may be empty, never null).
     */
    public void render(final Window window, final float alpha, final String[] extraLines) {
        glUseProgram(NO_SHADER);

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
    }
}
