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
uniform sampler2D ShellSampler; // 32x16 per-direction integrity, matching DomainShell

in vec2 texCoord;
in vec3 localPos;
out vec4 fragColor;

const float PI = 3.14159265359;

// ---- palette ---------------------------------------------------------------
// Ink and bone carry every edge. Blue and violet mean depth and proximity to the hole,
// nothing else. The references are near-black fields with hard white ink blots and broad
// pale-blue lens arcs, which is a completely different thing from a coloured nebula.
const vec3 INK      = vec3(0.008, 0.009, 0.014);
const vec3 BONE     = vec3(0.92, 0.93, 0.96);
const vec3 NAVY     = vec3(0.010, 0.016, 0.052);
const vec3 PALEBLUE = vec3(0.52, 0.66, 0.90);

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

/** L1: two star layers, bone-white, one slow twinkle. */
vec3 stars(vec3 d, float t) {
    float fine = noise3(d * 90.0 + BrushSeed);
    float coarse = noise3(d * 38.0 - BrushSeed);
    float twinkle = 0.75 + 0.25 * sin(t * 1.3 + fine * 40.0);
    vec3 col = BONE * step(0.988, fine) * 0.55 * twinkle;
    col += BONE * step(0.975, coarse) * smoothstep(0.975, 0.995, coarse) * 1.15;
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
    return mix(NAVY, PALEBLUE, 0.35) * band * grain * 0.34;
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
 * Everything that lives at infinity, evaluated on a possibly-bent ray.
 *
 * Split out because the floor reflects exactly this and nothing else — reflecting the
 * marched volume as well would double the cost of every floor fragment for something you
 * cannot see in a dark mirror anyway.
 */
vec3 skyAnalytic(vec3 d, float t) {
    // L0: near-black at the horizon, deep navy overhead, warmed toward the hole.
    vec3 col = mix(INK, NAVY, smoothstep(-0.25, 0.85, d.y));
    col += NAVY * 0.6 * pow(max(0.0, dot(d, BhDir)), 6.0);

    float c = dot(d, BhDir);
    vec3 tangent = d - BhDir * c;
    float s = length(tangent);
    float ang = atan(s, c);   // two-argument: precision is worst exactly at the ring

    vec3 lensed = d;
    if (s > 1e-5)
        lensed = normalize(d + (tangent / s) * deflection(ang));

    col += stars(lensed, t);
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

    // The hole itself, last, over everything it swallows.
    float aa = fwidth(ang) + 1e-4;
    float ring = exp(-pow((ang - BhAng.x * 1.05) / (BhAng.x * 0.10), 2.0)) * 4.0;
    ring += exp(-pow((ang - BhAng.x * 1.40) / (BhAng.x * 0.30), 2.0)) * 0.8;
    col += BONE * ring;

    if (DiscStrength > 0.001) {
        // A thin disc in the plane through the hole, brightened on the approaching side.
        float axial = dot(d, BhAxis);
        float disc = exp(-pow(axial / 0.055, 2.0));
        float span = smoothstep(BhAng.x * 1.2, BhAng.x * 2.0, ang) * (1.0 - smoothstep(BhAng.x * 4.5, BhAng.x * 8.0, ang));
        float texture = 0.55 + 0.45 * fbm3(vec3(ang * 26.0, atan(axial, s) * 3.0, t * 0.35));
        vec3 hot = mix(BONE, PALEBLUE, smoothstep(BhAng.x * 2.0, BhAng.x * 7.0, ang));
        col += hot * disc * span * texture * DiscStrength * 0.9;
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
        vec3 tint = mix(mix(INK, BONE, density), NAVY + PALEBLUE * 0.25, fi * 0.3);
        float a = density * 0.30 * w;
        acc += tint * a * (1.0 - alpha);
        alpha += a * (1.0 - alpha);
    }
    return acc;
}

// ---- main ------------------------------------------------------------------

void main() {
    float t = Time;

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

    // Fade the whole thing in as the shell opens and out as it goes.
    float phaseFade = 1.0;
    if (Phase < 0.5)
        phaseFade = smoothstep(0.0, 0.65, Progress);
    else if (Phase > 2.5) {
        // Gone by 55%, while the shards are still in the air, so the back half of the
        // collapse is real world seen through a cloud of glass.
        phaseFade = 1.0 - smoothstep(0.05, 0.55, Progress);
        // From the first collapse frame the shell is broken, so stop drawing it as a
        // closed surface. Keeping the opaque outer face would fire the shard pass off
        // inside a black sphere and you would see none of it. The interior, seen from
        // within, still fades out on phaseFade above.
        if (!gl_FrontFacing || Inside < 0.5)
            discard;
    }

    // THE fix. localPos is the unit sphere; CamOffset is in blocks. Subtracting them
    // without this made the view ray collapse toward -CamOffset as you walked off centre,
    // so the interior stopped varying across the screen and read as a flat texture.
    vec3 surf = localPos * Radius;

    if (!gl_FrontFacing) {
        // Seen from outside: an all but opaque black sphere with white shatter on it.
        vec3 outward = normalize(localPos);
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

    // The floor, as a real ray-plane intersection rather than a hemisphere test. Splitting
    // on localPos.y would be wrong: the band of sphere between FloorY and the equator is
    // reachable by rays that never cross the plane, and those have to show wall.
    float tPlane = (FloorY - CamOffset.y) / dir.y;
    if (CamOffset.y > FloorY && dir.y < 0.0 && tPlane > 0.0 && tPlane < tSphere) {
        vec3 mirrored = vec3(dir.x, -dir.y, dir.z);
        // Black lacquer: it reflects what is at infinity and nothing else.
        vec3 reflected = skyAnalytic(mirrored, t);
        // Near-matte underfoot, mirror-bright toward the horizon. This is what makes a
        // flat plane read as a surface rather than a hole in the world.
        float fresnel = mix(0.06, 0.55, pow(1.0 - abs(dir.y), 5.0));
        vec3 col = INK * 0.9 + reflected * fresnel;

        // Faint rings, for parallax reference, so walking feels like walking.
        vec3 hit = CamOffset + dir * tPlane;
        float rings = abs(fract(length(hit.xz) * 0.25) - 0.5);
        float ringFade = 1.0 - smoothstep(0.0, Radius, length(hit.xz));
        col += PALEBLUE * smoothstep(0.48, 0.5, rings) * 0.06 * ringFade;

        // Never evaluate fract() on an enormous coordinate at the horizon; drivers differ.
        col *= smoothstep(0.0, 0.02, -dir.y);
        // Seal the join with the dome.
        col = mix(col, INK, smoothstep(0.0, 1.0, tPlane / (Radius * 2.0)));
        // A breached floor cell is opaque black with white edges, not a window to the sky.
        col = mix(col, vec3(0.0), hole);
        col += BONE * shatter * 0.5;
        fragColor = vec4(col * Intensity, phaseFade);
        return;
    }

    vec3 col = skyAnalytic(dir, t);
    float volumeAlpha;
    col += volume(dir, t, volumeAlpha);
    col += BONE * shatter * 0.45;

    float alpha = clamp(0.72 + volumeAlpha * 0.28, 0.0, 1.0) * phaseFade * (1.0 - hole);
    fragColor = vec4(col * Intensity, alpha);
}
