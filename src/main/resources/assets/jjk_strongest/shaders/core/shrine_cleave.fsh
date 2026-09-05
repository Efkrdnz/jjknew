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

const vec3 HOT     = vec3(1.0, 0.97, 0.92);
const vec3 CRIMSON = vec3(0.85, 0.05, 0.03);
const vec3 EMBER   = vec3(1.0, 0.30, 0.08);

float hash21(vec2 p) {
    p = fract(p * vec2(123.34, 345.45));
    p += dot(p, p + 34.345);
    return fract(p.x * p.y);
}

/**
 * One cut through the world.
 *
 * The quad is a cylindrical billboard around the blade's axis, so v runs straight across
 * the cut on screen. Everything here is built on |v| against a handful of half-widths:
 * a razor core that is never under a pixel, one hard crimson edge, a dark WOUND that
 * shears the scene behind it so the world visibly splits along the line, and a brief
 * aura. The core dies first; the wound lingers and fades. Every edge is a step or a
 * one-sided smoothstep — nothing here is blurred on purpose.
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

    // Timing. The core holds, then dies; the wound outlives it and fades to nothing.
    float coreLife = 1.0 - smoothstep(0.50, 0.70, life);
    float woundLife = 1.0 - smoothstep(0.55, 1.0, life);
    if (style > 1.5) {
        // A strike is over almost as soon as it lands.
        coreLife = 1.0 - smoothstep(0.30, 0.55, life);
        woundLife = 1.0 - smoothstep(0.35, 0.85, life);
    }

    // Blade profile: thin at the origin, widest about two thirds along, a point at the tip.
    float profile = pow(smoothstep(0.0, 0.15, u), 0.5) * pow(1.0 - smoothstep(0.60, 1.0, u), 0.7);

    // Sweep: the blade draws itself from the origin. A hot point rides the leading edge and
    // is gone once the blade is complete.
    float drawn = 1.0 - smoothstep(sweep, sweep + 0.02, u);
    float tip = exp(-pow((u - sweep) * 40.0, 2.0)) * (1.0 - smoothstep(0.85, 1.0, sweep)) * profile;

    // Half-widths, in quad units (the quad's full height is the slash's width in blocks).
    float woundScale = style < 0.5 ? 2.0 : 1.0;
    float coreHalf = max(0.06 * profile, 0.9 * px);
    if (style > 1.5)
        coreHalf *= 1.3;
    // Serration: step-sharp teeth along both edges, not jitter.
    float tooth = (hash21(vec2(floor(u * 90.0), seed * 97.0)) - 0.5) * 0.05 * profile;
    float edgeHalf = coreHalf * 2.2 + max(tooth, 0.0);
    float woundHalf = (coreHalf * 3.5 + 0.12 * profile + tooth) * woundScale;
    float auraHalf = woundHalf * 2.5;

    float d = abs(v);
    float core = step(d, coreHalf);
    float edge = step(d, edgeHalf) * (1.0 - core);
    float wound = 1.0 - smoothstep(woundHalf * 0.85, woundHalf, d);   // sharp in, soft out
    float aura = 1.0 - smoothstep(woundHalf, auraHalf, d);

    // The world behind the cut, sheared apart across it. The screen-space gradient of v
    // points straight across the blade, whatever its orientation.
    vec2 perp = length(grad) > 1e-6 ? normalize(grad) : vec2(0.0, 1.0);
    vec2 suv = gl_FragCoord.xy / OutSize;
    float shearPx = 6.0 * wound * woundLife * sign(v);
    vec3 scene = texture(SceneSampler, clamp(suv + perp * shearPx / OutSize, 0.0, 1.0)).rgb;
    vec3 col = scene * (1.0 - 0.85 * wound * woundLife);

    vec3 coreCol = style < 1.5 && style > 0.5 ? mix(HOT, CRIMSON, 0.35) : HOT;
    float flicker = 0.92 + 0.08 * sin(Time * 60.0 + seed * 40.0);
    col = mix(col, CRIMSON, edge * coreLife);
    col = mix(col, coreCol * bright * flicker, core * coreLife);
    col += EMBER * aura * 0.35 * coreLife * bright;
    col += HOT * tip * 2.0 * coreLife;

    float alpha = wound * woundLife + (core + edge) * coreLife + aura * 0.5 * coreLife + tip * coreLife;
    alpha = clamp(alpha, 0.0, 1.0) * drawn * step(0.001, profile);
    if (alpha < 0.003)
        discard;
    fragColor = vec4(col, alpha);
}
