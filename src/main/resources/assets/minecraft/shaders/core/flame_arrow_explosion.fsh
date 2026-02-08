#version 150
uniform mat4 ProjMat;
uniform float Time;
uniform float ChargeProgress;
in vec2 texCoord;
out vec4 fragColor;

// simple hash
float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

// simple noise
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

// simplified fbm - 4 octaves for nice detail
float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 4; i++) {
        value += amplitude * noise(p);
        p *= 2.0;
        amplitude *= 0.5;
    }
    return value;
}

void main() {
    vec2 uv = texCoord;
    vec2 center = vec2(0.5, 0.5);
    vec2 toCenter = uv - center;
    float dist = length(toCenter);
    
    // === MASSIVE BRIGHT CORE - ALWAYS ACTIVE ===
    float coreRadius = 0.30; // HUGE bright center (30% of texture)
    float coreBrightness = smoothstep(coreRadius * 0.5, 0.0, dist); // super bright center
    float coreGlow = smoothstep(coreRadius, 0.0, dist); // outer core glow
    
    // === WIDE FIRE BEAM ===
    float beamRadius = 0.55; // very wide beam (55% of texture)
    float beamBody = smoothstep(beamRadius, coreRadius, dist);
    
    // === TURBULENT FIRE ANIMATION ===
    // vertical flowing fire
    vec2 fireUV = uv * 3.0;
    fireUV.y -= Time * 2.0; // fast upward flow
    float fire1 = fbm(fireUV);
    
    // horizontal swirls
    vec2 swirlUV = uv * 4.0;
    swirlUV.x += Time * 0.8;
    swirlUV.y -= Time * 1.5;
    float fire2 = fbm(swirlUV);
    
    // === FLAMING EDGES ===
    // animated flames on outer edge
    vec2 edgeUV = uv * 5.0;
    edgeUV.y -= Time * 2.5; // scrolling flames
    float edgeNoise = fbm(edgeUV);
    
    // make flames appear on the edge
    float edgeStart = beamRadius - 0.08;
    float edgeEnd = beamRadius + 0.15;
    float edgeZone = smoothstep(edgeStart, edgeEnd, dist);
    float edgeCutoff = 1.0 - smoothstep(edgeEnd, edgeEnd + 0.1, dist);
    float flames = edgeNoise * edgeZone * edgeCutoff;
    
    // === FIRE COLOR PALETTE ===
    // white-hot center → yellow → orange → red edges
    vec3 white = vec3(1.0, 1.0, 1.0);
    vec3 brightYellow = vec3(1.0, 1.0, 0.8);
    vec3 yellow = vec3(1.0, 0.95, 0.5);
    vec3 orange = vec3(1.0, 0.7, 0.3);
    vec3 deepOrange = vec3(1.0, 0.5, 0.2);
    vec3 red = vec3(0.95, 0.3, 0.1);
    vec3 darkRed = vec3(0.7, 0.2, 0.05);
    
    // === BUILD COLOR FROM CENTER OUTWARD ===
    vec3 color = vec3(0.0);
    
    // SUPER BRIGHT WHITE-HOT CORE (always active)
    color += white * coreBrightness * 2.0; // extra bright!
    color += brightYellow * coreGlow * 1.5;
    
    // WIDE FIRE BODY - yellow to orange gradient
    float bodyProgress = smoothstep(coreRadius, beamRadius, dist);
    color += mix(yellow, orange, bodyProgress * 0.6) * beamBody * (0.9 + fire1 * 0.3);
    color += mix(orange, deepOrange, bodyProgress) * beamBody * fire2 * 0.4;
    
    // FLAMING EDGES - orange to red
    color += mix(deepOrange, red, edgeNoise) * flames * 1.2;
    color += darkRed * flames * edgeNoise * 0.5;
    
    // === PULSING GLOW ===
    float pulse = 0.92 + 0.08 * sin(Time * 5.0);
    color *= pulse;
    
    // add extra brightness boost
    color *= 1.3;
    
    // === ALPHA - SOLID CENTER, SOFT EDGES ===
    float alpha = 0.0;
    
    // ALWAYS VISIBLE CORE
    alpha = max(alpha, coreBrightness * 1.5); // super bright center
    alpha = max(alpha, coreGlow * 1.2); // bright core glow
    
    // SOLID FIRE BODY
    alpha = max(alpha, beamBody * (0.95 + fire1 * 0.15));
    alpha = max(alpha, beamBody * fire2 * 0.6);
    
    // FLAMING EDGES
    alpha = max(alpha, flames * (0.8 + edgeNoise * 0.3));
    
    // soft outer falloff
    alpha *= smoothstep(0.70, 0.40, dist);
    
    // boost overall visibility
    alpha = min(alpha * 1.4, 1.0);
    
    // === GLOBAL FADE-OUT EFFECT ===
    // ChargeProgress is used as a fade-out parameter (1.0 = bright, 0.0 = faded)
    // This allows the renderer to control overall fade
    float globalFade = ChargeProgress;
    color *= globalFade; // darken colors
    alpha *= globalFade; // fade transparency
    
    fragColor = vec4(color, alpha);
}
