#version 150

uniform float Time;
uniform float Intensity;

in vec2 texCoord;
in vec3 viewPos;

out vec4 fragColor;

float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));
    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 6; i++) {
        v += a * noise(p);
        p *= 2.0;
        a *= 0.5;
    }
    return v;
}

float ridged(vec2 p) {
    float v = 0.0;
    float a = 0.55;
    for (int i = 0; i < 5; i++) {
        float n = noise(p);
        n = 1.0 - abs(n * 2.0 - 1.0);
        v += a * n;
        p *= 2.05;
        a *= 0.5;
    }
    return clamp(v, 0.0, 1.0);
}

mat2 rot(float a) {
    float s = sin(a), c = cos(a);
    return mat2(c, -s, s, c);
}

float sdSpikeUp(vec2 p, float len, float thick, float tipMin, float taperPow) {
    float u = p.y;
    float v = abs(p.x);

    float t = clamp(1.0 - (u / len), 0.0, 1.0);
    float w = thick * pow(t, taperPow) + tipMin;

    float dSide = v - w;
    float dTop  = u - len;
    float dBot  = -u;

    return max(dSide, max(dTop, dBot));
}

float sdFourSpikes(vec2 p, float len, float thick, float tipMin, float taperPow) {
    float d0 = sdSpikeUp(p, len, thick, tipMin, taperPow);
    float d1 = sdSpikeUp(rot(3.14159265) * p, len, thick, tipMin, taperPow);
    float d2 = sdSpikeUp(rot(1.57079633) * p, len, thick, tipMin, taperPow);
    float d3 = sdSpikeUp(rot(-1.57079633) * p, len, thick, tipMin, taperPow);
    return min(min(d0, d1), min(d2, d3));
}

void main() {
    vec2 p = texCoord - vec2(0.5);
    float t = Time;

    vec2 pr = rot(t * 0.85) * p;

    float len      = 0.255;
    float thick    = 0.040;
    float tipMin   = 0.00002;
    float taperPow = 3.1;

    float d = sdFourSpikes(pr, len, thick, tipMin, taperPow);

    float aa = 0.0012;
    float core = 1.0 - smoothstep(0.0, aa, d);

    float outlineWidth = 0.0055;
    float outer = 1.0 - smoothstep(outlineWidth, outlineWidth + aa, d);
    float outline = clamp(outer - core, 0.0, 1.0);

    float dp = max(d, 0.0);

    float bandOuter = 0.018;
    float bandInner = 0.012;
    float outerBand = clamp(step(dp, bandOuter) - step(dp, bandInner), 0.0, 1.0);

    vec3 N = normalize(cross(dFdx(viewPos), dFdy(viewPos)));
    vec3 V = normalize(-viewPos);
    float fres = pow(1.0 - clamp(dot(N, V), 0.0, 1.0), 2.8);

    vec2 viewShift = V.xy * 0.22;

    vec2 base = p;
    vec2 q = rot(-0.55) * (base * 3.6);

    float band = exp(-abs(q.y) * 1.20);
    band = pow(band, 0.90);

    vec2 driftN = vec2(sin(t * 0.03), cos(t * 0.025)) * 0.35;
    float cloudA = fbm(q * 1.1 + driftN + vec2(11.2, 4.7));
    float cloudB = fbm(q * 2.2 - driftN * 0.7 + vec2(-3.6, 9.1));
    float clouds = clamp(cloudA * 0.62 + cloudB * 0.38, 0.0, 1.0);
    clouds = pow(clouds, 1.55);

    float dust = ridged(q * 3.1 + vec2(5.3, -2.2));
    dust = pow(dust, 2.2);

    float neb = band * (0.45 + clouds * 1.20);
    neb *= (1.0 - dust * 0.72);
    neb = clamp(neb, 0.0, 1.0);

    float haze = fbm(base * 1.4 + vec2(2.1, -7.4));
    haze = pow(haze, 2.3) * 0.22;

    vec2 s1uv = (base + viewShift * 0.25) * 95.0 + vec2(t * 0.010, -t * 0.008);
    float s1 = noise(s1uv + vec2(10.0, 30.0));
    float stars1 = step(0.9928, s1);

    vec2 s2uv = (base + viewShift * 0.55) * 60.0 + vec2(-t * 0.020, t * 0.014);
    float s2 = noise(s2uv + vec2(-22.0, 5.0));
    float stars2 = step(0.9892, s2) * smoothstep(0.9892, 0.999, s2);

    vec2 s3uv = (base + viewShift * 0.95) * 30.0 + vec2(t * 0.035, t * 0.020);
    float s3 = noise(s3uv + vec2(7.0, -9.0));
    float stars3 = step(0.985, s3) * smoothstep(0.985, 0.999, s3);

    float cluster = clamp(band * 1.35, 0.0, 1.0);
    float tw = 0.86 + 0.14 * sin(t * 2.4 + (s1 + s2 + s3) * 25.0);

    vec3 spaceDeep = vec3(0.001, 0.002, 0.006);
    vec3 spaceBlue = vec3(0.008, 0.014, 0.045);

    vec3 violet  = vec3(0.090, 0.030, 0.200);
    vec3 magenta = vec3(0.180, 0.060, 0.150);
    vec3 cyan    = vec3(0.140, 0.260, 0.480);

    vec3 galaxy = mix(spaceDeep, spaceBlue, 0.55 + haze);

    vec3 bandCol = mix(violet, cyan, clamp(clouds, 0.0, 1.0));
    bandCol = mix(bandCol, magenta, pow(clouds, 2.0) * 0.45);

    galaxy = mix(galaxy, bandCol, neb * 1.25);

    float coreGlow = pow(band, 2.0) * (0.28 + clouds * 0.85);
    galaxy += cyan * coreGlow * 0.40;
    galaxy += magenta * coreGlow * 0.22;

    galaxy *= (1.0 - dust * band * 0.30);

    vec3 starCol = vec3(0.92, 0.97, 1.0);
    galaxy += starCol * stars1 * (0.10 + cluster * 0.25) * tw;
    galaxy += starCol * stars2 * (0.18 + cluster * 0.60) * tw;
    galaxy += starCol * stars3 * (0.22 + cluster * 0.80) * tw;

    float shimmer = 1.0 + sin(t * 1.2 + neb * 10.0) * 0.06;
    galaxy *= shimmer;

    galaxy = pow(galaxy, vec3(0.82));

    float r = length(base);
    galaxy *= (0.82 + (1.0 - smoothstep(0.12, 0.85, r)) * 0.45);

    vec3 outlineCol = vec3(0.003, 0.040, 0.110);
    vec3 bandCol2   = vec3(0.006, 0.060, 0.180);

    float flick = 0.92 + 0.08 * sin(t * 5.0 + fbm(pr * 10.0) * 4.0);

    vec3 col = vec3(0.0);
    col += galaxy * core;

    float edgeBoost = 0.65 + fres * 1.15;

    col += outlineCol * outline * (1.10 * flick) * edgeBoost;
    col += bandCol2 * outerBand * (0.45 + fres * 0.45);

    float a = clamp(core * 0.95 + outline * 0.88 + outerBand * 0.30, 0.0, 1.0);
    a *= (0.90 + fres * 0.25);

    col *= Intensity;
    a *= Intensity;

    fragColor = vec4(col, a);
}
