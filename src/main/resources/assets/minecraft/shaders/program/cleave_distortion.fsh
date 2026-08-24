#version 150

uniform sampler2D DiffuseSampler;
uniform float DistortionIntensity; // must be used (non-zero) or mc will error
uniform float SlashCount;
uniform float Progress;

uniform vec4 Slash1;
uniform vec4 Slash2;
uniform vec4 Slash3;
uniform vec4 Slash4;
uniform vec4 Slash5;
uniform vec4 Slash6;
uniform vec4 Slash7;
uniform vec4 Slash8;

in vec2 texCoord;
out vec4 fragColor;

float sat(float x) { return clamp(x, 0.0, 1.0); }

float distanceToLine(vec2 point, vec2 linePos, float lineAngle) {
    vec2 lineDir = vec2(cos(lineAngle), sin(lineAngle));
    vec2 linePerp = vec2(-lineDir.y, lineDir.x);
    vec2 toPoint = point - linePos;
    return abs(dot(toPoint, linePerp));
}

void evalCut(vec2 uv, vec4 slash, float p, inout float lineSum, inout float gapSum) {
    if (slash.w <= 0.0) return;

    float dist = distanceToLine(uv, slash.xy, slash.z);

    float lineWidth = mix(0.0035, 0.0020, p);
    float gapWidth  = mix(0.0120, 0.0080, p);

    float line = 1.0 - smoothstep(0.0, lineWidth, dist);
    float gap  = 1.0 - smoothstep(0.0, gapWidth, dist);

    lineSum += line * slash.w;
    gapSum  += gap  * slash.w;
}

void main() {
    vec2 uv = texCoord;

    vec3 col = texture(DiffuseSampler, uv).rgb;

    float p = sat(Progress);

    float lineSum = 0.0;
    float gapSum = 0.0;

    if (SlashCount >= 1.0) evalCut(uv, Slash1, p, lineSum, gapSum);
    if (SlashCount >= 2.0) evalCut(uv, Slash2, p, lineSum, gapSum);
    if (SlashCount >= 3.0) evalCut(uv, Slash3, p, lineSum, gapSum);
    if (SlashCount >= 4.0) evalCut(uv, Slash4, p, lineSum, gapSum);
    if (SlashCount >= 5.0) evalCut(uv, Slash5, p, lineSum, gapSum);
    if (SlashCount >= 6.0) evalCut(uv, Slash6, p, lineSum, gapSum);
    if (SlashCount >= 7.0) evalCut(uv, Slash7, p, lineSum, gapSum);
    if (SlashCount >= 8.0) evalCut(uv, Slash8, p, lineSum, gapSum);

    float cutLine = sat(lineSum * 1.4);
    float cutGap  = sat(gapSum  * 1.2);

    // keep uniform alive with a tiny, visually negligible contribution
    col += vec3(DistortionIntensity) * 0.0000001;

    // full inversion over time
    vec3 invCol = vec3(1.0) - col;
    col = mix(col, invCol, p);

    // contrast ramps up during transition
    float contrast = mix(1.0, 1.85, p);
    col = (col - 0.5) * contrast + 0.5;

    // cut look: dark gap + bright edge
    col *= (1.0 - cutGap * mix(0.30, 0.70, p));
    col += vec3(1.0, 0.95, 1.0) * cutLine * mix(0.06, 0.22, p);

    fragColor = vec4(col, 1.0);
}
