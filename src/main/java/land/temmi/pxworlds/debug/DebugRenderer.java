package land.temmi.pxworlds.debug;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBEasyFont;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glDisableClientState;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glVertexPointer;

/**
 * Helper class for rendering debug text with STBEasyFont.
 *
 * <p>No external font assets are required — the font is built into the STB
 * library. Uses the legacy OpenGL fixed-function pipeline (compatibility
 * profile).</p>
 */
public final class DebugRenderer {

    /** Bytes per character in the STBEasyFont vertex buffer. */
    private static final int BYTES_PER_CHAR = 270;
    /** Byte stride between vertices in the STBEasyFont vertex buffer. */
    private static final int VERTEX_STRIDE = 16;
    /** Number of vertices per rendered glyph quad. */
    private static final int VERTICES_PER_QUAD = 4;
    /** Number of position components (x, y) per vertex. */
    private static final int POSITION_COMPONENTS = 2;

    /** Shadow color red: dark gray, looks slightly transparent on any background. */
    private static final float SHADOW_R = 0.1f;
    /** Shadow color green. */
    private static final float SHADOW_G = 0.1f;
    /** Shadow color blue. */
    private static final float SHADOW_B = 0.1f;
    /** Foreground color red: white. */
    private static final float FG_R = 1.0f;
    /** Foreground color green. */
    private static final float FG_G = 1.0f;
    /** Foreground color blue. */
    private static final float FG_B = 1.0f;
    /** Shadow pixel offset in font coordinates (before TEXT_SCALE). */
    private static final float SHADOW_OFFSET = 1.0f;

    /** Private constructor — static utility class, not instantiable. */
    private DebugRenderer() { }

    /**
     * Draws a string at the given position in STBEasyFont coordinate space.
     * The caller is responsible for setting up the OpenGL projection and
     * modelview matrices.
     * @param text the string to render
     * @param x    X position in font coordinates
     * @param y    Y position in font coordinates
     */
    public static void drawString(final String text, final float x, final float y) {
        if (text.isEmpty()) {
            return; // a 0-capacity buffer would trigger a native STB write-past-end
        }
        final ByteBuffer charBuffer = BufferUtils.createByteBuffer(text.length() * BYTES_PER_CHAR);
        final int numQuads = STBEasyFont.stb_easy_font_print(x, y, text, null, charBuffer);
        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(POSITION_COMPONENTS, GL_FLOAT, VERTEX_STRIDE, charBuffer);
        glDrawArrays(GL_QUADS, 0, numQuads * VERTICES_PER_QUAD);
        glDisableClientState(GL_VERTEX_ARRAY);
    }

    /**
     * Draws a string with a dark drop shadow.
     * Manages its own colors — no manual {@code glColor3f} needed.
     * @param text the string to render
     * @param x    X position in font coordinates
     * @param y    Y position in font coordinates
     */
    public static void drawStringShadowed(final String text, final float x, final float y) {
        glColor3f(SHADOW_R, SHADOW_G, SHADOW_B);
        drawString(text, x + SHADOW_OFFSET, y + SHADOW_OFFSET);
        glColor3f(FG_R, FG_G, FG_B);
        drawString(text, x, y);
    }
}
