#version 150

in vec3 Position;
in vec2 UV0;

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;

out vec2 texCoord;
// Distance from the eye. The whole card set is one draw call, so the fragment stage can
// not be told per-card how far away it is; taking it from the view matrix costs nothing.
out float viewDist;

void main() {
    vec4 viewPos = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * viewPos;
    texCoord = UV0;
    viewDist = length(viewPos.xyz);
}
