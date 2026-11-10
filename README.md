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

- Reports unbreakable hardness for protected Waystones, letting AoE planners skip them
  and finish mining surrounding blocks.
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

See the [full-pack tool/miner comparison with Unbreakables 1.20.1](docs/waystone-mining-comparison.md)
for the tested paths, separate configurations, and limits of those checks.

## EvilCraft broom flight cleanup

Fixes temporary flight remaining after dismounting an
[EvilCraft Broom](https://evilcraft.rubensworks.net/book/second_age/tools/broom/introduction.html),
including dimension teleports. In EvilCraft 1.3.5, the rider cache is filled on the
next tick, so immediate dismounts can miss cleanup. Its cleanup also leaves the
active `flying` flag unchanged and does not synchronize abilities.

The tweak records flight permission before the broom grants it, then restores it
when the actual passenger leaves. It clears unauthorized active flight and sends
an ability update. Forge dimension transfers that retain a broom in the old world
also detach that stale ride. Command teleports and ordinary dismounts use the same
cleanup; there is no periodic player scan or blanket flight reset on teleport.

When Mekanism 10.4 is installed, releasing the broom's flight also refreshes
Mekanism's flight cache through its normal API. Otherwise, its dimension-change
handler can restore the broom's cached permission after dismount cleanup. This
interaction was reproduced in ATM9 Expert 0.7.2 and in a focused integration test.

Creative/spectator flight and survival flight present before mounting are preserved.
Vanilla's shared flight boolean cannot identify another mod granting or revoking
flight during an existing ride; this is not a general flight-provider arbitration
system. It also does not guess ownership of leaked flight from before installation.
The implementation targets [EvilCraft 1.3.5's broom lifecycle](https://github.com/CyclopsMC/EvilCraft/blob/master-1.20-lts/src/main/java/org/cyclops/evilcraft/entity/item/EntityBroom.java).

## Install and configure

Build with `./gradlew build`, then put
`build/libs/allthetweaks-gregedition-0.2.2.jar` in the server's `mods` directory
(or the local instance's `mods` directory for singleplayer). No client-only code
or custom networking is added. Waystones and EvilCraft are optional; each integration
stays inactive when its mod is absent. This build targets Waystones 14.1.21,
EvilCraft 1.3.5, and Forge 47.3.0 or newer on 1.20.1.

`config/allthetweaks_gregedition-common.toml`:

```toml
[tweaks]
waystoneProtection = true
broomFlightCleanup = true
```

Protection applies only while this switch **and** Waystones'
`restrictions.generatedWaystonesUnbreakable` are true. Turning the tweak off restores
Waystones' original behavior. The broom switch is independent; disabling it prevents
new flight tracking, while already tracked rides still clean up on dismount.

## Build and verification

Use Java 17. Dependencies and the Gradle distribution are version-pinned; the wrapper
also verifies the distribution checksum. Development runs use Waystones 14.1.21,
Balm 7.3.38, EvilCraft 1.3.5, Cyclops Core 1.22.3, and Forge 47.3.0.

```sh
./gradlew build
./gradlew runGameTestServer
./gradlew runGameTestServer -PwithoutOptionalMods
./gradlew runGameTestServer -PforgeVersion=47.4.10 -PbalmVersion=7.3.44
# Interactive AoE testing with GregTech CEu 7.5.3 and JEI 15.59.0.210:
./gradlew runClient -PforgeVersion=47.4.10 -PbalmVersion=7.3.44 -PaoeTestClient
# Add the pack's FTB Essentials teleport commands for interactive testing:
./gradlew runClient -PforgeVersion=47.4.10 -PbalmVersion=7.3.44 -PaoeTestClient -PteleportTestClient
# Full disabled/enabled/optional-dependency verification:
./scripts/verify.sh
```

The `aoeTestClient` flag adds development runtime dependencies only. It does not
bundle GregTech or JEI in the release. For manual Waystone tests, enable generated
Waystone protection in `run/config/waystones-common.toml`, use a generated Waystone,
and mine adjacent blocks with a mining hammer in Survival. Creative mining is
intentionally allowed, and player-placed Waystones are outside this protection.

The verification script requires separate disabled-tweak runs to reproduce both the AoE
and broom flight failures, then builds the JAR and requires the enabled suite and optional-dependency
smoke test to pass. Logs are saved under `build/reports/verification/`. GameTests are
excluded from the released JAR. Generated regression worlds under `run-gametest/`
are reset for each run, and the absent-mod smoke test uses a separate directory.
The Gradle task verifies the expected passing test count, so zero discovered tests
cannot silently pass.

It also runs the enabled suite with Forge 47.4.10 and Balm 7.3.44 from the
[ATM9 Expert 0.7.1 manifest](https://www.curseforge.com/minecraft/modpacks/all-the-mods-9-expert/files/9474720).
That manifest pins [Waystones 14.1.21](https://www.curseforge.com/minecraft/mc-mods/waystones/files/8849482)
and [Balm 7.3.44](https://www.curseforge.com/minecraft/mc-mods/balm/files/8969708).

The suite covers 42 real Minecraft/Forge GameTests. The 27 Waystone tests cover: removal APIs, events, early
callbacks, loot-before-removal, an AoE loop around ordinary blocks, all six Waystone
variants, wilderness/village/dungeon origins, legacy data, configuration switches,
creative mining and cleanup, and allowed state changes. The 15 broom tests cover immediate/after-tick teleports,
command and Forge dimension transfers, normal dismounts, broom removal, ability
updates, creative/spectator and prior survival flight, repeated grants, configuration
changes, and unrelated flight. A separate test starts without Waystones, Balm,
EvilCraft, or Cyclops Core and checks ordinary mining.

These are headless integration tests with the real mod entities and Minecraft APIs;
they do not constitute a full-pack interactive client playtest of every `/back` or
`/spawn` command provider.

The broom tests explicitly set the player's active `flying` flag to exercise cleanup
of that state. A real broom ride does not necessarily set that flag. Consequently,
those tests establish the cleanup contract and immediate-mount race, not a complete
reproduction of visible infinite flight in the modpack. See
[the manual FTB command test results](docs/broom-manual-testing.md).

Verified on 2026-11-10:

| Run | Result |
| --- | --- |
| Waystone tweak disabled | Expected AoE destruction reproduced |
| Broom tweak disabled | Expected leaked flight reproduced |
| Forge 47.3.0 / Balm 7.3.38 | All 42 tests passed |
| ATM9 Expert versions: Forge 47.4.10 / Balm 7.3.44 | All 42 tests passed |
| With Mekanism 10.4.16.80 | All 45 tests passed; two new stale-cache tests failed before the cache-refresh fix |
| ATM9 Expert 0.7.2 dedicated server + real client | `/home`, `/back`, `/spawn`: stale permission reproduced with cleanup disabled; 0.2.1 clears both sides |
| With GregTech 7.5.3 | All 44 tests passed, including lower/upper-half hammer AoE controls |
| Without optional mods | Startup/mining smoke test passed |
| Release packaging | Build/reobfuscation passed; both mixin configs and all six mixins/refmap included; tests excluded |

There are no tick handlers, world scans, stack inspection, or allocations on the
ordinary-block mutation path. The enabled chunk guard adds one existing-state read
and a type check. An informational warmed microbenchmark measured approximately
182 ns/mutation with the tweak disabled versus 295 ns enabled on this development
machine. This measures direct chunk mutations, not whole-pack TPS; timings vary.


## Adding another tweak

See [CONTRIBUTING.md](CONTRIBUTING.md). Each fix lives under its own
`dev.mpthlee.allthetweaks.tweak.<name>` package with an independent configuration
switch, hooks, mixin configuration, and regression tests. The mod entry point only
registers configuration and delegates lifecycle work to the explicit tweak registry.

## Automated builds and releases

GitHub Actions builds and runs `scripts/verify.sh` on branch pushes, pull requests,
and version tags. You can also start it manually from the Actions tab. Successful
runs provide a `release-jar` artifact containing the reobfuscated mod JAR and its
SHA-256 checksum. Verification logs are uploaded even when a check fails.

To publish a release, update `version` in `build.gradle`, commit and push it, then
push a matching tag (for example, `v0.2.2` for version `0.2.2`):

```sh
git tag v0.2.2
git push origin v0.2.2
```

The tag must match the built JAR's version. After all checks pass, Actions creates
a GitHub release with generated notes, the tested JAR, and `SHA256SUMS.txt`. No
extra repository secret is required; publishing uses GitHub's built-in token.
