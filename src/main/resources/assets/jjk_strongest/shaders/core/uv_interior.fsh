#version 150

uniform float Time;
uniform float BrushSeed;      // per-domain seed
uniform float Intensity;
uniform float Radius;         // the domain's real radius, in blocks
uniform float Progress;       // 0..1 through the current phase
uniform float Phase;          // DomainPhase ordinal: 0 expanding, 1 settling, 2 active, 3 collapsing
uniform vec3  CamOffset;      // camera relative to the sphere centre, in blocks
uniform float Integrity;      // whole-barrier integrity, 0..1
uniform float HasShell;       // 1 once ShellSampler holds real data
uniform float FloorY;         // floor plane relative to the centre, in blocks
uniform float Inside;         // 1 when the camera is within the shell
uniform vec3  BhDir;          // unit camera -> black hole
uniform vec2  BhAng;          // (angular radius, distance)
uniform vec3  BhAxis;         // accretion disc normal
uniform float DiscStrength;   // 0 takes the disc away entirely
uniform float Surface;        // 0 the dome, 1 the floor disc
uniform float RippleData[64]; // 16 x (dx, dz, birth seconds, strength), see RippleField
uniform sampler2D ShellSampler; // 32x16 per-direction integrity, matching DomainShell

in vec2 texCoord;
in vec3 localPos;   // domain-local, in blocks — the true point on whichever surface this is
out vec4 fragColor;

const float PI = 3.14159265359;
const int RIPPLES = 16;

// ---- palette ---------------------------------------------------------------
// Ink and bone carry every edge. Blue means depth and light; violet is the receding side of
// the disc and nothing else. Brighter than the version before it — the room is a sea under a
// lit horizon now, not a cave — but still a field of dark with hard white marks on it.
const vec3 INK      = vec3(0.008, 0.009, 0.014);
const vec3 BONE     = vec3(0.92, 0.93, 0.96);
const vec3 SKY_LOW  = vec3(0.020, 0.028, 0.070);
const vec3 SKY_HIGH = vec3(0.035, 0.055, 0.140);
const vec3 SEA_DEEP = vec3(0.020, 0.028, 0.070);
const vec3 ABYSS    = vec3(0.004, 0.005, 0.012);
const vec3 PALEBLUE = vec3(0.52, 0.66, 0.90);
const vec3 HORIZON  = vec3(0.62, 0.74, 0.95);
const vec3 VIOLET   = vec3(0.45, 0.35, 0.80);

// ---- noise -----------------------------------------------------------------

float hash11(float p) {
    p = fract(p * 0.1031);
    p *= p + 33.33;
    p *= p + p;
    return fract(p);
}

vec2 random2(vec2 p) {
    return fract(sin(vec2(dot(p, vec2(127.1, 311.7)), dot(p, vec2(269.5, 183.3)))) * 43758.5453);
}

float hash13(vec3 p) {
    p = fract(p * 0.1031);
    p += dot(p, p.yzx + 33.33);
    return fract((p.x + p.y) * p.z);
}

float noise3(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);
    float n000 = hash13(i);
    float n100 = hash13(i + vec3(1.0, 0.0, 0.0));
    float n010 = hash13(i + vec3(0.0, 1.0, 0.0));
    float n110 = hash13(i + vec3(1.0, 1.0, 0.0));
    float n001 = hash13(i + vec3(0.0, 0.0, 1.0));
    float n101 = hash13(i + vec3(1.0, 0.0, 1.0));
    float n011 = hash13(i + vec3(0.0, 1.0, 1.0));
    float n111 = hash13(i + vec3(1.0, 1.0, 1.0));
    float nx00 = mix(n000, n100, f.x);
    float nx10 = mix(n010, n110, f.x);
    float nx01 = mix(n001, n101, f.x);
    float nx11 = mix(n011, n111, f.x);
    return mix(mix(nx00, nx10, f.y), mix(nx01, nx11, f.y), f.z);
}

/** Three octaves. The old shader ran four, five times over, and averaged to grey mush. */
float fbm3(vec3 p) {
    float sum = 0.0;
    float amp = 0.5;
    for (int i = 0; i < 3; i++) {
        sum += noise3(p) * amp;
        p *= 2.03;
        amp *= 0.5;
    }
    return sum;
}

/** A stable direction per index, for the ink blot sites. */
vec3 hashDir(float i, float seed) {
    float a = hash11(i * 1.37 + seed) * 2.0 - 1.0;
    float b = hash11(i * 2.91 + seed * 1.7 + 5.0) * PI * 2.0;
    float r = sqrt(max(0.0, 1.0 - a * a));
    return vec3(r * cos(b), a, r * sin(b));
}

// ---- barrier damage --------------------------------------------------------
// Unchanged in behaviour from the shader this replaces. It is the one part of the
// interior that is gameplay rather than decoration: a cell driven to zero is a hole
// collision will actually let you walk through, and the cracks are how you find it.

/**
 * Distance to the nearest Voronoi *boundary* — F2 minus F1.
 *
 * The previous version returned F1, the distance to the nearest seed point, and the cracks
 * were thresholded near zero. F1 is near zero *at the points*, so that drew a small disc
 * around every seed: a field of dots, not a fracture network. F2 - F1 goes to zero exactly
 * where the two nearest seeds are equidistant, which is the boundary between their cells,
 * so thresholding it gives connected lines.
 *
 * Both minima come out of the same nine samples, so this costs nothing over what it replaces.
 * Line width varies with the angle between neighbouring cells, which for fracture is closer
 * to how glass actually breaks than a uniform-width alternative would be.
 */
float voronoiEdge(vec2 uv) {
    vec2 cell = floor(uv);
    vec2 f = fract(uv);
    float first = 8.0;
    float second = 8.0;
    for (int j = -1; j <= 1; j++) {
        for (int i = -1; i <= 1; i++) {
            vec2 neighbour = vec2(float(i), float(j));
            vec2 point = random2(cell + neighbour);
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

/**
 * White fracture on the barrier.
 *
 * Two lattices rather than one, and deliberately not harmonics of each other: a coarse
 * network that carries the break, and a finer web inside it that only shows up once a patch
 * is badly hurt. That is what makes cracks look like they branched outward from a hit rather
 * than like tiling.
 */
float shatterMask(vec2 uv, float localDamage, float globalDamage) {
    // x2 on u undoes the equirectangular stretch so shards stay roughly square
    vec2 aspect = vec2(2.0, 1.0);

    // Sharper lines on fresh damage, broadening as a patch is worn down — a new hit reads as
    // a tight star, a beaten one as a white web.
    float width = mix(0.020, 0.075, clamp(localDamage, 0.0, 1.0));

    float coarse = voronoiEdge(uv * 14.0 * aspect);
    float crack = (1.0 - smoothstep(0.0, width, coarse)) * smoothstep(0.04, 0.45, localDamage);

    float fine = voronoiEdge(uv * 34.0 * aspect + 11.7);
    crack = max(crack, (1.0 - smoothstep(0.0, width * 0.7, fine)) * smoothstep(0.35, 0.9, localDamage));

    // The whole-shell web, driven by overall integrity rather than by this patch, so a
    // barrier being pressed evenly from every side crazes over as a piece.
    float shell = voronoiEdge(uv * 6.0 * aspect);
    crack = max(crack, (1.0 - smoothstep(0.0, 0.055, shell)) * globalDamage * globalDamage);

    return clamp(crack, 0.0, 1.0);
}

// ---- the black hole --------------------------------------------------------

/**
 * How far a ray observed at this angle is bent around the hole.
 *
 * Einstein deflection is 2Rs/b, i.e. exactly 1/b to leading order, so the cheap 1/r warp
 * everyone uses is not an approximation of the shape — it is the shape. What it does not
 * give you is the photon ring or multiple images, so the ring is drawn explicitly below.
 */
float deflection(float ang) {
    return (1.6 * BhAng.x * BhAng.x) / max(ang, BhAng.x * 0.5);
}

// ---- sky layers ------------------------------------------------------------

/**
 * L1: two star layers, bone-white, one slow twinkle. The threshold is a parameter because
 * the reflection is deliberately given more of them than the sky.
 */
vec3 stars(vec3 d, float t, float threshold) {
    float fine = noise3(d * 90.0 + BrushSeed);
    float coarse = noise3(d * 38.0 - BrushSeed);
    float twinkle = 0.75 + 0.25 * sin(t * 1.3 + fine * 40.0);
    vec3 col = BONE * step(threshold, fine) * 0.55 * twinkle;
    col += BONE * step(threshold - 0.013, coarse) * smoothstep(threshold - 0.013, 0.995, coarse) * 1.15;
    return col;
}

/**
 * L2: one tilted dust band.
 *
 * The layer the old interior was missing entirely, and the one that creates depth. A
 * galactic plane gives the eye a horizon to read the volume against; isotropic noise at
 * five scales gives it nothing.
 */
vec3 dustBand(vec3 d, float t) {
    vec3 normal = normalize(vec3(sin(t * 0.013), 0.82, cos(t * 0.011)));
    float band = exp(-abs(dot(d, normal)) * 3.5);
    float grain = fbm3(d * 2.2 + vec3(t * 0.012, 0.0, -t * 0.009) + BrushSeed);
    return mix(SKY_HIGH, PALEBLUE, 0.35) * band * grain * 0.54;
}

/**
 * L4: hard-edged ink blots, and small black flecks.
 *
 * The signature layer, and the whole reason this reads like the reference rather than
 * fog. Twelve discs would be twelve discs; displacing every edge by ONE shared warp field
 * turns them into twelve ragged islands that share a paper texture, which is what the
 * source images actually look like.
 */
void inkBlots(vec3 d, float warp, out float blot, out float fleck) {
    float wobble = (warp - 0.5) * 0.045;
    blot = 0.0;
    fleck = 0.0;
    for (int i = 0; i < 12; i++) {
        float fi = float(i);
        vec3 site = hashDir(fi, BrushSeed);
        float a = 1.0 - dot(d, site);
        float rad = 0.010 + 0.055 * hash11(fi + 7.3);
        blot = max(blot, smoothstep(rad + 0.004, rad - 0.004, a + wobble));
    }
    for (int i = 0; i < 8; i++) {
        float fi = float(i) + 40.0;
        // Biased toward the hole, so a cluster silhouettes against the photon ring.
        vec3 site = normalize(hashDir(fi, BrushSeed) + BhDir * 1.4);
        float a = 1.0 - dot(d, site);
        float rad = 0.0025 + 0.010 * hash11(fi + 3.1);
        fleck = max(fleck, smoothstep(rad + 0.002, rad - 0.002, a + wobble * 0.5));
    }
}

/** L5: broad lens arcs swept around the hole's axis. Ties the field to the centre. */
float lensArcs(float ang, float warp) {
    float arcs = 0.0;
    for (int i = 0; i < 3; i++) {
        float r = BhAng.x * (3.5 + float(i) * 2.6);
        arcs += exp(-pow((ang - r + (warp - 0.5) * 0.06) / (BhAng.x * 0.9), 2.0));
    }
    return arcs;
}

/**
 * The photon rings, the shadow's bright edge.
 *
 * The n=1 ring is the one everybody sees; it gets a chromatic fringe by being drawn at three
 * slightly different radii into the three channels, which is what light of three colours
 * bent by three slightly different amounts looks like. Inside it, a thinner and fainter n=2
 * ring — the second image of the sky — and outside, a soft halo.
 */
vec3 photonRings(float ang) {
    float r1 = BhAng.x * 1.05;
    float w1 = BhAng.x * 0.10;
    vec3 ring1 = vec3(exp(-pow((ang - r1 * 0.99) / w1, 2.0)), exp(-pow((ang - r1) / w1, 2.0)), exp(-pow((ang - r1 * 1.01) / w1, 2.0))) * 4.0;
    float ring2 = exp(-pow((ang - BhAng.x * 1.015) / (BhAng.x * 0.035), 2.0)) * 1.6;
    float halo = exp(-pow((ang - BhAng.x * 1.6) / (BhAng.x * 0.55), 2.0)) * 0.35;
    ring1 += exp(-pow((ang - BhAng.x * 1.40) / (BhAng.x * 0.30), 2.0)) * 0.8;
    return BONE * (ring1 + ring2) + PALEBLUE * halo;
}

/**
 * One image of the accretion disc, seen along a (bent) ray.
 *
 * @param L      the lensed direction the ray actually came from
 * @param ang    angle from the hole's centre, unbent
 * @param e1,e2  a basis in the disc plane, for the texture's azimuth
 */
vec3 discImage(vec3 L, float ang, vec3 e1, vec3 e2, float t) {
    float axial = dot(L, BhAxis);
    // Thickness in proportion to the hole, so it stays a thin disc at any distance.
    float thin = exp(-pow(axial / (BhAng.x * 0.33), 2.0));
    float span = smoothstep(BhAng.x * 1.2, BhAng.x * 2.0, ang) * (1.0 - smoothstep(BhAng.x * 4.5, BhAng.x * 8.0, ang));
    if (thin * span < 0.002)
        return vec3(0.0);

    vec3 inPlane = L - BhAxis * axial;
    float len = length(inPlane);
    vec3 p = inPlane / max(len, 1e-5);
    // Orbital velocity is axis x position. Its component toward the eye — along -BhDir — is
    // the Doppler term: the side coming at you is beamed brighter and bluer, the side going
    // away is dim and violet.
    float doppler = -dot(cross(BhAxis, p), BhDir);
    float beam = 1.0 + 0.9 * doppler;
    vec3 hot = mix(VIOLET, mix(BONE, PALEBLUE, 0.35), 0.5 + 0.5 * doppler);
    // Gravitational redshift: the inner edge, nearest the shadow, is the dimmest.
    float redshift = smoothstep(1.2, 2.2, ang / BhAng.x);
    // Differential rotation: the inner disc turns faster than the outer.
    float az = atan(dot(p, e2), dot(p, e1));
    float spin = t * 1.2 * BhAng.x / max(ang, BhAng.x);
    float texture = 0.55 + 0.45 * fbm3(vec3(ang * 26.0, az * 3.0 - spin * 3.0, t * 0.35));
    return hot * thin * span * texture * beam * redshift;
}

/**
 * Information streams: fine dotted filaments spiralling into the hole.
 *
 * The one layer that says what this domain is made of. Six log-spiral arms in the plane you
 * are looking across, each a string of flecks drifting inward — and, in the reflection,
 * outward.
 */
float infoStreams(float ang, float phi, float t) {
    float u = ang / BhAng.x;
    float window = smoothstep(1.4, 2.2, u) * (1.0 - smoothstep(9.0, 12.0, u));
    if (window <= 0.0)
        return 0.0;
    float lu = log(u);
    float s = fract(phi * 6.0 / (2.0 * PI) + lu * 2.4 - t * 0.05);
    float line = 1.0 - smoothstep(0.0, 0.05, min(s, 1.0 - s));
    float along = lu * 30.0 + t * 1.5;
    float bead = smoothstep(0.30, 0.48, abs(fract(along) - 0.5));
    return line * bead * window;
}

/**
 * Everything that lives at infinity, evaluated on a possibly-bent ray.
 *
 * Split out because the floor reflects exactly this and nothing else — reflecting the
 * marched volume as well would double the cost of every floor fragment for something you
 * cannot see in a dark mirror anyway.
 *
 * @param mirror true when this is the sea's reflection. The reflection is given more than
 *               the sky has: denser stars, a brighter horizon, and information streams that
 *               flow the other way. Nothing else differs, so it reads as wrong rather than
 *               as broken — the water shows you more than the sky does.
 */
vec3 skyAnalytic(vec3 d, float t, bool mirror) {
    // L0: a lit sea-sky. Deep navy low, a shade lighter overhead, warmed toward the hole,
    // and a horizon at eye level whatever the room's actual size — the thing that makes
    // thirty blocks read as an endless shore.
    vec3 col = mix(SKY_LOW, SKY_HIGH, smoothstep(-0.2, 0.9, d.y));
    col += SKY_HIGH * 0.8 * pow(max(0.0, dot(d, BhDir)), 6.0);
    float horizon = exp(-abs(d.y) * 9.0) * 0.55 + exp(-abs(d.y) * 2.5) * 0.12;
    col += HORIZON * horizon * (mirror ? 1.25 : 1.0);

    float c = dot(d, BhDir);
    vec3 tangent = d - BhDir * c;
    float s = length(tangent);
    float ang = atan(s, c);   // two-argument: precision is worst exactly at the ring
    vec3 tangentDir = s > 1e-5 ? tangent / s : vec3(0.0);

    vec3 lensed = d;
    if (s > 1e-5)
        lensed = normalize(d + tangentDir * deflection(ang));

    // Stars pile up where the shadow is stretching them into a ring.
    float einstein = 1.0 + 2.0 * exp(-pow((ang - 1.3 * BhAng.x) / (0.25 * BhAng.x), 2.0));
    col += stars(lensed, t, mirror ? 0.978 : 0.984) * einstein;
    col += dustBand(lensed, t);

    // One shared warp field for both the blot edges and the arcs: it is what makes them
    // look like they were drawn on the same paper, and it saves evaluating it twice.
    float warp = fbm3(d * 5.5 + BrushSeed + t * 0.02);
    float blot;
    float fleck;
    inkBlots(lensed, warp, blot, fleck);
    col = mix(col, BONE, blot * 0.92);
    col = mix(col, INK, fleck);

    col += PALEBLUE * lensArcs(ang, warp) * 0.10;

    // A basis around the line of sight to the hole, for anything that needs an azimuth.
    vec3 ref = abs(BhDir.y) < 0.9 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0);
    vec3 e1 = normalize(cross(BhDir, ref));
    vec3 e2 = cross(BhDir, e1);
    float phi = atan(dot(tangentDir, e2), dot(tangentDir, e1));
    col += BONE * 0.35 * infoStreams(ang, phi, mirror ? -t : t);

    // The hole itself, last, over everything it swallows.
    float aa = fwidth(ang) + 1e-4;
    col += photonRings(ang);

    if (DiscStrength > 0.001) {
        // The disc's own basis: d1 lies in the disc plane and across the line of sight.
        vec3 d1 = cross(BhAxis, BhDir);
        if (dot(d1, d1) < 1e-6)
            d1 = cross(BhAxis, vec3(1.0, 0.0, 0.0));
        d1 = normalize(d1);
        vec3 d2 = cross(BhAxis, d1);
        // The near image, on the lensed ray, and the far side of the disc bent over and
        // under the shadow: the same disc seen along a ray thrown across the plane and bent
        // harder. That second image is the pair of arcs the shape is known for.
        vec3 disc = discImage(lensed, ang, d1, d2, t);
        vec3 across = d - 2.0 * BhAxis * dot(tangent, BhAxis);
        vec3 farside = normalize(across + tangentDir * deflection(ang) * 1.6);
        disc += discImage(farside, ang, d1, d2, t) * 0.55 * smoothstep(BhAng.x * 1.05, BhAng.x * 1.6, ang);
        col += disc * DiscStrength * 0.9;
    }

    // The event horizon has no gradient in the reference and should not have one here.
    col *= smoothstep(BhAng.x - aa, BhAng.x + aa, ang);
    return col;
}

/** L3: the only marched layer. Three steps, thresholded hard so it has edges, not haze. */
vec3 volume(vec3 d, float t, out float alpha) {
    vec3 acc = vec3(0.0);
    alpha = 0.0;
    float scale = 30.0 / max(Radius, 1.0);
    for (int i = 0; i < 3; i++) {
        float fi = float(i);
        vec3 p = d * (2.4 + fi * 0.85) * scale + BrushSeed;
        p += vec3(t * 0.02, -t * 0.015, t * 0.018);
        float density = fbm3(p * 1.15);
        // Hard threshold: contrast is what reads as volume, not step count.
        density = smoothstep(0.52, 0.78, density);
        float w = exp(-fi * 0.42);
        vec3 tint = mix(mix(INK, BONE, density), SKY_HIGH + PALEBLUE * 0.25, fi * 0.3);
        float a = density * 0.30 * w;
        acc += tint * a * (1.0 - alpha);
        alpha += a * (1.0 - alpha);
    }
    return acc;
}

// ---- the sea ---------------------------------------------------------------

/**
 * Height of the water at a point on the floor, from the footstep rings.
 *
 * Each ripple is a damped sine travelling outward at a walking pace, gone within a few
 * seconds. {@code crest} is the wavefront itself, for the one bright line the floor draws.
 * Sixteen slots, most of them empty most of the time, and the empty ones cost a compare.
 */
float rippleHeight(vec2 p, float t, out float crest) {
    float h = 0.0;
    crest = 0.0;
    for (int i = 0; i < RIPPLES; i++) {
        float strength = RippleData[i * 4 + 3];
        if (strength <= 0.0)
            continue;
        float age = t - RippleData[i * 4 + 2];
        if (age <= 0.0)
            continue;
        vec2 c = vec2(RippleData[i * 4], RippleData[i * 4 + 1]);
        float r = length(p - c);
        float front = age * 2.6;
        float amp = strength * 0.06 * exp(-age * 0.9) * smoothstep(0.0, 0.4, age);
        float dr = r - front;
        h += amp * sin(dr * 6.0) * exp(-abs(dr) * 1.2);
        crest = max(crest, amp * exp(-dr * dr * 9.0));
    }
    return h;
}

/**
 * The floor: black lacquer that is mostly a mirror.
 *
 * It reflects the sky — and, drawn a moment before it, the room — and nothing on it is hard
 * white except the crests of the rings you make walking on it. It is translucent, so the
 * mirrored entities beneath show through in proportion to how flat you are looking at it,
 * which is how a wet surface actually behaves. Because what is under it is the abyss, the
 * colour is divided back out by the alpha; otherwise the most reflective angles would be
 * the dimmest.
 */
vec4 shadeFloor(vec3 hit, float t, float phaseFade) {
    vec3 toHit = hit - CamOffset;
    float dist = length(toHit);
    vec3 dir = toHit / max(dist, 1e-4);

    float crest;
    float h = rippleHeight(hit.xz, t, crest);
    float unusedX;
    float unusedZ;
    float hx = rippleHeight(hit.xz + vec2(0.05, 0.0), t, unusedX);
    float hz = rippleHeight(hit.xz + vec2(0.0, 0.05), t, unusedZ);
    vec3 n = normalize(vec3(-(hx - h) / 0.05, 1.0, -(hz - h) / 0.05));

    vec3 m = reflect(dir, n);
    m.y = max(m.y, 0.02);   // a wave never shows you what is under the horizon
    vec3 refl = skyAnalytic(normalize(m), t, true);

    float facing = abs(dot(dir, n));
    float fresnel = mix(0.18, 0.85, pow(1.0 - facing, 4.0));
    vec3 col = SEA_DEEP + refl * fresnel;
    col += BONE * crest * 6.0;

    // The shore: the last stretch of sea dissolves into the horizon's light, so the disc
    // and the dome meet in brightness rather than along a line.
    float discR = sqrt(max(Radius * Radius - FloorY * FloorY, 1.0));
    float edge = smoothstep(0.92, 1.0, length(hit.xz) / discR);
    col = mix(col, HORIZON * 0.9, edge);

    float reflectivity = mix(0.30, 0.72, pow(1.0 - abs(dir.y), 3.0));
    float alpha = mix(1.0 - reflectivity, 1.0, edge);
    return vec4(col / max(alpha, 0.25) * Intensity, alpha * phaseFade);
}

// ---- main ------------------------------------------------------------------

void main() {
    float t = Time;

    // Fade the whole thing in as the shell opens and out as it goes.
    float phaseFade = 1.0;
    if (Phase < 0.5)
        phaseFade = smoothstep(0.0, 0.65, Progress);
    else if (Phase > 2.5) {
        // Gone by 55%, while the shards are still in the air, so the back half of the
        // collapse is real world seen through a cloud of glass.
        phaseFade = 1.0 - smoothstep(0.05, 0.55, Progress);
    }

    // The floor disc, before anything that assumes it is on the sphere. It has no shell
    // damage — the barrier is the dome — and no facing test: it fades with the phase and
    // is otherwise always drawn, whichever way its triangles happen to face.
    if (Surface > 0.5) {
        fragColor = shadeFloor(localPos, t, phaseFade);
        return;
    }

    // From the first collapse frame the shell is broken, so stop drawing it as a closed
    // surface. Keeping the opaque outer face would fire the shard pass off inside a black
    // sphere and you would see none of it. The interior, seen from within, still fades out
    // on phaseFade above.
    if (Phase > 2.5 && (!gl_FrontFacing || Inside < 0.5))
        discard;

    // The damage grid is keyed on the mesh's own equirectangular UV, not on the view ray:
    // the cracks belong where the barrier was hit, not wherever you happen to be looking.
    float cellIntegrity = mix(1.0, texture(ShellSampler, texCoord).r, HasShell);
    float localDamage = 1.0 - cellIntegrity;
    float globalDamage = 1.0 - mix(1.0, Integrity, HasShell);
    // Gated. Undamaged, this was eighteen voronoi samples per fragment contributing zero.
    float shatter = 0.0;
    if (max(localDamage, globalDamage) > 0.02)
        shatter = shatterMask(texCoord, localDamage, globalDamage);
    float hole = smoothstep(0.12, 0.0, cellIntegrity) * HasShell;

    // Domain-local blocks, straight from the vertex stage. The version before this treated
    // localPos as a unit vector and scaled it by Radius, which made every view ray a ray
    // from the sphere's centre rather than from the eye: no parallax.
    vec3 surf = localPos;

    if (!gl_FrontFacing) {
        // Seen from outside: an all but opaque black sphere with white shatter on it.
        vec3 outward = normalize(surf);
        vec3 toEye = normalize(CamOffset - surf);
        float rim = pow(1.0 - abs(dot(outward, toEye)), 3.5);
        vec3 outer = mix(vec3(0.004, 0.004, 0.012), vec3(0.16, 0.22, 0.38), rim);
        outer += BONE * shatter * (0.85 + 1.7 * max(localDamage, globalDamage));
        // Fully opaque, so the near and far hemispheres cannot bleed through one another
        // in a render type that does not sort.
        fragColor = vec4(outer * Intensity, (1.0 - hole) * phaseFade);
        return;
    }

    vec3 toSurf = surf - CamOffset;
    float tSphere = length(toSurf);
    vec3 dir = toSurf / max(tSphere, 1e-4);

    if (Inside < 0.5) {
        // The far hemisphere, while the camera is outside. It is about to be painted over
        // by the near one; running the whole interior for it is pure waste.
        fragColor = vec4(INK * Intensity, 0.85 * phaseFade * (1.0 - hole));
        return;
    }

    // Under the plane there is only the abyss: it is what the sea is translucent over, and
    // the pit walls behind it must never show. Skipping the sky here is also most of what
    // the floor pass costs, given back.
    if (surf.y < FloorY) {
        fragColor = vec4(ABYSS * Intensity, phaseFade);
        return;
    }

    vec3 col = skyAnalytic(dir, t, false);
    float volumeAlpha;
    col += volume(dir, t, volumeAlpha);
    col += BONE * shatter * 0.45;

    float alpha = clamp(0.72 + volumeAlpha * 0.28, 0.0, 1.0) * phaseFade * (1.0 - hole);
    fragColor = vec4(col * Intensity, alpha);
}
