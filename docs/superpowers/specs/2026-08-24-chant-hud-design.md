# Chant HUD — design

An on-screen list of the incantation lines you can say right now, narrowing as
you recite. Part of the `jjkvoice` addon; nothing in the host mod's own HUD
changes.

## Why

Incantations are ordered lines and there is currently no way to see them in game.
A player has to remember that Purple opens on "Nine Ropes" and that the line
after "Polarized Light" is "Crow and Declaration", or alt-tab to the config. The
overlay makes the next word visible and, in doing so, makes a mis-heard line
obvious the moment it fails to advance.

It also resolves a rule the current design had to impose: Blue and Red share the
opening line "Phase", and today a shared line is read as belonging to whichever
ability is *selected*. That works but must be explained. Showing both
continuations and letting the second line choose is what a player would expect,
and needs no explaining.

## What it draws

One row per candidate, each showing the line that would come next, the ability it
belongs to, and that ability's colour.

| State | Rows |
| --- | --- |
| At rest | Every chantable ability the player's technique includes, showing its opening line |
| Mid-recital, ambiguous | Only the abilities still possible, showing each one's next line |
| Mid-recital, committed | One row: the next line, progress pips, and the word that fires it |

Pips are one per line of that incantation, filled for lines already recited — so
Purple shows four and Blue three. They track the recital, not the charge; the two
agree once committed but not while lines are banked.

A shared opening appears **once per ability**, so "Phase" occupies two rows at
rest, labelled Blue and Red. Merging them into one row would be tidier and would
hide the fact that two techniques are open to you, which is the thing the list
exists to say.

Ability colours:

| Ability | Colour |
| --- | --- |
| Blue | `#85B7EB` |
| Red | `#F09595` |
| Purple | `#AFA9EC` |
| Dismantle | `#F0997B` |

Dismantle is deliberately not another red. Red and Dismantle would otherwise sit
adjacent in the at-rest list in near-identical hues.

**Un-enrolled lines** render dim and struck through. The overlay is a list of
what you can say, and a line with no voiceprint cannot match however clearly it
is spoken — showing it as ordinary text would be an advertisement for something
that does not work.

**The overlay hides entirely** when the player's technique includes no chantable
ability (Inumaki, Yuji), or when Simple Voice Chat is not connected, since
nothing on it could be acted on. It does *not* hide merely because nothing is
enrolled — that is the case where the struck-through rows are most useful.

## Where the state comes from

The at-rest list needs no server data: the client knows the player's sorcerer
(the host mod syncs it) and owns the config that holds the lines.

Recital state is different — it lives on the server and the client cannot infer
it, because a line the client believed landed may have been refused: the ability
is not the speaker's, or the press was gated on charges they do not have. A
client tracking its own progress would show a recital the server is not running.

So one payload, server to client:

```
ChantStatePayload(List<String> candidates, int recited, int tier)
```

Empty `candidates` means no recital is running and the overlay falls back to the
at-rest list. Sent whenever the recital changes: a line advances it, it commits
to one ability, it is cancelled by switching ability, it lapses, or it is spent
by firing.

The client renders what it is told and never guesses.

## The branching recital

This is the substantive change, and it is in the host mod's `JjkVoiceApi`.

Today a recital is one ability and a line index. It becomes a **set** of
candidate abilities and a line index:

- The client sends every ability whose line at that index matches the phrase it
  heard, rather than picking one.
- The server intersects that with the candidates it is holding.
- **Still more than one candidate:** bank the line, advance the index, charge
  nothing. There is no way to charge Blue and Red at once, and guessing would
  charge the wrong one half the time.
- **Down to one:** select that ability, commit the recital, and grant the tiers
  the banked lines earned.
- **Down to none:** the line does not belong to this recital. If it is the
  opening line of some ability, start a fresh recital from it; otherwise ignore
  it, leaving the current recital untouched.

Because the banked lines are granted on commit, the end state after a full
recital is identical to today's. The only observable difference is that firing on
the opening line alone is no longer possible — which it was not usefully anyway,
one tier being one tier.

`VoiceCastPayload` grows a bounded list of ability keys in place of its single
key for the incantation case. The server still validates ownership, chantability,
and line ordering; it cannot validate the phrase-to-ability mapping because it
does not have the player's config, which is the same position it is already in
for every other voice request and is why the server treats these as requests
rather than instructions.

## Positioning

`hudX` and `hudY` are stored as **fractions of the screen**, not pixels, so the
overlay stays where it was put across a resolution or GUI-scale change. `hudEnabled`
turns it off without unbinding anything.

`/jjkvoice hud` opens a full-screen editor:

- The overlay renders live, with a representative mid-recital state so the player
  positions what they will actually see rather than an empty box.
- Drag to move. Snapping to screen edges, horizontal and vertical centre lines,
  within a few pixels.
- Esc saves and closes.

The editor is a client screen and writes only to the client config; no packet is
involved.

## Files

Addon, all client-scoped except the payload:

| File | Responsibility |
| --- | --- |
| `client/hud/ChantHud` | Draws the rows on `RenderGuiEvent.Pre` |
| `client/hud/ChantHudState` | Holds what the server last said; nothing else writes it |
| `client/hud/ChantHudScreen` | The drag editor |
| `network/ChantStatePayload` | Server-to-client recital state |

Host mod: the recital change in `JjkVoiceApi`, and sending the payload wherever a
recital changes.

`ChantHud` decides *what* the rows are from `ChantHudState` plus the config, and
draws them. If that grows past a couple of hundred lines the row-building should
split from the drawing, so what is on screen can be reasoned about without
reading render code.

Follows the host mod's existing overlay pattern: `@EventBusSubscriber(Dist.CLIENT)`,
`@SubscribeEvent` on `RenderGuiEvent.Pre`, drawing through `event.getGuiGraphics()`.

## Failure and edge cases

| Case | Behaviour |
| --- | --- |
| Recital lapses (10s) | Server sends empty candidates; overlay returns to the at-rest list |
| Player switches ability mid-recital | Recital already cancels; payload clears the overlay |
| Line spoken out of order | Recital unchanged, overlay unchanged; the line still charges its tier as it does today |
| Ability chantable but nothing enrolled | Row shown dim and struck |
| Voice chat not connected | Overlay hidden |
| Config names an ability that is not chantable | Already dropped on load; never reaches the overlay |
| Payload arrives for an ability the client's config does not know | Row skipped rather than drawn blank |

## Verification

The addon has no test source set, matching the other addons in this workspace, so
the gate is `clean build` on both projects plus in-game checks:

- At rest, with Gojo: three rows (Blue, Red, Purple), Dismantle absent.
- Say "Phase": two rows, Twilight and Paramita, both live; no tier sound yet,
  since nothing is charged while the recital is ambiguous.
- Say "Twilight": one row, Blue-tinted, Red gone; Blue is now selected.
- Say "Eyes of Wisdom" then "Lapse Blue": fires at full.
- Enroll nothing for one ability and confirm its row is struck.
- `/jjkvoice hud`, drag to a corner, Esc, change GUI scale, confirm it holds.

## Out of scope

- Any second charge indicator. The host mod already draws charge state
  (`PurpleChargeOverlayRenderer`, `WCSProgressionIndicatorOverlay`); the pips here
  show recital progress, not power.
- Animation, fading, or easing.
- Showing actions or ability names. The overlay is for incantations, which are the
  only thing with a next step to prompt.
- Per-ability icons or textures.
