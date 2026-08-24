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
| `/jjkvoice hud` | Drag the chant overlay somewhere else |
| `/jjkvoice reload` | Re-read `jjkvoice.json` |

`<technique>` tab-completes from the host mod's own command list, so there is
nothing to memorise.

## What a name does

Naming a technique is how you **throw** it, never how you wind it up.

| When you say an ability's name | What happens |
| --- | --- |
| It is not selected | It becomes your selection, as the radial menu does |
| It is selected | It comes out, at whatever charge is on it |

So *"Reversal Red"* fires Red. If an incantation has been building it, that is
the charge it goes out at; if not, it is a tap of the technique key and fires at
base output. Charging is the incantation's job and nothing else's — see
[Chanting](#chanting).

**Actions** skip selection entirely and happen at once: `domain_expansion`,
`kaisen`, `fuga`, and Inumaki's Cursed Speech words (`die`, `blast`, `crush`,
`burst`, `sleep`, `flee`, `rot`, `twist`, `burn`, `fall`, `spit`, `pull`,
`shrink`, `weep`, `kneel`, `dont_move`). Cursed Speech takes effect the moment it
is spoken and has no charge to build.

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

Reciting an ability's incantation charges it. **One line at a time** — you say a
line, it charges a tier, and getting to the end of the incantation carries the
technique to full output.

```
"Phase"               ->  selects Red, charges one tier
"Paramita"            ->  two
"Pillars of Light"    ->  full charge  (incantation complete)
"Reversal Red"        ->  fires it, at full
```

Saying *"Reversal Red"* at any point fires whatever is on it, so a half-recited
incantation still throws a half-charged technique.

Lines are not optional flavour, they are how it has to work. Recognition matches
one fixed utterance at a time rather than listening continuously, and a whole
incantation in one breath runs past the clip length limit. Saying it in pieces is
also simply what reciting one sounds like.

Only reciting **in order, to the end** tops the technique out. A line out of
order still charges its tier, so nothing is wasted, but starting from the last
line gains you nothing that saying the ability's name would not.

Nothing about the charge is reimplemented, and neither is the setup. Starting a
chant runs the mod's own press handler, so the charge cost, the sorcerer check,
the base output, the charge animation and the charging effect all apply exactly
as they do from the keyboard — and if that handler refuses you (no Blue charges,
fewer than three Purple) its decision stands and nothing happens.

A **near miss** on a line is worth half a tier, so two of them add up to one and
being slightly off is never simply wasted. Nothing else charges: an ability's
name throws it, which is why an incantation is the only way to get a technique
out above base output by voice.

| Ability | Incantation |
| --- | --- |
| Blue | *Phase* → *Twilight* → *Eyes of Wisdom* |
| Red | *Phase* → *Paramita* → *Pillars of Light* |
| Purple | *Nine Ropes* → *Polarized Light* → *Crow and Declaration* → *Between Front and Back* |
| Dismantle | *Dragon Scales* → *Repulsion* → *Twin Meteor* |

Each has exactly as many lines as the ability has output tiers, so reciting one
through lands on maximum and no line is spare.

Blue and Red both open on *"Phase"*. That line is enrolled once — the two sound
identical and no amount of bookkeeping would separate them — and is read as
belonging to whichever ability you have **selected**, so select before you recite
when the opening is shared.

Those four are the only ones that can be charged, and the list is not arbitrary.
An ability can be chanted only if the mod actually climbs a charge counter for
it: Limitless and Fuga have no such counter, and Cleave's "hold" state *performs*
the technique rather than powering it up, so chanting Cleave would cast it. World
Slash has no voice or chant handling at all, deliberately.

Put whatever you actually say in `chants`, split however you actually pause —
matching is acoustic and has no dictionary, so it does not care whether your
wording is canon. The lines are **in order**, and a line must not collide with a
command phrase, where there would be no way to resolve it.

## Dismantle

Dismantle is the one technique where the chant does not decide what comes out,
only how much of it. Wind it up, then say which shape you want, and the same
charge is spent differently by each.

| Say | What comes out |
| --- | --- |
| *"Dismantle"* (or *"Kaisen"*) | One slash, at the charged power |
| *"Dismantle Net"* | A net — 3×3, 4×4 or 5×5 by tier chanted |
| *"Dismantle Barrage"* | A stream of slashes, lasting longer the more you chanted |

The barrage's slashes are all **base, unchanted** Dismantles however far you
recited — the chant buys duration, not power. That is not a rule imposed here:
the barrage never reads the charge multiplier in the first place.

All three are **projectiles**. Dismantle's own release picks the precision
raycast when that toggle is on, but a spoken one is always thrown, because there
is nothing being aimed at the moment the word lands.

Saying any of the three selects Dismantle first if it is not already, so a chant
can start with *"Dragon Scales"* from anywhere.

`/jjkvoice enroll <ability>` records the ability's name and its incantation lines
together, since a line with no voiceprint can never match.

### The overlay

The lines you can say next are listed on screen, so an incantation is something
you read rather than memorise.

At rest it shows the opening line of every chantable ability your technique
includes. Say one and it narrows to what could follow: because Blue and Red both
open on *"Phase"*, saying it leaves **both** rows up — *Twilight* and *Paramita* —
and the line you say next is what chooses between them. Nothing is charged while
it is still undecided; the tiers owed are banked and paid the moment one wins.

Once an incantation is recited through, the row shows the word that throws it and
the pips fill. Lines with no voiceprint are dim and struck, because saying one
cannot work however clearly you say it.

`/jjkvoice hud` opens an editor: drag it where you want, it snaps to edges and
centre lines, Esc saves. The position is kept as a fraction of the screen, so it
stays put if you change resolution or GUI scale. `hudEnabled` turns it off.

### Releasing

Three ways, all equivalent:

- say the ability's name
- say *"release"*, which fires whatever is charged without naming it
- tap its technique key

A chant you never release lapses after ten seconds, the same as letting the key
go without firing. Hollow Purple has a wind-up of its own on top of that: it will
not come out for the first two and a half seconds, and calling it inside that
window dismisses it instead. Reciting its four lines takes longer than that
anyway, so it only bites if you try to rush it. Switching to another ability drops it, and any part-recited
incantation with it — you cannot hold Red's charge while reaching for Blue.

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
| `chants` | 5 abilities | Ability to its incantation, as ordered lines |
| `chantNearMultiplier` | `1.75` | How far past the threshold still counts as a near chant, worth half a tier. Ability names and incantations get this looser band; actions keep the tight one |
| `shoutCommand` | `domain_expansion` | What `shout` mode triggers |
| `thresholdMultiplier` | `1.35` | How far past your own natural variation still counts. Raise if it refuses you, lower if unrelated words trigger it |
| `absoluteMaxDistance` | `60.0` | Safety ceiling so an inconsistent enrollment cannot accept everything |
| `enrollmentSamples` | `3` | Recordings taken per phrase. More is steadier and slower |
| `minSpeechSeconds` / `maxSpeechSeconds` | `0.25` / `3.0` | Clips outside this range are discarded before any matching |
| `maxIncantationSeconds` | `6.0` | The same ceiling for a recited line, which is allowed to run longer. A long clip is only kept if a line is what it matched |
| `shoutRmsThreshold` | `0.06` | Loudness required in `shout` mode |
| `announceMatches` | `true` | Print the matched phrase and distance. Useful while tuning |
| `hudEnabled` | `true` | Show the on-screen list of lines you can say next |
| `hudX` / `hudY` | `0.012` / `0.62` | Where it sits, as fractions of the screen. Set these by dragging in `/jjkvoice hud` |

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
