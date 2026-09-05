# Domain Expansion System — Current State Reference

Audit of the domain expansion implementation on `claude/domain-expansions-analysis-jofyxd`
(NeoForge 1.21.1, `net.efkrdnz.jjkstrongest`). Written as the baseline document for a
redesign toward an analytic sphere with real collision, an interior-fitted shader, and
richer clash mechanics.

Companion document: `docs/shader_rendering_system.md` (shader registration, render types,
scene-copy pattern).

---

## 1. TL;DR

There are **two completely independent domain implementations** that share almost nothing
but a naming prefix and one clash bridge:

| | Unlimited Void (Gojo) | Malevolent Shrine (Sukuna) |
|---|---|---|
| Entity | `DomainUVEntity` (0.1 × 0.1) | `MalevolentShrineEntity` (4 × 4) |
| Containment | **Real voxel shell** of `domain_barrier` blocks, r = 30 | **None at all** |
| Collision | Block collision (voxelized sphere) | None |
| Interior visual | 3 shader layers, depth test **disabled** | Model entity + red sky/fog override |
| World damage | Stores + restores every touched block | Deletes blocks via `/setblock` commands |
| Offence | Information Overload effect, 58-block **cube** | 60–80 slashes/tick, 2 dmg / 4 ticks, r = 100 |
| Tick driver | `DomainUVEntity.baseTick()` | Global `EntityTickEvent.Pre` handler |
| Duration | 600 ticks (+ 80 startup), hard cap 1200 | 600 ticks, hard cap 1200 |

All domain state lives in `entity.getPersistentData()` (NeoForge NBT). **None of it is
synchronised to the client.** Every client-side visual therefore keys off
`entity.tickCount` instead of the real phase — and the clash HUD, which reads NBT on the
client, can never draw.

---

## 2. Cast entry points

Both domains are cast from the same two places:

| Caller | File | Line |
|---|---|---|
| Key release (moveset `gojo_limitless`) | `procedures/Technique4OnKeyReleasedProcedure.java` | 262–284 |
| Key release (moveset `sukuna_shrine`) | `procedures/Technique4OnKeyReleasedProcedure.java` | 117–139 |
| Voice command `domain_expansion` (gojo) | `procedures/VCTexeProcedure.java` | 24–45 |
| Voice command `domain_expansion` (sukuna) | `procedures/VCTexeProcedure.java` | 46–64 |
| Gojo NPC AI | `procedures/GojoNPCAIProcedure.java` | 568 |
| Sukuna NPC AI | `procedures/SukunaNPCAIProcedure.java` | 673, 918 |

Both paths are guarded by `DomainCollapseManualProcedure.hasActiveDomain(...)`, which does
a 200-block AABB scan for *either* domain type owned by that UUID. Pressing the key again
while a domain is up calls `collapsePlayerDomain(...)` instead — a toggle.

`hasActiveDomain` is a **200-block box scan of two entity classes on every cast and every
NPC AI tick** (`GojoNPCAIProcedure:435`, `SukunaNPCAIProcedure:882/909`). There is no
owner→domain index.

`DomainExpansionStartProcedure.execute(world, x, y, z, caster, domainType)` takes a
`domainType` int. It is written to NBT and **never branched on anywhere** — currently
decorative. Both call sites pass `0`.

---

## 3. Unlimited Void — server side

### 3.1 Spawn (`DomainExpansionStartProcedure.java`)

```java
DomainUVEntity domainEntity = new DomainUVEntity(JjkStrongestModEntities.DOMAIN_UV.get(), serverLevel);
domainEntity.setPos(x, y, z);
domainEntity.setPersistenceRequired();
domainEntity.setInvulnerable(true);
```

Then the full NBT block is written (lines 40–54), the entity is added, and
`captureEntities(...)` runs once.

The caster is teleported to `(casterX, y, casterZ)`.

Debug chat messages (`§a[DEBUG] Domain entity spawned!` / `§c[DEBUG] Failed…`) are still in
this file at lines 26, 59, 61 and fire on every cast.

### 3.2 State model

Everything is in `entity.getPersistentData()`. There is no `SynchedEntityData`, no
`IEntityAdditionalSpawnData`, no custom packet.

| Key | Init | Meaning | Read by |
|---|---|---|---|
| `domainType` | `0` | unused discriminator | existence check only (`DomainUVEntityTickProcedure:27`) |
| `domainRadius` | `30.0` | barrier radius | expansion, pull, clash |
| `captureRadius` | `35.0` | capture / blindness radius | capture, pull, post-lines |
| `ownerUUID` | caster | owner | validity, sure-hit exclusion |
| `duration` | `600` | active countdown | tick |
| `expansionTick` | `0` | 0…39 build progress | tick |
| `isExpanding` | `true` | phase flag | tick |
| `isActive` | `false` | phase flag | tick |
| `isPostLines` | `false` | phase flag | tick |
| `postTick` | `0` | 0…39 post-line progress | post-lines |
| `storedBlocks` | `{}` | every replaced block, keyed `"x,y,z"` | collapse |
| `domainAbsoluteTicks` | — | hard-cap counter | tick |
| `isClashing` / `uvClashHP` / `clashLostTicks` / `rivalUUID` | — | clash state | clash manager |
| `wallDamageWindow`, `wallBrokenCount`, `repairCooldown` | `0` | **dead — written once, never read** | — |

### 3.3 Phase timeline

Driven by `DomainUVEntity.baseTick()` → `DomainUVEntityTickProcedure.execute()`.

| Server tick | Phase | What happens |
|---:|---|---|
| 0 | spawn | NBT written; `captureEntities` teleports everything in r = 35 (with a −15/+35 Y clamp) to `centerY`, halves horizontal velocity, resets fall distance |
| 0–39 | `isExpanding` | `expandDomainProgressive` builds the voxel shell, one growth step per tick |
| 39 | — | `isExpanding = false`, `isPostLines = true`, `postTick = 0` |
| 40–79 | `isPostLines` | `DomainUVPostLinesPhaseProcedure`; at `postTick == 30` applies Blindness (15 ticks, amp 0) to every non-owner in r = 35 |
| 79 | — | `isPostLines = false`, `isActive = true` |
| 80+ | `isActive` | each tick: `UVDomainSureHitProcedure`, `pullEntities`, `duration--` |
| 680 | collapse | `duration <= 0` → restore blocks, discard |
| 1200 | hard cap | `ABSOLUTE_MAX_LIFETIME` → forced collapse |

Collapse also fires early if the caster is gone, dead, spectating, or has left
`domainRadius` (`shouldCollapseDueToCaster`, lines 92–110) — checked every tick.

### 3.4 The "sphere" — `expandDomainProgressive` (lines 112–151)

This is the current geometry, and it is the part a real sphere would replace.

```
platformY   = centerY - 1
bottomProgress = min(1, (tick+1)/20)          // ticks 0..19
topProgress    = tick < 21 ? 0 : min(1, (tick-20)/20)   // ticks 21..39
wallThickness  = 1.6      eps = 0.55
searchRadius   = ceil(max(bottomR, topR) + wallThickness + 2)   // up to 34
```

Then a full cubic iteration `BlockPos.betweenClosed(center ± searchRadius)` with three
cases per position:

- **`y < platformY`** → if `dist <= bottomRadius + eps`, place barrier. **Solid fill.**
  The entire lower hemisphere becomes barrier blocks (≈ 56,000 blocks at r = 30).
- **`y == platformY`** → 2D disc of radius `bottomRadius`, place barrier. The floor.
- **`y > platformY`** → if `dist < innerTopRadius - eps` set to **air** (carve the cavity);
  else if within `[innerTopRadius, topRadius]` place barrier. **Hollow dome.**

Every replaced block — including every carved-to-air block — is serialised into
`storedBlocks` before replacement, with its `BlockState` and, if present, its
`BlockEntity` NBT.

Notable properties:

- **Cost.** The cube is up to 69³ ≈ 328,000 positions, with a `sqrt` each, **every tick for
  40 ticks** — ~13 M position evaluations per cast, on the server thread, single-threaded,
  no chunk batching, `setBlock(..., 3)` (flag 3 = update + notify neighbours).
- **NBT size.** `storedBlocks` ends up well over 100,000 compound entries (solid lower
  hemisphere + carved upper hemisphere + shell). This tag is serialised with the entity.
- **`topProgress` never reaches 1.0.** At the last expansion tick (`tick == 39`) it is
  `19/20 = 0.95`, so the dome tops out at radius 28.5 while the base is 30. The shell is
  not a sphere; it is a 30-radius solid bowl with a 28.5-radius dome on top.
- Bedrock and existing barrier blocks are skipped (`placeBarrierBlock:173`).

### 3.5 The barrier block (`block/DomainBarrierBlock.java`)

```java
BlockBehaviour.Properties.of()
    .sound(SoundType.GLASS)
    .strength(-1, 3600000)      // unbreakable, blast-proof
    .lightLevel(s -> 15)        // emits full light
getLightBlock(...) -> 15        // blocks all light
getBlockPathType(...) -> BLOCKED
```

Model `domain_barrier.json` is a plain `block/cube` with `jjk_strongest:block/domain_outer`
on all faces and `"render_type": "solid"`.

**This is where the domain's collision actually comes from today** — ordinary full-cube
block collision. It is a voxelized sphere: stair-stepped, opaque, and it lights the
interior at level 15.

### 3.6 Offence and containment

- `UVDomainSureHitProcedure` — applies `INFORMATION_OVERLOAD` (200 ticks, amp 1) every
  tick to every living entity in `new AABB(center, center).inflate(29.0)`. That is a
  **58-block cube, not a sphere**, and it is larger than the 30-radius dome along the
  axes but reaches ~50 blocks into the corners — outside the barrier. Excludes the owner
  and anything tagged `minecraft:technique`.
- `pullEntities` — searches a 35-cube, and for anything past `domainRadius - 2` (28) adds
  `0.3 * toCenter` to its velocity each tick. Creative/spectator exempt.
- `DomainsDamageCancelProcedure` — global `LivingIncomingDamageEvent` handler that cancels
  **all** damage to any entity tagged `minecraft:technique`, which is how the domain
  entities themselves stay invulnerable.

### 3.7 Collapse

`collapseDomain` (lines 201–216) walks `storedBlocks`, parses each `"x,y,z"` key, restores
the `BlockState` with flag 3, reloads any `BlockEntity`, then `discard()`s the entity.
There is no staging — the full restore happens in a single tick.

---

## 4. Unlimited Void — client side

### 4.1 `DomainUVRenderer` (`client/renderer/DomainUVRenderer.java`)

A `MobRenderer` over `Modelblank_entity` with `shouldRender()` forced to `true` (no
frustum culling). Everything is gated on:

```java
if (entity.tickCount >= 80) { … }
```

`tickCount` is the **client** entity age. It happens to line up with the server's
`isActive` transition (80) only because nothing perturbs it. There is no phase flag on the
client to key off.

Three layers, all wrapped in `RenderSystem.disableDepthTest()` + `depthMask(false)`:

| Method | Geometry | Transform | Size | Shader |
|---|---|---|---|---|
| `renderWhiteBrushes` | inverted sphere, 20 lat × 32 lon = 640 quads, equirectangular UV | none | **radius 25.2** | `void_brush` |
| `renderRift` | 32-segment circular fan (as degenerate quads) | `translate(0, 18, 0)`, `rotX(90°)` — a horizontal disc | **44** | `void_rift` |
| `renderBlackHole` | 32-segment circular fan | `translate(18, 7, 0)`, camera-billboarded | **72** | `void_blackhole` |

`renderCircularQuad` emits `(0,0), (x1,y1), (x2,y2), (0,0)` — a triangle fan faked as
`QUADS` by duplicating the centre vertex.

Render types come from `JjkShaderManager.makeRenderType(...)` (line 283): `POSITION_TEX`,
`QUADS`, translucent `SRC_ALPHA/ONE_MINUS_SRC_ALPHA`, `lequal` depth test, culling off,
colour-write-only mask. The `lequal` state is moot because depth testing is turned off at
the `RenderSystem` level around each draw.

**The four radii in play are all different and unrelated:** visual sphere 25.2, barrier 30
(base) / 28.5 (dome), capture & pull 35, sure-hit cube half-extent 29, rift 44, black hole
72. Nothing derives from `domainRadius`.

### 4.2 `void_brush.fsh`

The interior shader (221 lines). Takes `Time`, `BrushSeed`, `Intensity`. It rebuilds a
sphere direction from the UV so the texture is seamless:

```glsl
float theta = texCoord.x * 2.0 * PI;
float phi   = texCoord.y * PI;
vec3 dir = vec3(sin(phi)*cos(theta), cos(phi), sin(phi)*sin(theta));
```

Then: global drift rotation, a 2-octave flow field, a **9-step volumetric raymarch** with
6-octave `fbm3` per step (so ~54 fbm evaluations, each 6 noise lookups = ~324 noise calls
per fragment), 4 layers of "information shard" streaks, 3 star layers, chroma shimmer,
pulse, and a vignette. Alpha is clamped to `[0.55, 0.95] * Intensity`.

This is the "perfect fit inside" candidate — it is already written in direction space, so
it will map onto any sphere radius without change. It is also **very** expensive per
fragment, and it currently covers the entire screen when you are inside it.

### 4.3 `DomainUVLinesClientRenderer`

`RenderLevelStageEvent` at `AFTER_TRANSLUCENT_BLOCKS`. Gated on `domain.tickCount` in
`[40, 80)`. Draws **140 rays × 2 quads** (outer glow + inner core) of length 36, additive
blend, `NEW_ENTITY` format with `FULL_BRIGHT` lightmap, using
`jjk_strongest:textures/entities/lightning_bolt.png`.

Colour is one of red / purple / pink per ray. The RNG is reseeded every tick with
`seed + tickCount`, so the ray set is completely different each tick — a strobe, not a
motion.

Note the NeoForge 1.21.1 workaround at line 57–61: `AFTER_TRANSLUCENT_BLOCKS` supplies a
null `PoseStack`, so the renderer builds its own.

### 4.4 Overlays

| File | Trigger | Draws |
|---|---|---|
| `screens/DomainCastOverlayGojoOverlay` | `domain_image_2 > 0` and `ReturnBoolGojoProcedure` | `unlimited_void_hand_2.png` at screen centre |
| `screens/DomainCastOverlaySukunaOverlay` | same pattern | Sukuna cast image |
| `screens/DomainScreenEffectRendererOverlay` | — | screen effect |
| `client/DomainClashHudOverlay` | both domains `isClashing` | VS HP bars |

`domain_image_1` / `domain_image_2` are player capability doubles set to 1 on cast and
decayed in `DomainEffectTickProcedure` (0.025/tick and 0.05/tick respectively) — a 40-tick
and 20-tick fade used directly as the overlay alpha.

### 4.5 There is no server → client state sync

`getPersistentData()` is server-authoritative NBT. It is not in `SynchedEntityData` and not
in the spawn packet, and no packet in `network/` carries it (`DomainSlashNetworkHandler`
only ships slash spawns). Consequences:

1. **`DomainClashHudOverlay` can never render.** It requires
   `uv.getPersistentData().getBoolean("isClashing")` to be true *on the client*. It is
   always false, so the method returns at line 61–62 every frame. The bars, the VS label,
   the low-HP pulse — all dead code as wired.
2. Every client visual has to infer phase from `tickCount`, which desyncs on lag, on
   reload, and on any future change to phase durations.
3. The client has no idea what `domainRadius` is, which is why the render radii are
   hard-coded constants.

---

## 5. Malevolent Shrine — server side

### 5.1 Spawn (`MalevolentShrineSummonProcedure.java`)

Spawns 4 blocks *behind* the player (yaw + 180). Sets `ownerUUID`, `domainCastY`,
`domainLifetimeTicks = 0`, `destructionProgress = 0`. Entity is 4 × 4, `setNoAi(true)`,
persistence required.

Unlike `DomainUVEntity`, this entity **does** mirror four NBT keys through
`addAdditionalSaveData` / `readAdditionalSaveData` (lines 84–116) — redundant, since
NeoForge already persists `persistentData`, but harmless.

### 5.2 Tick driver

`MalevolentShrineTickingEventProcedure` subscribes to `EntityTickEvent.Pre` — **globally,
for every entity in the world** — and filters with `instanceof MalevolentShrineEntity`.
It then runs two procedures.

**`MalevolentShrineTickBlockBreakingProcedure`** — the terrain destruction:

- `life` counter; at `life == 1` plays `sukuna_domain_ost` (AMBIENT); at 60 sets `active`.
- After 60, every 4 ticks: `domainBBRadius++`, then iterate a hemisphere of that radius and
  for every non-air, non-unbreakable block, dispatch
  `_level.getServer().getCommands().performPrefixedCommand(… "setblock ~ ~ ~ air")`.

  This is **one full command-parse-and-dispatch per block**, inside a triple nested loop
  that grows every 4 ticks. It is by far the most expensive thing in the domain system,
  and blocks are destroyed outright — there is no `storedBlocks` equivalent and no restore.

- `active` and `domainBBRadius` are otherwise unread; `destructionProgress` is written at
  spawn and in the save/load hooks but never used.

**`MalevolentShrineTickProcedure`** — the offence:

- `ABSOLUTE_MAX_LIFETIME = 1200`, `MAX_LIFETIME = 600`, `STARTUP_DELAY = 40`,
  `RADIUS = 100`, `DAMAGE_INTERVAL = 4`, `BASE_SLASH_COUNT = 60` + `nextInt(20)`.
- Owner validated every 20 ticks (alive, not spectator, within r = 100).
- Every tick after startup: 60–80 slashes, each with a random position sampled uniformly in
  the r = 100 sphere, random direction, random style (30% white / 70% red), length 25–35,
  width 1.5–3.0. Each one is sent as an individual `SpawnDomainSlashPacket` **to every
  player within 150 blocks**. That is up to 80 packets per player per tick.
- Every 4 ticks: `damageEntitiesOptimized` — 200-cube AABB, true-sphere filter, 2.0 damage
  of `jjk_strongest:technique_cleave`, `invulnerableTime = 0` so it always lands, velocity
  restored after the hit so knockback is suppressed, plus 2–3 `SWEEP_ATTACK` particle
  bursts per target.

There is **no barrier, no containment, no pull, no capture, and no collision** — the Shrine
is a damage field with a model in the middle.

### 5.3 Client side

- `MalevolentShrineRenderer` — plain `MobRenderer` over `Modelmalevolent_shrine` with
  `textures/entities/malevolent_shrine.png`. No shader.
- `MalevolentShrineSlashManager` — client-side ring buffer, `MAX_SLASHES = 300`, each slash
  has a 3-tick ease-out expansion and a 4-tick fade over a 12-tick life.
- `MalevolentShrineSlashRenderer` — `AFTER_PARTICLES`, draws each slash through the
  **Dismantle** shader. Per the shader doc, `beginFrameCaptureDismantle(...)` does a full
  framebuffer blit per slash because each has different uniforms — with up to 300 live
  slashes this is the dominant client cost.
- `MalevolentShrineClientTicker` — advances slash ages.
- `SkyBoxOverrideShrineProcedure` (530 lines) — replaces the dimension's
  `DimensionSpecialEffects` via reflection on `DimensionSpecialEffectsManager.EFFECTS`
  (line 490–500) and hand-renders abyss / deep sky / skybox / stars / sun / moon layers.
  Triggered when any shrine is within a 200-cube of the player.
- `SkyColorOverrideDomainProcedure` — `ViewportEvent.ComputeFogColor`, forces fog to
  `#DE0000` when any shrine is within 200 blocks. **Handles the Shrine only — there is no
  fog override for Unlimited Void.**

---

## 6. The clash system

`procedures/DomainClashManagerProcedure.java`.

### 6.1 Constants

```java
UV_DRAIN_PER_TICK    = 100f / 300f;   // UV auto-loses in 15 s
SHRINE_HP_PER_HIT    = 5f;            // 20 melee hits to break the Shrine
MAX_CLASH_HP         = 100f;
CLASH_DETECT_RADIUS  = 130.0;
CLASH_END_GRACE_TICKS = 40;
```

### 6.2 Detection

Only ever called from the **UV side** (`DomainUVEntityTickProcedure:54`). Each tick an
active UV does a 260-cube AABB scan for `MalevolentShrineEntity`, then:

```java
double overlapThreshold = uvRadius + 100.0;   // 130
if (distSq <= overlapThreshold * overlapThreshold) { execute(...); return true; }
```

Pure centre-to-centre distance. No sphere-sphere intersection, no barrier contact, no
overlap depth, no contact point. A Shrine 129 blocks away — nowhere near the 30-radius
dome — is "clashing".

If no rival is found, a 40-tick grace counter runs before the clash actually ends. The
Shrine mirrors this independently in `MalevolentShrineTickProcedure.reconcileClashState`.

### 6.3 Resolution

Deeply asymmetric:

| | Unlimited Void | Malevolent Shrine |
|---|---|---|
| HP | 100 | 100 |
| Loses HP | **passively, 0.333/tick** | only when the **UV owner melees the Shrine owner** |
| Time to lose unaided | 15 s | never |
| Player agency | none | 20 melee hits |

So the clash is a 15-second timer that the UV owner can beat by landing 20 melee hits on
the Shrine owner. The Shrine owner has no input at all.

`DomainClashMeleeHitProcedure` hooks `LivingDamageEvent.Pre`, requires
`getDirectEntity() == attacker` (melee only), and forwards to `onMeleeHitShrineOwner`,
which resolves attacker → UV and victim → Shrine by **linear scan of a ±30000 × ±512 AABB**
(`findUVByOwner` / `findShrineByOwner`, lines 126–142) — a full-world entity sweep per hit.

### 6.4 During a clash

- **UV**: `duration` frozen, `UVDomainSureHitProcedure` suspended, `pullEntities` still runs.
- **Shrine**: `domainLifetimeTicks` frozen; slashes and damage both skip anything inside a
  UV barrier via `isPosInsideUV` — which is itself a 300-cube entity scan **per slash
  candidate**, i.e. up to 80 full scans per tick.

### 6.5 Outcomes

- `collapseUV` — sets `duration = 0`, `isActive = true`, clears clash flags, removes
  `uvClashHP`. The next tick sees `duration <= 0` and collapses (blocks restore normally).
- `collapseShrine` — sets `domainLifetimeTicks = 600`, which trips `MAX_LIFETIME` next tick.
- Winner keeps residual HP (`endClashWinner`); a genuinely-vanished rival resets both HP
  pools (`endClashLoser`).

---

## 7. Findings that matter for the redesign

Ordered by how much they will get in the way.

1. **No client knowledge of domain state.** This is the root blocker. Any "perfect fit"
   shader needs radius, centre, phase and progress on the client; any clash visual needs
   HP. Today the client has `tickCount` and hard-coded constants. `DomainClashHudOverlay`
   is already dead because of this.

2. **Collision is voxel, and the voxels *are* the domain.** Removing the barrier blocks
   removes containment entirely. An analytic sphere needs a replacement collision path —
   a custom `VoxelShape`/entity collider, a movement-clamping tick handler, or a thin
   shell of blocks kept only for collision while the visual comes from the shader.

3. **Every radius is a different hard-coded number.** 25.2 (visual sphere), 28.5 (dome),
   30 (`domainRadius`), 29 (sure-hit half-extent), 35 (capture/pull), 44 (rift), 72 (black
   hole). Nothing derives from a single source of truth. A "perfect fit" requires one.

4. **Depth testing is disabled for all three UV layers.** The interior shader draws over
   everything already in the buffer, and entities rendered *after* the domain draw over
   *it*, so which entities are visible inside the domain is effectively render-order
   dependent. This must change for an inside-facing sphere that occludes correctly.

5. **The expansion loop is ~13 M `sqrt`-ed position checks per cast** across 40 ticks, plus
   a `storedBlocks` NBT tag with 100 k+ entries. The Shrine's `/setblock`-per-block
   destruction is worse still. Both need to go if the domain is to be sphere-driven.

6. **Clash detection is distance-only and one-sided.** Real sphere-sphere clash needs:
   intersection test, contact plane/point, overlap depth, and symmetric per-tick evaluation
   from a neutral driver rather than from the UV's tick.

7. **Clash resolution has no Shrine-side agency** and no visible feedback (the HUD never
   draws). Both HP curves are constants in one file, easy to redesign.

8. **The Shrine has no barrier at all.** Giving it a sphere is net-new work, not a port.

9. **Dead / vestigial state**: `wallDamageWindow`, `wallBrokenCount`, `repairCooldown`
   (written at spawn, never read); `domainType` (never branched on); `destructionProgress`,
   `active`, `domainBBRadius` (write-only); the `findSafeSpotForEntity` /
   `isStandableAndFits` / `isSafeStandPos` / `isTwoBlocksFree` helpers in
   `DomainExpansionStartProcedure` (lines 95–166, unreachable — `captureEntities` teleports
   straight to `centerY` instead); `rendertype_domain_skybox` / `domain_skybox.vsh|fsh`
   (referenced only by `rendertype_custom_portal.json`); `DomainUVLineFlashProcedure`
   (never called from anywhere).

10. **Debug chat spam** in `DomainExpansionStartProcedure` lines 26, 59, 61.

11. **Owner→domain lookups are full-world scans** (`findUVByOwner`, `findShrineByOwner`,
    `hasActiveDomain`, `isPosInsideUV`). A server-level registry keyed by owner UUID would
    remove all of them.

---

## 8. File map for the redesign

| Concern | Files |
|---|---|
| Cast / toggle | `Technique4OnKeyReleasedProcedure`, `VCTexeProcedure`, `DomainCollapseManualProcedure` |
| UV lifecycle & phases | `DomainExpansionStartProcedure`, `DomainUVEntityTickProcedure`, `DomainUVPostLinesPhaseProcedure` |
| UV geometry / collision | `DomainUVEntityTickProcedure.expandDomainProgressive`, `block/DomainBarrierBlock`, `models/block/domain_barrier.json` |
| UV interior visual | `client/renderer/DomainUVRenderer`, `shaders/core/void_brush.{json,vsh,fsh}`, `void_rift.*`, `void_blackhole.*` |
| UV ray burst | `client/renderer/DomainUVLinesClientRenderer` |
| UV offence | `UVDomainSureHitProcedure`, `potion/InformationOverload*` |
| Shrine lifecycle | `MalevolentShrineSummonProcedure`, `MalevolentShrineTickingEventProcedure`, `MalevolentShrineTickProcedure` |
| Shrine terrain | `MalevolentShrineTickBlockBreakingProcedure` |
| Shrine visual | `MalevolentShrineRenderer`, `MalevolentShrineSlashManager`, `MalevolentShrineSlashRenderer`, `SkyBoxOverrideShrineProcedure`, `SkyColorOverrideDomainProcedure` |
| Clash | `DomainClashManagerProcedure`, `DomainClashMeleeHitProcedure`, `client/DomainClashHudOverlay` |
| Networking (where a state-sync packet would go) | `network/DomainSlashNetworkHandler`, `network/SpawnDomainSlashPacket` |
| Shader registration | `client/JjkShaderManager` (`makeRenderType` line 283, `beginVoid*` lines 418–449) |
| Entity registration / sizes | `init/JjkStrongestModEntities` lines 56–59 |
