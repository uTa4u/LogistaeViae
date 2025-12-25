#version 330 core

layout (location = 0) in vec3 in_pos;
layout (location = 1) in vec2 in_tex;

uniform mat4 projMatrix;
uniform mat4 viewMatrix;

out vec2 frag_tex;

void main() {
    gl_Position = projMatrix * viewMatrix * vec4(in_pos, 1.0);
    frag_tex = in_tex;
}