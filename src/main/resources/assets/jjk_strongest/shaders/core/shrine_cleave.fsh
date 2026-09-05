#version 150

uniform sampler2D SceneSampler;   // the frame so far, copied once per frame
uniform vec2 OutSize;
uniform float Time;

in vec2 vUv;
// The Color attribute, repurposed. Bytes, as MalevolentShrineSlashRenderer packs them:
//   r  life, 0 at birth .. 1 at death
//   g  style in the top two bits (0 Cleave, 1 Dismantle, 2 strike), a 6-bit seed below
//   b  brightness jitter, 0.7 .. 1.0
//   a  sweep: how far along the blade the leading edge has drawn, 0 .. 1
in vec4 vParams;
out vec4 fragColor;

const vec3 BLACK   = vec3(0.015, 0.0, 0.0);
const vec3 CRIMSON = vec3(0.85, 0.05, 0.03);
const vec3 BLOOD   = vec3(0.55, 0.04, 0.02);

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 345.45));
    p += dot(p, p + 34.345);
    return fract(p.x * p.y);
}

/**
 * One cut through the world: a thin black blade with a red rim.
 *
 * The quad is a cylindrical billboard around the blade's axis, so v runs straight across
 * the cut on screen. The interior is black — a line of nothing where the world was — with
 * one hard crimson outline, a narrow red aura outside that, and a faint shear of the scene
 * right at the rim so the edge reads as a real edge. The blade draws itself tip to tip in
 * about a tick and a half, holds, and goes. Every edge is a step or a one-sided smoothstep;
 * nothing here is blurred on purpose, and nothing here is white.
 */
void main() {
    float u = vUv.x;
    float v = vUv.y - 0.5;
    float life = vParams.r;
    float gb = floor(vParams.g * 255.0 + 0.5);
    float style = floor(gb / 64.0);
    float seed = mod(gb, 64.0) / 63.0;
    float bright = 0.7 + 0.3 * vParams.b;
    float sweep = vParams.a;

    // Derivatives first, before any branch.
    float px = fwidth(v);
    vec2 grad = vec2(dFdx(v), dFdy(v));

    // Timing: hold, then gone. The aura lets go a little before the blade does.
    float bladeLife = 1.0 - smoothstep(0.55, 0.85, life);
    float auraLife = 1.0 - smoothstep(0.40, 0.70, life);
    if (style > 1.5) {
        // A strike is over almost as soon as it lands.
        bladeLife = 1.0 - smoothstep(0.30, 0.60, life);
        auraLife = 1.0 - smoothstep(0.20, 0.50, life);
    }

    // Blade profile: thin at the origin, widest about two thirds along, a point at the tip.
    float profile = pow(smoothstep(0.0, 0.15, u), 0.5) * pow(1.0 - smoothstep(0.60, 1.0, u), 0.7);

    // Sweep: the blade draws itself from the origin, with a brief red flare at the leading
    // edge that is gone once the blade is complete.
    float drawn = 1.0 - smoothstep(sweep, sweep + 0.02, u);
    float tip = exp(-pow((u - sweep) * 40.0, 2.0)) * (1.0 - smoothstep(0.85, 1.0, sweep)) * profile;

    // Half-widths, in quad units (the quad's full height is the slash's width in blocks).
    // Thin: the black interior is a few hundredths of the quad, never under a pixel.
    float coreHalf = max(0.032 * profile, 0.9 * px);
    if (style > 1.5)
        coreHalf *= 1.2;
    // Serration: step-sharp teeth on the rim, not jitter.
    float tooth = max(hash21(vec2(floor(u * 90.0), seed * 97.0)) - 0.5, 0.0) * 0.03 * profile;
    float rim = max(0.018 * profile, 0.9 * px) + tooth;
    if (style < 0.5)
        rim *= 1.6;   // a Cleave wears a heavier outline
    float edgeHalf = coreHalf + rim;
    float auraHalf = edgeHalf * 2.2;

    float d = abs(v);
    float core = step(d, coreHalf);
    float edge = step(d, edgeHalf) * (1.0 - core);
    float aura = 1.0 - smoothstep(edgeHalf, auraHalf, d);
    float rimZone = 1.0 - smoothstep(edgeHalf, edgeHalf * 1.6, d);

    // The world just outside the rim, nudged apart across the cut: a few pixels, enough
    // for the edge to read as an edge in the world rather than a line drawn over it.
    vec2 perp = length(grad) > 1e-6 ? normalize(grad) : vec2(0.0, 1.0);
    vec2 suv = gl_FragCoord.xy / OutSize;
    float shearPx = 3.0 * rimZone * bladeLife * sign(v);
    vec3 col = texture(SceneSampler, clamp(suv + perp * shearPx / OutSize, 0.0, 1.0)).rgb;

    float flicker = 0.92 + 0.08 * sin(Time * 60.0 + seed * 40.0);
    col += BLOOD * aura * 0.55 * auraLife * bright;
    col = mix(col, CRIMSON * (0.85 + 0.35 * bright) * flicker, edge * bladeLife);
    col = mix(col, BLACK, core * bladeLife);
    col += CRIMSON * tip * 1.5 * bladeLife;

    float alpha = (core + edge) * bladeLife + aura * 0.5 * auraLife + rimZone * 0.6 * bladeLife + tip * bladeLife;
    alpha = clamp(alpha, 0.0, 1.0) * drawn * step(0.001, profile);
    if (alpha < 0.003)
        discard;
    fragColor = vec4(col, alpha);
}
