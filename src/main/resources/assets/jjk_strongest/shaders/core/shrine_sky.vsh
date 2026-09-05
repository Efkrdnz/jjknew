#version 150

in vec3 Position;   // the unit sphere, drawn around the camera and scaled up
in vec2 UV0;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec3 dir;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    // The dome is centred on the eye, so the vertex position IS the view direction.
    dir = Position;
}
