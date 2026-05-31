package temmiland.pxworlds.game.rendering;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * Dynamischer Sprite-Batcher.
 *
 * <p>Sammelt Sprite-Geometrie in einem CPU-seitigen {@code float[]}-Buffer und
 * schickt alles in <strong>einem einzigen</strong> {@code glDrawArrays}-Call auf
 * die GPU. Ideal für Entities, Partikel und andere dynamische Sprites die jeden
 * Frame neu gebaut werden müssen.</p>
 *
 * <p>Vertex-Format (interleaved): {@code x, y, u, v} — 4 Floats pro Vertex.<br>
 * Pro Sprite: 6 Vertices (2 Dreiecke, kein Index-Buffer).</p>
 *
 * <p>Nutzt den {@code batch}-Shader ({@code batch.vs} / {@code batch.fs}) und
 * den {@link TextureAtlas} für texturwechselfreies Rendering.</p>
 *
 * <pre>
 * batch.begin();
 * batch.draw(cx, cy, hw, hh, u0, v0, u1, v1);
 * ...
 * batch.end(shader, projectionMatrix);
 * </pre>
 */
public class SpriteBatch implements AutoCloseable {

    /** Floats pro Vertex: x, y, u, v. */
    private static final int FLOATS_PER_VERTEX  = 4;
    /** Vertices pro Sprite (2 Dreiecke). */
    private static final int VERTICES_PER_SPRITE = 6;
    /** Floats gesamt pro Sprite. */
    private static final int FLOATS_PER_SPRITE   = FLOATS_PER_VERTEX * VERTICES_PER_SPRITE;
    /** Maximale Anzahl Sprites pro Batch-Durchlauf. */
    private static final int MAX_SPRITES         = 8192;

    /** Attribute location: Position (x, y). Muss mit batch.vs übereinstimmen. */
    private static final int ATTRIB_POSITION = 0;
    /** Attribute location: Texturkoordinate (u, v). */
    private static final int ATTRIB_TEXCOORD = 1;
    /** Stride in Bytes (4 Floats × 4 Bytes). */
    private static final int STRIDE          = FLOATS_PER_VERTEX * Float.BYTES;
    /** Offset des UV-Attributs in Bytes (nach x, y). */
    private static final long UV_OFFSET      = 2L * Float.BYTES;

    /** CPU-seitiger Geometrie-Buffer. */
    private final float[]      buffer       = new float[MAX_SPRITES * FLOATS_PER_SPRITE];
    /** Wiederverwendbarer FloatBuffer für den GPU-Upload (vermeidet Allokation pro Frame). */
    private final FloatBuffer  uploadBuffer = BufferUtils.createFloatBuffer(MAX_SPRITES * FLOATS_PER_SPRITE);

    /** Anzahl der akkumulierten Sprites seit dem letzten begin(). */
    private int spriteCount;
    /** OpenGL VBO-ID. */
    private final int vboId;

    public SpriteBatch() {
        vboId = glGenBuffers();
        // GPU-Buffer vorab allokieren (GL_DYNAMIC_DRAW signalisiert häufige Uploads)
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER,
                     (long) MAX_SPRITES * FLOATS_PER_SPRITE * Float.BYTES,
                     GL_DYNAMIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    /**
     * Beginnt einen neuen Batch. Muss vor dem ersten {@link #draw} aufgerufen werden.
     */
    public void begin() {
        spriteCount = 0;
    }

    /**
     * Fügt einen Sprite dem Buffer hinzu.
     *
     * @param cx  Mittelpunkt X in Modell-Koordinaten
     * @param cy  Mittelpunkt Y in Modell-Koordinaten
     * @param hw  halbe Breite
     * @param hh  halbe Höhe
     * @param u0  linke UV-Kante
     * @param v0  obere UV-Kante
     * @param u1  rechte UV-Kante
     * @param v1  untere UV-Kante
     */
    public void draw(final float cx, final float cy,
                     final float hw, final float hh,
                     final float u0, final float v0,
                     final float u1, final float v1) {
        if (spriteCount >= MAX_SPRITES) {
            return; // Buffer voll — ggf. künftig auto-flush
        }
        int i = spriteCount * FLOATS_PER_SPRITE;

        final float x0 = cx - hw;
        final float x1 = cx + hw;
        final float y0 = cy - hh;
        final float y1 = cy + hh;

        // Dreieck 1: TL → TR → BR
        buffer[i++] = x0; buffer[i++] = y1; buffer[i++] = u0; buffer[i++] = v0;
        buffer[i++] = x1; buffer[i++] = y1; buffer[i++] = u1; buffer[i++] = v0;
        buffer[i++] = x1; buffer[i++] = y0; buffer[i++] = u1; buffer[i++] = v1;
        // Dreieck 2: TL → BR → BL
        buffer[i++] = x0; buffer[i++] = y1; buffer[i++] = u0; buffer[i++] = v0;
        buffer[i++] = x1; buffer[i++] = y0; buffer[i++] = u1; buffer[i++] = v1;
        buffer[i]   = x0; buffer[i + 1] = y0; buffer[i + 2] = u0; buffer[i + FLOATS_PER_VERTEX - 1] = v1;

        spriteCount++;
    }

    /**
     * Schließt den Batch ab: lädt den Buffer auf die GPU und zeichnet alles
     * in einem einzigen {@code glDrawArrays}-Call.
     *
     * @param shader      der aktive Batch-Shader (muss "projection" und "sampler" kennen)
     * @param projection  kombinierte View-Projection-Matrix
     */
    public void end(final Shader shader, final Matrix4f projection) {
        if (spriteCount == 0) {
            return;
        }

        shader.bind();
        shader.setUniform("sampler", 0);
        shader.setUniform("projection", projection);

        // Daten in GPU-Buffer schreiben (nur den genutzten Bereich)
        uploadBuffer.clear();
        uploadBuffer.put(buffer, 0, spriteCount * FLOATS_PER_SPRITE);
        uploadBuffer.flip();

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferSubData(GL_ARRAY_BUFFER, 0L, uploadBuffer);

        glEnableVertexAttribArray(ATTRIB_POSITION);
        glEnableVertexAttribArray(ATTRIB_TEXCOORD);

        glVertexAttribPointer(ATTRIB_POSITION, 2, GL_FLOAT, false, STRIDE, 0L);
        glVertexAttribPointer(ATTRIB_TEXCOORD, 2, GL_FLOAT, false, STRIDE, UV_OFFSET);

        glDrawArrays(GL_TRIANGLES, 0, spriteCount * VERTICES_PER_SPRITE);

        glDisableVertexAttribArray(ATTRIB_POSITION);
        glDisableVertexAttribArray(ATTRIB_TEXCOORD);
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        spriteCount = 0;
    }

    @Override
    public void close() {
        glDeleteBuffers(vboId);
    }
}
