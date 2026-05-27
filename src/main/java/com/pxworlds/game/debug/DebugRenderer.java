package com.pxworlds.game.debug;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBEasyFont;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_VERTEX_ARRAY;
import static org.lwjgl.opengl.GL11.glDisableClientState;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnableClientState;
import static org.lwjgl.opengl.GL11.glVertexPointer;

/**
 * Utility class for rendering debug text using STBEasyFont.
 * No external font assets required — the font is built into the STB library.
 * Uses the legacy OpenGL fixed-function pipeline (compatibility profile).
 */
public final class DebugRenderer {

    /** Bytes allocated per character in the STBEasyFont vertex buffer. */
    private static final int BYTES_PER_CHAR = 270;
    /** Byte stride between vertices in the STBEasyFont vertex buffer. */
    private static final int VERTEX_STRIDE = 16;
    /** Number of vertices per rendered glyph quad. */
    private static final int VERTICES_PER_QUAD = 4;
    /** Number of position components (x, y) per vertex. */
    private static final int POSITION_COMPONENTS = 2;

    /** Private constructor — static utility class, not instantiable. */
    private DebugRenderer() { }

    /**
     * Draws a string at the given position in STBEasyFont coordinate space.
     * The caller is responsible for setting up the OpenGL projection and modelview matrices.
     * @param text The string to render.
     * @param x    X position in font coordinates.
     * @param y    Y position in font coordinates.
     */
    public static void drawString(final String text, final float x, final float y) {
        final ByteBuffer charBuffer = BufferUtils.createByteBuffer(text.length() * BYTES_PER_CHAR);
        final int numQuads = STBEasyFont.stb_easy_font_print(x, y, text, null, charBuffer);
        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(POSITION_COMPONENTS, GL_FLOAT, VERTEX_STRIDE, charBuffer);
        glDrawArrays(GL_QUADS, 0, numQuads * VERTICES_PER_QUAD);
        glDisableClientState(GL_VERTEX_ARRAY);
    }
}
