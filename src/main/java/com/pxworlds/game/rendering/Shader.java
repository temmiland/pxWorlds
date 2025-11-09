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
	private static final Logger LOGGER = LoggerFactory.getLogger(Shader.class);
	/** The OpenGL program object ID. */
	private int programObject;
	/** The OpenGL vertex shader object ID. */
	private int vertexShaderObject;
	/** The OpenGL fragment shader object ID. */
	private int fragmentShaderObject;

	public Shader(String filename) {
		programObject = glCreateProgram();

		vertexShaderObject = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertexShaderObject, readFile(filename + ".vs"));
		glCompileShader(vertexShaderObject);
		if (glGetShaderi(vertexShaderObject, GL_COMPILE_STATUS) != 1) {
			String errorLog = glGetShaderInfoLog(vertexShaderObject);
			logger.error("Vertex shader compilation failed: {}", errorLog);
			throw new RuntimeException("Failed to compile vertex shader: " + errorLog);
		}

		fragmentShaderObject = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragmentShaderObject, readFile(filename + ".fs"));
		glCompileShader(fragmentShaderObject);
		if (glGetShaderi(fragmentShaderObject, GL_COMPILE_STATUS) != 1) {
			String errorLog = glGetShaderInfoLog(fragmentShaderObject);
			logger.error("Fragment shader compilation failed: {}", errorLog);
			throw new RuntimeException("Failed to compile fragment shader: " + errorLog);
		}		glAttachShader(programObject, vertexShaderObject);
		glAttachShader(programObject, fragmentShaderObject);

		glBindAttribLocation(programObject, 0, "vertices");
		glBindAttribLocation(programObject, 1, "textures");

		glLinkProgram(programObject);
		if (glGetProgrami(programObject, GL_LINK_STATUS) != 1) {
			String errorLog = glGetProgramInfoLog(programObject);
			logger.error("Shader program linking failed: {}", errorLog);
			throw new RuntimeException("Failed to link shader program: " + errorLog);
		}
		glValidateProgram(programObject);
		if (glGetProgrami(programObject, GL_VALIDATE_STATUS) != 1) {
			String errorLog = glGetProgramInfoLog(programObject);
			logger.error("Shader program validation failed: {}", errorLog);
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
		int location = glGetUniformLocation(programObject, uniformName);
		if (location != -1) glUniform1i(location, value);
	}

	public void setUniform(String uniformName, Vector4f value) {
		int location = glGetUniformLocation(programObject, uniformName);
		if (location != -1) glUniform4f(location, value.x, value.y, value.z, value.w);
	}

	public void setUniform(String uniformName, Matrix4f value) {
		int location = glGetUniformLocation(programObject, uniformName);
		FloatBuffer matrixData = BufferUtils.createFloatBuffer(16);
		value.get(matrixData);
		if (location != -1) glUniformMatrix4fv(location, false, matrixData);
	}

	public void bind() {
		glUseProgram(programObject);
	}

	private String readFile(String filename) {
		StringBuilder outputString = new StringBuilder();
		BufferedReader bufferedReader;
		try {
			URI filePath = getClass().getResource("/shaders/" + filename).toURI();
			bufferedReader = new BufferedReader(new FileReader(new File(filePath)));
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
