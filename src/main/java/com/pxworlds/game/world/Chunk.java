package com.pxworlds.game.world;

import com.pxworlds.Constants;
import com.pxworlds.game.rendering.TextureAtlas;
import com.pxworlds.game.rendering.tiles.Tile;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.util.Arrays;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

/**
 * Ein {@value Constants#CHUNK_SIZE}×{@value Constants#CHUNK_SIZE} Tile-Bereich
 * mit eigenem statischen OpenGL-VBO.
 *
 * <p>Der VBO wird nur dann neu gebaut, wenn {@code dirty == true}
 * (gesetzt bei jedem {@link #setTile} oder explizitem {@link #markDirty}).
 * Im Normalfall kostet ein Chunk-Render genau einen {@code glDrawArrays}-Call.</p>
 *
 * <p>Koordinaten-Konvention:
 * <ul>
 *   <li>{@code (localX, localY)} — Position innerhalb des Chunks (0..SIZE-1)</li>
 *   <li>{@code (chunkX, chunkY)} — Position des Chunks im Chunk-Grid</li>
 *   <li>Globale Tile-Position: {@code gx = chunkX*SIZE+localX}, {@code gy = chunkY*SIZE+localY}</li>
 *   <li>World-Space: {@code wx = gx * TILE_SIZE}, {@code wy = -gy * TILE_SIZE} (Y-Achse negiert)</li>
 * </ul>
 * </p>
 */
public class Chunk {

    /** Tiles pro Seite. */
    public static final int SIZE = Constants.CHUNK_SIZE;

    /** Floats pro Vertex: x, y, u, v. */
    private static final int FLOATS_PER_VERTEX   = 4;
    /** Vertices pro Tile-Quad (2 Dreiecke). */
    private static final int VERTICES_PER_QUAD   = 6;
    /** Maximale Floats im VBO: alle Tiles vollständig gefüllt. */
    private static final int MAX_FLOATS          = SIZE * SIZE * VERTICES_PER_QUAD * FLOATS_PER_VERTEX;

    /** Attribute location: Position (muss mit batch.vs übereinstimmen). */
    private static final int ATTRIB_POSITION = 0;
    /** Attribute location: UV. */
    private static final int ATTRIB_TEXCOORD = 1;
    /** Stride in Bytes. */
    private static final int STRIDE          = FLOATS_PER_VERTEX * Float.BYTES;
    /** UV-Offset in Bytes (nach x, y). */
    private static final long UV_OFFSET      = 2L * Float.BYTES;

    /**
     * Sentinel-Wert für "kein Tile gesetzt" in {@link #tiles}.
     * Tile-IDs beginnen bei 0 — daher eignet sich -1 als sicherer Sentinel
     * (als unsigned byte wäre das 255, ein üblicherweise ungenutzter Index).
     */
    private static final byte EMPTY = (byte) -1;

    // -------------------------------------------------------------------------

    /** Position im Chunk-Grid. */
    private final int chunkX;
    /** Position im Chunk-Grid. */
    private final int chunkY;

    /** Tile-IDs für diesen Chunk; EMPTY (-1) = kein Tile (Luft). */
    private final byte[] tiles;

    /** OpenGL VBO-ID. */
    private final int vboId;
    /** Anzahl der aktuell zu zeichnenden Vertices. */
    private int vertexCount;
    /** Muss der VBO neu gebaut werden? */
    private boolean dirty = true;

    // -------------------------------------------------------------------------

    public Chunk(final int chunkX, final int chunkY) {
        this.chunkX = chunkX;
        this.chunkY = chunkY;

        tiles = new byte[SIZE * SIZE];
        Arrays.fill(tiles, EMPTY);

        vboId = glGenBuffers();
        // Speicher auf der GPU vorab reservieren
        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferData(GL_ARRAY_BUFFER, (long) MAX_FLOATS * Float.BYTES, GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    // -------------------------------------------------------------------------
    // Tile-Zugriff

    /**
     * Setzt das Tile an der lokalen Position und markiert den Chunk als dirty.
     *
     * @param localX  X innerhalb des Chunks (0..SIZE-1)
     * @param localY  Y innerhalb des Chunks (0..SIZE-1)
     * @param tile    zu setzendes Tile oder {@code null} für Luft
     */
    public void setTile(final int localX, final int localY, final Tile tile) {
        tiles[localX + localY * SIZE] = (tile != null) ? tile.getId() : EMPTY;
        dirty = true;
    }

    /**
     * Gibt das Tile an der lokalen Position zurück oder {@code null} für Luft.
     */
    public Tile getTile(final int localX, final int localY) {
        final byte id = tiles[localX + localY * SIZE];
        if (id == EMPTY) {
            return null;
        }
        final int idx = Byte.toUnsignedInt(id);
        if (idx >= Tile.getTiles().length) {
            return null;
        }
        return Tile.getTiles()[idx];
    }

    // -------------------------------------------------------------------------
    // Rendering

    /**
     * Baut den VBO neu falls {@code dirty}, andernfalls No-Op.
     * Muss im GL-Thread aufgerufen werden, bevor {@link #render()} genutzt wird.
     *
     * @param atlas  der aktuelle {@link TextureAtlas} für UV-Koordinaten
     * @return {@code true} wenn der VBO tatsächlich neu gebaut wurde
     */
    public boolean rebuildIfDirty(final TextureAtlas atlas) {
        if (!dirty) {
            return false;
        }
        rebuild(atlas);
        dirty = false;
        return true;
    }

    /**
     * Rendert diesen Chunk mit einem einzigen {@code glDrawArrays}-Call.
     *
     * <p>Voraussetzung: Shader ist gebunden, "projection"-Uniform ist gesetzt,
     * Atlas ist an Sampler-Unit 0 gebunden.</p>
     */
    public void render() {
        if (vertexCount == 0) {
            return;
        }
        glBindBuffer(GL_ARRAY_BUFFER, vboId);

        glEnableVertexAttribArray(ATTRIB_POSITION);
        glEnableVertexAttribArray(ATTRIB_TEXCOORD);

        glVertexAttribPointer(ATTRIB_POSITION, 2, GL_FLOAT, false, STRIDE, 0L);
        glVertexAttribPointer(ATTRIB_TEXCOORD, 2, GL_FLOAT, false, STRIDE, UV_OFFSET);

        glDrawArrays(GL_TRIANGLES, 0, vertexCount);

        glDisableVertexAttribArray(ATTRIB_POSITION);
        glDisableVertexAttribArray(ATTRIB_TEXCOORD);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    /** Erzwingt einen VBO-Rebuild beim nächsten {@link #rebuildIfDirty}-Aufruf. */
    public void markDirty() {
        dirty = true;
    }

    /** Gibt die OpenGL-Ressourcen frei. Muss im GL-Thread aufgerufen werden. */
    public void close() {
        glDeleteBuffers(vboId);
    }

    // -------------------------------------------------------------------------

    private void rebuild(final TextureAtlas atlas) {
        final float[] buf  = new float[MAX_FLOATS];
        int idx = 0;

        // Halbe Tile-Größe = Ausdehnung des Quads vom Mittelpunkt aus
        final float half = Constants.TILE_SIZE * 0.5f; // = 1.0f bei TILE_SIZE=2

        for (int ly = 0; ly < SIZE; ly++) {
            for (int lx = 0; lx < SIZE; lx++) {
                final Tile tile = getTile(lx, ly);
                if (tile == null) {
                    continue; // Luft — keine Geometrie
                }
                final float[] uv = atlas.getUV(tile.getTexture());
                if (uv == null) {
                    continue; // Textur nicht im Atlas — Tile überspringen
                }

                // Globale Tile-Position
                final int gx = chunkX * SIZE + lx;
                final int gy = chunkY * SIZE + ly;

                // Tile-Mittelpunkt in Modell-Koordinaten (Y-Achse negiert)
                final float cx = gx * Constants.TILE_SIZE;
                final float cy = -gy * Constants.TILE_SIZE;

                final float x0 = cx - half;
                final float x1 = cx + half;
                final float y0 = cy - half;
                final float y1 = cy + half;

                final float u0 = uv[0]; // linke UV-Kante des Tiles im Atlas
                final float v0 = uv[1]; // obere  UV-Kante
                final float u1 = uv[2]; // rechte UV-Kante
                final float v1 = uv[3]; // untere UV-Kante

                // Dreieck 1: TL → TR → BR
                buf[idx++] = x0; buf[idx++] = y1; buf[idx++] = u0; buf[idx++] = v0;
                buf[idx++] = x1; buf[idx++] = y1; buf[idx++] = u1; buf[idx++] = v0;
                buf[idx++] = x1; buf[idx++] = y0; buf[idx++] = u1; buf[idx++] = v1;
                // Dreieck 2: TL → BR → BL
                buf[idx++] = x0; buf[idx++] = y1; buf[idx++] = u0; buf[idx++] = v0;
                buf[idx++] = x1; buf[idx++] = y0; buf[idx++] = u1; buf[idx++] = v1;
                buf[idx++] = x0; buf[idx++] = y0; buf[idx++] = u0; buf[idx++] = v1;
            }
        }

        vertexCount = idx / FLOATS_PER_VERTEX;

        if (idx == 0) {
            return; // Leerer Chunk — kein Upload nötig
        }

        final FloatBuffer fb = BufferUtils.createFloatBuffer(idx);
        fb.put(buf, 0, idx);
        fb.flip();

        glBindBuffer(GL_ARRAY_BUFFER, vboId);
        glBufferSubData(GL_ARRAY_BUFFER, 0L, fb);
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }
}
