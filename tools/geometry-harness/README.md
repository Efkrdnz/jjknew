# Domain geometry harness

Runs the domain system's pure maths — the parts with no Minecraft behind them — with
nothing but a JDK. `./run.sh`.

## Why this exists

Two bugs in the domain rework were the kind that look almost right and are entirely
wrong, and neither would have shown up as a compile error:

- A slash's long axis is not its `direction`. The renderer maps local +Z to `direction`
  and scales length along local +X, so `direction` is the quad's *normal* and the blade
  runs perpendicular to it. Clipping along `direction` cuts an axis the slash does not
  occupy.
- `DomainShell.cellFor` was half a turn out of step with the sphere mesh in longitude,
  and banded latitude by height where the mesh bands by angle — so every crack would have
  appeared somewhere other than the damage that caused it.

Both were caught by reading. The harness turns that reading into something that fails on
its own, and it runs in about a second, so the geometry can be iterated on without a
Gradle cycle.

## How it works

`net/minecraft/...` here holds three hand-written stand-ins — `Vec3`, `AABB`,
`CompoundTag` — small, stable value types whose behaviour is easy to reproduce exactly.
The classes under test are copied in from `src/main/java` **unmodified** at run time, so
the harness always tests the shipping code and can never drift from it.

That boundary is also the limit of what this proves. It exercises geometry and the shell's
damage model. It says nothing about any Minecraft or NeoForge API call, the mixin, the
shaders, networking or rendering — those need a real build.

## What it checks

- `longAxis` is unit and perpendicular to the direction, over 20k random orientations,
  plus the straight-up degenerate case
- `DomainOcclusion.clip` on a known crossing, a clear miss, and a slash wholly inside
- `DomainShell.cellFor` agrees with `DomainUVRenderer.buildUnitSphere`'s UV convention
  over 50k random directions, plus the four cardinal directions by hand
- Both barrier failure shapes fall out of the one grid: even pressure shatters the whole
  shell in roughly the intended time, while concentrated strikes open a hole where they
  landed, in roughly the intended number of hits, with the shell otherwise healthy
- `DomainIntersect` handles the containment case a 100-block field over a 30-block dome
  actually produces
