package temmiland.pxworlds.game.rendering;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

public class Model implements AutoCloseable {
	/** Anzahl der zu zeichnenden Elemente. */
	private int drawCount;

	/** OpenGL Vertex-Buffer-Objekt. */
	private int vertexObject;
	/** OpenGL Texturkoordinaten-Buffer-Objekt. */
	private int textureCoordObject;

	/** OpenGL Index-Buffer-Objekt. */
	private int indexObject;

	// OpenGL-Attribut-Konstanten
	/** Attribut-Index für Vertices. */
	private static final int VERTICES_ATTRIB_INDEX = 0;
	/** Attribut-Index für Texturkoordinaten. */
	private static final int TEXTURES_ATTRIB_INDEX = 1;
	/** Größe der Vertex-Daten (x, y, z). */
	private static final int VERTEX_SIZE = 3;
	/** Größe der Texturkoordinaten-Daten (u, v). */
	private static final int TEXTURE_COORD_SIZE = 2;
	/** Ob die Daten normalisiert werden sollen. */
	private static final boolean NORMALIZED = false;
	/** Stride zwischen zwei aufeinanderfolgenden Vertex-Einträgen. */
	private static final int STRIDE = 0;
	/** Offset zum ersten Vertex-Eintrag. */
	private static final int OFFSET = 0;

	public Model(float[] vertices, float[] textureCoords, int[] indices) {
		drawCount = indices.length;

		vertexObject = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vertexObject);
		glBufferData(GL_ARRAY_BUFFER, createBuffer(vertices), GL_STATIC_DRAW);

		textureCoordObject = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, textureCoordObject);
		glBufferData(GL_ARRAY_BUFFER, createBuffer(textureCoords), GL_STATIC_DRAW);

		indexObject = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexObject);

		final IntBuffer buffer = BufferUtils.createIntBuffer(indices.length);
		buffer.put(indices);
		buffer.flip();

		glBufferData(GL_ELEMENT_ARRAY_BUFFER, buffer, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	@Override
	public void close() {
		glDeleteBuffers(vertexObject);
		glDeleteBuffers(textureCoordObject);
		glDeleteBuffers(indexObject);
	}

	public void render() {
		glEnableVertexAttribArray(VERTICES_ATTRIB_INDEX);
		glEnableVertexAttribArray(TEXTURES_ATTRIB_INDEX);

		glBindBuffer(GL_ARRAY_BUFFER, vertexObject);
		glVertexAttribPointer(VERTICES_ATTRIB_INDEX, VERTEX_SIZE, GL_FLOAT, NORMALIZED, STRIDE, OFFSET);

		glBindBuffer(GL_ARRAY_BUFFER, textureCoordObject);
		glVertexAttribPointer(TEXTURES_ATTRIB_INDEX, TEXTURE_COORD_SIZE, GL_FLOAT, NORMALIZED, STRIDE, OFFSET);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexObject);
		glDrawElements(GL_TRIANGLES, drawCount, GL_UNSIGNED_INT, OFFSET);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		glDisableVertexAttribArray(VERTICES_ATTRIB_INDEX);
		glDisableVertexAttribArray(TEXTURES_ATTRIB_INDEX);

	}

	private FloatBuffer createBuffer(float[] data) {
		final FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}
}
