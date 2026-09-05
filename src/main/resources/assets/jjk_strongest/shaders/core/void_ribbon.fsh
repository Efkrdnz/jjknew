#version 150

uniform float Time;
uniform float BrushSeed;
uniform float Alpha;    // global strength, faded with the domain's phase
uniform float FadeFar;  // distance at which a stroke has faded out entirely

in vec2 texCoord;
in float viewDist;
out vec4 fragColor;

const float PI = 3.14159265359;

float hash11(float p) {
    p = fract(p * 0.1031);
    p *= p + 33.33;
    p *= p + p;
    return fract(p);
}

float hash12(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

// Same two colours the shell's palette is built from, so a stroke floating in front of
// the wall reads as the same substance rather than an effect pasted over it.
vec3 palette(float x, float depth) {
    vec3 ink  = vec3(0.014, 0.015, 0.019);
    vec3 bone = vec3(0.92, 0.93, 0.96);
    vec3 mono = mix(ink, bone, clamp(x, 0.0, 1.0));
    vec3 deepBlue = vec3(0.05, 0.10, 0.30);
    vec3 violet   = vec3(0.16, 0.07, 0.32);
    vec3 bleed = mix(deepBlue, violet, clamp(x * x, 0.0, 1.0));
    return mix(mono, mono * 0.35 + bleed, clamp(depth, 0.0, 1.0) * 0.8);
}

void main() {
    // The stroke index rides in the V channel: each quad is emitted with
    // v = id + sv * 0.5, so the whole set stays one draw call and one uniform set.
    float id = floor(texCoord.y);
    float sv = clamp((texCoord.y - id) * 2.0, 0.0, 1.0);
    float su = clamp(texCoord.x, 0.0, 1.0);

    float t = Time;
    float r1 = hash11(id * 1.37 + BrushSeed);
    float r2 = hash11(id * 2.91 + BrushSeed * 1.7 + 5.0);

    // A hand-drawn spine: the centre line wanders instead of running dead straight,
    // which is most of what separates a brush stroke from a rectangle.
    float wobble = 0.10 * sin(su * (4.0 + r1 * 3.0) + id * 2.3 + t * 0.35)
                 + 0.05 * sin(su * (9.0 + r2 * 5.0) - id * 1.1 - t * 0.22);
    float centre = 0.5 + wobble;

    // Loaded at the start of the stroke, running dry at the end.
    float load = mix(1.0, 0.35, su * su);
    float halfWidth = 0.5 * load * (0.75 + 0.25 * sin(su * 3.0 + r1 * 6.28));

    float across = 1.0 - clamp(abs(sv - centre) / max(halfWidth, 1e-4), 0.0, 1.0);
    across = across * across;

    // Both ends lift off the surface rather than stopping square.
    float along = smoothstep(0.0, 0.16, su) * (1.0 - smoothstep(0.72, 1.0, su));

    // Bristle streaks: gaps that run the length of the stroke, not across it.
    float bristle = 0.72 + 0.28 * hash12(vec2(floor(sv * 26.0) + id * 31.0, floor(su * 3.0)));
    bristle *= 0.80 + 0.20 * sin(su * 40.0 + sv * 60.0 + id * 4.0);

    float mask = across * along * bristle;
    if (mask <= 0.002) {
        fragColor = vec4(0.0);
        return;
    }

    // Close up a stroke would be a white slab across the screen, so it thins out as you
    // walk into it; far off it dissolves before it can pepper the far wall.
    float near = smoothstep(1.5, 7.0, viewDist);
    float far = 1.0 - smoothstep(FadeFar * 0.7, FadeFar, viewDist);
    float fade = near * far;
    if (fade <= 0.002) {
        fragColor = vec4(0.0);
        return;
    }

    // Strokes further into the volume carry more of the blue bleed, matching the way
    // the shell's march layers deepen with distance.
    float depth = clamp(viewDist / max(FadeFar, 1.0), 0.0, 1.0);
    vec3 col = palette(mask, depth * 0.9);
    col *= 0.85 + 0.35 * mask;

    // A slow breath so the set never looks frozen between drifts.
    float breath = 0.82 + 0.18 * sin(t * 0.6 + id * 1.9);

    float a = clamp(mask * fade * breath * Alpha, 0.0, 0.85);
    fragColor = vec4(col, a);
}
