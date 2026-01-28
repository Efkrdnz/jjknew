#version 150

uniform sampler2D SceneSampler;
uniform vec2 OutSize;
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
    
    float gravityStrength = smoothstep(Radius * 2.0, 0.0, dist) * Intensity;
    
    float angle = atan(toCenter.y, toCenter.x);
    float spiralAngle = angle + gravityStrength * 3.14159 * sin(Time * 2.0);
    float spiralDist = dist * (1.0 - gravityStrength * 0.3);
    
    vec2 spiralOffset = vec2(
        cos(spiralAngle) * spiralDist,
        sin(spiralAngle) * spiralDist
    );
    
    float aberration = gravityStrength * DistortStrength * 0.02;
    vec2 distortUV = center + spiralOffset;
    
    float r = texture(SceneSampler, distortUV + toCenter * aberration * 1.2).r;
    float g = texture(SceneSampler, distortUV + toCenter * aberration).g;
    float b = texture(SceneSampler, distortUV - toCenter * aberration * 0.8).b;
    
    float edgeNoise = noise(uv * 10.0 + Time * 2.0) * 0.5 + 0.5;
    float edgeGlow = smoothstep(Radius * 1.5, Radius * 0.8, dist) * 
                     (1.0 - smoothstep(Radius * 0.8, Radius * 0.4, dist));
    
    vec3 purpleTint = vec3(0.6, 0.2, 1.0) * edgeGlow * edgeNoise * Intensity * 0.3;
    
    float darken = smoothstep(Radius * 0.5, 0.0, dist) * Intensity * 0.6;
    
    vec3 finalColor = vec3(r, g, b) + purpleTint;
    finalColor = mix(finalColor, vec3(0.1, 0.0, 0.2), darken);
    
    fragColor = vec4(finalColor, 1.0);
}