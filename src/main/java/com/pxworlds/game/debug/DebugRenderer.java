package com.pxworlds.game.debug;

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
 * Hilfsklasse zum Rendern von Debug-Text mit STBEasyFont.
 * Keine externen Font-Assets erforderlich — der Font ist in der STB-Bibliothek eingebaut.
 * Nutzt die Legacy-OpenGL-Fixed-Function-Pipeline (Compatibility Profile).
 */
public final class DebugRenderer {

    /** Bytes pro Zeichen im STBEasyFont-Vertex-Buffer. */
    private static final int BYTES_PER_CHAR = 270;
    /** Byte-Stride zwischen Vertices im STBEasyFont-Vertex-Buffer. */
    private static final int VERTEX_STRIDE = 16;
    /** Anzahl Vertices pro gerendertem Glyph-Quad. */
    private static final int VERTICES_PER_QUAD = 4;
    /** Anzahl Positionskomponenten (x, y) pro Vertex. */
    private static final int POSITION_COMPONENTS = 2;

    /** Privater Konstruktor — statische Utility-Klasse, nicht instanziierbar. */
    private DebugRenderer() { }

    /** Schattenfarbe Rot: dunkelgrau, wirkt leicht transparent auf jedem Hintergrund. */
    private static final float SHADOW_R = 0.1f;
    /** Schattenfarbe Grün. */
    private static final float SHADOW_G = 0.1f;
    /** Schattenfarbe Blau. */
    private static final float SHADOW_B = 0.1f;
    /** Vordergrundfarbe Rot: weiß. */
    private static final float FG_R = 1.0f;
    /** Vordergrundfarbe Grün. */
    private static final float FG_G = 1.0f;
    /** Vordergrundfarbe Blau. */
    private static final float FG_B = 1.0f;
    /** Schatten-Pixel-Offset in Font-Koordinaten (vor TEXT_SCALE). */
    private static final float SHADOW_OFFSET = 1.0f;

    /**
     * Zeichnet einen String an der angegebenen Position im STBEasyFont-Koordinatenraum.
     * Der Aufrufer ist für das Aufsetzen der OpenGL-Projektions- und Modelview-Matrizen verantwortlich.
     * @param text der zu rendernde String
     * @param x    X-Position in Font-Koordinaten
     * @param y    Y-Position in Font-Koordinaten
     */
    public static void drawString(final String text, final float x, final float y) {
        if (text.isEmpty()) {
            return; // 0-capacity buffer würde nativen STB-Write-past-end auslösen
        }
        final ByteBuffer charBuffer = BufferUtils.createByteBuffer(text.length() * BYTES_PER_CHAR);
        final int numQuads = STBEasyFont.stb_easy_font_print(x, y, text, null, charBuffer);
        glEnableClientState(GL_VERTEX_ARRAY);
        glVertexPointer(POSITION_COMPONENTS, GL_FLOAT, VERTEX_STRIDE, charBuffer);
        glDrawArrays(GL_QUADS, 0, numQuads * VERTICES_PER_QUAD);
        glDisableClientState(GL_VERTEX_ARRAY);
    }

    /**
     * Zeichnet einen String mit dunklem Schlagschatten.
     * Verwaltet eigene Farben — kein manuelles {@code glColor3f} nötig.
     * @param text der zu rendernde String
     * @param x    X-Position in Font-Koordinaten
     * @param y    Y-Position in Font-Koordinaten
     */
    public static void drawStringShadowed(final String text, final float x, final float y) {
        glColor3f(SHADOW_R, SHADOW_G, SHADOW_B);
        drawString(text, x + SHADOW_OFFSET, y + SHADOW_OFFSET);
        glColor3f(FG_R, FG_G, FG_B);
        drawString(text, x, y);
    }
}
