#version 150
uniform mat4 ProjMat;
uniform float Time;
uniform float ChargeProgress;
in vec2 texCoord;
out vec4 fragColor;

// hash for noise
float hash(vec2 p) {
    p = fract(p * vec2(123.34, 456.21));
    p += dot(p, p + 45.32);
    return fract(p.x * p.y);
}

// noise function
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

// fractal brownian motion
float fbm(vec2 p) {
    float value = 0.0;
    float amplitude = 0.5;
    for (int i = 0; i < 5; i++) {
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
    
    // simple radial distance - no arrow shape, just circular
    float dist = length(toCenter);
    float angle = atan(toCenter.y, toCenter.x);
    
    // rotating fire
    float rotation = Time * 2.5;
    vec2 rotUV = vec2(
        toCenter.x * cos(rotation) - toCenter.y * sin(rotation),
        toCenter.x * sin(rotation) + toCenter.y * cos(rotation)
    );
    
    // animated fire layers
    vec2 fireUV1 = rotUV * 3.0;
    fireUV1.y -= Time * 2.0;
    float fire1 = fbm(fireUV1);
    
    vec2 fireUV2 = uv * 4.0;
    fireUV2 += Time * vec2(1.5, -1.0);
    float fire2 = fbm(fireUV2);
    
    // turbulent swirls
    float swirl = fbm(vec2(angle * 2.5 + Time * 3.0, dist * 7.0));
    
    // concentric layers - all radial, no directional bias
    float coreGlow = smoothstep(0.35, 0.08, dist);
    float innerFire = smoothstep(0.45, 0.15, dist) * fire1;
    float outerFire = smoothstep(0.52, 0.20, dist) * fire2;
    float edgeFlames = smoothstep(0.58, 0.25, dist) * swirl;
    
    // hot center
    float hotSpot = smoothstep(0.12, 0.02, dist);
    
    // color gradient - fire colors
    vec3 darkRed = vec3(0.7, 0.15, 0.0);
    vec3 deepOrange = vec3(1.0, 0.35, 0.0);
    vec3 brightOrange = vec3(1.0, 0.65, 0.1);
    vec3 yellow = vec3(1.0, 0.95, 0.4);
    vec3 white = vec3(1.0, 1.0, 1.0);
    
    // build fire from center outward
    vec3 finalColor = vec3(0.0);
    
    // hottest core
    finalColor += white * hotSpot * 1.3;
    finalColor += yellow * coreGlow * 1.0;
    
    // inner flames
    finalColor += mix(brightOrange, yellow, fire1) * innerFire;
    
    // middle flames
    finalColor += mix(deepOrange, brightOrange, fire2) * outerFire;
    
    // outer wispy flames
    finalColor += mix(darkRed, deepOrange, swirl) * edgeFlames * 0.7;
    
    // pulsing
    float pulse = 0.88 + sin(Time * 5.5) * 0.08 + ChargeProgress * 0.12;
    finalColor *= pulse;
    
    // radial alpha falloff - smooth on all sides
    float alpha = hotSpot * 0.9 + coreGlow * 0.7 + innerFire * 0.6 + outerFire * 0.5 + edgeFlames * 0.4;
    alpha *= smoothstep(0.6, 0.35, dist); // soft outer edge
    alpha = clamp(alpha, 0.0, 1.0);
    
    fragColor = vec4(finalColor, alpha);
}