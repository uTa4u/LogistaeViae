#version 150

#define EPS 0.0001

in vec3 vertexPosition;// one of: 0.0, 0.25, 0.75, 1.0
in int vertexFace;// order is D-U-N-S-W-E
in vec3 instancePos;
in vec4 instanceUV;// minU, minV, maxU, maxV

uniform mat4 projMatrix;
uniform mat4 viewMatrix;

out vec2 frag_tex;

void main() {
    gl_Position = projMatrix * viewMatrix * vec4(vertexPosition + instancePos, 1.0);

    float uCoord, vCoord;
    if (vertexFace == 0) {
        // DOWN
        uCoord = 1.0 - vertexPosition.x;
        vCoord = vertexPosition.z;
    } else if (vertexFace == 1) {
        // UP
        uCoord = vertexPosition.x;
        vCoord = vertexPosition.z;
    } else if (vertexFace == 2) {
        // NORTH
        uCoord = 1.0 - vertexPosition.x;
        vCoord = vertexPosition.y;
    } else if (vertexFace == 3) {
        // SOUTH
        uCoord = vertexPosition.x;
        vCoord = vertexPosition.y;
    } else if (vertexFace == 4) {
        // WEST
        uCoord = 1.0 - vertexPosition.z;
        vCoord = vertexPosition.y;
    } else {
        // EAST
        uCoord = vertexPosition.z;
        vCoord = vertexPosition.y;
    }

    float u = mix(instanceUV.x, instanceUV.z, uCoord);
    float v = mix(instanceUV.y, instanceUV.w, vCoord);
    frag_tex = vec2(u, v);
}