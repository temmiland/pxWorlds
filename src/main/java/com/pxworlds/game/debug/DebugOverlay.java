package com.pxworlds.game.debug;

import com.pxworlds.Constants;
import com.pxworlds.game.io.Window;

import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.glColor3f;
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
 * F3 schaltet eine erweiterte Ansicht mit TPS und Alpha-Wert ein und aus.</p>
 */
public class DebugOverlay {

    /** Scale factor applied to the debug font for readability on modern screens. */
    private static final float TEXT_SCALE = 2.0f;
    /** Left and top margin in STBEasyFont coordinate units (before TEXT_SCALE is applied). */
    private static final float MARGIN = 4.0f;
    /** Vertical line spacing in STBEasyFont coordinate units (before TEXT_SCALE is applied). */
    private static final float LINE_HEIGHT = 10.0f;
    /** OpenGL program ID 0 — unbinds any active shader. */
    private static final int NO_SHADER = 0;
    /** Full white, red channel. */
    private static final float WHITE = 1.0f;
    /** Hint shown in compact mode. */
    private static final String F3_HINT = "[F3]";
    /** Header shown in detailed mode. */
    private static final String F3_HEADER = "[F3 Debug]";

    /** Whether the detailed debug view is currently visible. */
    private boolean showDetailed = false;
    /** Most recently recorded frames per second. */
    private int currentFps = 0;

    /**
     * Toggles the detailed debug view on or off.
     */
    public void toggleDetailed() {
        showDetailed = !showDetailed;
    }

    /**
     * @return {@code true} wenn die erweiterte Debug-Ansicht (F3) aktiv ist
     */
    public boolean isShowDetailed() {
        return showDetailed;
    }

    /**
     * Updates the displayed FPS value. Call this once per second when the frame counter resets.
     * @param fps The measured frames per second.
     */
    public void updateFps(final int fps) {
        currentFps = fps;
    }

    /**
     * Renders the debug overlay on top of the current frame.
     * Must be called after {@code gsm.render()} and before {@code window.swapBuffers()}.
     * @param window The game window, used to read current dimensions.
     * @param alpha  The current render-interpolation factor (0.0–1.0).
     */
    public void render(final Window window, final float alpha) {
        // Unbind any active game shader so fixed-function GL works
        glUseProgram(NO_SHADER);

        // Switch to orthographic screen-space projection (top-left origin)
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, window.getWidth(), window.getHeight(), 0, -1, 1);

        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glScalef(TEXT_SCALE, TEXT_SCALE, 1.0f);

        glColor3f(WHITE, WHITE, WHITE);

        // --- Always-visible line: version + FPS ---
        DebugRenderer.drawString("pxWorlds v" + Constants.VERSION + "  " + currentFps + " fps", MARGIN, MARGIN);

        // --- Second line: hint or detailed header ---
        float lineY = MARGIN + LINE_HEIGHT;

        if (showDetailed) {
            DebugRenderer.drawString(F3_HEADER, MARGIN, lineY);
            lineY += LINE_HEIGHT;
            DebugRenderer.drawString("FPS:   " + currentFps, MARGIN, lineY);
            lineY += LINE_HEIGHT;
            DebugRenderer.drawString("TPS:   " + Constants.TICK_RATE + " (fixed)", MARGIN, lineY);
            lineY += LINE_HEIGHT;
            DebugRenderer.drawString("Alpha: " + String.format("%.3f", alpha), MARGIN, lineY);
        } else {
            DebugRenderer.drawString(F3_HINT, MARGIN, lineY);
        }

        // Restore previous matrices
        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
    }
}
