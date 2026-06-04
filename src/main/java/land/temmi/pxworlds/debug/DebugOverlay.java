package land.temmi.pxworlds.debug;

import land.temmi.pxworlds.Version;
import land.temmi.rollercoaster.debug.DebugHud;
import land.temmi.rollercoaster.platform.Window;

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
 * HUD overlay for debug information.
 *
 * <p>Always shows the version number and FPS in the top left corner. F3 toggles
 * an extended view with TPS, the alpha value and state-specific lines (e.g.
 * chunk stats).</p>
 *
 * <p>All texts are rendered with a drop shadow.</p>
 */
public class DebugOverlay implements DebugHud {

    /** Scale factor of the debug font for readability on modern screens. */
    private static final float TEXT_SCALE = 2.0f;
    /** Left and top margin in STBEasyFont coordinate units (before TEXT_SCALE). */
    private static final float MARGIN = 4.0f;
    /** Vertical line spacing in STBEasyFont coordinate units (before TEXT_SCALE). */
    private static final float LINE_HEIGHT = 10.0f;
    /** OpenGL program id 0 — unbinds any active shader. */
    private static final int NO_SHADER = 0;
    /** Separator between built-in lines and state-specific lines. */
    private static final String SEPARATOR = "---";
    /** Hint shown in compact mode. */
    private static final String F3_HINT = "[F3]";
    /** Header shown in detailed mode. */
    private static final String F3_HEADER = "[F3 Debug]";

    /** Whether the extended debug view is currently visible. */
    private boolean showDetailed = false;
    /** Most recently measured frames per second. */
    private int currentFps = 0;

    /** Toggles the extended debug view on or off. */
    public void toggleDetailed() {
        showDetailed = !showDetailed;
    }

    /** @return {@code true} if the extended debug view (F3) is active */
    public boolean isShowDetailed() {
        return showDetailed;
    }

    /**
     * Updates the displayed FPS value.
     * @param fps the measured frames per second
     */
    public void updateFps(final int fps) {
        currentFps = fps;
    }

    /**
     * Draws the debug overlay on top of the current frame.
     *
     * @param window     the game window
     * @param alpha      the current render interpolation factor (0.0–1.0)
     * @param extraLines state-specific debug lines (may be empty, but not null)
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
                "pxWorlds " + Version.GAME + " - " + "rollercoaster: " + Version.ROLLERCOASTER,
                MARGIN, MARGIN);

        float lineY = MARGIN + LINE_HEIGHT;

        if (showDetailed) {
            DebugRenderer.drawStringShadowed(F3_HEADER, MARGIN, lineY);
            lineY += LINE_HEIGHT;
            DebugRenderer.drawStringShadowed("FPS:    " + currentFps, MARGIN, lineY); lineY += LINE_HEIGHT;

            // State-specific extra lines (e.g. chunk statistics from LevelState)
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
