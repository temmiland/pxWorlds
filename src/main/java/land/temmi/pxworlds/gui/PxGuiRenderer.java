package land.temmi.pxworlds.gui;

import land.temmi.pxworlds.debug.DebugRenderer;
import land.temmi.pxworlds.tiles.Tile;
import land.temmi.rollercoaster.graphics.Camera;
import land.temmi.rollercoaster.graphics.Shader;
import land.temmi.rollercoaster.graphics.SpriteBatch;
import land.temmi.rollercoaster.graphics.TextureAtlas;
import land.temmi.rollercoaster.gui.GuiRenderer;
import land.temmi.rollercoaster.platform.Window;
import org.lwjgl.stb.STBEasyFont;

import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor3f;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLoadIdentity;
import static org.lwjgl.opengl.GL11.glMatrixMode;
import static org.lwjgl.opengl.GL11.glOrtho;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glScalef;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL20.glUseProgram;

/**
 * pxWorlds implementation of {@link GuiRenderer} built on {@link TextureAtlas}
 * and {@link SpriteBatch} — the same pattern used for world tiles and entities.
 *
 * <p>The GUI atlas is built from a spritesheet ({@code /textures/sheets/<name>.png})
 * subdivided into a {@code gridSize×gridSize} cell grid. Each cell is addressed
 * by {@code (col, row)} and stored in the atlas under the key {@code "col_row"}.</p>
 */
public final class PxGuiRenderer implements GuiRenderer {

    /** Font line height in STBEasyFont units (before scaling). */
    private static final float FONT_HEIGHT = 7.0f;
    /** Halving factor for centering. */
    private static final float HALF = 2.0f;
    /** On-screen edge length of a single background tile, in GUI units. */
    private static final float BACKGROUND_TILE = 64f;

    /** GUI shader (batch.vs / batch.fs — UV-based, no texModifier). */
    private final Shader shader;
    /** Centered GUI camera. */
    private final Camera camera;
    /** GUI texture atlas (cells from the spritesheet). */
    private final TextureAtlas atlas;
    /** Dynamic sprite batch for GUI tiles. */
    private final SpriteBatch batch;

    /**
     * @param window the window (for the initial GUI projection size)
     */
    public PxGuiRenderer(final Window window) {
        this.shader = new Shader("gui");
        this.camera = new Camera(window.getWidth(), window.getHeight());
        this.atlas  = new TextureAtlas(Tile.getTiles(), Tile.TILE_PX);
        this.batch  = new SpriteBatch();
    }

    /** @return virtual GUI width (for the input adapter) */
    public int virtualWidth() {
        return camera.getVirtualWidth();
    }

    /** @return virtual GUI height (for the input adapter) */
    public int virtualHeight() {
        return camera.getVirtualHeight();
    }

    @Override
    public void begin() {
        shader.bind();
    }

    @Override
    public void resize(final int width, final int height) {
        camera.setProjection(width, height);
    }

    @Override
    public void drawBackground(final String textureName) {
        final float[] uv = atlas.getUV(textureName);
        if (uv == null) {
            return;
        }

        final int vw = camera.getVirtualWidth();
        final int vh = camera.getVirtualHeight();
        final float half = BACKGROUND_TILE / HALF;

        // One extra tile per axis guarantees full coverage at the edges.
        final int cols = (int) Math.ceil(vw / BACKGROUND_TILE) + 1;
        final int rows = (int) Math.ceil(vh / BACKGROUND_TILE) + 1;
        final float startX = -(cols * BACKGROUND_TILE) / HALF + half;
        final float startY = -(rows * BACKGROUND_TILE) / HALF + half;

        batch.begin();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                final float cx = startX + col * BACKGROUND_TILE;
                final float cy = startY + row * BACKGROUND_TILE;
                batch.draw(cx, cy, half, half, uv[0], uv[1], uv[2], uv[3]);
            }
        }
        atlas.bind(0);
        batch.end(shader, camera.getProjection());
    }

    @Override
    public void drawTile(final String textureName, final float cx, final float cy,
                         final float halfWidth, final float halfHeight) {
        final float[] uv = atlas.getUV(textureName);
        if (uv == null) {
            return;
        }
        batch.begin();
        batch.draw(cx, cy, halfWidth, halfHeight, uv[0], uv[1], uv[2], uv[3]);
        atlas.bind(0);
        batch.end(shader, camera.getProjection());
    }

    @Override
    public void drawText(final String text, final float cx, final float cy, final float scale,
                         final float r, final float g, final float b, final boolean shadow) {
        final int vw = camera.getVirtualWidth();
        final int vh = camera.getVirtualHeight();
        final float fontH = FONT_HEIGHT * scale;
        final float textW = STBEasyFont.stb_easy_font_width(text) * scale;
        final float drawX = (cx - textW / HALF + vw / HALF) / scale;
        final float drawY = (-cy - fontH / HALF + vh / HALF) / scale;

        glUseProgram(0);
        glDisable(GL_TEXTURE_2D);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, vw, vh, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();
        glScalef(scale, scale, 1f);

        if (shadow) {
            DebugRenderer.drawStringShadowed(text + " ", drawX, drawY);
        } else {
            glColor3f(r, g, b);
            DebugRenderer.drawString(text + " ", drawX, drawY);
        }

        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glEnable(GL_TEXTURE_2D);
    }

    @Override
    public void drawQuad(final float cx, final float cy, final float halfWidth, final float halfHeight,
                         final float r, final float g, final float b) {
        final int vw = camera.getVirtualWidth();
        final int vh = camera.getVirtualHeight();
        final float screenX = cx - halfWidth + vw / HALF;
        final float screenY = -cy - halfHeight + vh / HALF;
        final float screenW = halfWidth * HALF;
        final float screenH = halfHeight * HALF;

        glUseProgram(0);
        glDisable(GL_TEXTURE_2D);
        glMatrixMode(GL_PROJECTION);
        glPushMatrix();
        glLoadIdentity();
        glOrtho(0, vw, vh, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        glPushMatrix();
        glLoadIdentity();

        glColor3f(r, g, b);
        glBegin(GL_QUADS);
        glVertex2f(screenX, screenY);
        glVertex2f(screenX + screenW, screenY);
        glVertex2f(screenX + screenW, screenY + screenH);
        glVertex2f(screenX, screenY + screenH);
        glEnd();

        glMatrixMode(GL_PROJECTION);
        glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
        glPopMatrix();
        glEnable(GL_TEXTURE_2D);
    }
}
