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
    
    // rotating core (faster, opposite direction)
    float rotAngle = angle - Time * 3.0;
    
    // core orb with smooth edges
    float coreRadius = 0.15;
    float coreMask = smoothstep(coreRadius + 0.03, coreRadius - 0.01, dist);
    
    // rotating energy pattern (faster)
    float spiral = sin(dist * 15.0 + Time * 10.0 + rotAngle * 3.0) * 0.5 + 0.5;
    float energyRings = sin(dist * 20.0 + Time * 15.0) * 0.5 + 0.5;
    
    // bright blue colors
    vec3 blueCore = vec3(0.2, 0.5, 1.0);
    vec3 blueBright = vec3(0.5, 0.8, 1.0);
    vec3 coreColor = mix(blueCore, blueBright, spiral * energyRings);
    
    // smooth inner glow
    float innerGlow = smoothstep(coreRadius * 1.8, coreRadius * 0.8, dist) * 0.8;
    
    // fast particles pulling INWARD - reversed direction (+ instead of -)
    float particleSpeed = Time * 8.0;
    float particleNoise = noise(vec2(angle * 12.0, dist * 15.0 + particleSpeed));
    float particles = step(0.65, particleNoise);
    
    // particles come FROM outside (reverse range)
    particles *= smoothstep(coreRadius * 0.5, coreRadius * 4.0, dist);
    particles *= (1.0 - smoothstep(coreRadius * 4.5, coreRadius * 4.0, dist));
    
    // fast particle trails going INWARD - reversed
    float trailSpeed = Time * 10.0;
    float trailNoise = noise(vec2(angle * 10.0, dist * 12.0 + trailSpeed));
    float trails = smoothstep(0.5, 0.95, trailNoise);
    
    // trails pull inward from outside
    trails *= smoothstep(coreRadius * 0.3, coreRadius * 5.0, dist);
    trails *= (1.0 - smoothstep(coreRadius * 5.5, coreRadius * 5.0, dist));
    trails *= 0.5;
    
    // smooth outer energy aura (circular)
    float auraGlow = smoothstep(coreRadius * 3.5, coreRadius * 0.3, dist) * 0.4;
    auraGlow *= smoothstep(0.5, coreRadius * 3.5, dist);
    
    // combine all layers
    vec3 finalColor = vec3(0.0);
    finalColor += coreColor * coreMask;
    finalColor += blueCore * innerGlow;
    finalColor += blueBright * particles * 1.5;  // brighter particles
    finalColor += blueCore * trails;
    finalColor += blueCore * auraGlow;
    
    // pulsing intensity based on charge
    float pulse = 0.7 + ChargeProgress * 0.3 + sin(Time * 4.0) * 0.15;
    finalColor *= pulse;
    
    // smooth circular alpha with fade at edges
    float totalAlpha = coreMask + innerGlow * 0.5 + particles * 0.9 + trails * 0.7 + auraGlow;
    totalAlpha *= smoothstep(0.5, 0.45, dist);
    totalAlpha = min(1.0, totalAlpha);
    
    fragColor = vec4(finalColor, totalAlpha);
}