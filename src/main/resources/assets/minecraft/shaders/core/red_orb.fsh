#version 150
uniform mat4 ProjMat;
uniform float Time;
uniform float ChargeProgress;
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

void main() {
    vec2 uv = texCoord;
    vec2 center = vec2(0.5, 0.5);
    vec2 toCenter = uv - center;
    float dist = length(toCenter);
    float angle = atan(toCenter.y, toCenter.x);
    
    // rotating core
    float rotAngle = angle + Time * 3.0;
    
    // core orb
    float coreRadius = 0.15;
    float coreMask = smoothstep(coreRadius + 0.03, coreRadius - 0.01, dist);
    
    // rotating energy pattern
    float spiral = sin(dist * 15.0 - Time * 10.0 + rotAngle * 3.0) * 0.5 + 0.5;
    float energyRings = sin(dist * 20.0 - Time * 15.0) * 0.5 + 0.5;
    
    // pure red colors
    vec3 redCore = vec3(1.0, 0.0, 0.0);
    vec3 redBright = vec3(1.0, 0.2, 0.2);
    vec3 coreColor = mix(redCore, redBright, spiral * energyRings);
    
    // smooth inner glow
    float innerGlow = smoothstep(coreRadius * 1.8, coreRadius * 0.8, dist) * 0.8;
    
    // smooth outer energy aura
    float auraGlow = smoothstep(coreRadius * 3.5, coreRadius * 0.3, dist) * 0.4;
    auraGlow *= smoothstep(0.5, coreRadius * 3.5, dist);
    
    // combine layers - just core and glows
    vec3 finalColor = vec3(0.0);
    finalColor += coreColor * coreMask;
    finalColor += redCore * innerGlow;
    finalColor += redCore * auraGlow;
    
    // pulsing intensity based on charge
    float pulse = 0.7 + ChargeProgress * 0.3 + sin(Time * 4.0) * 0.15;
    finalColor *= pulse;
    
    // smooth alpha with fade at edges
    float totalAlpha = coreMask + innerGlow * 0.5 + auraGlow;
    totalAlpha *= smoothstep(0.5, 0.45, dist);
    totalAlpha = min(1.0, totalAlpha);
    
    fragColor = vec4(finalColor, totalAlpha);
}