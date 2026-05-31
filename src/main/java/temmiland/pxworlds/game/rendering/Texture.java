package temmiland.pxworlds.game.rendering;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;

public class Texture implements AutoCloseable {
	/** Die OpenGL-Textur-Objekt-ID. */
	private int textureObject;
	/** Die Breite der Textur. */
	private int width;
	/** Die Höhe der Textur. */
	private int height;

	// Textur-Ladekonstanten
	/** Anzahl Bytes pro Pixel. */
	private static final int BYTES_PER_PIXEL = 4;
	/** Bit-Shift für die Rot-Farbkomponente. */
	private static final int RED_SHIFT = 16;
	/** Bit-Shift für die Grün-Farbkomponente. */
	private static final int GREEN_SHIFT = 8;
	/** Bit-Shift für die Blau-Farbkomponente. */
	private static final int BLUE_SHIFT = 0;
	/** Bit-Shift für die Alpha-Farbkomponente. */
	private static final int ALPHA_SHIFT = 24;
	/** Farbmaske zum Extrahieren von Farbkomponenten. */
	private static final int COLOR_MASK = 0xFF;
	/** Textur-Level für OpenGL. */
	private static final int TEXTURE_LEVEL = 0;
	/** Maximale Anzahl an Sampler-Units. */
	private static final int MAX_SAMPLER_UNITS = 31;

	public Texture(String filename) {
		final BufferedImage bufferedImage;
		try {
			bufferedImage = ImageIO.read(getClass().getResourceAsStream("/textures/" + filename));
			width = bufferedImage.getWidth();
			height = bufferedImage.getHeight();

			final int[] pixelsRaw = bufferedImage.getRGB(0, 0, width, height, null, 0, width);
			final ByteBuffer pixels = BufferUtils.createByteBuffer(width * height * BYTES_PER_PIXEL);
			for (int i = 0; i < width; i++) {
				for (int j = 0; j < height; j++) {
					final int pixel = pixelsRaw[i * width + j];
					pixels.put((byte) ((pixel >> RED_SHIFT) & COLOR_MASK)); // RED
					pixels.put((byte) ((pixel >> GREEN_SHIFT) & COLOR_MASK));  // GREEN
					pixels.put((byte) ((pixel >> BLUE_SHIFT) & COLOR_MASK));		  // BLUE
					pixels.put((byte) ((pixel >> ALPHA_SHIFT) & COLOR_MASK)); // ALPHA
				}
			}

			pixels.flip();

			textureObject = glGenTextures();

			glBindTexture(GL_TEXTURE_2D, textureObject);

			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

			glTexImage2D(GL_TEXTURE_2D, TEXTURE_LEVEL, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixels);

		} catch (IOException e) {
			throw new IllegalStateException("Textur konnte nicht geladen werden: /textures/" + filename, e);
		}
	}

	@Override
	public void close() {
		glDeleteTextures(textureObject);
	}

	public void bind(int sampler) {
		if (sampler >= 0 && sampler <= MAX_SAMPLER_UNITS) {
			glActiveTexture(GL_TEXTURE0 + sampler);
			glBindTexture(GL_TEXTURE_2D, textureObject);
		}
	}
}
