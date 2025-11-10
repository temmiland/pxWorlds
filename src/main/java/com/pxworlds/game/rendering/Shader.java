package com.pxworlds.game.rendering;

import static org.lwjgl.opengl.GL20.*;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Shader implements AutoCloseable {
	/** The logger for this class. */
	private static final Logger LOGGER = LoggerFactory.getLogger(Shader.class);
	/** The OpenGL program object ID. */
	private int programObject;
	/** The OpenGL vertex shader object ID. */
	private int vertexShaderObject;
	/** The OpenGL fragment shader object ID. */
	private int fragmentShaderObject;

	// Shader constants
	/** The OpenGL success code. */
	private static final int GL_SUCCESS = 1;
	/** The attribute location for vertices. */
	private static final int VERTICES_ATTRIB_LOCATION = 0;
	/** The attribute location for textures. */
	private static final int TEXTURES_ATTRIB_LOCATION = 1;
	/** The invalid uniform location value. */
	private static final int INVALID_UNIFORM_LOCATION = -1;
	/** The buffer size for matrices. */
	private static final int MATRIX_BUFFER_SIZE = 16;

	public Shader(String filename) {
		programObject = glCreateProgram();

		vertexShaderObject = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertexShaderObject, readFile(filename + ".vs"));
		glCompileShader(vertexShaderObject);
		if (glGetShaderi(vertexShaderObject, GL_COMPILE_STATUS) != GL_SUCCESS) {
			final String errorLog = glGetShaderInfoLog(vertexShaderObject);
			LOGGER.error("Vertex shader compilation failed: {}", errorLog);
			throw new RuntimeException("Failed to compile vertex shader: " + errorLog);
		}

		fragmentShaderObject = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragmentShaderObject, readFile(filename + ".fs"));
		glCompileShader(fragmentShaderObject);
		if (glGetShaderi(fragmentShaderObject, GL_COMPILE_STATUS) != GL_SUCCESS) {
			final String errorLog = glGetShaderInfoLog(fragmentShaderObject);
			LOGGER.error("Fragment shader compilation failed: {}", errorLog);
			throw new RuntimeException("Failed to compile fragment shader: " + errorLog);
		}
		glAttachShader(programObject, vertexShaderObject);
		glAttachShader(programObject, fragmentShaderObject);

		glBindAttribLocation(programObject, VERTICES_ATTRIB_LOCATION, "vertices");
		glBindAttribLocation(programObject, TEXTURES_ATTRIB_LOCATION, "textures");

		glLinkProgram(programObject);
		if (glGetProgrami(programObject, GL_LINK_STATUS) != GL_SUCCESS) {
			final String errorLog = glGetProgramInfoLog(programObject);
			LOGGER.error("Shader program linking failed: {}", errorLog);
			throw new RuntimeException("Failed to link shader program: " + errorLog);
		}
		glValidateProgram(programObject);
		if (glGetProgrami(programObject, GL_VALIDATE_STATUS) != GL_SUCCESS) {
			final String errorLog = glGetProgramInfoLog(programObject);
			LOGGER.error("Shader program validation failed: {}", errorLog);
			throw new RuntimeException("Failed to validate shader program: " + errorLog);
		}
	}

	@Override
	public void close() {
		glDetachShader(programObject, vertexShaderObject);
		glDetachShader(programObject, fragmentShaderObject);
		glDeleteShader(vertexShaderObject);
		glDeleteShader(fragmentShaderObject);
		glDeleteProgram(programObject);
	}

	public void setUniform(String uniformName, int value) {
		final int location = glGetUniformLocation(programObject, uniformName);
		if (location != INVALID_UNIFORM_LOCATION) {
			glUniform1i(location, value);
		}
	}

	public void setUniform(String uniformName, Vector4f value) {
		final int location = glGetUniformLocation(programObject, uniformName);
		if (location != INVALID_UNIFORM_LOCATION) {
			glUniform4f(location, value.x, value.y, value.z, value.w);
		}
	}

	public void setUniform(String uniformName, Matrix4f value) {
		final int location = glGetUniformLocation(programObject, uniformName);
		final FloatBuffer matrixData = BufferUtils.createFloatBuffer(MATRIX_BUFFER_SIZE);
		value.get(matrixData);
		if (location != INVALID_UNIFORM_LOCATION) {
			glUniformMatrix4fv(location, false, matrixData);
		}
	}

	public void bind() {
		glUseProgram(programObject);
	}

	private String readFile(String filename) {
		final StringBuilder outputString = new StringBuilder();
		try {
			final URI filePath = getClass().getResource("/shaders/" + filename).toURI();
			final BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(filePath)));
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				outputString.append(line);
				outputString.append("\n");
			}
			bufferedReader.close();
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
		}
		return outputString.toString();
	}
}
