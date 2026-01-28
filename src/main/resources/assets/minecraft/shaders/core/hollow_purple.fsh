#version 150

uniform sampler2D SceneSampler;
uniform mat4 ProjMat;
uniform float Time;
uniform float Intensity;
uniform float Radius;
uniform float DistortStrength;

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
    
    // smooth distortion falloff - much gentler fade
    float distortionRange = 0.5; // how far distortion reaches
    float distortionFalloff = smoothstep(distortionRange, 0.0, dist);
    distortionFalloff = distortionFalloff * distortionFalloff; // extra smoothing
    
    // only apply distortion if within range
    vec2 sampleUV = uv;
    if (distortionFalloff > 0.001) {
        float angle = atan(toCenter.y, toCenter.x);
        float spiralAngle = angle + distortionFalloff * 3.14159 * sin(Time * 2.0);
        float spiralDist = dist * (1.0 - distortionFalloff * 0.2);
        
        vec2 spiralOffset = vec2(
            cos(spiralAngle) * spiralDist,
            sin(spiralAngle) * spiralDist
        );
        
        float aberration = distortionFalloff * DistortStrength * 0.015;
        sampleUV = center + spiralOffset;
        
        // chromatic aberration with smooth blending
        float r = texture(SceneSampler, sampleUV + toCenter * aberration * 1.2).r;
        float g = texture(SceneSampler, sampleUV + toCenter * aberration).g;
        float b = texture(SceneSampler, sampleUV - toCenter * aberration * 0.8).b;
        
        vec3 distortedScene = vec3(r, g, b);
        vec3 normalScene = texture(SceneSampler, uv).rgb;
        
        // blend distorted and normal scene for ultra-smooth transition
        float blendFactor = smoothstep(distortionRange * 0.9, distortionRange * 0.6, dist);
        vec3 sceneColor = mix(normalScene, distortedScene, blendFactor);
        
    } else {
        // no distortion, use normal scene
        vec3 sceneColor = texture(SceneSampler, uv).rgb;
    }
    
    vec3 sceneColor = texture(SceneSampler, sampleUV).rgb;
    
    // create purple sphere
    float sphereRadius = 0.15; // core sphere size
    float glowRadius = 0.25;   // outer glow size
    
    // core sphere (solid purple)
    float coreMask = smoothstep(sphereRadius + 0.02, sphereRadius, dist);
    vec3 coreColor = vec3(0.8, 0.4, 1.0); // bright purple
    
    // animated energy waves
    float wave1 = sin(dist * 20.0 - Time * 3.0) * 0.5 + 0.5;
    float wave2 = sin(dist * 15.0 + Time * 2.0) * 0.5 + 0.5;
    float energyPattern = (wave1 * 0.5 + wave2 * 0.5);
    
    // pulsing brightness
    float pulse = sin(Time * 2.0) * 0.2 + 0.8;
    coreColor *= (1.0 + energyPattern * 0.3) * pulse;
    
    // outer glow layers with smoother falloff
    float glow1 = smoothstep(glowRadius + 0.05, sphereRadius, dist) * 0.8;
    float glow2 = smoothstep(glowRadius * 1.5 + 0.05, sphereRadius, dist) * 0.4;
    float glow3 = smoothstep(glowRadius * 2.0 + 0.1, sphereRadius, dist) * 0.2;
    
    vec3 glowColor1 = vec3(0.7, 0.3, 1.0); // purple glow
    vec3 glowColor2 = vec3(0.6, 0.2, 0.9); // darker purple
    vec3 glowColor3 = vec3(0.5, 0.1, 0.8); // outermost glow
    
    // bright center core
    float centerGlow = smoothstep(0.05, 0.0, dist);
    vec3 centerColor = vec3(1.0, 0.9, 1.0); // white-purple center
    
    // combine all layers
    vec3 finalColor = sceneColor;
    
    // add outer glows (farthest first)
    finalColor = mix(finalColor, glowColor3, glow3);
    finalColor = mix(finalColor, glowColor2, glow2);
    finalColor = mix(finalColor, glowColor1, glow1);
    
    // add core sphere
    finalColor = mix(finalColor, coreColor, coreMask);
    
    // add bright center
    finalColor = mix(finalColor, centerColor, centerGlow);
    
    // add sparkles
    float sparkleNoise = noise(uv * 100.0 + Time * 5.0);
    float sparkle = step(0.98, sparkleNoise) * smoothstep(glowRadius * 1.2, 0.0, dist);
    finalColor += vec3(1.0, 0.8, 1.0) * sparkle * 0.5;
    
    // smooth alpha fade at very edges to prevent hard cutoff
    float edgeFade = smoothstep(0.5, 0.4, dist);
    
    fragColor = vec4(finalColor, edgeFade);
}