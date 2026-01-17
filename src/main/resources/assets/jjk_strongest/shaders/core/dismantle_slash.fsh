#version 150

uniform sampler2D SceneSampler;

uniform vec2 OutSize;
uniform float Time;

// color aura tint
uniform float RandA; // R
uniform float RandB; // G
uniform float RandC; // B

// slash parameters
uniform float Style;       // 0,1,2
uniform float Seed;        // random seed
uniform float SlashLength; // world units blocks
uniform float SlashWidth;  // world units blocks

in vec2 vUv;
out vec4 fragColor;

float sat(float x) { return clamp(x, 0.0, 1.0); }

vec3 sampleScene(vec2 uv) {
    return texture(SceneSampler, clamp(uv, 0.0, 1.0)).rgb;
}

// hash for jaggedness
float hash21(vec2 p){
    p = fract(p * vec2(123.34, 345.45));
    p += dot(p, p + 34.345);
    return fract(p.x * p.y);
}

vec3 hsv2rgb(vec3 c){
    vec4 K = vec4(1.0, 2.0/3.0, 1.0/3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

void main() {
    vec2 suv = gl_FragCoord.xy / OutSize;
    vec3 scene = sampleScene(suv);

    float u = vUv.x;
    float v = vUv.y;

    // taper slash ends meet in center
    float taper = 1.0 - abs(u - 0.5) * 2.0;
    taper = sat(taper);

    float aspect = SlashWidth / max(SlashLength, 0.001);
    aspect = clamp(aspect, 0.01, 0.25);

    float baseHalf = (0.08 + 0.22 * aspect) * (0.25 + 0.75 * taper);

    // style 1 jagged edges
    float jag = 0.0;
    if (Style > 0.5) {
        float n = hash21(vec2(u * 64.0, (v + Seed) * 24.0));
        jag = (n - 0.5) * (0.06 + 0.06 * taper);
    }

    float halfThick = baseHalf + jag;

    float d = abs(v - 0.5);

    float core = 1.0 - smoothstep(halfThick * 0.55, halfThick * 0.80, d);

    float aura = 1.0 - smoothstep(halfThick * 0.85, halfThick * 2.8, d);
    aura = sat(aura - core);

    float endFade = smoothstep(0.0, 0.10, taper);

    vec3 tint = vec3(RandA, RandB, RandC);
    tint = mix(vec3(0.15), vec3(1.0), tint);

    // style 2 iridescent shimmer
    if (Style > 1.5) {
        float hue = fract(u * 2.5 + Time * 0.25 + Seed * 0.01);
        vec3 rainbow = hsv2rgb(vec3(hue, 0.85, 1.0));
        tint = mix(tint, rainbow, 0.85);
    }

    vec3 col = scene;

    // black slash core
    col = mix(col, vec3(0.02), core * endFade);

    float auraPow = 0.75;
    if (Style < 0.5) auraPow = 0.35;
    else if (Style < 1.5) auraPow = 0.75;
    else auraPow = 1.15;

    col += tint * aura * auraPow * endFade;

    float alpha = sat((core * 0.95 + aura * 0.9) * endFade);
    fragColor = vec4(col, alpha);
}