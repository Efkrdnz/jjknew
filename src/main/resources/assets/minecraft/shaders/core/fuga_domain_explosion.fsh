#version 150
uniform float Time;
uniform float ChargeProgress; // fade 1 -> 0
uniform float Progress;       // expand 0 -> 1
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
    for (int i = 0; i < 4; i++) {
        v += a * noise(p);
        p *= 2.0;
        a *= 0.5;
    }
    return v;
}

float smooth01(float x) { return x * x * (3.0 - 2.0 * x); }

void main() {
    vec2 uv = texCoord;
    vec2 c = vec2(0.5);
    vec2 d2 = uv - c;

    float dist = length(d2) / 0.5; // 0 center -> 1 edge
    float p = clamp(Progress, 0.0, 1.0);

    float ringR = mix(0.08, 1.0, smooth01(p));
    float ringW = mix(0.16, 0.06, p);

    vec2 nUV = uv * 5.0;
    nUV.y -= Time * 1.8;
    float n1 = fbm(nUV);

    vec2 nUV2 = uv * 7.0;
    nUV2.x += Time * 1.1;
    nUV2.y -= Time * 2.4;
    float n2 = fbm(nUV2);

    float ring = 1.0 - smoothstep(ringW, ringW * 0.35, abs(dist - ringR));
    ring *= (0.7 + 0.6 * n1);

    float inside = smoothstep(ringR, 0.0, dist);
    inside *= (0.55 + 0.55 * n2);

    float glow = smoothstep(ringR + 0.35, ringR, dist) * 0.6;

    vec3 white = vec3(1.0);
    vec3 yellow = vec3(1.0, 0.95, 0.55);
    vec3 orange = vec3(1.0, 0.55, 0.18);
    vec3 red = vec3(0.95, 0.25, 0.08);

    vec3 col = vec3(0.0);

    float core = smoothstep(0.45, 0.0, dist) * (1.0 - p);
    col += white * core * 2.2;
    col += yellow * inside * 1.1;

    col += mix(yellow, orange, clamp(dist, 0.0, 1.0)) * inside;
    col += mix(orange, red, n1) * ring * 1.6;
    col += orange * glow;

    float alpha = 0.0;
    alpha = max(alpha, ring * 0.95);
    alpha = max(alpha, inside * 0.65);
    alpha = max(alpha, glow * 0.55);
    alpha *= (1.0 - smoothstep(1.05, 1.25, dist));

    float fade = clamp(ChargeProgress, 0.0, 1.0);
    col *= fade;
    alpha *= fade;

    fragColor = vec4(col, min(alpha, 1.0));
}
