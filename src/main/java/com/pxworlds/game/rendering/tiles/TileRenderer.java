package com.pxworlds.game.rendering.tiles;

import com.pxworlds.game.rendering.Shader;
import com.pxworlds.game.rendering.TextureAtlas;

/**
 * Hält den {@link TextureAtlas} und den Batch-Shader für das Tile-Rendering.
 *
 * <p>Das eigentliche Rendering übernimmt das Chunk-System in {@code World.render()}.
 * {@code TileRenderer} dient als Initialisierungs- und Lifecycle-Wrapper, damit
 * {@code LevelState} seinen bisherigen Aufbau behalten kann.</p>
 */
public class TileRenderer implements AutoCloseable {

    /** Runtime-Atlas aller Tile-Texturen. */
    private final TextureAtlas atlas;

    /** Batch-Shader für Chunk-Rendering (batch.vs / batch.fs). */
    private final Shader batchShader;

    /**
     * Baut den Atlas aus allen registrierten {@link Tile}-Typen und
     * lädt den Batch-Shader. Muss im GL-Thread aufgerufen werden.
     */
    public TileRenderer() {
        this.atlas       = new TextureAtlas();
        this.batchShader = new Shader("batch");
    }

    public TextureAtlas getAtlas() {
        return atlas;
    }

    public Shader getBatchShader() {
        return batchShader;
    }

    @Override
    public void close() {
        atlas.close();
        batchShader.close();
    }
}
