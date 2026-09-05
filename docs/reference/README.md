# Visual reference

Art reference for the Unlimited Void interior. These are **not shipped** — they live
outside `src/main/resources` on purpose, so they stay to hand without adding ~12 MB to
the jar. Nothing in the mod has ever loaded them.

They are what `uv_interior.fsh` is built from, and the two that mattered:

- `unlimited_void.png` — near-black field, large **hard-edged white ink blots**, broad
  smooth **pale-blue lens arcs**, small black flecks. This is where the "monochrome with
  blue bleed" direction comes from, and why the shader spends its budget on a few big
  shapes with crisp edges rather than on more octaves of noise.
- `unlimited_void3.png` — a small, distant black hole with a **very bright hard white
  ring**, a soft dust band across a navy field, black debris silhouetted on the ring.
  The event horizon has no gradient at all, which is why the shader's does not either.

`unlimited_void1.png` was actually a JPEG despite the extension — renamed to `.jpg`, since
Minecraft's PNG reader would have thrown on it if anything had ever tried to load it.
