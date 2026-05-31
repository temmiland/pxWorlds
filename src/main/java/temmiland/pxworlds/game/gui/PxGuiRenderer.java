package temmiland.pxworlds.game.gui;

import temmiland.pxworlds.game.assets.Assets;
import temmiland.pxworlds.game.debug.DebugRenderer;
import temmiland.pxworlds.game.rendering.Camera;
import temmiland.pxworlds.game.rendering.Shader;
import temmiland.pxworlds.game.rendering.tiles.TileSheet;
import temmiland.rollercoaster.gui.GuiRenderer;
import temmiland.rollercoaster.platform.Window;
import org.joml.Matrix4f;
import org.lwjgl.stb.STBEasyFont;

import static org.lwjgl.opengl.GL11.GL_QUADS;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_MODELVIEW;
import static org.lwjgl.opengl.GL11.GL_PROJECTION;
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
 * pxWorlds-Implementierung von {@link GuiRenderer} auf Basis der vorhandenen
 * Render-Infrastruktur (Shader, {@link Camera}, {@link TileSheet}, Quad-Modell,
 * {@link DebugRenderer}/STBEasyFont).
 *
 * <p>Diese Klasse hält nur die <b>Render-Primitive</b> ({@link #drawTile}, {@link #drawText},
 * {@link #drawQuad}) samt der zugehörigen OpenGL- und Koordinaten-Zentrier-Logik. Das
 * konkrete Erscheinungsbild zusammengesetzter Widgets liegt in den Skin-Implementierungen
 * (siehe {@code skins}-Paket), die das Framework über die Engine-Verträge ansteuert.
 * Das Engine-Framework liefert nur die Mechanik.</p>
 */
public final class PxGuiRenderer implements GuiRenderer {

    /** Schriftzeilenhöhe in STBEasyFont-Einheiten (vor Skalierung). */
    private static final float FONT_HEIGHT = 7.0f;
    /** Halbierungsfaktor für Zentrierung. */
    private static final float HALF = 2.0f;

    /** GUI-Shader. */
    private final Shader shader;
    /** Zentrierte GUI-Kamera. */
    private final Camera camera;
    /** GUI-Tilesheet. */
    private final TileSheet sheet;
    /** Wiederverwendete Transformationsmatrix für Tile-Rendering. */
    private final Matrix4f transform = new Matrix4f();

    /**
     * @param window       das Fenster (für die initiale GUI-Projektionsgröße)
     * @param tileSheet    Name der Tilesheet-Textur (ohne Endung; ".png" wird ergänzt)
     * @param amountOfTiles Anzahl der Kacheln pro Achse im Tilesheet
     */
    public PxGuiRenderer(final Window window, final String tileSheet, final int amountOfTiles) {
        this.shader = new Shader("gui");
        this.camera = new Camera(window.getWidth(), window.getHeight());
        this.sheet = new TileSheet(tileSheet + ".png", amountOfTiles);
    }

    /** @return virtuelle GUI-Breite (für den Input-Adapter) */
    public int virtualWidth() {
        return camera.getVirtualWidth();
    }

    /** @return virtuelle GUI-Höhe (für den Input-Adapter) */
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
    public void drawTile(final int col, final int row, final float cx, final float cy,
                         final float halfWidth, final float halfHeight) {
        shader.bind();
        transform.identity().translate(cx, cy, 0).scale(halfWidth, halfHeight, 1);
        shader.setUniform("projection", camera.getProjection().mul(transform));
        sheet.bindTile(shader, col, row);
        Assets.getModel().render();
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

        // Trailing Space verhindert, dass STBEasyFont den letzten schmalen Glyph abschneidet.
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
