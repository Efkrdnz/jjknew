#version 150

in vec3 Position;
in vec2 UV0;

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;

out vec2 texCoord;
// The UNIT sphere position. The renderer scales the mesh by the radius afterwards, so
// this is length 1 and the fragment stage has to multiply by Radius before comparing it
// against anything measured in blocks. Getting that wrong is what made the old interior
// look like a flat painted texture from anywhere but dead centre.
out vec3 localPos;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    texCoord = UV0;
    localPos = Position;
}
