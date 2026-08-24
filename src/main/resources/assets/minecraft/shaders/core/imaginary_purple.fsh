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
    
    // rapid merge phase (0.0 to 0.3)
    float mergePhase = smoothstep(0.0, 0.3, ChargeProgress);
    
    // orb positions
    vec2 redStart = vec2(0.38, 0.5);
    vec2 blueStart = vec2(0.62, 0.5);
    vec2 currentRedPos = mix(redStart, center, mergePhase);
    vec2 currentBluePos = mix(blueStart, center, mergePhase);
    
    float distToRed = length(uv - currentRedPos);
    float distToBlue = length(uv - currentBluePos);
    
    // red/blue orbs
    float orbSize = 0.06 * (1.0 - mergePhase * 0.7);
    float redCore = smoothstep(orbSize + 0.01, orbSize, distToRed);
    float redGlow = smoothstep(orbSize * 2.5, orbSize, distToRed) * 0.6;
    float redOrb = (redCore + redGlow) * (1.0 - mergePhase);
    
    float blueCore = smoothstep(orbSize + 0.01, orbSize, distToBlue);
    float blueGlow = smoothstep(orbSize * 2.5, orbSize, distToBlue) * 0.6;
    float blueOrb = (blueCore + blueGlow) * (1.0 - mergePhase);
    
    vec3 redColor = vec3(1.0, 0.2, 0.2);
    vec3 blueColor = vec3(0.2, 0.4, 1.0);
    
    // purple explosion phase
    float explosionPhase = smoothstep(0.2, 1.0, ChargeProgress);
    
    // intense white-cyan core
    float coreSize = 0.025 * explosionPhase;
    float whiteCore = smoothstep(coreSize + 0.01, coreSize * 0.3, dist);
    whiteCore = pow(whiteCore, 0.5); // bright falloff
    
    // electric rays shooting out
    float rayCount = 24.0;
    float rayAngle = angle + Time * 1.5;
    float rayPattern = abs(sin(rayAngle * rayCount * 0.5));
    rayPattern = pow(rayPattern, 0.3);
    
    // chaotic ray length variations
    float rayNoise = noise(vec2(angle * 10.0, Time * 8.0));
    float rayLength = 0.12 + rayNoise * 0.08;
    float rays = smoothstep(rayLength, 0.0, dist) * rayPattern;
    rays *= smoothstep(0.015, 0.04, dist); // fade near center
    
    // lightning bolts
    float boltNoise = noise(vec2(angle * 15.0 + Time * 12.0, dist * 40.0));
    float bolts = step(0.85, boltNoise) * smoothstep(0.18, 0.03, dist);
    bolts *= noise(vec2(Time * 20.0, angle * 8.0)) * 0.5 + 0.5;
    
    // purple corona around white core
    float corona = smoothstep(0.15, 0.02, dist) * smoothstep(0.01, 0.05, dist);
    
    // outer purple glow
    float outerGlow = smoothstep(0.25, 0.05, dist) * 0.4;
    
    // chaotic energy swirls
    float swirl = sin(angle * 8.0 + dist * 30.0 - Time * 10.0) * 0.5 + 0.5;
    float swirlMask = smoothstep(0.2, 0.08, dist) * smoothstep(0.04, 0.09, dist) * swirl * 0.3;
    
    // electric sparkles
    float sparkles = noise(uv * 180.0 + Time * 25.0);
    sparkles = step(0.97, sparkles) * smoothstep(0.22, 0.0, dist) * 0.8;
    
    // color palette - white to cyan to purple
    vec3 white = vec3(1.0, 1.0, 1.0);
    vec3 cyan = vec3(0.7, 0.9, 1.0);
    vec3 purple = vec3(0.6, 0.2, 0.9);
    vec3 purpleDark = vec3(0.4, 0.1, 0.6);
    
    // build explosion color
    vec3 explosionColor = vec3(0.0);
    explosionColor += white * whiteCore * 3.0;
    explosionColor += cyan * rays * 1.5;
    explosionColor += white * bolts * 2.0;
    explosionColor += purple * corona * 1.2;
    explosionColor += purpleDark * outerGlow;
    explosionColor += purple * swirlMask;
    explosionColor += cyan * sparkles * 1.5;
    
    // intense flickering
    float flicker = 0.8 + noise(vec2(Time * 18.0)) * 0.2;
    explosionColor *= flicker;
    
    // violent pulse
    float pulse = 0.85 + sin(Time * 12.0) * 0.15;
    explosionColor *= pulse;
    
    // combine all phases
    vec3 finalColor = vec3(0.0);
    finalColor += redColor * redOrb * 2.0;
    finalColor += blueColor * blueOrb * 2.0;
    finalColor += explosionColor * explosionPhase;
    
    // alpha
    float explosionAlpha = whiteCore + rays * 0.7 + bolts * 0.8 + corona * 0.5 + outerGlow * 0.3 + sparkles * 0.4;
    float totalAlpha = explosionAlpha * explosionPhase + (redOrb + blueOrb) * 0.8;
    totalAlpha = min(1.0, totalAlpha * 1.2);
    
    fragColor = vec4(finalColor, totalAlpha);
}