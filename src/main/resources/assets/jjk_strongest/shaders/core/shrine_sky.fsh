#version 150

uniform float Time;
uniform float Alpha;   // how much of the shrine's sky is in force: phase x distance
uniform float Seed;    // per-shrine, 0..1

in vec3 dir;
out vec4 fragColor;

const float PI = 3.14159265359;

// ---- palette: blood dusk ----------------------------------------------------
const vec3 ZENITH   = vec3(0.06, 0.005, 0.010);
const vec3 BLOOD    = vec3(0.55, 0.04, 0.02);
const vec3 EMBER    = vec3(0.95, 0.35, 0.08);
const vec3 CHARCOAL = vec3(0.05, 0.02, 0.02);
const vec3 HOT      = vec3(1.0, 0.97, 0.92);
const vec3 CRIMSON  = vec3(0.85, 0.05, 0.03);

/** How many cuts stand in the sky. */
const int SCARS = 6;

// ---- noise -----------------------------------------------------------------

float hash11(float p) {
    p = fract(p * 0.1031);
    p *= p + 33.33;
    p *= p + p;
    return fract(p);
}

float hash13(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.yzx + 33.33);
    return fract((p.x + p.y) * p.z);
}

float noise3(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float n000 = hash13(i);
    float n100 = hash13(i + vec3(1.0, 0.0, 0.0));
    float n010 = hash13(i + vec3(0.0, 1.0, 0.0));
    float n110 = hash13(i + vec3(1.0, 1.0, 0.0));
    float n001 = hash13(i + vec3(0.0, 0.0, 1.0));
    float n101 = hash13(i + vec3(1.0, 0.0, 1.0));
    float n011 = hash13(i + vec3(0.0, 1.0, 1.0));
    float n111 = hash13(i + vec3(1.0, 1.0, 1.0));
    float nx00 = mix(n000, n100, f.x);
    float nx10 = mix(n010, n110, f.x);
    float nx01 = mix(n001, n101, f.x);
    float nx11 = mix(n011, n111, f.x);
    return mix(mix(nx00, nx10, f.y), mix(nx01, nx11, f.y), f.z);
}

float fbm3(vec3 p) {
    float sum = 0.0;
    float amp = 0.5;
    for (int i = 0; i < 3; i++) {
        sum += noise3(p) * amp;
        p *= 2.03;
        amp *= 0.5;
    }
    return sum;
}

/**
 * The sky over Malevolent Shrine.
 *
 * A blood dusk: near-black maroon overhead down to a blood-red band and an ember line at
 * the horizon. Over it, two layers of slow domain-warped cloud, charcoal where thick and lit
 * from beneath in ember where thin, with rifts of maroon between. Cut across all of it, six
 * scars fixed by the shrine's seed — razor lines with dark wounds, each pulsing on its own
 * beat. The sky itself has been cut. Direction-only, so it is at infinity.
 */
void main() {
    vec3 d = normalize(dir);
    float t = Time;
    float h = d.y;
    float aa = fwidth(h) + 1e-4;

    // L0: the dusk.
    vec3 col = mix(BLOOD, ZENITH, smoothstep(-0.05, 0.55, h));
    float az = atan(d.z, d.x);
    float hot = 0.6 + 0.4 * cos(az - Seed * 2.0 * PI);
    col += EMBER * exp(-abs(h) * 14.0) * (0.45 + 0.55 * hot);
    col += BLOOD * exp(-abs(h) * 4.0) * 0.35;

    // L1: clouds, two layers warped by the same field and drifting against each other.
    vec3 p = d * 2.6;
    vec3 warp = vec3(fbm3(p + t * 0.012), fbm3(p + 5.2 - t * 0.009), 0.0);
    float c1 = fbm3(p + warp * 0.9 + vec3(t * 0.020, 0.0, 0.0) + Seed);
    float c2 = fbm3(p * 2.1 - warp * 0.6 + vec3(-t * 0.014, 0.0, t * 0.010) + Seed * 3.0);
    float density = c1 * 0.65 + c2 * 0.35;
    float cover = smoothstep(0.42, 0.62, density);
    float thin = 1.0 - smoothstep(0.42, 0.75, density);
    float skyMask = smoothstep(-0.02, 0.15, h);
    vec3 cloud = mix(CHARCOAL, EMBER * 0.6, thin * thin);
    col = mix(col, cloud, cover * skyMask * 0.9);

    // L2: the scars. Each is a great circle (a plane through the eye), cut to a partial arc.
    for (int i = 0; i < SCARS; i++) {
        float fi = float(i);
        vec3 n = normalize(vec3(hash11(fi * 1.7 + Seed) * 2.0 - 1.0, hash11(fi * 2.9 + Seed + 3.0) * 2.0 - 1.0, hash11(fi * 4.3 + Seed + 7.0) * 2.0 - 1.0));
        float dist = abs(dot(d, n));
        vec3 ref = normalize(cross(n, vec3(0.3, 1.0, 0.2)));
        float along = dot(d, ref);
        float extent = smoothstep(-0.9, -0.6, along) * (1.0 - smoothstep(0.5, 0.85, along));
        float pulse = 0.55 + 0.45 * sin(t * (1.6 + 0.3 * fi) + fi * 1.9);
        float w = max(0.0025 + 0.002 * pulse, aa * 0.9);
        float core = 1.0 - smoothstep(w * 0.5, w, dist);
        float wound = 1.0 - smoothstep(w * 2.0, w * 7.0, dist);
        col = mix(col, col * 0.15, wound * extent * 0.85);
        col += mix(HOT, CRIMSON, 0.4) * core * extent * (0.8 + 0.6 * pulse);
        col += CRIMSON * (1.0 - smoothstep(w, w * 3.5, dist)) * extent * 0.5 * pulse;
    }

    // Under the horizon there is only dark; the ground hides it anyway.
    col = mix(col, vec3(0.02, 0.0, 0.0), smoothstep(-0.02, -0.4, h));

    fragColor = vec4(col, Alpha);
}
