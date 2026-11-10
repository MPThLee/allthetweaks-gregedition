# All The Tweaks: Greg Edition

A standalone, configurable compatibility-tweaks mod for Minecraft 1.20.1 / Forge.
The name is inspired by [All The Tweaks](https://github.com/AllTheMods/AllTheTweaks).
This is an independent project: GregTech and the ATM modpack are not required.
Java packages use `dev.mpthlee.allthetweaks`.

## Generated Waystone protection

Fixes [ATM9 Expert issue #36](https://github.com/AllTheMods/atm9-expert/issues/36):
AoE tools can remove world-generated Waystones even when Waystones sets
`restrictions.generatedWaystonesUnbreakable = true`.

Waystones checks permission in its normal player mining-progress calculation.
Other mining code can skip that calculation and directly run destruction callbacks,
request drops, or replace the block. A canceled block replacement alone is too late
if another callback has already destroyed the other half or produced loot.

This mod protects shared APIs rather than patching individual tools:

- Cancels Forge break events for protected generated Waystones.
- Cancels `Level.destroyBlock` before drops and callbacks run.
- Rejects destructive `LevelChunk.setBlockState` calls, also covering ordinary
  level removal/replacement and direct chunk writes.
- Stops Waystones' early `playerWillDestroy` side effects.
- Suppresses normal loot queries for protected Waystones so collecting loot before
  attempting removal cannot duplicate them.

Both halves, their block entities, and the registered teleport destination survive.
Player-placed Waystones and unrelated blocks keep their existing behavior. Rotation
and waterlogging updates remain possible. Normal creative mining still works; its
exemption is scoped to the target Waystone and cleared even if a callback throws.
Legacy generated destinations with an `UNKNOWN` blockstate origin are also protected.

Actorless removal APIs fail closed for protected Waystones, including command/world
editing calls using those APIs. Administrators can use creative mining or temporarily
disable this tweak. The fix specifically concerns generated Waystones; it does not
introduce new rules for player-placed global Waystones.

The coverage is independent of tool name and AoE shape. Mods that directly modify
chunk-section storage or invent and spawn item stacks without normal loot APIs can
still bypass these hooks. This is not a claim that every mod in the full pack has
been individually playtested.

## Install and configure

Build with `./gradlew build`, then put
`build/libs/allthetweaks-gregedition-0.1.0.jar` in the server's `mods` directory
(or the local instance's `mods` directory for singleplayer). No client-only code
or networking is added. Waystones is optional; its integration stays inactive when
it is absent. This build targets Waystones 14.1.21 and Forge 47.3.0 or newer on 1.20.1.

`config/allthetweaks_gregedition-common.toml`:

```toml
[tweaks]
waystoneProtection = true
```

Protection applies only while this switch **and** Waystones'
`restrictions.generatedWaystonesUnbreakable` are true. Turning the tweak off restores
Waystones' original behavior. Future tweaks should have independent switches.

## Build and verification

Use Java 17. Dependencies and the Gradle distribution are version-pinned; the wrapper
also verifies the distribution checksum. Development runs use Waystones 14.1.21,
Balm 7.3.38, and Forge 47.3.0.

```sh
./gradlew build
./gradlew runGameTestServer
./gradlew runGameTestServer -PwithoutWaystones
./gradlew runGameTestServer -PforgeVersion=47.4.10 -PbalmVersion=7.3.44
# Full disabled/enabled/optional-dependency verification:
./scripts/verify.sh
```

The verification script first requires the disabled-tweak run to reproduce the AoE
failure, then builds the JAR and requires the enabled suite and optional-dependency
smoke test to pass. Logs are saved under `build/reports/verification/`. GameTests are
excluded from the released JAR. Generated regression worlds under `run-gametest/`
are reset for each run, and the absent-mod smoke test uses a separate directory.
The Gradle task verifies the expected passing test count, so zero discovered tests
cannot silently pass.

It also runs the enabled suite with Forge 47.4.10 and Balm 7.3.44 from the
[ATM9 Expert 0.7.1 manifest](https://www.curseforge.com/minecraft/modpacks/all-the-mods-9-expert/files/9474720).
That manifest pins [Waystones 14.1.21](https://www.curseforge.com/minecraft/mc-mods/waystones/files/8849482)
and [Balm 7.3.44](https://www.curseforge.com/minecraft/mc-mods/balm/files/8969708).

The suite covers 25 real Minecraft/Forge GameTests: removal APIs, events, early
callbacks, loot-before-removal, an AoE loop around ordinary blocks, all six Waystone
variants, wilderness/village/dungeon origins, legacy data, configuration switches,
creative mining and cleanup, and allowed state changes. A separate test starts
without either Waystones or Balm and checks ordinary mining.

Verified on 2026-11-10:

| Run | Result |
| --- | --- |
| Tweak disabled, generated protection enabled | 19 expected failures, including AoE destruction |
| Forge 47.3.0 / Balm 7.3.38 | All 25 tests passed |
| ATM9 Expert versions: Forge 47.4.10 / Balm 7.3.44 | All 25 tests passed |
| Without Waystones or Balm | Startup/mining smoke test passed |
| Release packaging | Build/reobfuscation passed; all four mixins/refmaps included; tests excluded |

There are no tick handlers, world scans, stack inspection, or allocations on the
ordinary-block mutation path. The enabled chunk guard adds one existing-state read
and a type check. An informational warmed microbenchmark measured approximately
182 ns/mutation with the tweak disabled versus 295 ns enabled on this development
machine. This measures direct chunk mutations, not whole-pack TPS; timings vary.
