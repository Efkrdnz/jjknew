#version 150

in vec3 Position;   // the quad's own corner: entityCentre + unit sphere, see main()
in vec2 UV0;        // the quad's CENTRE uv — see below

uniform mat4 ProjMat;
uniform mat4 ModelViewMat;
uniform float Radius;
uniform float Progress;          // 0..1 through the collapse
uniform float CollapseSeconds;   // how long the whole collapse lasts
uniform vec3 BreakDir;           // where the shell gave way first
uniform vec3 CamOffset;          // camera position, domain-local

out vec2 texCoord;      // the true equirectangular uv, for the damage grid
out vec3 shardNormal;   // this shard's outward normal, tumbled with it
out vec3 worldPos;      // domain-local position after the break, for the rim term
out float shardFade;
out float shardEdge;

const float PI = 3.14159265359;
const float TAU = 6.28318530718;

/**
 * Shard clustering. MUST match the constant of the same name in uv_shards.fsh — the
 * geometry tears along these cells and the fragment stage draws their edges, so if the two
 * disagree the bright lines land somewhere other than the breaks.
 */
const vec2 SHARD_CELLS = vec2(24.0, 12.0);

vec2 random2(vec2 p) {
    return fract(sin(vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)))) * 43758.5453);
}

float hash11(float p) {
    p = fract(p * 0.1031);
    p *= p + 33.33;
    p *= p + p;
    return fract(p);
}

vec3 hashDir3(vec2 site) {
    float a = hash11(site.x * 3.17 + site.y * 7.31) * 2.0 - 1.0;
    float b = hash11(site.x * 5.71 + site.y * 2.13 + 4.0) * TAU;
    float r = sqrt(max(0.0, 1.0 - a * a));
    return normalize(vec3(r * cos(b), a, r * sin(b)) + vec3(1e-4));
}

/** Direction of a point given as an equirectangular uv. Exactly inverts buildUnitSphere. */
vec3 dirOfUv(vec2 uv) {
    float th = uv.y * PI;
    float ph = uv.x * TAU;
    return vec3(sin(th) * cos(ph), cos(th), sin(th) * sin(ph));
}

/**
 * Which shard this quad belongs to.
 *
 * <p>Assigned from the quad's CENTRE, which is the whole reason UV0 carries the centre
 * rather than the corner: all four corners must get the same answer or the shell tears
 * along quad edges instead of along shard edges. The lattice wraps in u, or the seam column
 * never joins up — invisible on a static shell, very visible on a piece flying away.
 */
vec2 shardSite(vec2 uv) {
    vec2 scaled = uv * SHARD_CELLS;
    vec2 cell = floor(scaled);
    vec2 f = fract(scaled);
    vec2 best = vec2(0.0);
    float bestD = 8.0;
    for (int j = -1; j <= 1; j++) {
        for (int i = -1; i <= 1; i++) {
            vec2 neighbour = vec2(float(i), float(j));
            vec2 id = cell + neighbour;
            vec2 wrapped = vec2(mod(id.x, SHARD_CELLS.x), id.y);
            vec2 point = random2(wrapped);
            float d = length(neighbour + point - f);
            if (d < bestD) {
                bestD = d;
                best = id + point;
            }
        }
    }
    return best;
}

void main() {
    vec2 centreUv = UV0;

    // Position is NOT the unit sphere. The renderer bakes its PoseStack into the vertex on
    // the CPU, and the entity render dispatcher has already translated that stack to the
    // domain's camera-relative position — which is exactly what makes poseStack.translate
    // place the ink cards in blocks. So Position is (entityCentre + unit), and every bit of
    // sphere maths below has to run on the unit part or it reads a point that has nothing
    // to do with the sphere: atan would return a near-constant angle, the pole test would
    // never fire, and the burst would start 30x too far out.
    vec3 entityCentre = -CamOffset;
    vec3 corner = Position - entityCentre;

    // The true uv of THIS corner, recovered analytically. buildUnitSphere emits
    // (sin(th)cos(ph), cos(th), sin(th)sin(ph)) from th = v*PI, ph = u*TAU, so acos and
    // atan invert it exactly.
    vec2 uvV;
    if (abs(corner.y) > 0.99999) {
        // Both poles have x = z = 0, where atan(0, 0) is undefined. A NaN varying takes the
        // whole triangle with it, silently, and 128 vertices sit exactly here.
        uvV = vec2(centreUv.x, corner.y > 0.0 ? 0.0 : 1.0);
    } else {
        float u = atan(corner.z, corner.x) / TAU;
        if (u < 0.0)
            u += 1.0;
        // The quad straddling u = 1|0 gets corners on both sides; pull them back together.
        if (u - centreUv.x > 0.5)
            u -= 1.0;
        if (centreUv.x - u > 0.5)
            u += 1.0;
        uvV = vec2(u, acos(clamp(corner.y, -1.0, 1.0)) / PI);
    }
    texCoord = uvV;

    vec2 site = shardSite(centreUv);
    vec3 anchor = dirOfUv(centreUv);

    float h0 = hash11(site.x * 1.37 + site.y * 4.11);
    float h1 = hash11(site.x * 2.91 + site.y * 1.77 + 3.0);
    float h2 = hash11(site.x * 5.13 + site.y * 3.29 + 9.0);
    float h3 = hash11(site.x * 7.77 + site.y * 6.01 + 17.0);

    float T = max(0.05, CollapseSeconds);
    float t = Progress * T;

    // The side that broke lets go first, so the failure propagates instead of the whole
    // shell firing at once.
    float lean = 0.5 + 0.5 * dot(anchor, BreakDir);
    float t0 = T * (0.02 + 0.13 * h0) * (1.0 - 0.75 * lean);
    float ts = max(0.0, t - t0);

    // Burst outward with drag, so pieces settle to a reach instead of accelerating forever.
    float v0 = 3.5 + 3.5 * h1 + 3.0 * lean;
    float k = 1.7;
    float travel = v0 * (1.0 - exp(-k * ts)) / k;

    // Then gravity takes them. This is what makes it a thing breaking rather than a thing
    // dissolving.
    float g = 11.0;
    vec3 sideways = normalize(cross(anchor, hashDir3(site + 17.0)) + vec3(1e-5));
    vec3 disp = anchor * travel + sideways * (h2 - 0.5) * travel * 0.5 + vec3(0.0, -0.5 * g * ts * ts, 0.0);

    // Tumble about a random axis through the shard's own anchor. Rodrigues written out;
    // there is no rotation helper in GLSL 150.
    vec3 axis = hashDir3(site);
    float a = (h3 - 0.5) * 5.0 * ts;
    float c = cos(a);
    float sn = sin(a);
    vec3 r = corner * Radius - anchor * Radius;
    vec3 rot = r * c + cross(axis, r) * sn + axis * dot(axis, r) * (1.0 - c);
    vec3 n = anchor * c + cross(axis, anchor) * sn + axis * dot(axis, anchor) * (1.0 - c);

    vec3 local = anchor * Radius + rot + disp;
    worldPos = local;
    shardNormal = normalize(n);

    // The fade has to reach zero before the phase does. Progress pins at exactly 1.0 from
    // when the collapse clock finishes until the terrain restore catches up, which is
    // routinely several seconds — anything still visible at 1.0 hangs motionless in the air.
    float fadeStart = 0.30 + 0.25 * h0;
    shardFade = 1.0 - smoothstep(fadeStart, fadeStart + 0.40, Progress);
    // Fracture faces glow while they are fresh and dull as the piece tumbles away.
    shardEdge = exp(-ts * 2.2);

    // Back into the space the vertex arrived in: local is domain-local blocks, and the
    // entity offset that was baked out at the top has to be baked back in.
    gl_Position = ProjMat * ModelViewMat * vec4(local + entityCentre, 1.0);
}
