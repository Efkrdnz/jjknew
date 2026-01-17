#version 150

uniform sampler2D DiffuseSampler;
uniform float DarkenAmount;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec3 color = texture(DiffuseSampler, texCoord).rgb;
    
    // simple darken - lerp towards black
    color = mix(color, vec3(0.0), DarkenAmount);
    
    fragColor = vec4(color, 1.0);
}