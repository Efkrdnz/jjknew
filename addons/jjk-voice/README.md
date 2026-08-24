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
4. Recite its incantation to charge it — see [Chanting](#chanting).

Twenty-six commands and five incantations ship configured, but you only ever see
your own technique's share of them — five or so for Gojo, seven for Sukuna,
seventeen for Inumaki. You do not have to enroll all of them: partial enrollment is a supported
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

## What a name does

Saying a technique's name does one of four things, depending on where you already
are. That is what lets you recite an incantation and then name the technique and
have it mean *"charge this, now fire it"*.

| When you say an ability's name | What happens |
| --- | --- |
| It is not selected | It becomes your selection, as the radial menu does |
| It is selected | It starts charging, as holding its technique key does |
| It is already charging | It is released, as letting that key up does |

**Actions** skip all of that and happen at once: `domain_expansion`, `kaisen`,
`fuga`, and Inumaki's Cursed Speech words (`die`, `blast`, `crush`, `burst`,
`sleep`, `flee`, `rot`, `twist`, `burn`, `fall`, `spit`, `pull`, `shrink`,
`weep`, `kneel`, `dont_move`). Cursed Speech takes effect the moment it is
spoken and has no charge to build.

The decision is made on the server, because it depends on things the client
cannot see and should not be trusted about. `/jjkvoice status` labels each
configured command `[action]` or `[select]`.

## Only your own technique

You can only say what your technique includes. Another sorcerer's abilities are
not merely refused, they are never heard: the recogniser is handed only the
phrases you are entitled to before it compares anything.

That is a recognition decision as much as a permission one. Leaving other
techniques in the search lets them win — a Gojo player saying *"purple"* could
lose to an enrolled *"fuga"* they could never use. Removing them makes what is
left the only thing competing, so matching gets **more** accurate the more
specific your sorcerer is. The server checks the same thing again.

## Chanting

Reciting an ability's incantation charges it. Heard cleanly, a full incantation
takes it straight to maximum output — which is what reciting one is for.

```
"Phase, Paramita, Pillars of Light"   -> Red, fully charged
"Reversal Red"                        -> fires it
```

Nothing about the charge is reimplemented, and neither is the setup. Starting a
chant runs the mod's own press handler, so the charge cost, the sorcerer check,
the base output, the charge animation and the charging effect all apply exactly
as they do from the keyboard — and if that handler refuses you (no Blue charges,
fewer than three Purple) its decision stands and nothing happens.

Saying the **ability's own name** instead of its incantation charges it one tier,
so output rises per chant. A **near miss** is worth half a tier, so two of them
add up to one and being slightly off is never simply wasted.

| Ability | Tiers | Default incantation |
| --- | --- | --- |
| Blue | 3 | *Cursed Technique Lapse: Blue* |
| Red | 3 | *Phase, Paramita, Pillars of Light* |
| Purple | 4 | *Imaginary Technique: Hollow Purple* |
| Dismantle | 3 | *Cursed Technique: Dismantle* |
| World Slash | 3 | *World Dismantling Slash* |

Those five are the only ones that can be charged, and the list is not arbitrary.
An ability can be chanted only if the mod actually climbs a charge counter for
it: Limitless and Fuga have no such counter, and Cleave's "hold" state *performs*
the technique rather than powering it up, so chanting Cleave would cast it.

The incantations above are a starting point, not scripture. Put whatever you
actually say in `chants`; matching is acoustic and has no dictionary, so it does
not care whether your wording is canon.

### Releasing

Three ways, all equivalent:

- say the ability's name
- say *"release"*
- tap its technique key

A chant you never release lapses after ten seconds, the same as letting the key
go without firing. Switching to another ability drops it too — you cannot hold
Red's charge while reaching for Blue.

### Coming from the old phrase file

The previous setup's `phrases.txt` used four keys the mod has no action for.
They were ability names, and they map straight across:

| Old key | Now |
| --- | --- |
| `hollow_purple` | `gojo_purple` (selection) |
| `reversal_red` | `gojo_red` (selection) |
| `lapse_blue` | `gojo_blue` (selection) |
| `cleave` | `sukuna_cleave` (selection) |

One phrase did move: *"dismantle"* now selects and charges the Dismantle stance,
and the immediate slash answers to *"kaisen"* or *"slash"* instead. They had to
be separated because one phrase cannot mean both "do it now" and "wind it up".

The rest are unchanged — you still say "hollow purple". Only the key
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
| `commands` | 26 commands | Command key to the phrases that trigger it |
| `chants` | 5 abilities | Ability to the incantations that charge it to full |
| `chantNearMultiplier` | `1.75` | How far past the threshold still counts as a near chant, worth half a tier. Ability names and incantations get this looser band; actions keep the tight one |
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
- **The client never decides what a word means.** It sends the key it heard and
  how well it heard it; select-versus-charge-versus-release is resolved server
  side, because it depends on state the client cannot see and would let a
  modified client ask for a release it had not earned.
- **Charging is the host mod's own code.** A chant drives the real press and
  release handlers and nudges the real counter onto the mod's own thresholds, so
  the multipliers, tier sounds and gates are identical by construction rather
  than by being kept in step.
- **Cancel only while armed.** The Simple Voice Chat hook intercepts audio only
  while the key is held, so proximity chat is untouched and other voice addons
  following the same rule can share the microphone.
- **Off the render thread.** Feature extraction and matching run on a background
  executor; only the result is applied on the client thread. This matters more
  here than in a single-skill addon, because the search is across every enrolled
  phrase rather than one.
