package land.temmi.pxworlds.tiles;

import land.temmi.rollercoaster.graphics.Shader;
import land.temmi.rollercoaster.graphics.SpriteBatch;
import land.temmi.rollercoaster.graphics.TextureAtlas;

/**
 * Holds the {@link TextureAtlas}, the batch shader, and the entity
 * {@link SpriteBatch} for world rendering.
 *
 * <p>The tile atlas is shared between chunk rendering and entity rendering —
 * both use the same {@code batch} shader and the same GL texture, so no
 * texture rebind is needed between the two passes.</p>
 */
public class TileRenderer implements AutoCloseable {

    /** Runtime atlas of all tile textures (world tiles + entity frames). */
    private final TextureAtlas atlas;

    /** Batch shader for chunk and entity rendering (batch.vs / batch.fs). */
    private final Shader batchShader;

    /** Dynamic sprite batch for entity rendering. */
    private final SpriteBatch entityBatch;

    /**
     * Builds the atlas from all registered {@link Tile} types, loads the
     * batch shader, and allocates the entity sprite batch.
     * Must be called on the GL thread.
     */
    public TileRenderer() {
        this.atlas       = new TextureAtlas(Tile.getTiles(), Tile.TILE_PX);
        this.batchShader = new Shader("batch");
        this.entityBatch = new SpriteBatch();
    }

    /** @return the runtime texture atlas */
    public TextureAtlas getAtlas() {
        return atlas;
    }

    /** @return the batch shader */
    public Shader getBatchShader() {
        return batchShader;
    }

    /** @return the entity sprite batch */
    public SpriteBatch getEntityBatch() {
        return entityBatch;
    }

    /** Releases the atlas, batch shader, and entity batch. */
    @Override
    public void close() {
        atlas.close();
        batchShader.close();
        entityBatch.close();
    }
}
