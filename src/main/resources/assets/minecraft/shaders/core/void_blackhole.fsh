#version 150

uniform float Time;
uniform float Intensity;

in vec2 texCoord;
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

void main() {
    vec2 uv = texCoord;
    vec2 c = vec2(0.5, 0.5);
    vec2 q = uv - c;

    float r0 = length(q);
    float ang0 = atan(q.y, q.x);

    // stronger lensing so it reads at huge size
    float lensStrength = 0.85;
    float lens = lensStrength / (r0 + 0.055);
    vec2 p = q * (1.0 + lens);

    float r = length(p);
    float ang = atan(p.y, p.x);

    // bigger core in UV space
    float coreR = 0.46;
    float core = smoothstep(coreR + 0.012, coreR - 0.010, r);

    // rim
    float rimOuter = coreR + 0.13;
    float rimInner = coreR + 0.03;
    float rimMask = smoothstep(rimOuter, rimInner, r) * (1.0 - smoothstep(rimInner, coreR - 0.006, r));
    rimMask = clamp(rimMask, 0.0, 1.0);

    // contrast ring
    float darkOuter = coreR + 0.30;
    float darkInner = coreR + 0.14;
    float darkRing = smoothstep(darkOuter, darkInner, r) * (1.0 - smoothstep(darkInner, coreR + 0.10, r));
    darkRing = clamp(darkRing, 0.0, 1.0);

    // smoke
    float swirlAng = ang + r * 6.0 - Time * 1.35;
    float swirlBands = sin(swirlAng * 4.0) * 0.5 + 0.5;

    vec2 polar = vec2(ang * 0.85, r * 3.0);
    float turb = fbm(polar * vec2(2.4, 1.3) + vec2(Time * 0.18, -Time * 0.07));
    turb = pow(clamp(turb, 0.0, 1.0), 1.25);

    float ringR = coreR + 0.14;
    float ringW = 0.18;
    float ring = smoothstep(ringR + ringW, ringR, r) * smoothstep(ringR - ringW, ringR, r);
    ring = clamp(ring, 0.0, 1.0);

    float outsideCore = 1.0 - smoothstep(coreR + 0.002, coreR - 0.002, r);

    float smoke = ring * outsideCore;
    smoke *= (0.35 + 0.65 * turb);
    smoke *= (0.55 + 0.45 * swirlBands);

    vec3 rimCol = vec3(1.0);
    vec3 smokeCol = vec3(0.70, 0.82, 1.0);
    vec3 smokeDeep = vec3(0.04, 0.07, 0.14);

    vec3 col = vec3(0.0);

    // dark ring (alpha makes it boost contrast)
    col += vec3(0.0) * darkRing;

    // smoke
    vec3 sc = mix(smokeDeep, smokeCol, clamp(smoke * 1.05, 0.0, 1.0));
    col += sc * smoke * 0.90;

    // rim
    float rimGlow = pow(rimMask, 0.48) * 4.2;
    col += rimCol * rimGlow;

    // core black
    col *= (1.0 - core);

    float pulse = sin(Time * 1.0) * 0.05 + 0.95;
    col *= pulse * Intensity;

    float a = 0.0;
    a = max(a, darkRing * 0.55);
    a = max(a, smoke * 0.85);
    a = max(a, rimMask);
    a = max(a, core);
    a = mix(a, 1.0, core);

    float edgeFade = smoothstep(0.85, 0.60, r0);
    a *= edgeFade;

    fragColor = vec4(col, clamp(a, 0.0, 1.0));
}
