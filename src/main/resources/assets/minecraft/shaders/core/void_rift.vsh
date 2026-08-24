#version 150

in vec3 Position;
in vec2 UV0;

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;

out vec2 texCoord;
out vec3 viewPos;

void main() {
    vec4 vp = ModelViewMat * vec4(Position, 1.0);
    gl_Position = ProjMat * vp;
    texCoord = UV0;
    viewPos = vp.xyz;
}
