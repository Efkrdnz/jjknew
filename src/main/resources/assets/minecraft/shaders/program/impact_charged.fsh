#version 150

uniform sampler2D DiffuseSampler;
uniform float DesaturateAmount;
uniform float GammaBoost;
uniform float Contrast;
uniform float RedTint;
uniform float Saturation;

in vec2 texCoord;
out vec4 fragColor;

float sat(float x) { return clamp(x, 0.0, 1.0); }

float luminance(vec3 c) {
    return dot(c, vec3(0.299, 0.587, 0.114));
}

float avgLum9(vec2 uv) {
    vec2 o = vec2(0.006, 0.006);

    float a = luminance(texture(DiffuseSampler, uv).rgb);
    float b = luminance(texture(DiffuseSampler, uv + vec2( o.x, 0.0)).rgb);
    float c = luminance(texture(DiffuseSampler, uv + vec2(-o.x, 0.0)).rgb);
    float d = luminance(texture(DiffuseSampler, uv + vec2(0.0,  o.y)).rgb);
    float e = luminance(texture(DiffuseSampler, uv + vec2(0.0, -o.y)).rgb);
    float f = luminance(texture(DiffuseSampler, uv + vec2( o.x,  o.y)).rgb);
    float g = luminance(texture(DiffuseSampler, uv + vec2(-o.x,  o.y)).rgb);
    float h = luminance(texture(DiffuseSampler, uv + vec2( o.x, -o.y)).rgb);
    float i = luminance(texture(DiffuseSampler, uv + vec2(-o.x, -o.y)).rgb);

    return (a+b+c+d+e+f+g+h+i) / 9.0;
}

// detect near-white only (prevents sky bypass)
bool isNearWhite(vec3 c) {
    float mx = max(max(c.r, c.g), c.b);
    float mn = min(min(c.r, c.g), c.b);
    return (mx > 0.93) && (mn > 0.80);
}

void main() {
    vec2 uv = texCoord;
    vec3 src = texture(DiffuseSampler, uv).rgb;

    if (isNearWhite(src)) {
        fragColor = vec4(clamp(src, 0.0, 1.0), 1.0);
        return;
    }

    float lum = luminance(src);
    float localAvg = avgLum9(uv);

    // exposure-relative luminance (night/day consistent)
    float norm = lum / (localAvg * 1.55 + 0.04);
    norm = sat(norm);

    // --- base: force almost-black (impact frame feel) ---
    // keep some structure using norm, but mostly black
    float baseV = pow(norm, 1.85);                 // crush midtones hard
    float baseFloor = 0.015;                       // tiny visibility in pure dark
    float baseR = sat(baseFloor + baseV * 0.18);   // subtle red tint in shadows only
    vec3 base = vec3(baseR, 0.0, 0.0);

    // --- red highlights: only where "energy" should show ---
    // edge/highlight mask from normalized luminance
    float hiMask = smoothstep(0.55, 0.90, norm);   // only brighter parts become strong red
    hiMask = pow(hiMask, 1.25);

    // highlight red value
    float redHi = pow(norm, 0.42);                 // bright mapping
    redHi *= (0.70 + 0.35 * sat(RedTint * 0.55));  // red intensity control
    redHi = sat(redHi);

    vec3 graded = vec3(redHi * hiMask, 0.0, 0.0);

    // --- overall strength (keep it short + punchy) ---
    // lower overall strength so it doesn't wash the screen
    float strength = sat(0.32 + 0.03 * Contrast + 0.06 * RedTint);

    // tiny influence of desaturate/gamma/saturation so uniforms stay alive without lifting brightness
    strength *= sat(0.85 + 0.05 * DesaturateAmount);
    strength *= sat(0.90 + 0.03 * Saturation);
    strength *= sat(0.90 + 0.03 * GammaBoost);

    // final mix: black-dominant + red highlights
    vec3 outCol = mix(base, graded, strength);

    // clamp + enforce red/black
    outCol.g = 0.0;
    outCol.b = 0.0;

    fragColor = vec4(clamp(outCol, 0.0, 1.0), 1.0);
}
