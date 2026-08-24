#version 150

uniform sampler2D SceneSampler;
uniform float Time;
uniform float Life;
uniform float Seed;

in vec2 texCoord;
out vec4 fragColor;

float sat(float x) { return clamp(x, 0.0, 1.0); }

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

void main() {
    vec2 uv = texCoord;
    vec2 c = vec2(0.5, 0.5);
    vec2 d = uv - c;
    float dist = length(d);
    float ang = atan(d.y, d.x);

    float life = Life;

    // timeline (matches your long dramatic version)
    float charge = sat(life / 150.0);
    float twitch = sat((life - 150.0) / 30.0);
    float explode = sat((life - 180.0) / 60.0);

    // fade-out from 210 -> 240
    float fade = 1.0 - sat((life - 210.0) / 30.0);

    vec3 scene = texture(SceneSampler, uv).rgb;

    // ------------------------------------------------------------
    // new charge look: single unstable circle/ring that flickers red/blue
    // ------------------------------------------------------------

    // violent vibration but still circular: wobble the radius, not the center
    float vibSpeed = mix(6.0, 40.0, charge * charge);
    float vibA = noise(vec2(ang * 9.0, Time * vibSpeed + Seed * 0.17));
    float vibB = noise(vec2(ang * 21.0 + 7.3, Time * (vibSpeed * 0.9) + Seed * 0.11));
    float vib = (vibA * 0.6 + vibB * 0.4);

    // base ring radius slowly tightens a bit while charging
    float ringR = mix(0.22, 0.17, charge);
    float ringJitter = (vib - 0.5) * mix(0.010, 0.030, charge); // stronger with charge
    float ringDist = abs(dist - (ringR + ringJitter));

    // ring width also jitters (violent shimmer)
    float baseW = mix(0.030, 0.020, charge);
    float wJ = (noise(vec2(ang * 17.0, Time * (vibSpeed * 0.8) + Seed * 0.33)) - 0.5) * mix(0.010, 0.020, charge);
    float ringW = max(0.010, baseW + wJ);

    float ringMask = 1.0 - smoothstep(ringW, ringW * 2.2, ringDist);

    // inner core glow to make it more visible
    float coreMask = smoothstep(0.20, 0.0, dist);
    coreMask *= (0.25 + 0.75 * charge);

    // red/blue clear flashing with stronger contrast
    float speed = mix(2.0, 34.0, charge * charge * charge);
    float flick = step(0.5, fract((Time + Seed * 0.01) * speed));
    float flick2 = step(0.5, fract((Time + 1.37 + Seed * 0.01) * (speed * 0.83)));
    float mixRB = mix(flick, flick2, 0.35);

    vec3 colRed = vec3(1.0, 0.18, 0.12);
    vec3 colBlue = vec3(0.10, 0.55, 1.0);
    vec3 colPurple = vec3(0.78, 0.33, 1.0);

    // base charge color flips, but also “punches” brightness near 0/1 to feel snappy
    vec3 rb = mix(colRed, colBlue, mixRB);
    float snap = 0.85 + 0.15 * step(0.95, abs(mixRB - 0.5) * 2.0); // brighter on strong red/blue moments

    // brightness ramps up as it approaches merge
    float chargeBright = mix(1.8, 3.6, charge * charge);
    float pulse = 0.70 + 0.30 * sin(Time * mix(3.0, 16.0, charge));
    chargeBright *= (0.85 + 0.15 * pulse) * snap;

    // add “electric” streak noise along the ring
    float arcN = noise(vec2(ang * 38.0, Time * (10.0 + 18.0 * charge) + Seed));
    float arcs = smoothstep(0.78, 0.98, arcN) * ringMask;
    arcs *= mix(0.15, 0.85, charge);

    // merge factor after charge ends (same as before, used to slide into purple)
    float merge = smoothstep(0.62, 1.0, charge);
    vec3 chargeCol = mix(rb, colPurple, merge);

    // ------------------------------------------------------------
    // keep your later phases: twitch + explosion + distortion + fade
    // ------------------------------------------------------------

    float ringWob = noise(vec2(ang * 6.0, Time * 2.0 + Seed * 0.1));
    float edge = smoothstep(0.52, 0.46, dist + (ringWob - 0.5) * 0.035 * (0.2 + 0.8 * charge));

    float warpPow = (0.02 + 0.07 * twitch + 0.12 * explode) * edge;
    vec2 warp = normalize(d + 1e-6) * warpPow * (noise(uv * 9.0 + Time * 1.2) - 0.5);
    vec2 suv = uv + warp;

    vec3 sceneWarp = texture(SceneSampler, suv).rgb;

    float streakBase = noise(vec2(ang * 44.0, Time * (6.0 + 14.0 * twitch) + Seed));
    float streaks = smoothstep(0.80, 0.985, streakBase) * smoothstep(0.70, 0.0, dist);
    streaks *= (0.15 + 0.85 * twitch);

    float purpleTwitch = (noise(uv * 36.0 + Time * 10.0 + Seed) - 0.5) * 0.08 * twitch;
    float purpleMask = smoothstep(0.36, 0.0, dist + purpleTwitch);

    // shock ring expands during explosion window
    float shockR = mix(0.06, 0.70, explode);
    float shockW = mix(0.018, 0.10, explode);
    float shock = 1.0 - smoothstep(shockW, shockW * 2.0, abs(dist - shockR));
    shock *= explode;

    float bloom = smoothstep(0.78, 0.0, dist) * explode;
    float whiteCore = smoothstep(0.11, 0.0, dist) * (0.35 + 0.65 * explode);

    vec3 outCol = mix(scene, sceneWarp, warpPow * 6.0);

    // charge layer (now single ring + core glow + arcs)
    float chargeAlpha = sat((ringMask * 1.10 + coreMask * 0.65 + arcs * 0.90) * (0.35 + 0.65 * (1.0 - merge)));
    outCol += chargeCol * (ringMask * 1.20 + coreMask * 0.85) * chargeBright;
    outCol += vec3(1.0, 0.92, 1.0) * arcs * (1.3 + 1.0 * charge);

    // purple twitch layer
    outCol += colPurple * purpleMask * (0.65 + 1.55 * twitch);
    outCol += colPurple * streaks * 1.35;

    // explosion layer
    outCol += colPurple * bloom * 2.6;
    outCol += vec3(1.0) * whiteCore * (1.0 + 2.6 * explode);
    outCol += vec3(0.95, 0.85, 1.0) * shock * 3.2;

    // apply fade to intensity + alpha
    outCol *= (0.55 + 0.45 * fade);

    float alpha = 0.0;
    alpha += chargeAlpha;
    alpha += sat(purpleMask * 0.85);
    alpha += sat(streaks * 0.95);
    alpha += sat(bloom * 0.95);
    alpha += sat(shock * 1.25);
    alpha = sat(alpha);

    float edgeFade = smoothstep(0.52, 0.42, dist);
    alpha *= edgeFade;
    alpha *= fade;

    fragColor = vec4(outCol, alpha);
}
