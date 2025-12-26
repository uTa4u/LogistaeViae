#version 330 core

//#define Z 0.0
//#define P 1.0
//#define N -1.0
//
//const mat3 ROTATION_LOOKUP[] = mat3[](
//        // X 0deg
//        mat3(
//            P, Z, Z,
//            Z, P, Z,
//            Z, Z, P
//        ),
//        // X 90deg
//        mat3(
//            P, Z, Z,
//            Z, Z, P,
//            Z, N, Z
//        ),
//        // X 180deg
//        mat3(
//            P, Z, Z,
//            Z, N, Z,
//            Z, Z, N
//        ),
//        // X 270deg
//        mat3(
//            P, Z, Z,
//            Z, Z, N,
//            N, P, N
//        ),
//        // Y 0deg
//        mat3(
//            P, Z, Z,
//            Z, P, Z,
//            Z, Z, P
//        ),
//        // Y 90deg
//        mat3(
//            Z, Z, N,
//            Z, P, Z,
//            P, Z, Z
//        ),
//        // Y 180deg
//        mat3(
//            N, Z, Z,
//            Z, P, Z,
//            Z, Z, N
//        ),
//        // Y 270deg
//        mat3(
//            Z, Z, P,
//            Z, P, Z,
//            Z, Z, P
//        ),
//        // Z 0deg
//        mat3(
//            P, Z, Z,
//            Z, P, Z,
//            Z, Z, P
//        ),
//        // Z 90deg
//        mat3(
//            Z, P, Z,
//            N, Z, Z,
//            Z, Z, P
//        ),
//        // Z 180deg
//        mat3(
//            N, Z, Z,
//            Z, N, Z,
//            Z, Z, P
//        ),
//        // Z 270deg
//        mat3(
//            Z, N, Z,
//            P, Z, Z,
//            Z, Z, P
//        )
//);

in vec3 vertexPosition;
in vec3 instancePosition;
in vec2 texCoord;

uniform mat4 projMatrix;
uniform mat4 viewMatrix;

out vec2 frag_tex;

void main() {
    gl_Position = projMatrix * viewMatrix * vec4(vertexPosition + instancePosition, 1.0);
    frag_tex = texCoord;
}