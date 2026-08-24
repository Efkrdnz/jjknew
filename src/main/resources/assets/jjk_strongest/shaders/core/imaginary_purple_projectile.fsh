#version 150
uniform mat4 ProjMat;
uniform float Time;
uniform float Intensity;
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
    
    // intense white-cyan core
    float coreSize = 0.04;
    float whiteCore = smoothstep(coreSize + 0.015, coreSize * 0.3, dist);
    whiteCore = pow(whiteCore, 0.5);
    
    // electric rays shooting out
    float rayCount = 24.0;
    float rayAngle = angle + Time * 2.0;
    float rayPattern = abs(sin(rayAngle * rayCount * 0.5));
    rayPattern = pow(rayPattern, 0.3);
    
    // chaotic ray length variations
    float rayNoise = noise(vec2(angle * 10.0, Time * 10.0));
    float rayLength = 0.18 + rayNoise * 0.12;
    float rays = smoothstep(rayLength, 0.0, dist) * rayPattern;
    rays *= smoothstep(0.02, 0.06, dist);
    
    // lightning bolts
    float boltNoise = noise(vec2(angle * 15.0 + Time * 15.0, dist * 50.0));
    float bolts = step(0.82, boltNoise) * smoothstep(0.25, 0.04, dist);
    bolts *= noise(vec2(Time * 25.0, angle * 8.0)) * 0.5 + 0.5;
    
    // purple corona around white core
    float corona = smoothstep(0.2, 0.03, dist) * smoothstep(0.015, 0.07, dist);
    
    // outer purple glow
    float outerGlow = smoothstep(0.35, 0.08, dist) * 0.5;
    
    // chaotic energy swirls
    float swirl = sin(angle * 8.0 + dist * 35.0 - Time * 12.0) * 0.5 + 0.5;
    float swirlMask = smoothstep(0.28, 0.12, dist) * smoothstep(0.06, 0.14, dist) * swirl * 0.4;
    
    // electric sparkles
    float sparkles = noise(uv * 220.0 + Time * 30.0);
    sparkles = step(0.965, sparkles) * smoothstep(0.3, 0.0, dist) * 0.9;
    
    // color palette
    vec3 white = vec3(1.0, 1.0, 1.0);
    vec3 cyan = vec3(0.7, 0.9, 1.0);
    vec3 purple = vec3(0.6, 0.2, 0.9);
    vec3 purpleDark = vec3(0.4, 0.1, 0.6);
    
    // build explosion color
    vec3 sparkColor = vec3(0.0);
    sparkColor += white * whiteCore * 3.5;
    sparkColor += cyan * rays * 1.8;
    sparkColor += white * bolts * 2.2;
    sparkColor += purple * corona * 1.4;
    sparkColor += purpleDark * outerGlow;
    sparkColor += purple * swirlMask;
    sparkColor += cyan * sparkles * 1.8;
    
    // intense flickering
    float flicker = 0.75 + noise(vec2(Time * 22.0)) * 0.25;
    sparkColor *= flicker;
    
    // violent pulse
    float pulse = 0.8 + sin(Time * 15.0) * 0.2;
    sparkColor *= pulse;
    
    // apply intensity
    sparkColor *= Intensity;
    
    // alpha
    float totalAlpha = whiteCore + rays * 0.7 + bolts * 0.8 + corona * 0.6 + outerGlow * 0.4 + sparkles * 0.5;
    totalAlpha = min(1.0, totalAlpha * 1.3);
    
    fragColor = vec4(sparkColor, totalAlpha);
}