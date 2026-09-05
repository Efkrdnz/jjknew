#version 150

in vec3 Position;
in vec2 UV0;

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;
uniform vec3 CamOffset;   // camera relative to the sphere centre, in blocks

out vec2 texCoord;
// Domain-local position, in BLOCKS, for whichever surface is being drawn — the dome or
// the floor disc. Position is not that: the renderer bakes its PoseStack into the vertex
// on the CPU, and the entity dispatcher has already translated that stack to the domain's
// camera-relative position, so what arrives is (entityCentre + surface point), with
// entityCentre = -CamOffset. Adding CamOffset back gives the true point on the surface.
// The previous version passed Position through as if it were the unit sphere, which cost
// the interior almost all of its parallax: the sky was centred on the domain rather than
// on your eye.
out vec3 localPos;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
    texCoord = UV0;
    localPos = Position + CamOffset;
}
