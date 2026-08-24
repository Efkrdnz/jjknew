#version 150

uniform float Time;
uniform float Intensity;

in vec2 texCoord;
out vec4 fragColor;

float hash(vec2 p) {
    p = fract(p * vec2(127.1, 311.7));
    p += dot(p, p + 19.19);
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

float ring(float d, float radius, float width) {
    return smoothstep(radius + width, radius, d) * (1.0 - smoothstep(radius, radius - width, d));
}

void main() {
    vec2 p = texCoord - vec2(0.5);
    p.x *= 1.08;
    float d = length(p);
    float a = atan(p.y, p.x);

    float spinFast = a * 5.0 - Time * 11.0;
    float spinSlow = a * -3.0 + Time * 4.0;
    float inward = d * 46.0 - Time * 18.0;

    float mask = smoothstep(0.52, 0.42, d);
    float hardCore = smoothstep(0.115, 0.045, d);
    float darkCore = smoothstep(0.16, 0.055, d);
    float accretion = ring(d, 0.245 + sin(Time * 2.0) * 0.006, 0.045);
    float outerRing = ring(d, 0.365, 0.035);

    float armA = sin(spinFast + inward) * 0.5 + 0.5;
    float armB = sin(spinSlow + d * 34.0) * 0.5 + 0.5;
    float spiral = pow(armA, 4.0) * smoothstep(0.10, 0.42, d) * mask;
    float counterSpiral = pow(armB, 5.0) * smoothstep(0.14, 0.48, d) * mask * 0.55;

    float grit = noise(vec2(a * 9.0 + Time * 0.7, d * 28.0 - Time * 7.0));
    float sparks = smoothstep(0.82, 0.97, grit) * smoothstep(0.18, 0.44, d) * mask;

    vec3 blackBlue = vec3(0.0, 0.015, 0.08);
    vec3 deepBlue = vec3(0.015, 0.10, 0.42);
    vec3 blue = vec3(0.04, 0.36, 1.0);
    vec3 cyan = vec3(0.48, 0.92, 1.0);
    vec3 whiteHot = vec3(0.82, 0.97, 1.0);

    vec3 color = vec3(0.0);
    color += deepBlue * mask * smoothstep(0.50, 0.08, d) * 0.85;
    color += blue * spiral * 2.6;
    color += cyan * counterSpiral * 1.7;
    color += cyan * accretion * 3.3;
    color += blue * outerRing * 1.25;
    color += whiteHot * hardCore * 2.2;
    color += cyan * sparks * 1.45;

    float pulse = 0.88 + 0.12 * sin(Time * 8.0);
    color *= pulse * Intensity;
    color = mix(color, blackBlue, darkCore * 0.55);
    color += whiteHot * hardCore * 0.9;

    float alpha = mask * 0.08;
    alpha += spiral * 0.85;
    alpha += counterSpiral * 0.55;
    alpha += accretion * 0.95;
    alpha += outerRing * 0.45;
    alpha += hardCore * 0.88;
    alpha += sparks * 0.65;
    alpha *= mask * Intensity;
    alpha = clamp(alpha, 0.0, 1.0);

    fragColor = vec4(color, alpha);
}
