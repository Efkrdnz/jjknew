#version 150

in vec2 vUv;

uniform float Time;
uniform float Intensity;

out vec4 fragColor;

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 345.45));
    p += dot(p, p + 34.345);
    return fract(p.x * p.y);
}

float noise2(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);

    float a = hash21(i);
    float b = hash21(i + vec2(1.0, 0.0));
    float c = hash21(i + vec2(0.0, 1.0));
    float d = hash21(i + vec2(1.0, 1.0));

    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

void main() { // liquid bowl aura
    vec2 uv = vUv;
    vec2 p = uv - vec2(0.5);
    float r = length(p);

    float t = Time;

    // filled volume mask (no donut hole)
    float body = smoothstep(0.55, 0.05, r);

    // swirl / rotation field
    float ang = atan(p.y, p.x);
    float twist = (1.15 - r) * 3.1;
    float swirlAng = ang + t * 2.4 + twist;

    // bowl shading (fake normal)
    float h = 1.0 - smoothstep(0.0, 0.60, r);
    vec3 n = normalize(vec3(p.x * 2.2, p.y * 2.2, h * 2.7));
    vec3 l = normalize(vec3(-0.25, 0.35, 1.0));
    float bowl = clamp(dot(n, l), 0.0, 1.0);

    // liquid waves (wrap around any angle)
    vec2 q = vec2(cos(swirlAng), sin(swirlAng)) * (0.60 + 0.80 * r);
    float n1 = noise2(q * 3.2 + vec2(t * 0.65, -t * 0.50));
    float n2 = noise2((p * 4.1) + vec2(-t * 0.40, t * 0.30));
    float waves = (n1 * 0.65 + n2 * 0.35);

    float bands = sin(swirlAng * 5.5 + waves * 4.5);
    bands = 0.5 + 0.5 * bands;

    // rim emphasis (like a bowl edge highlight, but still filled)
    float rim = smoothstep(0.20, 0.55, r) * smoothstep(0.75, 0.40, r);

    float glow = (0.45 + 0.55 * bowl) * (0.55 + 0.45 * bands);
    float a = body * glow;
    a *= (0.55 + 0.55 * rim);
    a *= clamp(Intensity, 0.0, 3.0);

    // color
    vec3 deep = vec3(0.03, 0.18, 0.92);
    vec3 cyan = vec3(0.25, 0.95, 1.00);
    vec3 col = mix(deep, cyan, (0.30 * bowl + 0.70 * rim) * glow);
    col *= (0.55 + 0.70 * glow);

    fragColor = vec4(col, a);
}