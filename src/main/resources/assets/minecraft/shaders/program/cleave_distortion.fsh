#version 150

uniform sampler2D DiffuseSampler;
uniform float DistortionIntensity;
uniform float SlashCount;
uniform vec4 Slash1;
uniform vec4 Slash2;
uniform vec4 Slash3;
uniform vec4 Slash4;

in vec2 texCoord;
out vec4 fragColor;

const float PI = 3.14159265359;

// distance from point to line
float distanceToLine(vec2 point, vec2 linePos, float lineAngle) {
    vec2 lineDir = vec2(cos(lineAngle), sin(lineAngle));
    vec2 linePerpendicular = vec2(-lineDir.y, lineDir.x);
    vec2 toPoint = point - linePos;
    return abs(dot(toPoint, linePerpendicular));
}

// calculate displacement for a single slash
vec2 calculateSlashDisplacement(vec2 uv, vec4 slash) {
    if (slash.w <= 0.0) return vec2(0.0);
    
    vec2 slashPos = slash.xy;
    float slashAngle = slash.z;
    float slashStrength = slash.w;
    
    float dist = distanceToLine(uv, slashPos, slashAngle);
    float falloff = 1.0 - smoothstep(0.0, 0.15, dist);
    
    if (falloff <= 0.0) return vec2(0.0);
    
    vec2 slashDir = vec2(cos(slashAngle), sin(slashAngle));
    vec2 displaceDir = vec2(-slashDir.y, slashDir.x);
    
    vec2 toPoint = uv - slashPos;
    float side = sign(dot(toPoint, displaceDir));
    
    float displaceAmount = falloff * slashStrength * DistortionIntensity * 0.03 * side;
    
    return displaceDir * displaceAmount;
}

void main() {
    vec2 uv = texCoord;
    
    // ONLY DISTORTION - accumulate displacement from all slashes
    vec2 totalDisplacement = vec2(0.0);
    
    if (SlashCount >= 1.0) totalDisplacement += calculateSlashDisplacement(uv, Slash1);
    if (SlashCount >= 2.0) totalDisplacement += calculateSlashDisplacement(uv, Slash2);
    if (SlashCount >= 3.0) totalDisplacement += calculateSlashDisplacement(uv, Slash3);
    if (SlashCount >= 4.0) totalDisplacement += calculateSlashDisplacement(uv, Slash4);
    
    // apply displacement
    uv = clamp(uv + totalDisplacement, 0.0, 1.0);
    
    // sample texture with distorted coordinates
    vec3 color = texture(DiffuseSampler, uv).rgb;
    
    fragColor = vec4(color, 1.0);
}