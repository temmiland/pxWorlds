package land.temmi.pxworlds.world;

import land.temmi.rollercoaster.physics.AABB;
import land.temmi.pxworlds.entity.Entity;
import land.temmi.rollercoaster.graphics.Camera;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import land.temmi.pxworlds.tiles.Tile;

import java.nio.FloatBuffer;

import static land.temmi.pxworlds.world.World.BOUNDING_BOX_SIZE;
import static land.temmi.pxworlds.world.World.HALF_DIVISOR;
import static land.temmi.pxworlds.world.World.RENDER_OFFSET;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;

/**
 * Renders debug visualizations (collision boxes, chunk borders) over the world.
 *
 * <p>Uses legacy immediate-mode OpenGL (GL 1.x) and must therefore run on the
 * render thread. Only active when the F3 debug overlay is visible.</p>
 */
public final class WorldDebugRenderer {

    /** Number of floats in a 4×4 matrix. */
    private static final int   MATRIX_FLOAT_COUNT = 16;
    /** Shader program id for "no shader" (legacy GL). */
    private static final int   NO_SHADER          = 0;
    /** Fully saturated color channel (1.0). */
    private static final float COLOR_FULL         = 1.0f;
    /** Unsaturated color channel (0.0). */
    private static final float COLOR_NONE         = 0.0f;
    /** Line width for debug outlines. */
    private static final float DEBUG_LINE_WIDTH   = 2.0f;
    /** Default line width (OpenGL default). */
    private static final float DEFAULT_LINE_WIDTH  = 1.0f;

    /** The world to render debug info for. */
    private final World      world;
    /** Reusable scratch AABB for interpolated bounding box queries of entities. */
    private final AABB       scratchEntityBox = new AABB(0, 0, 0, 0);
    /** Reusable native buffer for uploading the projection matrix via legacy GL. */
    private final FloatBuffer matBuf          = BufferUtils.createFloatBuffer(MATRIX_FLOAT_COUNT);

    /**
     * Creates a debug renderer for the given world.
     *
     * @param world the world to visualize
     */
    public WorldDebugRenderer(final World world) {
        this.world = world;
    }

    /**
     * Draws tile AABBs (green), entity AABBs (red) and chunk borders (blue).
     *
     * @param camera camera from which the projection is derived
     * @param alpha  interpolation factor 0.0–1.0
     */
    public void renderDebugBoxes(final Camera camera, final float alpha) {
        final Matrix4f combined = camera.getProjection(alpha).mul(world.getWorldMatrix());
        matBuf.clear();
        combined.get(matBuf);

        glUseProgram(NO_SHADER);
        glDisable(GL_TEXTURE_2D);
        glLineWidth(DEBUG_LINE_WIDTH);

        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadMatrixf(matBuf);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        renderTileBoxes(camera);
        renderEntityBoxes(alpha);
        renderChunkBorders();

        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();

        glLineWidth(DEFAULT_LINE_WIDTH);
        glEnable(GL_TEXTURE_2D);
    }

    private void renderTileBoxes(final Camera camera) {
        final int scale = world.getScale();
        final int posX  = (int) camera.getPosition().x / (scale * HALF_DIVISOR);
        final int posY  = (int) camera.getPosition().y / (scale * HALF_DIVISOR);
        final int halfX = world.getViewX() / HALF_DIVISOR;
        final int halfY = world.getViewY() / HALF_DIVISOR;
        final int minTX = Math.max(0,                  -posX - halfX - RENDER_OFFSET);
        final int maxTX = Math.min(world.getWidth()  - 1, -posX + halfX + RENDER_OFFSET);
        final int minTY = Math.max(0,                   posY - halfY - RENDER_OFFSET);
        final int maxTY = Math.min(world.getHeight() - 1,  posY + halfY + RENDER_OFFSET);

        glColor3f(COLOR_NONE, COLOR_FULL, COLOR_NONE);
        for (int ty = minTY; ty <= maxTY; ty++) {
            for (int tx = minTX; tx <= maxTX; tx++) {
                final Tile tile = world.getTile(tx, ty);
                if (tile != null && tile.isSolid()) {
                    final float bx = tx * World.TILE_SIZE;
                    final float by = -ty * World.TILE_SIZE;
                    drawDebugBox(bx - BOUNDING_BOX_SIZE, by - BOUNDING_BOX_SIZE,
                                 bx + BOUNDING_BOX_SIZE, by + BOUNDING_BOX_SIZE);
                }
            }
        }
    }

    private void renderEntityBoxes(final float alpha) {
        glColor3f(COLOR_FULL, COLOR_NONE, COLOR_NONE);
        for (final Entity entity : world.getEntities()) {
            entity.getInterpolatedBoundingBox(alpha, scratchEntityBox);
            drawDebugBox(scratchEntityBox.minX, scratchEntityBox.minY,
                         scratchEntityBox.maxX, scratchEntityBox.maxY);
        }
    }

    private void renderChunkBorders() {
        final float half       = World.TILE_SIZE * 0.5f;
        final float worldLeft  = -half;
        final float worldRight =  world.getWidth()  * World.TILE_SIZE - half;
        final float worldTop   =  half;
        final float worldBot   = -world.getHeight() * World.TILE_SIZE + half;

        glColor3f(COLOR_NONE, COLOR_NONE, COLOR_FULL);

        for (int cx = 1; cx < world.getChunkCountX(); cx++) {
            final float x = cx * World.CHUNK_SIZE * World.TILE_SIZE - half;
            glBegin(GL_LINES);
            glVertex2f(x, worldTop);
            glVertex2f(x, worldBot);
            glEnd();
        }

        for (int cy = 1; cy < world.getChunkCountY(); cy++) {
            final float y = -cy * World.CHUNK_SIZE * World.TILE_SIZE + half;
            glBegin(GL_LINES);
            glVertex2f(worldLeft,  y);
            glVertex2f(worldRight, y);
            glEnd();
        }
    }

    private static void drawDebugBox(final float minX, final float minY,
                                     final float maxX, final float maxY) {
        glBegin(GL_LINE_LOOP);
        glVertex2f(minX, minY);
        glVertex2f(maxX, minY);
        glVertex2f(maxX, maxY);
        glVertex2f(minX, maxY);
        glEnd();
    }
}
