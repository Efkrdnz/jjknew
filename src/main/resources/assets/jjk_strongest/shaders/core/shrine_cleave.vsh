#version 150

in vec3 Position;
in vec2 UV0;     // u along the blade (0 origin, 1 tip), v across it
in vec4 Color;   // NOT a colour: four per-slash parameters, see MalevolentShrineSlashRenderer

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 vUv;
out vec4 vParams;

void main() {
    vUv = UV0;
    vParams = Color;
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
}
