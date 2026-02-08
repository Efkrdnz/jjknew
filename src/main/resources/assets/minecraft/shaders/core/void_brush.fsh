#version 150

uniform float Time;
uniform float BrushSeed;
uniform float Intensity;

in vec2 texCoord;
out vec4 fragColor;

const float PI = 3.14159265359;

float hash12(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
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

    float n000 = hash13(i + vec3(0.0, 0.0, 0.0));
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

    float nxy0 = mix(nx00, nx10, f.y);
    float nxy1 = mix(nx01, nx11, f.y);

    return mix(nxy0, nxy1, f.z);
}

float fbm3(vec3 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 6; i++) {
        v += a * noise3(p);
        p *= 2.0;
        a *= 0.5;
    }
    return v;
}

mat3 rotY(float a){
    float s = sin(a), c = cos(a);
    return mat3(
        c, 0.0, -s,
        0.0, 1.0, 0.0,
        s, 0.0,  c
    );
}

mat3 rotX(float a){
    float s = sin(a), c = cos(a);
    return mat3(
        1.0, 0.0, 0.0,
        0.0,  c,  s,
        0.0, -s,  c
    );
}

vec3 palette(float x){
    // deep blue -> violet -> cold white
    vec3 a = vec3(0.02, 0.03, 0.10);
    vec3 b = vec3(0.12, 0.18, 0.36);
    vec3 c = vec3(0.22, 0.10, 0.38);
    vec3 d = vec3(0.75, 0.85, 1.00);
    vec3 col = mix(a, b, clamp(x, 0.0, 1.0));
    col = mix(col, c, clamp(x * x, 0.0, 1.0) * 0.65);
    col = mix(col, d, clamp(x * x * x, 0.0, 1.0) * 0.35);
    return col;
}

void main() {
    float t = Time;

    // seamless sphere direction from uv (periodic => no seam)
    float theta = texCoord.x * 2.0 * PI;
    float phi   = texCoord.y * PI;

    vec3 dir = vec3(
        sin(phi) * cos(theta),
        cos(phi),
        sin(phi) * sin(theta)
    );

    // subtle global drift for hypnotic motion
    dir = rotY(t * 0.025) * rotX(t * 0.018) * dir;

    float seed = BrushSeed * 9.7;
    vec3 seedV = vec3(seed, seed * 0.37, seed * 0.91);

    // flow field (prevents "boiling"; looks like currents)
    float f0 = fbm3(dir * 2.3 + seedV * 0.7 + vec3(t * 0.03, -t * 0.02, t * 0.025));
    float f1 = fbm3(dir * 3.2 - seedV * 0.5 + vec3(-t * 0.02, t * 0.028, -t * 0.018));
    vec3 flow = vec3(f0 - 0.5, f1 - 0.5, (f0 + f1) * 0.5 - 0.5) * 0.85;

    // volumetric accumulation
    vec3 colAcc = vec3(0.0);
    float aAcc = 0.0;

    // feel knobs
    float baseScale = 2.35;
    float stepSize  = 0.40;

    for (int i = 0; i < 9; i++) { // 9 steps: good look, still ok performance
        float fi = float(i);

        vec3 p = dir * (baseScale + fi * stepSize);
        p += seedV;
        p += flow * (0.6 + fi * 0.08);

        // layer-specific swirl
        float layerSpin = t * (0.03 + fi * 0.004);
        p = rotY(layerSpin) * rotX(layerSpin * 0.8) * p;

        float nA = fbm3(p * 1.15 + vec3(t * 0.02, -t * 0.015, t * 0.018));
        float nB = fbm3(p * 2.05 - vec3(t * 0.028, t * 0.012, -t * 0.020));

        float density = nA * 0.62 + nB * 0.38;
        density = smoothstep(0.22, 0.95, density);

        // depth weighting (near layers contribute more)
        float w = exp(-fi * 0.24);

        vec3 layerCol = palette(density);
        layerCol *= (0.85 + density * 0.55);

        float a = density * (0.20 * w);
        colAcc += layerCol * a * (1.0 - aAcc);
        aAcc   += a * (1.0 - aAcc);
    }

    // "information shards" (white streak fragments), depth aware
    // build in direction space so it's seamless
    float shardAcc = 0.0;
    vec3 shardCol = vec3(0.95, 0.98, 1.0);

    for (int k = 0; k < 4; k++) {
        float fk = float(k);
        vec3 sp = dir * (6.0 + fk * 2.0) + seedV * (1.3 + fk * 0.2);

        // project to pseudo-2D for streaking
        vec2 sUV = vec2(sp.x + sp.z, sp.y - sp.z);
        sUV += vec2(t * (0.06 + fk * 0.01), -t * (0.04 + fk * 0.008));

        // cells
        vec2 cell = floor(sUV * 6.0);
        vec2 f = fract(sUV * 6.0) - 0.5;

        float rnd = hash12(cell + fk * 13.7);
        vec2 axis = normalize(vec2(cos(rnd * 6.2831), sin(rnd * 6.2831)));

        // elongated distance to axis => thin streak
        float d = abs(dot(f, vec2(-axis.y, axis.x)));
        float l = abs(dot(f, axis));

        float streak = smoothstep(0.08, 0.00, d) * smoothstep(0.55, 0.10, l);
        streak *= step(0.78, rnd); // sparsity
        streak *= (0.55 + 0.45 * sin(t * 2.2 + rnd * 12.0));

        // depth fade (far streaks weaker)
        float depthFade = 1.0 / (1.0 + fk * 0.9);
        shardAcc += streak * depthFade;
    }

    shardAcc = clamp(shardAcc, 0.0, 1.0);
    colAcc += shardCol * shardAcc * 0.95;

    // richer star layers (tiny + mid + a few big)
    float sTiny = noise3(dir * 110.0 + seedV + t * 0.01);
    float tinyStars = step(0.986, sTiny) * 0.75;

    float sMid = noise3(dir * 65.0 - seedV + t * 0.015);
    float midStars = step(0.975, sMid) * smoothstep(0.975, 0.995, sMid);

    float sBig = noise3(dir * 30.0 + seedV * 0.5 - t * 0.01);
    float bigStars = step(0.965, sBig) * smoothstep(0.965, 0.995, sBig);

    vec3 starC = vec3(0.92, 0.96, 1.0);
    colAcc += starC * tinyStars * 0.40;
    colAcc += starC * midStars * 0.95;
    colAcc += starC * bigStars * 1.25;

    // subtle chroma shimmer in highlights
    float highlight = clamp((dot(colAcc, vec3(0.333)) - 0.35) * 1.8, 0.0, 1.0);
    vec3 tint = vec3(0.05, -0.02, 0.08) * (0.5 + 0.5 * sin(t * 1.7 + aAcc * 7.0));
    colAcc += tint * highlight;

    // pulse + exposure lift so it's not "too dark"
    float pulse = sin(t * 0.55) * 0.08 + 0.92;
    colAcc *= pulse;
    colAcc *= 1.45;

    // soft vignette for immersion (darker edges)
    float v = texCoord.y * (1.0 - texCoord.y);
    float u = texCoord.x * (1.0 - texCoord.x);
    float vignette = pow(clamp(u * v * 18.0, 0.0, 1.0), 0.55);
    colAcc *= (0.70 + vignette * 0.55);

    float alpha = clamp(0.60 + aAcc * 0.85 + shardAcc * 0.10, 0.55, 0.95) * Intensity;

    fragColor = vec4(colAcc, alpha);
}
