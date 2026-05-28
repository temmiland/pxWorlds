package com.pxworlds.game.rendering;

import com.pxworlds.game.rendering.tiles.Tile;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

/**
 * Runtime Texture Atlas — lädt alle registrierten Tile-Texturen als Einzel-PNGs
 * und packt sie beim Spielstart zu einer einzigen OpenGL-Textur zusammen.
 *
 * <p>Layout: horizontaler Strip, jedes Tile belegt {@value #TILE_PX}×{@value #TILE_PX}
 * Pixel. Einzel-PNGs werden per nearest-neighbour auf diese Größe skaliert,
 * vollständig ohne Graphics2D (direkte Pixel-Kopie → keine JVM-seitigen
 * Farbkonvertierungs-Artefakte).</p>
 *
 * <p>Kein UV-Inset: mit {@code GL_NEAREST} snappt der Sampler immer exakt
 * zum nächsten Texel-Zentrum — Bleeding zwischen Tiles ist ausgeschlossen.</p>
 */
public class TextureAtlas implements AutoCloseable {

    /** Tile-Größe in Pixeln im Atlas. Muss mit der tatsächlichen Sprite-Auflösung übereinstimmen. */
    private static final int TILE_PX = 32;
    /** Bytes pro Pixel (RGBA). */
    private static final int BYTES_PER_PIXEL = 4;

    // Bit-Shifts für java.awt.image ARGB-Format (Rückgabe von getRGB)
    private static final int RED_SHIFT   = 16;
    private static final int GREEN_SHIFT = 8;
    private static final int ALPHA_SHIFT = 24;
    private static final int COLOR_MASK  = 0xFF;

    /** OpenGL-Textur-ID. 0 = nicht initialisiert. */
    private int textureId;

    /** UV-Map: Texturname → float[]{u0, v0, u1, v1} (mit Texel-Inset). */
    private final Map<String, float[]> uvMap = new LinkedHashMap<>();

    /**
     * Baut den Atlas. Muss im GL-Thread aufgerufen werden.
     */
    public TextureAtlas() {
        // 1. Alle eindeutigen Tile-Texturen einsammeln
        final LinkedHashMap<String, int[]> images = new LinkedHashMap<>(); // name → ARGB pixels (TILE_PX × TILE_PX)
        for (final Tile tile : Tile.tiles) {
            if (tile == null) {
                continue;
            }
            final String name = tile.getTexture();
            if (images.containsKey(name)) {
                continue;
            }
            final int[] pixels = loadAndScale(name);
            if (pixels != null) {
                images.put(name, pixels);
            }
        }

        if (images.isEmpty()) {
            return;
        }

        // 2. Atlas-Pixel direkt zusammenbauen — kein Graphics2D
        final int count  = images.size();
        final int atlasW = count * TILE_PX;
        final int atlasH = TILE_PX;

        final int[] atlasPixels = new int[atlasW * atlasH];

        // Kein UV-Inset nötig: GL_NEAREST snappt immer zum nächsten Texel-Zentrum,
        // Bleeding zwischen Atlas-Tiles ist physikalisch ausgeschlossen.
        // Insets wären nur bei GL_LINEAR (Bilinear-Filterung) notwendig.
        int slot = 0;
        for (final Map.Entry<String, int[]> entry : images.entrySet()) {
            final int[] src    = entry.getValue(); // TILE_PX × TILE_PX ARGB
            final int   startX = slot * TILE_PX;

            for (int row = 0; row < TILE_PX; row++) {
                for (int col = 0; col < TILE_PX; col++) {
                    atlasPixels[(startX + col) + row * atlasW] = src[col + row * TILE_PX];
                }
            }

            final float u0 = (float) slot       / count;
            final float u1 = (float) (slot + 1) / count;
            uvMap.put(entry.getKey(), new float[]{u0, 0f, u1, 1f});
            slot++;
        }

        // 3. ARGB → RGBA ByteBuffer (wie Texture.java, zeilenweise)
        final ByteBuffer buf = BufferUtils.createByteBuffer(atlasW * atlasH * BYTES_PER_PIXEL);
        for (int row = 0; row < atlasH; row++) {
            for (int col = 0; col < atlasW; col++) {
                final int px = atlasPixels[col + row * atlasW];
                buf.put((byte) ((px >> RED_SHIFT)   & COLOR_MASK));
                buf.put((byte) ((px >> GREEN_SHIFT)  & COLOR_MASK));
                buf.put((byte) (px                   & COLOR_MASK)); // B (shift 0)
                buf.put((byte) ((px >> ALPHA_SHIFT)  & COLOR_MASK));
            }
        }
        buf.flip();

        // 4. OpenGL-Textur hochladen
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, atlasW, atlasH,
                     0, GL_RGBA, GL_UNSIGNED_BYTE, buf);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    /** Bindet den Atlas an den angegebenen Sampler-Slot. */
    public void bind(final int sampler) {
        glActiveTexture(GL_TEXTURE0 + sampler);
        glBindTexture(GL_TEXTURE_2D, textureId);
    }

    /**
     * @return float[]{u0, v0, u1, v1} oder {@code null} wenn Textur nicht im Atlas
     */
    public float[] getUV(final String textureName) {
        return uvMap.get(textureName);
    }

    @Override
    public void close() {
        if (textureId != 0) {
            glDeleteTextures(textureId);
            textureId = 0;
        }
    }

    // -------------------------------------------------------------------------

    /**
     * Lädt eine PNG und skaliert sie per nearest-neighbour auf TILE_PX×TILE_PX.
     * Gibt ein ARGB-int[TILE_PX*TILE_PX]-Array zurück, oder null bei Fehler.
     */
    private int[] loadAndScale(final String name) {
        final BufferedImage src;
        try (InputStream is = getClass().getResourceAsStream("/textures/" + name + ".png")) {
            if (is == null) {
                return null;
            }
            src = ImageIO.read(is);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        if (src == null) {
            return null;
        }

        final int srcW = src.getWidth();
        final int srcH = src.getHeight();

        // getRGB gibt immer ARGB zurück, unabhängig vom internen BufferedImage-Typ
        final int[] srcPixels = src.getRGB(0, 0, srcW, srcH, null, 0, srcW);
        final int[] out       = new int[TILE_PX * TILE_PX];

        // Nearest-neighbour Skalierung
        for (int row = 0; row < TILE_PX; row++) {
            for (int col = 0; col < TILE_PX; col++) {
                final int srcCol = col * srcW / TILE_PX;
                final int srcRow = row * srcH / TILE_PX;
                out[col + row * TILE_PX] = srcPixels[srcCol + srcRow * srcW];
            }
        }
        return out;
    }
}
