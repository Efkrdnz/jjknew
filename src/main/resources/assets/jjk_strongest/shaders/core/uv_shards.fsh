#version 150

uniform float Time;
uniform float BrushSeed;
uniform float Intensity;
uniform float Radius;
uniform float Progress;
uniform float CollapseSeconds;
uniform vec3 BreakDir;
uniform vec3 CamOffset;
uniform float Integrity;
uniform float HasShell;
uniform sampler2D ShellSampler;

in vec2 texCoord;
in vec3 shardNormal;
in vec3 worldPos;
in float shardFade;
in float shardEdge;
out vec4 fragColor;

const vec3 BONE = vec3(0.92, 0.93, 0.96);

/** MUST match uv_shards.vsh — the geometry tears on these cells and this draws their edges. */
const vec2 SHARD_CELLS = vec2(24.0, 12.0);

vec2 random2(vec2 p) {
    return fract(sin(vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)))) * 43758.5453);
}

/** F2 - F1: zero on the boundary between the two nearest cells, so it draws lines. */
float voronoiEdge(vec2 uv) {
    vec2 cell = floor(uv);
    vec2 f = fract(uv);
    float first = 8.0;
    float second = 8.0;
    for (int j = -1; j <= 1; j++) {
        for (int i = -1; i <= 1; i++) {
            vec2 neighbour = vec2(float(i), float(j));
            vec2 id = cell + neighbour;
            vec2 point = random2(vec2(mod(id.x, SHARD_CELLS.x), id.y));
            float d = length(neighbour + point - f);
            if (d < first) {
                second = first;
                first = d;
            } else if (d < second) {
                second = d;
            }
        }
    }
    return second - first;
}

float shatterMask(vec2 uv, float localDamage, float globalDamage) {
    vec2 aspect = vec2(2.0, 1.0);
    float width = mix(0.020, 0.075, clamp(localDamage, 0.0, 1.0));
    float coarse = voronoiEdge(uv * 14.0 * aspect);
    float crack = (1.0 - smoothstep(0.0, width, coarse)) * smoothstep(0.04, 0.45, localDamage);
    float shell = voronoiEdge(uv * 6.0 * aspect);
    crack = max(crack, (1.0 - smoothstep(0.0, 0.055, shell)) * globalDamage * globalDamage);
    return clamp(crack, 0.0, 1.0);
}

/**
 * One piece of the broken barrier.
 *
 * <p>Deliberately the same material as the outer shell it used to be part of — near-black
 * with a faint rim and white shatter — because it <em>is</em> that shell, an instant later.
 * What is new is the fracture face: the shard's own boundary, lit while the break is fresh
 * and dulling as the piece tumbles away. That edge is what makes a dark piece read as glass
 * rather than as a hole in the world.
 */
void main() {
    // texCoord can leave 0..1 after the vertex stage de-seams it, and DomainShellTexture
    // never sets a wrap mode, so fract it rather than trusting the default.
    vec2 uv = vec2(fract(texCoord.x), clamp(texCoord.y, 0.0, 1.0));

    float cellIntegrity = mix(1.0, texture(ShellSampler, uv).r, HasShell);
    float localDamage = 1.0 - cellIntegrity;
    float globalDamage = 1.0 - mix(1.0, Integrity, HasShell);
    // A shard over a hole is holed too. Better than omitting the piece: a whole missing
    // plate reads as a rendering fault, a holed one reads as the damage that caused it.
    float hole = smoothstep(0.12, 0.0, cellIntegrity) * HasShell;

    float shatter = 0.0;
    if (max(localDamage, globalDamage) > 0.02)
        shatter = shatterMask(uv, localDamage, globalDamage);

    vec3 toEye = normalize(CamOffset - worldPos);
    float rim = pow(1.0 - abs(dot(shardNormal, toEye)), 3.5);
    vec3 col = mix(vec3(0.004, 0.004, 0.012), vec3(0.16, 0.22, 0.38), rim);
    col += BONE * shatter * (0.85 + 1.7 * max(localDamage, globalDamage));

    float edge = 1.0 - smoothstep(0.0, 0.055, voronoiEdge(uv * SHARD_CELLS));
    col += BONE * edge * (0.35 + 1.4 * shardEdge);

    float alpha = shardFade * (1.0 - hole);
    if (alpha < 0.004)
        discard;
    fragColor = vec4(col * Intensity, alpha);
}
