#version 150

in vec3 Position;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec2 texCoord;
out vec3 vertexPos;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    
    // pass vertex position for cubemap calculation
    vertexPos = Position;
    
    // basic UV mapping
    texCoord = vec2(Position.x, Position.y);
}