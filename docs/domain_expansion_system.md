# Domain expansions — how the system works

Reference for the domain engine as it stands. This file used to be an audit of the system
*before* the rework; almost none of what it described exists any more, so it has been
rewritten against the current design.

---

## 1. The shape of it

One sphere, described in one place, read by everything.

`domain/DomainSphere` is a record — `(center, radius, floorY, phase, progress)` — and
collision, the terrain carve, the renderer, the fog, and every "is this inside a domain"
query read it and nothing else. Before the rework there were eight unrelated hard-coded
radii spread across gameplay and render code (25.2, 28.5, 29, 30, 35, 44, 72, 100), so the
thing you could see was never quite the thing you could touch.

The shape is a sphere cut by a flat floor plane at `floorY`. The dome above it is hollowed
out; the hemisphere below is left alone and is what you stand on.

**Key files:** `domain/DomainSphere`, `domain/DomainPhase`, `domain/DomainDefinition`

---

## 2. Mechanics are shared, looks are not

`domain/DomainDefinition` carries everything two domains genuinely have in common — radius,
floor offset, the four phase timings, duration, hard lifetime, the shell profile, collapse
rules, and the sure-hit effect. It carries nothing about how any of that looks.

That line is the whole point of the type. A second technique should be a definition plus a
renderer, not a second copy of the phase machine; and a domain that borrowed another's
visuals would stop being a different technique.

| Carried | Not carried |
|---|---|
| radius, floor offset | shaders, render types, palette |
| expansion / settle / collapse timings, duration | textures, models, particles |
| barrier kind (open or closed) | cast sounds, screen overlays |
| sure-hit effect, amplifier, cadence | sky and fog treatment |
| shell profile: regen, pressure resistance | |
| collapse rules: breach threshold, grace | |

`domain/DomainShellProfile` is separate and top-level because it is the one part with no
Minecraft types in it, which is what keeps `DomainShell` runnable in the geometry harness.

---

## 3. Open and closed

`domain/DomainBarrierKind` is the distinction the old code never made, and it decides how
two domains meet.

- **CLOSED** (Unlimited Void) — a real shell. It holds people in and keeps things out.
- **OPEN** (Malevolent Shrine) — no surface at all. It projects effects over a volume.

The rule that falls out: **an open domain's output cannot cross a closed barrier.** What
would have crossed is stopped at the surface and spent damaging it instead. So a closed
domain's health *is* its barrier, and an open one, having no barrier to attack, has to be
beaten through its caster.

Both anchor entities implement `domain/DomainSource`, so interaction rules are written
once against "a domain" rather than against two unrelated entity classes.

---

## 4. The phase machine

`EXPANDING → SETTLING → ACTIVE → COLLAPSING`, driven from the definition's timings.
`DomainPhase.isSealed()` is `SETTLING || ACTIVE` — the only phases in which the shell is
solid.

Both techniques run it. The Shrine used to keep its lifecycle in three pieces of persistent
data with no logical-side guard, so both sides ran the arithmetic and happened to agree;
the screen shake depended on that accident. It is synced entity data now.

**Key files:** `procedures/DomainUVEntityTickProcedure`, `procedures/MalevolentShrineTickProcedure`

---

## 5. Collision

There are no barrier blocks. `mixins/EntityDomainCollisionMixin` injects at the return of
`Entity#collide` and hands the resolved movement to `domain/DomainCollision.clamp`, which
clamps it against every closed domain in the level.

Two things are worth knowing because they were both bugs:

- **The floor holds in every phase**, not just while the shell is solid. The carve starts
  removing ground on the first tick and is still putting it back after the shell has shrunk
  away, so for most of a domain's life the plane is the only thing under you.
- **The floor catches things crossing it and nothing else.** Without that check it is not a
  floor but a magnet: anything already below it and inside the footprint gets yanked up to
  the plane in a single tick.

Exempt: spectators, entities tagged `technique` (the mod's own projectiles, which have
their own containment), and anyone who has run `/jjk sim noclip`. **Creative mode is not
exempt** — it used to be, and it made a domain look completely broken while behaving
exactly as written, with no way to tell those two apart from inside the game.

**Key files:** `domain/DomainCollision`, `domain/DomainNoclip`, `mixins/EntityDomainCollisionMixin`

---

## 6. Terrain

`domain/DomainCarve` hollows the dome — the sphere above the floor plane — in budgeted
slices, recording every block it removes into `domain/DomainSavedData` so collapse can put
it all back. Blocks are written with `UPDATE_CLIENTS` only; neighbour notification across a
volume this size sets off gravity and redstone cascades for terrain that is coming back
shortly.

Bedrock is taken and restored like anything else. What is never touched, because a restore
cannot honestly undo it: barriers, the three command blocks, structure and jigsaw blocks,
and every portal.

Note a domain cast near the Nether roof will punch through the bedrock ceiling, and you can
fly out on top until it collapses.

---

## 7. The barrier, and how it fails

`domain/DomainShell` is a 32 × 16 grid of per-direction integrity — 512 cells, one byte
each on the wire, synced by `network/DomainShellSyncPacket` and drawn as white voronoi
cracks over the black shell.

Per-direction rather than a single number because the two ways a barrier fails should look
and play differently:

- **Even wear** — a rival *open* domain leaning on it from every side at once. Every cell
  runs down together, so the shell reaches zero as a piece and shatters.
- **A concentrated attack** — punches, or a rival *closed* domain's contact face. One patch
  reaches zero first, and that hole is a hole: `clampMovement` consults the grid, so you can
  physically walk out through it. After a short grace the domain gives out.

**Key files:** `domain/DomainShell`, `domain/DomainShellProfile`, `client/DomainShellTexture`

---

## 8. Clashes

`procedures/DomainClashManagerProcedure` handles both kinds.

**Open versus closed.** The shrine has no surface, so all it can do is press evenly on the
Void's shell; its slashes are clipped at the barrier by `domain/DomainOcclusion` and spend
themselves damaging it instead of cutting through. It is beaten by damaging its caster.

**Closed versus closed.** `domain/DomainIntersect` gives the radical plane where two spheres
actually meet, and each shell takes pressure concentrated on the cells facing its rival,
scaled by overlap depth. The contact face holes first and the domain fails inward from that
side. Each domain damages only its own facing side on its own tick — both tick, so the
exchange comes out symmetric.

---

## 9. What it looks like

Deliberately not shared, and deliberately not in the definition.

`uv_interior` is one shader on one inward-wound 32 × 64 sphere, branching three ways:

- **Outside** — a near-opaque black sphere with white shatter on it.
- **The floor** — split off by a real ray-plane intersection, not a hemisphere test, because
  the band of sphere between the floor plane and the equator is reachable by rays that never
  cross the plane and has to show wall. Black lacquer: it reflects the analytic sky about the
  plane with Fresnel from matte underfoot to mirror-bright at the horizon.
- **The dome** — near-black going navy overhead, two star layers, a tilted dust band, three
  hard-thresholded volume steps, twelve ink blots whose rims are all displaced by one shared
  warp field, and pale-blue lens arcs.

The black hole is at infinity: a direction (`BhDir`, from the caster's facing at cast,
synced as `HOLE_YAW`), an angular radius (`BhAng.x`, 35 degrees across) and a fixed disc
axis. Nothing about it depends on where you stand, so it never parallaxes — a thing the size
of a sky inside a thirty-block room. Lensing is `1/r`, the leading term of Einstein
deflection; the background is sampled on the bent ray so stars stretch into a ring on their
own, and the accretion disc is a real thin-disc intersection — the ray runs to its closest
approach, bends there, and what it hits on the far side is the arcs over and under the
shadow. The near side of the disc is hit before the bend and drawn last, over the shadow.

The floor is a disc at the plane, drawn with depth and translucent, over a ball the carve has
emptied. It reflects the analytic sky and, because every entity in the room is drawn again
mirrored under the plane before it, the room too. Footsteps ripple it (`RippleField`,
`DomainFloorRipples`, the `RippleData` uniform array).

**The forming beat.** A domain is a closed room from the moment it is cast —
`DomainPhase.isSealed()` covers EXPANDING, and `DomainUVEntity.sphere()` reports the target
radius during that phase so collision is full size from tick one while only the visible wall
rushes out. Without that split everyone caught in the cast would be crushed into a ball at the
centre as the shell grew, because the capture pass gathers from thirty-five blocks and leaves
people where they stand.

Nothing hostile happens during it: the sure-hit and the pull both live inside `tickActive`, so
EXPANDING and SETTLING are a pure loading beat. The room is black for all of it — one uniform,
`Reveal`, is zero until the domain turns hostile, and `skyAnalytic` returns near-black with a
slow pulse in it while that holds, so the dome and the sea go dark together for free. The rays
(`DomainUVLinesClientRenderer`) burst through SETTLING in purple, crimson and blue, each ray a
fixed line for the whole beat because the seed is the domain's UUID and the tick count is
deliberately not in it.

Then `Reveal` climbs: the void fades in over its first quarter, and fourteen white splashes
land on the barrier one at a time across the rest. They are keyed on `normalize(localPos)` —
the direction of the SURFACE POINT, not the view ray — which is the whole difference between
paint on the wall and a mark on the sky: you walk past them, and they drift on their own axes
over minutes. The sea does not carry them, because the sea reflects the sky and they are not
in it.

**The one trap:** the renderer bakes its PoseStack into the vertex on the CPU, and the entity
dispatcher has already translated that stack to the domain's camera-relative position. So
`Position` in these shaders is *not* the unit sphere: it is `entityCentre + surface point`,
with `entityCentre = -CamOffset`. `uv_interior.vsh` adds `CamOffset` back and hands the
fragment stage domain-local blocks; `uv_shards.vsh` does the same before its sphere maths.
Treating `Position` as a unit vector is what once cost the interior all its parallax.

**Key files:** `client/renderer/DomainUVRenderer`, `client/JjkShaderManager`,
`shaders/core/uv_interior.*`, `shaders/core/uv_shards.*`, `client/DomainFloorRipples`,
`client/renderer/DomainUVLinesClientRenderer`, `client/DomainAtmosphereRenderer`,
`client/DomainLightmap`, `client/DomainClashHudOverlay`

---

## 10. Testing it

Everything in this system was historically invisible from inside the game, which is how a
barrier that was not there and a clash that never ran both shipped.

    /jjk sim info                  phase, radius, integrity, breaches, clash HP, owner
    /jjk sim void [distance]       a rival closed domain to clash with
    /jjk sim shrine [distance]     a rival open domain
    /jjk sim damage <n> here|even  reach a breach, or a shatter, on demand
    /jjk sim clear                 collapse everything and restore the terrain
    /jjk sim noclip                the explicit way through a barrier

    /jjk bot spawn <name> <character>
    /jjk bot <name> aim <other>|me
    /jjk bot <name> use <ability>
    /jjk bot <name> chant <ability> | release
    /jjk bot <name> tp <x y z> | here | freeze | thaw

Two bots aimed at each other opening domains is the view this system never had — a clash
watched from outside rather than from inside one of the spheres.

`tools/geometry-harness/run.sh` runs the pure geometry — the sphere, the clamps, the shell
indexing, the contact-face maths — with no Gradle, no Minecraft and no network. It is the
only part of this that can be verified without launching the game.

**Key files:** `command/JjkSimCommand`, `command/JjkBotCommand`, `entity/DebugBotEntity`,
`entity/DebugBotAbilities`, `tools/geometry-harness/`
