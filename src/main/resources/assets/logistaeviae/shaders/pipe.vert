#version 330 core

in vec3 vertexPosition;
in vec3 instancePos;
in vec2 instanceUV;

uniform mat4 projMatrix;
uniform mat4 viewMatrix;

out vec2 frag_tex;

void main() {
    gl_Position = projMatrix * viewMatrix * vec4(vertexPosition + instancePos, 1.0);
    frag_tex = instanceUV;
}