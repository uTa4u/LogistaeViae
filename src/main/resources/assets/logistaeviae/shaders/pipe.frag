#version 330 core

in vec2 frag_tex;

uniform sampler2D tex;

out vec4 FragColor;

void main() {
//    FragColor = vec4(1.0, 0.0, 0.0, 1.0);
    FragColor = texture(tex, frag_tex);
}