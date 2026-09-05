#version 150

in vec3 Position;
in vec2 UV0;

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;

out vec2 texCoord;
// The sphere position as it arrives: the renderer bakes its PoseStack into the vertex on
// the CPU, and the entity dispatcher has already translated that stack to the domain's
// camera-relative position, so this is (entityCentre + Radius * unit) rather than the unit
// vector the fragment stage treats it as. The consequence is that the interior's parallax
// is roughly 1/Radius of what it should be: the sky is centred on the domain rather than
// on your eye, so it shifts far less than it ought to as you walk. Everything still lands
// in the right direction, which is why it reads correctly from the middle. Left alone
// deliberately -- correcting it changes how the whole interior looks and cannot be checked
// without running it. uv_shards.vsh, which needs true positions for the burst, undoes the
// offset explicitly.
out vec3 localPos;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    texCoord = UV0;
    localPos = Position;
}
