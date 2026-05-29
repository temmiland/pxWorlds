package com.pxworlds.game.world;

import com.pxworlds.Constants;
import com.pxworlds.game.collision.AABB;
import com.pxworlds.game.entity.Entity;
import com.pxworlds.game.rendering.Camera;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

import static com.pxworlds.game.world.World.BOUNDING_BOX_SIZE;
import static com.pxworlds.game.world.World.HALF_DIVISOR;
import static com.pxworlds.game.world.World.RENDER_OFFSET;
import static com.pxworlds.game.world.World.VIEW_OFFSET;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;

/**
 * Renders debug visualisations (collision boxes, chunk borders) on top of the world.
 *
 * <p>Uses legacy immediate-mode OpenGL (GL 1.x) so it must run on the render thread.
 * Only active when the F3 debug overlay is visible.</p>
 */
public final class WorldDebugRenderer {

    private static final int   MATRIX_FLOAT_COUNT = 16;
    private static final int   NO_SHADER          = 0;
    private static final float COLOR_FULL         = 1.0f;
    private static final float COLOR_NONE         = 0.0f;
    private static final float DEBUG_LINE_WIDTH   = 2.0f;
    private static final float DEFAULT_LINE_WIDTH  = 1.0f;

    private final World      world;
    /** Reusable scratch AABB for entity interpolated bounding-box queries. */
    private final AABB       scratchEntityBox = new AABB(0, 0, 0, 0);
    /** Reusable native buffer for uploading the projection matrix via legacy GL. */
    private final FloatBuffer matBuf          = BufferUtils.createFloatBuffer(MATRIX_FLOAT_COUNT);

    public WorldDebugRenderer(final World world) {
        this.world = world;
    }

    /**
     * Draws tile AABBs (green), entity AABBs (red) and chunk borders (blue).
     *
     * @param camera camera to derive the projection from
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
                final com.pxworlds.game.rendering.tiles.Tile tile = world.getTile(tx, ty);
                if (tile != null && tile.isSolid()) {
                    final float bx = tx * Constants.TILE_SIZE;
                    final float by = -ty * Constants.TILE_SIZE;
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
        final float half       = Constants.TILE_SIZE * 0.5f;
        final float worldLeft  = -half;
        final float worldRight =  world.getWidth()  * Constants.TILE_SIZE - half;
        final float worldTop   =  half;
        final float worldBot   = -world.getHeight() * Constants.TILE_SIZE + half;

        glColor3f(COLOR_NONE, COLOR_NONE, COLOR_FULL);

        for (int cx = 1; cx < world.getChunkCountX(); cx++) {
            final float x = cx * Chunk.SIZE * Constants.TILE_SIZE - half;
            glBegin(GL_LINES);
            glVertex2f(x, worldTop);
            glVertex2f(x, worldBot);
            glEnd();
        }

        for (int cy = 1; cy < world.getChunkCountY(); cy++) {
            final float y = -cy * Chunk.SIZE * Constants.TILE_SIZE + half;
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
