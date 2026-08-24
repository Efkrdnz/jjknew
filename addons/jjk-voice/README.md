# JJK Strongest: Voice Commands

An optional addon for **JJK Strongest**. Hold a key, say *"Domain Expansion"*,
and the domain opens.

Recognition runs entirely on your own machine. There is no AI, no model download,
no API key, no account, and no network traffic of any kind. The mod matches your
voice against recordings **you** make during a short enrollment, using classic
speech-signal processing (MFCC features + dynamic time warping).

## What this replaces

The mod previously read voice commands out of a text file: an external program
wrote a word into `Documents/JJKVoiceCommands/command.txt` and the mod polled
that file from the server tick, every five ticks, for every player. That is gone.
Audio now never leaves the client, nothing touches the disk on the server, and
the whole feature is optional — the host mod no longer carries any of it.

## Requirements

| Mod | Why |
| --- | --- |
| JJK Strongest `1.0.0+` | The technique being used. Hard dependency. |
| Simple Voice Chat `2.6.20+` | The microphone feed. Hard dependency. |

The addon refuses to load without both, rather than half-working.

## Usage

1. Join a world with voice chat connected.
2. Enroll the techniques you actually intend to speak:
   `/jjkvoice enroll dismantle`. It asks you to say each of that technique's
   phrases a few times — hold the **Voice Command** key (default `'`), speak,
   release.
3. In play: hold the key, say *"Dismantle"*, release.

Seven abilities are configured out of the box — the ones the old phrase file
covered. You do not have to enroll all of them: partial enrollment is a supported
state, and a command with nothing recorded is simply skipped during matching. So
teach the two or three you actually use and leave the rest. `/jjkvoice status`
shows where you are, and everything else the mod exposes is still reachable with
`/jjkvoice add`.

Your voice never reaches other players. Simple Voice Chat's proximity
transmission is cancelled for exactly as long as the key is held, so the
incantation stays between you and the mod.

### Commands

All client-side — a server never sees your voiceprints or settings.

| Command | Effect |
| --- | --- |
| `/jjkvoice enroll` | Enroll every configured phrase (long) |
| `/jjkvoice enroll <technique>` | Enroll just that technique's phrases |
| `/jjkvoice add <technique> <phrase>` | Bind a new phrase and enroll it |
| `/jjkvoice status` | Mode, voice-chat state, and per-technique progress |
| `/jjkvoice cancel` | Abort a running enrollment |
| `/jjkvoice forget <phrase>` / `forget all` | Delete voiceprints |
| `/jjkvoice mode voiceprint\|shout` | Switch recognition mode |
| `/jjkvoice reload` | Re-read `jjkvoice.json` |

`<technique>` tab-completes from the host mod's own command list, so there is
nothing to memorise.

## Two kinds of command

The mod works in two steps, so voice does too.

**Actions** fire immediately: `domain_expansion`, `dismantle`, `fuga`, and
Inumaki's Cursed Speech words (`die`, `blast`, `crush`, `burst`, `sleep`, `flee`,
`rot`, `twist`, `burn`, `fall`, `spit`, `pull`, `shrink`, `weep`, `kneel`,
`dont_move`).

**Selections** switch which ability is active, exactly as picking it in the
radial menu does — the technique keybinds then act on it. These are the mod's own
moveset names: `gojo_blue`, `gojo_red`, `gojo_purple`, `gojo_limitless`,
`sukuna_cleave`, `sukuna_wcs`, `sukuna_shrine`, `inumaki_*`, `yuji_*`, and the
rest.

`/jjkvoice status` labels each configured command `[action]` or `[select]`.

The split is not a stylistic choice. Several abilities are charge-and-release —
Hollow Purple will not fire until `charge_purple >= 3` and the charging effect is
on you — so there is no honest way to express them as a single spoken action.
Selecting is what a player does first anyway.

Which commands do anything depends on your sorcerer; the host mod decides that,
not this addon. A Gojo player saying "dismantle" is simply ignored, exactly as it
is from any other input.

### Coming from the old phrase file

The previous setup's `phrases.txt` used four keys the mod has no action for.
They were ability names, and they map straight across:

| Old key | Now |
| --- | --- |
| `hollow_purple` | `gojo_purple` (selection) |
| `reversal_red` | `gojo_red` (selection) |
| `lapse_blue` | `gojo_blue` (selection) |
| `cleave` | `sukuna_cleave` (selection) |

The spoken phrases are unchanged — you still say "hollow purple". Only the key
the config files it under is different, and tab-complete shows the real names.

Its long lists of near-spellings are gone on purpose. Entries like
"domain expression", "hello purple" and "this mantle" were not alternatives you
might say; they were the transcriber's mistakes, written down so a wrong
transcript still landed on the right ability. Nothing here transcribes — matching
is acoustic and has no dictionary, so there is no wrong transcript to catch.
Enrolling a misreading would only widen what counts as the phrase and make false
triggers *more* likely. Genuine alternatives ("imaginary purple", "open the
furnace", "kaisen") are kept, because those are things you would actually say.

## Sound-alikes

Recognition here is acoustic, not linguistic — the mod has no dictionary, so it
never "mishears one word as another". It only knows what you taught it. That is
why the phrase list matters: add every way you actually say it out loud.

```json
"dismantle": ["dismantle", "kaisen"]
```

Each entry is enrolled separately and any of them firing runs the technique. If
the game keeps refusing you, enroll again — or add the variant you *actually*
said with `/jjkvoice add`.

Short Cursed Speech words ("die", "rot") are the hardest case, because there is
less signal to match on. If one of them misfires, either say it more
deliberately or bind a longer phrase to it.

## Configuration

`config/jjkvoice.json`, created on first launch.

| Key | Default | Meaning |
| --- | --- | --- |
| `mode` | `voiceprint` | `shout` fires `shoutCommand` on any loud vocalisation — useful to verify your mic works before enrolling |
| `commands` | 7 abilities | Command key to the phrases that trigger it |
| `shoutCommand` | `domain_expansion` | What `shout` mode triggers |
| `thresholdMultiplier` | `1.35` | How far past your own natural variation still counts. Raise if it refuses you, lower if unrelated words trigger it |
| `absoluteMaxDistance` | `60.0` | Safety ceiling so an inconsistent enrollment cannot accept everything |
| `enrollmentSamples` | `3` | Recordings taken per phrase. More is steadier and slower |
| `minSpeechSeconds` / `maxSpeechSeconds` | `0.25` / `3.0` | Clips outside this range are discarded before any matching |
| `shoutRmsThreshold` | `0.06` | Loudness required in `shout` mode |
| `announceMatches` | `true` | Print the matched phrase and distance. Useful while tuning |

Thresholds are **not** guessed. Enrollment measures how much your own repeats of
a phrase differ from each other and derives the accept threshold from that, so it
self-tunes to your voice, microphone, and room.

A technique key the host mod does not recognise is dropped when the file loads,
so a typo shows up as a missing technique in `/jjkvoice status` rather than a
phrase you enrolled that silently never works. One phrase cannot be bound to two
techniques; the first binding wins.

## Building

The addon compiles against the host mod's jar, so build JJK Strongest first:

```bash
cd "../.." && ./gradlew.bat build
```

```bash
cd "addons/jjk-voice" && ./gradlew.bat build
```

Output: `build/libs/JJKVoice0.1.0-neoforge-1.21.1.jar`

`./gradlew.bat runClient` starts a dev client with JJK Strongest, GeckoLib, and
Simple Voice Chat on the runtime classpath. None of them is ever shaded into the
addon jar — it ships as ~50 KB of its own classes and nothing else.

## Design notes

- **Server-authoritative.** The client only ever *requests* a technique. The
  server allow-lists the key against the host mod's own command set, throttles
  the sender, and hands off to JJK Strongest, which applies every real check —
  sorcerer, cost, cooldown. A modified client gains nothing it could not get by
  pressing the keybind.
- **The allow-list is not a copy.** It comes from `JjkVoiceApi.commandKeys()`
  rather than a duplicate kept in the addon. A hardcoded copy would drift the
  first time a technique is added or renamed, and drift in an allow-list fails in
  the dangerous direction.
- **One seam into the host mod.** `compat/JjkBridge` is the only class that
  touches JJK Strongest, and it only calls `net.efkrdnz.jjkstrongest.api.JjkVoiceApi`.
- **Cancel only while armed.** The Simple Voice Chat hook intercepts audio only
  while the key is held, so proximity chat is untouched and other voice addons
  following the same rule can share the microphone.
- **Off the render thread.** Feature extraction and matching run on a background
  executor; only the result is applied on the client thread. This matters more
  here than in a single-skill addon, because the search is across every enrolled
  phrase rather than one.
