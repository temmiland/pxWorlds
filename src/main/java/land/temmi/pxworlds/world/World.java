package land.temmi.pxworlds.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import land.temmi.rollercoaster.physics.AABB;
import land.temmi.pxworlds.entity.Entity;
import land.temmi.pxworlds.entity.Player;
import land.temmi.rollercoaster.platform.Window;
import land.temmi.rollercoaster.graphics.Camera;
import land.temmi.rollercoaster.graphics.Shader;
import land.temmi.rollercoaster.graphics.SpriteBatch;
import land.temmi.rollercoaster.graphics.TextureAtlas;
import land.temmi.pxworlds.tiles.Tile;
import land.temmi.pxworlds.tiles.TileRenderer;
import land.temmi.rollercoaster.world.TileChunk;
import land.temmi.rollercoaster.world.TileWorld;
import org.joml.Matrix4f;

/**
 * The pxWorlds game world — extends {@link TileWorld} with entity management,
 * tile-based collision queries and player spawn logic.
 *
 * <h3>Tile storage</h3>
 * Tiles are stored as byte IDs in the parent {@link TileWorld}. {@link #setTile}
 * and {@link #getTile} translate between the game-side {@link Tile} type and the
 * raw IDs that the engine manages.
 *
 * <h3>Collision</h3>
 * {@link #getCollidingBoxes} scans the tile grid on the fly and returns AABBs for
 * all solid tiles that intersect the query area.
 *
 * <h3>Rendering</h3>
 * {@link #render} delegates chunk rendering to the parent and then draws entities
 * using the same atlas and shader — no texture rebind between the two passes.
 */
public class World extends TileWorld {

    /** Size of a single tile in world coordinates. */
    public static final float TILE_SIZE = 2.0f;

    /** Number of tiles per chunk edge (chunks are {@code CHUNK_SIZE × CHUNK_SIZE} tiles). */
    public static final int CHUNK_SIZE = 16;

    /** Extra tile padding for the render/debug boundary. */
    static final int RENDER_OFFSET = 1;
    /** Tiles to render left/right/above/below outside the calculated viewport. */
    static final int HALF_DIVISOR = 2;

    /** AABB half extent for solid tiles (= TILE_SIZE / 2). */
    static final float BOUNDING_BOX_SIZE = 1.0f;
    /** Thickness of the invisible boundary walls at the world edges. */
    private static final float BOUNDARY_THICKNESS = 8.0f;

    /** Sentinel value for "no spawn set". */
    public static final int NO_SPAWN = -1;

    /** World scale (pixel multiplier). */
    private final int scale;

    /** @return world scale (pixel multiplier) */
    public int getScale() {
        return scale;
    }

    /** All entities in the world. */
    private List<Entity> entities;

    /** Scratch list for collision queries — avoids heap allocation in the hot path. */
    private final List<AABB> scratchColliders = new ArrayList<>();

    /** Reusable array for getDebugLines() — avoids per-frame allocation. */
    private static final int DEBUG_LINE_COUNT = 3;
    /** Pre-allocated debug line buffer. */
    private final String[] debugLines = new String[DEBUG_LINE_COUNT];

    /** Invisible boundary wall on the left world edge. */
    private AABB boundaryLeft;
    /** Invisible boundary wall on the right world edge. */
    private AABB boundaryRight;
    /** Invisible boundary wall on the top world edge. */
    private AABB boundaryTop;
    /** Invisible boundary wall on the bottom world edge. */
    private AABB boundaryBottom;

    /** Spawn point and last position of the player. */
    private final PlayerWorldPosition spawnState = new PlayerWorldPosition();

    /**
     * Creates an empty world with the given dimensions.
     * Used internally by {@link WorldReader} to build a loaded world.
     *
     * @param width   world width in tiles
     * @param height  world height in tiles
     * @param scale   world scale (pixel multiplier)
     */
    World(final int width, final int height, final int scale) {
        super(width, height, scale, CHUNK_SIZE, TILE_SIZE, Tile.getTiles());
        this.scale = scale;
        entities   = new ArrayList<>();
        initBoundaries();
    }

    // -------------------------------------------------------------------------
    // Initialization

    private void initBoundaries() {
        final float left   = -BOUNDING_BOX_SIZE;
        final float right  =  getWidth()  * TILE_SIZE - BOUNDING_BOX_SIZE;
        final float top    =  BOUNDING_BOX_SIZE;
        final float bottom = -getHeight() * TILE_SIZE + BOUNDING_BOX_SIZE;

        boundaryLeft   = new AABB(left - BOUNDARY_THICKNESS, bottom, left,   top);
        boundaryRight  = new AABB(right, bottom, right + BOUNDARY_THICKNESS, top);
        boundaryTop    = new AABB(left, top,     right, top    + BOUNDARY_THICKNESS);
        boundaryBottom = new AABB(left, bottom   - BOUNDARY_THICKNESS, right, bottom);
    }

    // -------------------------------------------------------------------------
    // Tile API (game-side: Tile ↔ byte id)

    /**
     * Sets a tile at the global position and marks the affected chunk as dirty.
     *
     * @param tile the new tile, or {@code null} for air
     * @param x    global tile X coordinate
     * @param y    global tile Y coordinate
     */
    public void setTile(final Tile tile, final int x, final int y) {
        final byte tileId;
        if (tile != null) {
            tileId = tile.getId();
        } else {
            tileId = TileChunk.EMPTY;
        }
        setTileId(x, y, tileId);
    }

    /**
     * Returns the tile at the global position, or {@code null} for air / out-of-bounds.
     *
     * @param x global tile X coordinate
     * @param y global tile Y coordinate
     * @return the tile, or {@code null}
     */
    public Tile getTile(final int x, final int y) {
        final byte id = getTileId(x, y);
        if (id == TileChunk.EMPTY) {
            return null;
        }
        final int idx = Byte.toUnsignedInt(id);
        final Tile[] tiles = Tile.getTiles();
        if (idx >= tiles.length) {
            return null;
        }
        return tiles[idx];
    }

    // -------------------------------------------------------------------------
    // Update

    /**
     * Recalculates the visible tile range based on the given camera's virtual size.
     *
     * @param camera the camera whose virtual dimensions to use
     */
    public void calculateView(final Camera camera) {
        super.calculateView(camera, scale);
    }

    /**
     * Ticks all entities, performs entity-entity push-out and resolves tile overlaps.
     *
     * @param delta  time since the last tick in seconds
     * @param window the window
     * @param camera the active camera
     */
    public void update(final float delta, final Window window, final Camera camera) {
        camera.savePrevPosition();
        for (final Entity entity : entities) {
            entity.tick(delta, window, camera, this);
        }
        for (int i = 0; i < entities.size(); i++) {
            for (int j = i + 1; j < entities.size(); j++) {
                entities.get(i).collideWithEntity(entities.get(j));
            }
        }
        for (final Entity entity : entities) {
            entity.resolveAgainstTiles(this);
        }
    }

    // -------------------------------------------------------------------------
    // Rendering

    /**
     * Renders all visible chunks and then all entities.
     *
     * @param tileRenderer provides atlas, batch shader, and entity batch
     * @param cam          active camera
     * @param alpha        interpolation factor 0.0–1.0
     */
    public void render(final TileRenderer tileRenderer, final Camera cam, final float alpha) {
        final TextureAtlas atlas       = tileRenderer.getAtlas();
        final Shader       batchShader = tileRenderer.getBatchShader();
        final SpriteBatch  entityBatch = tileRenderer.getEntityBatch();

        renderChunks(atlas, batchShader, cam, alpha, scale);

        final Matrix4f viewProj = cam.getProjection(alpha).mul(getWorldMatrix());
        entityBatch.begin();
        for (final Entity entity : entities) {
            entity.render(entityBatch, atlas, alpha);
        }
        entityBatch.end(batchShader, viewProj);
    }

    /**
     * Clamps the camera position so it stays within the world bounds.
     *
     * @param camera the camera to correct
     */
    public void correctCamera(final Camera camera) {
        super.correctCamera(camera, scale);
    }

    // -------------------------------------------------------------------------
    // Collision

    /**
     * Computes the AABB of a solid tile on the fly.
     *
     * @param x global tile X
     * @param y global tile Y
     * @return the computed AABB, or {@code null} if air or out of bounds
     */
    public AABB getTileBoundingBox(final int x, final int y) {
        final Tile tile = getTile(x, y);
        if (tile == null || !tile.isSolid()) {
            return null;
        }
        final float bx = x * TILE_SIZE;
        final float by = -y * TILE_SIZE;
        return new AABB(bx - BOUNDING_BOX_SIZE, by - BOUNDING_BOX_SIZE,
                        bx + BOUNDING_BOX_SIZE, by + BOUNDING_BOX_SIZE);
    }

    /**
     * Returns all AABBs of solid tiles that intersect {@code area}.
     *
     * <p>AABBs are computed on the fly. The returned list is a scratch object —
     * do not cache it across calls.</p>
     *
     * @param area search region
     * @return reference to the internal scratch list (never null, do not cache!)
     */
    public List<AABB> getCollidingBoxes(final AABB area) {
        scratchColliders.clear();

        if (area.minX < boundaryLeft.maxX) {
            scratchColliders.add(boundaryLeft);
        }
        if (area.maxX > boundaryRight.minX) {
            scratchColliders.add(boundaryRight);
        }
        if (area.maxY > boundaryTop.minY) {
            scratchColliders.add(boundaryTop);
        }
        if (area.minY < boundaryBottom.maxY) {
            scratchColliders.add(boundaryBottom);
        }

        final int x0 = (int) Math.floor( area.minX / TILE_SIZE) - 1;
        final int x1 = (int) Math.ceil(  area.maxX / TILE_SIZE) + 1;
        final int y0 = (int) Math.floor(-area.maxY / TILE_SIZE) - 1;
        final int y1 = (int) Math.ceil( -area.minY / TILE_SIZE) + 1;

        for (int y = Math.max(0, y0); y <= Math.min(getHeight() - 1, y1); y++) {
            for (int x = Math.max(0, x0); x <= Math.min(getWidth() - 1, x1); x++) {
                final Tile tile = getTile(x, y);
                if (tile == null || !tile.isSolid()) {
                    continue;
                }
                final float bx  = x * TILE_SIZE;
                final float by  = -y * TILE_SIZE;
                final float mnX = bx - BOUNDING_BOX_SIZE;
                final float mnY = by - BOUNDING_BOX_SIZE;
                final float mxX = bx + BOUNDING_BOX_SIZE;
                final float mxY = by + BOUNDING_BOX_SIZE;
                if (mxX > area.minX && mnX < area.maxX && mxY > area.minY && mnY < area.maxY) {
                    scratchColliders.add(new AABB(mnX, mnY, mxX, mxY));
                }
            }
        }
        return scratchColliders;
    }

    // -------------------------------------------------------------------------
    // Entity management

    /**
     * Adds an entity to the world.
     *
     * @param entity the entity to add
     */
    public void addEntity(final Entity entity) {
        entities.add(entity);
    }

    /** @return unmodifiable view of the entity list */
    List<Entity> getEntities() {
        return Collections.unmodifiableList(entities);
    }

    /** Stops render interpolation for all entities. */
    public void snapAllEntities() {
        for (final Entity entity : entities) {
            entity.snapPrevPosition();
        }
    }

    /** Pauses the animations of all entities. */
    public void pauseAllAnimations() {
        for (final Entity entity : entities) {
            entity.pauseAnimations();
        }
    }

    /** Resumes the animations of all entities. */
    public void resumeAllAnimations() {
        for (final Entity entity : entities) {
            entity.resumeAnimations();
        }
    }

    // -------------------------------------------------------------------------
    // Spawn

    /** @return spawn tile X, or {@value #NO_SPAWN} if no spawn is set */
    public int getSpawnTileX() {
        return spawnState.getSpawnTileX();
    }

    /** @return spawn tile Y, or {@value #NO_SPAWN} if no spawn is set */
    public int getSpawnTileY() {
        return spawnState.getSpawnTileY();
    }

    /**
     * Sets the spawn point in tile coordinates.
     *
     * @param tileX spawn tile X
     * @param tileY spawn tile Y
     */
    public void setSpawn(final int tileX, final int tileY) {
        spawnState.setSpawn(tileX, tileY);
    }

    /** @return last saved X position, or {@link Float#NaN} if not set */
    public float getLastSavedPosX() {
        return spawnState.getLastSavedPosX();
    }

    /** @return last saved Y position, or {@link Float#NaN} if not set */
    public float getLastSavedPosY() {
        return spawnState.getLastSavedPosY();
    }

    /** @return {@code true} if a last saved position is available */
    public boolean hasLastPos() {
        return spawnState.hasLastPos();
    }

    /**
     * Sets the last saved position in entity world-space coordinates.
     *
     * @param x X position in world space
     * @param y Y position in world space
     */
    public void setLastPos(final float x, final float y) {
        spawnState.setLastPos(x, y);
    }

    /**
     * Reads the current position of the first {@link Player} entity and stores it
     * as the last saved position.
     */
    public void capturePlayerPosition() {
        for (final Entity e : entities) {
            if (e instanceof Player) {
                spawnState.setLastPos(e.getPosX(), e.getPosY());
                return;
            }
        }
    }

    /**
     * Creates the player and positions the camera.
     *
     * @param camera   set to the start position
     * @param playable whether a playable player should be created
     */
    public void spawnPlayer(final Camera camera, final boolean playable) {
        final float posX;
        final float posY;
        if (spawnState.hasLastPos()) {
            posX = spawnState.getLastSavedPosX();
            posY = spawnState.getLastSavedPosY();
        } else if (spawnState.getSpawnTileX() != NO_SPAWN && spawnState.getSpawnTileY() != NO_SPAWN) {
            posX =  spawnState.getSpawnTileX() * TILE_SIZE;
            posY = -spawnState.getSpawnTileY() * TILE_SIZE;
        } else {
            return;
        }
        if (playable) {
            entities.add(new Player(posX, posY));
        }
        camera.getPosition().set(-posX * scale, -posY * scale, 0f);
    }

    // -------------------------------------------------------------------------
    // Debug

    /**
     * Returns debug lines about the current world state.
     *
     * @param cam active camera (fallback if no player entity found)
     * @return debug lines for the F3 panel
     */
    public String[] getDebugLines(final Camera cam) {
        Entity playerEntity = null;
        for (final Entity e : entities) {
            if (e instanceof Player) {
                playerEntity = e;
                break;
            }
        }

        final int tileX;
        final int tileY;
        if (playerEntity != null) {
            tileX = (int) Math.floor( playerEntity.getPosX() / TILE_SIZE);
            tileY = (int) Math.floor(-playerEntity.getPosY() / TILE_SIZE);
        } else {
            tileX =  -(int) cam.getPosition().x / (scale * HALF_DIVISOR);
            tileY =   (int) cam.getPosition().y / (scale * HALF_DIVISOR);
        }

        final int chunkX = Math.floorDiv(tileX, CHUNK_SIZE);
        final int chunkY = Math.floorDiv(tileY, CHUNK_SIZE);

        debugLines[0] = "Tile:    " + tileX  + ", " + tileY;
        debugLines[1] = "Chunk:   " + chunkX + ", " + chunkY
                + "  (" + getChunkCountX() + "x" + getChunkCountY() + " total)";
        debugLines[2] = "Chunks:  " + getLastRenderedChunks() + " rendered, "
                + getLastRebuiltChunks() + " rebuilt";
        return debugLines;
    }
}
