#version 150

in vec3 Position;
in vec2 UV0;

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;

out vec2 texCoord;
// Position on the sphere in the domain's own space. The fragment stage needs this
// to build a real view ray; with only the UV it can do no better than paint the
// pattern flat onto the surface, which is why the interior used to look identical
// from everywhere inside.
out vec3 localPos;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    texCoord = UV0;
    localPos = Position;
}
