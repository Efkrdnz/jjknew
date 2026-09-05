#version 150

uniform float Time;
uniform float BrushSeed;
uniform float Alpha;    // global strength, faded with the domain's phase
uniform float FadeFar;  // distance at which a card has faded out entirely

in vec2 texCoord;
in float viewDist;
out vec4 fragColor;

const vec3 INK  = vec3(0.010, 0.011, 0.016);
const vec3 BONE = vec3(0.92, 0.93, 0.96);

float hash11(float p) {
    p = fract(p * 0.1031);
    p *= p + 33.33;
    p *= p + p;
    return fract(p);
}

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise2(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash21(i), hash21(i + vec2(1, 0)), f.x), mix(hash21(i + vec2(0, 1)), hash21(i + vec2(1, 1)), f.x), f.y);
}

float fbm2(vec2 p) {
    return noise2(p) * 0.62 + noise2(p * 2.07) * 0.38;
}

/**
 * Ink splatter suspended in the volume.
 *
 * These replace fourteen brush strokes that never convinced anybody. The reference motif
 * is splatter, not bristles — and unlike anything painted into the shell shader, real
 * cards pass in front of and behind entities and parallax properly against the far wall,
 * which is the only reason they exist as geometry at all.
 */
void main() {
    // The card index rides in the V channel as v = id + sv * 0.5, so the whole set stays
    // one draw call and one uniform set.
    float id = floor(texCoord.y);
    float sv = clamp((texCoord.y - id) * 2.0, 0.0, 1.0);
    float su = clamp(texCoord.x, 0.0, 1.0);

    vec2 p = vec2(su, sv) - 0.5;
    float r = length(p);
    float ang = atan(p.y, p.x);

    float seed = BrushSeed + id * 7.31;
    // A ragged rim rather than a circle: two octaves around the perimeter, plus a slow
    // breath so a frozen splatter never quite sits still.
    float rim = 0.24 + 0.10 * fbm2(vec2(ang * 1.6, seed)) + 0.03 * sin(ang * 5.0 + seed * 3.0 + Time * 0.25);
    float body = smoothstep(rim + 0.02, rim - 0.02, r);

    // Two or three satellite droplets flung off the main blot.
    float drops = 0.0;
    for (int i = 0; i < 3; i++) {
        float fi = float(i);
        float a = hash11(seed + fi * 2.7) * 6.2831;
        float d = 0.30 + 0.14 * hash11(seed + fi * 5.1);
        vec2 at = vec2(cos(a), sin(a)) * d;
        float size = 0.020 + 0.038 * hash11(seed + fi * 9.4);
        drops = max(drops, smoothstep(size, size * 0.4, length(p - at)));
    }

    float mask = clamp(body + drops * 0.85, 0.0, 1.0);
    if (mask <= 0.003) {
        fragColor = vec4(0.0);
        return;
    }

    // Close up a card would be a white slab across the screen; far off it would pepper the
    // wall. It only exists in the band between.
    float near = smoothstep(1.5, 7.0, viewDist);
    float far = 1.0 - smoothstep(FadeFar * 0.7, FadeFar, viewDist);
    float fade = near * far;
    if (fade <= 0.003) {
        fragColor = vec4(0.0);
        return;
    }

    // Bone in the middle, drying to ink at the edge — the same two colours as the shell.
    vec3 col = mix(INK, BONE, smoothstep(0.0, 0.45, mask));
    float a = clamp(mask * fade * Alpha, 0.0, 0.9);
    fragColor = vec4(col, a);
}
