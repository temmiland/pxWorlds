#version 120

attribute vec2 vertices;
attribute vec2 textures;

varying vec2 tex_coords;

uniform mat4 projection;

void main() {
    tex_coords = textures;
    gl_Position = projection * vec4(vertices, 0.0, 1.0);
}
