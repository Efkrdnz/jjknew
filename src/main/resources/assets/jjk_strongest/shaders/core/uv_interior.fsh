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
/**
 * How far the domain has finished arriving, 0..1. Zero for the whole forming beat — the
 * room is black while the shell closes and the rays burst — then climbing once the domain
 * turns hostile. The void fades in over the first quarter of it and the splashes land one
 * by one across the rest. Computed in DomainUVRenderer so the shader knows no tick counts.
 */
uniform float Reveal;
uniform float RippleData[64]; // 16 x (dx, dz, birth seconds, strength), see RippleField
uniform sampler2D ShellSampler; // 32x16 per-direction integrity, matching DomainShell

in vec2 texCoord;
in vec3 localPos;   // domain-local, in blocks — the true point on whichever surface this is
out vec4 fragColor;

const float PI = 3.14159265359;
const int RIPPLES = 16;

// ---- palette ---------------------------------------------------------------
// Deep space over a lit shore. Bone is stars, ring and crests; blue is the horizon, the
// Milky Way and the disc's cool side; violet is the disc's receding side and nothing else.
// Nothing white is painted on the sky any more — the paper blots went with the room they
// implied.
const vec3 INK      = vec3(0.008, 0.009, 0.014);
const vec3 BONE     = vec3(0.92, 0.93, 0.96);
const vec3 SPACE    = vec3(0.004, 0.005, 0.012);
const vec3 SKY_LOW  = vec3(0.020, 0.028, 0.070);
const vec3 SEA_DEEP = vec3(0.020, 0.028, 0.070);
const vec3 ABYSS    = vec3(0.004, 0.005, 0.012);
const vec3 PALEBLUE = vec3(0.52, 0.66, 0.90);
const vec3 HORIZON  = vec3(0.62, 0.74, 0.95);
const vec3 VIOLET   = vec3(0.45, 0.35, 0.80);

// ---- knobs -----------------------------------------------------------------
// The two switches are the revert: set either to false and the feature is gone, and the
// compiler folds its code away with it. The numbers are what they were tuned to.

/** Fraction of star cells that carry a star. Was 0.45 before the sky was thinned. */
const float STAR_FILL = 0.26;

/** One deep-blue cloud that drifts slowly around the sky. */
const bool  DRIFT_NEBULA = true;
/** Cobalt, on purpose: the horizon and the disc already own light blue. */
const vec3  NEBULA_BLUE  = vec3(0.05, 0.14, 0.60);
const vec3  NEBULA_CORE  = vec3(0.16, 0.30, 0.82);
/** Radians per second about the vertical. A full lap of the sky in about seventeen minutes. */
const float NEBULA_DRIFT = 0.006;

/** A glowing gold ring just outside the photon ring. */
const bool  GOLD_RING = true;
const vec3  GOLD      = vec3(1.0, 0.82, 0.28);

/**
 * Whether the black hole appears in the sea's reflection.
 *
 * False takes the whole of it out of the water — shadow, photon ring, gold ring, both disc
 * images, the plane glow, the information streams, and the lensing that bends everything
 * else around it — so the sea reflects stars, galaxy and nebulae over an unbent sky, and
 * the giant hangs in the air with nothing under it. Set true to put it back.
 */
const bool  BH_IN_REFLECTION = false;

/**
 * White paint on the barrier: how many, and the Reveal by which the last has landed.
 *
 * They are on the wall, so the sea does not carry them — the sea reflects the sky, and the
 * marks are not in the sky. Set SPLASHES to 0 to take them away entirely.
 */
const int   SPLASHES = 14;
const float SPLASH_START = 0.25;
const float SPLASH_END = 1.0;

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

/** A stable direction per index. Recovered from the ink blots this replaces. */
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
// The hole is at infinity: BhDir is a direction, BhAng.x the angular radius of its shadow,
// BhAxis the normal of its disc. Nothing here depends on where you stand, which is what
// makes a thirty-block room contain something the size of a sky.

/** Disc extent, in units of tan(shadow radius): from just outside the ISCO to well past it. */
const float DISC_INNER = 1.5;
const float DISC_OUTER = 3.2;

/**
 * How far a ray observed at this angle is bent around the hole. Leading-order deflection is
 * 1/b, so the cheap warp is the shape; the constant sets how hard the ring pulls.
 */
float deflection(float ang) {
    return (1.9 * BhAng.x * BhAng.x) / max(ang, 0.7 * BhAng.x);
}

/**
 * The accretion disc at a point on its plane, seen along a ray.
 *
 * @param Q     the hit point, with the hole at BhDir (unit distance)
 * @param view  the direction the ray was travelling when it hit — bent for the far image
 */
vec3 discShade(vec3 Q, vec3 view, float t) {
    vec3 rel = Q - BhDir;
    float r = length(rel);
    float rs = tan(BhAng.x);
    float rIn = DISC_INNER * rs;
    float rOut = DISC_OUTER * rs;
    if (r < rIn * 0.9 || r > rOut)
        return vec3(0.0);
    vec3 radial = rel / max(r, 1e-5);

    vec3 e1 = cross(BhAxis, BhDir);
    if (dot(e1, e1) < 1e-6)
        e1 = cross(BhAxis, vec3(1.0, 0.0, 0.0));
    e1 = normalize(e1);
    vec3 e2 = cross(BhAxis, e1);
    float theta = atan(dot(radial, e2), dot(radial, e1));

    float rn = r / rIn;
    float bright = pow(1.0 / rn, 1.6);
    float edges = smoothstep(rIn * 0.9, rIn * 1.15, r) * (1.0 - smoothstep(rOut * 0.8, rOut, r));
    // Keplerian: the inner disc laps the outer. Periodic in theta by construction, so there
    // is no seam where the angle wraps.
    float rot = theta + t * 0.6 / pow(rn, 1.5);
    float streak = 0.45 + 0.55 * fbm3(vec3(r / rs * 6.0 + t * 0.05, 2.5 * cos(rot), 2.5 * sin(rot)));
    // Orbital velocity is axis x position; its component toward the eye is the Doppler
    // term. Relativistic beaming goes as the cube, so one side is genuinely hot.
    vec3 v = cross(BhAxis, radial);
    float toward = dot(v, -view);
    float beam = pow(clamp(1.0 + 0.6 * toward, 0.2, 2.0), 3.0);
    float radialMix = clamp((r - rIn) / (rOut - rIn), 0.0, 1.0);
    vec3 col = mix(vec3(1.0, 0.96, 0.90), vec3(0.75, 0.82, 1.0), smoothstep(0.0, 0.45, radialMix));
    col = mix(col, vec3(0.30, 0.32, 0.70), smoothstep(0.35, 1.0, radialMix));
    col = mix(col, VIOLET, clamp(-toward, 0.0, 1.0) * 0.5);
    col = mix(col, vec3(1.0), clamp(toward, 0.0, 1.0) * 0.25);
    // Gravitational redshift: the inner edge, nearest the shadow, is the dimmest.
    float redshift = smoothstep(rIn * 0.9, rIn * 1.4, r);
    return col * bright * edges * streak * beam * redshift;
}

/**
 * Information streams: fine dotted filaments spiralling into the hole.
 *
 * The one layer that says what this domain is made of. Six log-spiral arms around the
 * giant, each a string of flecks drifting inward — and, in the reflection, outward.
 */
float infoStreams(float ang, float phi, float t) {
    float u = ang / BhAng.x;
    float window = smoothstep(1.25, 1.5, u) * (1.0 - smoothstep(3.0, 3.5, u));
    if (window <= 0.0)
        return 0.0;
    float lu = log(u);
    float s = fract(phi * 6.0 / (2.0 * PI) + lu * 2.4 - t * 0.05);
    float line = 1.0 - smoothstep(0.0, 0.05, min(s, 1.0 - s));
    float along = lu * 30.0 + t * 1.5;
    float bead = smoothstep(0.30, 0.48, abs(fract(along) - 0.5));
    return line * bead * window;
}

// ---- sky layers ------------------------------------------------------------

/** Cube-map face and its uv for a direction, so a star grid can be laid on the sky. */
vec2 faceUv(vec3 d, out float face) {
    vec3 a = abs(d);
    if (a.x >= a.y && a.x >= a.z) {
        face = d.x > 0.0 ? 0.0 : 1.0;
        return d.yz / a.x;
    }
    if (a.y >= a.z) {
        face = d.y > 0.0 ? 2.0 : 3.0;
        return d.xz / a.y;
    }
    face = d.z > 0.0 ? 4.0 : 5.0;
    return d.xy / a.z;
}

/**
 * One layer of round stars.
 *
 * A cell grid on each cube face, at most one star per cell at a hashed offset, drawn as a
 * disc. Round points are most of what makes a sky read as space rather than as speckle; the
 * old threshold-on-noise gave speckle. Stars keep off the cell edges so a face seam cannot
 * cut one in half.
 */
vec3 starLayer(vec3 d, float cells, float seed, float t) {
    float face;
    vec2 uv = (faceUv(d, face) * 0.5 + 0.5) * cells;
    vec2 cell = floor(uv);
    vec2 f = uv - cell;
    vec2 id = cell + face * 131.0 + seed;
    float h = hash11(dot(id, vec2(12.9898, 78.233)) + seed);
    // Most cells are empty. STAR_FILL is the one knob for how busy the sky is, and it thins
    // every layer at once, the Milky Way's included.
    float empty = 1.0 - STAR_FILL;
    if (h < empty)
        return vec3(0.0);
    vec2 pos = random2(id) * 0.7 + 0.15;
    float mag = (h - empty) / STAR_FILL;
    float radius = 0.035 + 0.11 * mag * mag;
    float star = smoothstep(radius, radius * 0.25, length(f - pos));
    float twinkle = 0.85 + 0.15 * sin(t * (0.8 + 1.7 * h) + h * 40.0);
    float pick = hash11(h * 91.7 + seed);
    vec3 tint = pick < 0.6 ? BONE : (pick < 0.85 ? PALEBLUE : vec3(1.0, 0.86, 0.70));
    return tint * star * (0.35 + 0.9 * mag) * twinkle;
}

/** The galaxy seen edge-on: a tilted band, grained, cut by dark dust lanes, thick with stars. */
vec3 milkyWay(vec3 d, float t) {
    vec3 normal = normalize(vec3(0.42, 0.78, -0.46));
    float band = exp(-pow(dot(d, normal) * 3.2, 2.0));
    if (band < 0.002)
        return vec3(0.0);
    float grain = fbm3(d * 3.0 + BrushSeed);
    float lane = smoothstep(0.45, 0.60, fbm3(d * 6.0 + 4.7 + BrushSeed * 0.3));
    vec3 col = mix(vec3(0.10, 0.13, 0.28), vec3(0.55, 0.60, 0.80), grain) * band * (1.0 - 0.75 * lane) * 0.5;
    col += starLayer(d, 140.0, 7.0, t) * band * 0.8;
    return col;
}

/** Two faint clouds, blue and violet, kept away from the hole so it stays clean. */
vec3 nebulae(vec3 d, float ang) {
    float away = smoothstep(2.0 * BhAng.x, 3.0 * BhAng.x, ang);
    float a = smoothstep(0.50, 0.85, fbm3(d * 2.2 + BrushSeed + 3.1));
    float b = smoothstep(0.52, 0.86, fbm3(d * 1.9 - BrushSeed + 9.4));
    return (vec3(0.12, 0.08, 0.30) * a + vec3(0.05, 0.15, 0.30) * b) * 0.35 * away;
}

/**
 * One cobalt cloud, a hand's width, that drifts around the sky.
 *
 * The direction is turned about the vertical before sampling, so the cloud moves along the
 * horizon at a fixed height — slowly enough that you notice it has moved rather than that
 * it is moving. Dark filaments through it are what make it read as gas rather than a spot,
 * and it stands off the hole so the disc keeps its own colours. Switched by DRIFT_NEBULA.
 */
vec3 driftNebula(vec3 d, float ang, float t) {
    float a = t * NEBULA_DRIFT;
    vec3 dr = vec3(d.x * cos(a) - d.z * sin(a), d.y, d.x * sin(a) + d.z * cos(a));
    vec3 home = normalize(vec3(0.62, 0.55, 0.56));
    float core = smoothstep(0.72, 0.97, dot(dr, home));
    if (core <= 0.0)
        return vec3(0.0);
    float body = fbm3(dr * 3.4 + BrushSeed * 0.7 + 21.0);
    float wisp = fbm3(dr * 9.0 - BrushSeed * 0.4 + 5.0);
    float dens = smoothstep(0.40, 0.80, body) * (0.55 + 0.45 * wisp);
    float lane = 1.0 - 0.6 * smoothstep(0.50, 0.62, wisp);
    float away = smoothstep(1.2 * BhAng.x, 2.0 * BhAng.x, ang);
    vec3 col = NEBULA_BLUE * core * dens * lane * 0.9;
    col += NEBULA_CORE * pow(core, 4.0) * dens * 0.25;
    return col * away;
}

/**
 * Everything that lives at infinity, evaluated for a view ray.
 *
 * The background is sampled on the ray as the hole bends it, so stars and galaxy stretch
 * into a ring around the shadow on their own. The disc is a real thin disc at the hole's
 * plane: the ray runs straight to its closest approach, bends there, and whatever it hits
 * on the far side is the far image — the arcs over and under the shadow fall out of that.
 * The near side of the disc is hit before the bend and is drawn last, over the shadow,
 * because the front of the disc really does cross in front of the hole.
 *
 * @param mirror true when this is the sea's reflection. The reflection is given more than
 *               the sky has: a third layer of stars, a brighter horizon, and information
 *               streams that flow the other way. Nothing else differs, so it reads as wrong
 *               rather than as broken — the water shows you more than the sky does. Unless
 *               BH_IN_REFLECTION is off, in which case it is also given rather less: no
 *               black hole at all.
 */
vec3 skyAnalytic(vec3 d, float t, bool mirror) {
    // Everything the hole does is gated on this one flag, so taking it out of the water
    // takes ALL of it — there is no path that draws half a black hole.
    bool showHole = !mirror || BH_IN_REFLECTION;

    float c = dot(d, BhDir);
    vec3 tangent = d - BhDir * c;
    float s = length(tangent);
    float ang = atan(s, c);   // two-argument: precision is worst exactly at the ring
    vec3 tangentDir = s > 1e-5 ? tangent / s : vec3(0.0);
    // No hole, no lensing: the reflected sky is sampled along the ray it came in on.
    vec3 bent = showHole ? normalize(d + tangentDir * deflection(ang)) : d;
    // The nebulae hold off the hole by angle. With no hole there is nothing to hold off,
    // and an angle past every threshold is how you say that without a second code path.
    float maskAng = showHole ? ang : 1.0e3;

    // The void arrives over the first quarter of the reveal. Gating it here rather than at
    // the call sites means the dome and the sea agree for nothing: a black room has a black
    // mirror, and both bloom on the same beat.
    float voidIn = smoothstep(0.0, 0.25, Reveal);
    if (voidIn <= 0.001) {
        // Forming. Near-black, with a slow breath in it so the dark reads as something
        // loading rather than as a shader that has failed.
        float pulse = 0.5 + 0.5 * sin(t * 1.6 - fbm3(d * 1.8) * 6.0);
        return SPACE * (0.6 + 0.8 * pulse);
    }

    // L0: black overhead, a lift toward the horizon, and the horizon itself — the shore.
    vec3 col = mix(SPACE, SKY_LOW, exp(-max(d.y, 0.0) * 4.0) * 0.6);
    float horizon = exp(-abs(d.y) * 9.0) * 0.55 + exp(-abs(d.y) * 2.5) * 0.12;
    col += HORIZON * horizon * (mirror ? 1.25 : 1.0);

    // L1-L3: the deep field, on the bent ray.
    col += starLayer(bent, 64.0, 1.0, t) * 0.9;
    col += starLayer(bent, 20.0, 2.0, t) * 1.6;
    if (mirror)
        col += starLayer(bent, 44.0, 5.0, t) * 0.8;
    col += milkyWay(bent, t);
    col += nebulae(bent, maskAng);
    if (DRIFT_NEBULA)
        col += driftNebula(bent, maskAng, t);

    if (!showHole)
        return col * voidIn;

    // A basis around the line of sight to the hole, for anything that needs an azimuth.
    vec3 ref = abs(BhDir.y) < 0.9 ? vec3(0.0, 1.0, 0.0) : vec3(1.0, 0.0, 0.0);
    vec3 e1 = normalize(cross(BhDir, ref));
    vec3 e2 = cross(BhDir, e1);
    float phi = atan(dot(tangentDir, e2), dot(tangentDir, e1));
    col += BONE * 0.25 * infoStreams(ang, phi, mirror ? -t : t);

    // The shadow: everything at infinity is behind it. No gradient at the edge.
    float aa = fwidth(ang) + 1e-4;
    float shadow = smoothstep(BhAng.x - aa, BhAng.x + aa, ang);
    col *= shadow;

    if (DiscStrength > 0.001) {
        // Far image: from the closest-approach point — the point on the VIEW ray nearest the
        // hole, d*c, not a point on the hole's own axis — along the bent ray, onto the plane.
        vec3 C = d * c;
        float denomF = dot(bent, BhAxis);
        if (abs(denomF) > 1e-4) {
            float tf = dot(BhDir - C, BhAxis) / denomF;
            if (tf > 0.0)
                col += discShade(C + bent * tf, bent, t) * DiscStrength * shadow;
        }
        // A soft glow in the disc's plane, the only bloom there is without a post pass.
        float planeBand = exp(-pow(dot(tangent, BhAxis) / (BhAng.x * 0.6), 2.0));
        float planeWindow = smoothstep(BhAng.x, BhAng.x * 1.3, ang) * (1.0 - smoothstep(BhAng.x * 2.5, BhAng.x * 4.0, ang));
        col += PALEBLUE * planeBand * planeWindow * 0.12 * DiscStrength;
    }

    // The photon ring, hot and thin, with a glow falling off outside the shadow.
    float ring = exp(-pow((ang - BhAng.x * 1.02) / (BhAng.x * 0.02), 2.0)) * 2.5;
    float glow = exp(-max(ang - BhAng.x, 0.0) / (BhAng.x * 0.25)) * 0.35;
    col += mix(BONE, PALEBLUE, 0.15) * (ring + glow) * shadow;

    if (GOLD_RING) {
        // A second, gold ring a little outside the photon ring: a sharp core with a wide
        // soft halo, which is as much glow as there is without a post pass. Under the near
        // disc image like the photon ring, so the front of the disc crosses over it.
        float gr = BhAng.x * 1.14;
        float goldCore = exp(-pow((ang - gr) / (BhAng.x * 0.025), 2.0)) * 1.8;
        float goldHalo = exp(-pow((ang - gr) / (BhAng.x * 0.14), 2.0)) * 0.45;
        col += GOLD * (goldCore + goldHalo) * shadow;
    }

    if (DiscStrength > 0.001) {
        // Near image: the unbent ray meets the plane before closest approach.
        float denomN = dot(d, BhAxis);
        if (abs(denomN) > 1e-4) {
            float tn = dot(BhDir, BhAxis) / denomN;
            if (tn > 0.0 && tn < c)
                col += discShade(d * tn, d, t) * DiscStrength;
        }
    }
    return col * voidIn;
}

/**
 * White paint thrown at the inside of the barrier.
 *
 * <p>Two things make this read as paint on the wall rather than as a mark on the sky. The
 * first is the argument: {@code wall} is the direction of the SURFACE POINT from the centre,
 * not the view ray. A splash is therefore pinned to its patch of barrier and you walk past
 * it, where anything keyed on the view ray would sit at infinity and follow you around. The
 * second is that every rim is displaced by one shared warp field, which is what made the ink
 * this replaces read as marks on a single sheet rather than as a dozen circles.
 *
 * <p>They land one at a time after the void has arrived, each blooming out fast and
 * overshooting slightly so it reads as thrown, then drifting for the rest of the domain's
 * life — a lap of the sphere takes minutes, so you notice they have moved rather than that
 * they are moving. Nothing keeps them clear of the black hole: they are paint on the glass
 * in front of the window, and crossing it is the point.
 */
float splashes(vec3 wall, float t, float reveal) {
    if (SPLASHES <= 0 || reveal <= SPLASH_START)
        return 0.0;
    // One field for every rim, drifting slowly: the shared paper texture.
    float warp = fbm3(wall * 5.5 + BrushSeed + t * 0.02);
    float wobble = (warp - 0.5) * 0.055;
    float paint = 0.0;
    for (int i = 0; i < SPLASHES; i++) {
        float fi = float(i);
        float born = SPLASH_START + (SPLASH_END - SPLASH_START) * (fi / float(SPLASHES));
        float age = reveal - born;
        if (age <= 0.0)
            continue;
        // Thrown, not faded up: out fast, overshoot, settle.
        float grow = smoothstep(0.0, 0.06, age);
        float settle = 1.0 + 0.30 * exp(-age * 45.0) * sin(age * 70.0);

        // Drift, about this splash's own axis. Rodrigues written out; GLSL 150 has no helper.
        vec3 site = hashDir(fi, BrushSeed);
        vec3 axis = normalize(hashDir(fi + 61.0, BrushSeed * 1.7) + vec3(1.0e-4));
        float turn = t * (0.006 + 0.010 * hash11(fi + 11.3));
        float cs = cos(turn);
        float sn = sin(turn);
        site = normalize(site * cs + cross(axis, site) * sn + axis * dot(axis, site) * (1.0 - cs));

        // Hard edges, as ink on paper has. The wobble is what makes them ragged.
        float a = 1.0 - dot(wall, site);
        float rad = (0.012 + 0.060 * hash11(fi + 7.3)) * grow * settle;
        paint = max(paint, smoothstep(rad + 0.005, rad - 0.005, a + wobble));
    }
    return clamp(paint, 0.0, 1.0);
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
        fragColor = vec4(INK * Intensity, phaseFade * (1.0 - hole));
        return;
    }

    // Under the plane there is only the abyss: it is what the sea is translucent over, and
    // the pit walls behind it must never show. Skipping the sky here is also most of what
    // the floor pass costs, given back.
    if (surf.y < FloorY) {
        fragColor = vec4(ABYSS * Intensity, phaseFade);
        return;
    }

    // Solid. The wall used to sit at three-quarters alpha and the moon came through it.
    vec3 col = skyAnalytic(dir, t, false);
    // Paint on the barrier, over everything the void shows through it. Keyed on the surface
    // point, so it stays on its patch of wall as you move.
    col = mix(col, BONE, splashes(normalize(surf), t, Reveal) * 0.94);
    col += BONE * shatter * 0.45;
    fragColor = vec4(col * Intensity, (1.0 - hole) * phaseFade);
}
