#version 150

uniform sampler2D Sampler0;

uniform float Time;
uniform float RingWidth;
uniform float Intensity;
uniform vec3 RingColor;

in vec2 vUv;
in vec4 vColor;
in float vProgress;

out vec4 fragColor;

float sat(float x) { return clamp(x, 0.0, 1.0); }

void main() {
    vec4 tex = texture(Sampler0, vUv);

    // convert uv (0..1) -> centered (-1..1)
    vec2 p = vUv * 2.0 - 1.0;
    float r = length(p);

    // radius expands with progress
    float radius = mix(0.05, 1.15, sat(vProgress));

    // ring mask
    float w = max(RingWidth, 0.0001);
    float inner = smoothstep(radius - w, radius, r);
    float outer = 1.0 - smoothstep(radius, radius + w, r);
    float ring = inner * outer;

    // punchy ripple detail
    float ripple = 0.5 + 0.5 * sin((r - radius) * 80.0 - Time * 18.0);
    ripple = mix(0.7, 1.3, ripple);

    // fade out as it expands (use progress)
    float fade = (1.0 - sat(vProgress));
    fade = fade * fade;

    // final alpha (use texture alpha as a mask)
    float a = ring * ripple * fade * tex.a * Intensity;

    // additive-ish bright core
    vec3 col = RingColor * (1.6 + 1.2 * ripple);

    fragColor = vec4(col, a);
}