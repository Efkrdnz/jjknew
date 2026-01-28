#version 150

uniform sampler2D SceneSampler;
uniform mat4 ProjMat;
uniform float Time;
uniform float ChargeProgress;
uniform float RedPosX;
uniform float RedPosY;
uniform float BluePosX;
uniform float BluePosY;

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
    
    // DON'T sample the scene - just render orbs on transparent background
    
    // orb positions - closer to center
    float lerpFactor = ChargeProgress;
    vec2 redPos = vec2(0.35, 0.5);
    vec2 bluePos = vec2(0.65, 0.5);
    vec2 centerPos = vec2(0.5, 0.5);
    
    // move orbs toward center as charge progresses
    vec2 currentRedPos = mix(redPos, centerPos, lerpFactor);
    vec2 currentBluePos = mix(bluePos, centerPos, lerpFactor);
    
    float distToRed = length(uv - currentRedPos);
    float distToBlue = length(uv - currentBluePos);
    float distToCenter = length(uv - centerPos);
    
    // red orb
    float redRadius = 0.05 * (1.0 - lerpFactor * 0.5);
    float redCore = smoothstep(redRadius + 0.01, redRadius, distToRed);
    float redGlow = smoothstep(redRadius * 2.5, redRadius, distToRed) * 0.6;
    
    vec3 redColor = vec3(1.0, 0.2, 0.2);
    vec3 redResult = redColor * (redCore + redGlow) * 1.5;
    
    // blue orb
    float blueRadius = 0.05 * (1.0 - lerpFactor * 0.5);
    float blueCore = smoothstep(blueRadius + 0.01, blueRadius, distToBlue);
    float blueGlow = smoothstep(blueRadius * 2.5, blueRadius, distToBlue) * 0.6;
    
    vec3 blueColor = vec3(0.2, 0.4, 1.0);
    vec3 blueResult = blueColor * (blueCore + blueGlow) * 1.5;
    
    // purple orb
    float purpleAlpha = smoothstep(0.5, 0.9, lerpFactor);
    float purpleRadius = 0.06 * lerpFactor;
    float purpleCore = smoothstep(purpleRadius + 0.015, purpleRadius, distToCenter);
    float purpleGlow = smoothstep(purpleRadius * 2.5, purpleRadius, distToCenter) * 0.5;
    
    float wave = sin(distToCenter * 30.0 - Time * 8.0) * 0.5 + 0.5;
    float energyPattern = wave * purpleAlpha * 0.3;
    
    vec3 purpleColor = vec3(0.7, 0.3, 0.9) * (1.0 + energyPattern);
    vec3 purpleResult = purpleColor * (purpleCore + purpleGlow) * purpleAlpha * 0.7;
    
    // energy trails
    vec2 toRed = currentRedPos - centerPos;
    vec2 toBlue = currentBluePos - centerPos;
    vec2 toPixel = uv - centerPos;
    
    float redTrail = 0.0;
    float blueTrail = 0.0;
    
    if (lerpFactor > 0.1 && lerpFactor < 0.9) {
        float dotRed = dot(normalize(toPixel), normalize(toRed));
        float dotBlue = dot(normalize(toPixel), normalize(toBlue));
        
        float distAlongRed = length(toPixel) * dotRed;
        float distAlongBlue = length(toPixel) * dotBlue;
        
        if (dotRed > 0.9 && distAlongRed < length(toRed)) {
            float perpDist = length(toPixel - normalize(toRed) * distAlongRed);
            redTrail = smoothstep(0.02, 0.0, perpDist) * (1.0 - lerpFactor) * 0.4;
        }
        
        if (dotBlue > 0.9 && distAlongBlue < length(toBlue)) {
            float perpDist = length(toPixel - normalize(toBlue) * distAlongBlue);
            blueTrail = smoothstep(0.02, 0.0, perpDist) * (1.0 - lerpFactor) * 0.4;
        }
    }
    
    vec3 trailColor = redColor * redTrail + blueColor * blueTrail;
    
    // sparkles
    float sparkleNoise = noise(uv * 200.0 + Time * 10.0);
    float sparkleRed = step(0.99, sparkleNoise) * smoothstep(redRadius * 2.0, 0.0, distToRed) * 0.5;
    float sparkleBlue = step(0.99, sparkleNoise) * smoothstep(blueRadius * 2.0, 0.0, distToBlue) * 0.5;
    float sparklePurple = step(0.99, sparkleNoise) * smoothstep(purpleRadius * 2.0, 0.0, distToCenter) * purpleAlpha * 0.4;
    
    vec3 sparkles = redColor * sparkleRed + blueColor * sparkleBlue + purpleColor * sparklePurple;
    
    // ONLY render the orbs - no scene background
    vec3 orbColor = vec3(0.0);
    orbColor += redResult * (1.0 - purpleAlpha * 0.8);
    orbColor += blueResult * (1.0 - purpleAlpha * 0.8);
    orbColor += purpleResult;
    orbColor += trailColor;
    orbColor += sparkles * 0.5;
    
    // output with proper alpha for blending
    float totalAlpha = min(1.0, length(orbColor) * 0.8);
    fragColor = vec4(orbColor, totalAlpha);
}