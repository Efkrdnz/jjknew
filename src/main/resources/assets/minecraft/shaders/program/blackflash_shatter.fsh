#version 150

uniform sampler2D DiffuseSampler;
uniform float Intensity;
uniform float Time;

in vec2 texCoord;
out vec4 fragColor;

// generate random value
float random(vec2 st) {
    return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453123);
}

// generate random 2d vector
vec2 random2(vec2 st) {
    return vec2(
        fract(sin(dot(st, vec2(12.9898, 78.233))) * 43758.5453),
        fract(sin(dot(st, vec2(39.3468, 11.135))) * 43758.5453)
    );
}

// create voronoi cells for glass shards
float voronoi(vec2 uv, out vec2 cellOffset) {
    vec2 i = floor(uv);
    vec2 f = fract(uv);
    
    float minDist = 1.0;
    vec2 minPoint = vec2(0.0);
    
    for(int y = -1; y <= 1; y++) {
        for(int x = -1; x <= 1; x++) {
            vec2 neighbor = vec2(float(x), float(y));
            vec2 point = random2(i + neighbor);
            vec2 diff = neighbor + point - f;
            float dist = length(diff);
            
            if(dist < minDist) {
                minDist = dist;
                minPoint = point + neighbor;
                cellOffset = diff;
            }
        }
    }
    
    return minDist;
}

void main() {
    vec2 uv = texCoord;
    vec2 center = vec2(0.5, 0.5);
    
    // radial distance from center for falloff
    float distFromCenter = length(uv - center);
    float radialFalloff = smoothstep(0.8, 0.2, distFromCenter);
    
    // create glass shard cells (smaller cells = more shards)
    float cellScale = 12.0 + Time * 3.0;
    vec2 shatterUV = uv * cellScale;
    vec2 cellOffset;
    float cellDist = voronoi(shatterUV, cellOffset);
    
    // random displacement per shard
    vec2 cellId = floor(shatterUV);
    vec2 randomDisplace = (random2(cellId) - 0.5) * 2.0;
    
    // displacement strength based on intensity and radial falloff
    float displaceStrength = Intensity * 0.04 * radialFalloff;
    vec2 displacement = randomDisplace * displaceStrength;
    
    // add extra chaos with time
    displacement += (random2(cellId + Time) - 0.5) * displaceStrength * 0.3;
    
    // sample texture with displacement
    vec4 color = texture(DiffuseSampler, uv + displacement);
    
    // create crack lines at cell edges
    float edgeWidth = 0.02;
    float edge = smoothstep(edgeWidth, edgeWidth * 2.0, cellDist);
    
    // darken cracks
    float crackDarkness = 1.0 - (1.0 - edge) * Intensity * 0.6;
    color.rgb *= crackDarkness;
    
    // slight vignette
    float vignette = smoothstep(1.2, 0.3, distFromCenter);
    color.rgb *= mix(0.8, 1.0, vignette);
    
    // add subtle color shift on displacement (black/red theme)
    float displaceMag = length(displacement);
    color.r += displaceMag * Intensity * 2.0;
    color.gb *= 1.0 - displaceMag * Intensity * 0.3;
    
    fragColor = color;
}