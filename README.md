# All The Tweaks: Greg Edition

A standalone, configurable compatibility-tweaks mod for Minecraft 1.20.1 / Forge.
The name is inspired by [All The Tweaks](https://github.com/AllTheMods/AllTheTweaks).
This project is independent; it does not require GregTech or the ATM modpack.

**Status: development scaffold, not a working protection mod yet.** The first
regression tests are written but have not been compiled or executed. No fix mixin
is installed, and the configuration switch currently has no effect.

## First fix: generated Waystone protection

[ATM9 Expert issue #36](https://github.com/AllTheMods/atm9-expert/issues/36)
reports that AoE / 3×3 tools can break Waystones intended to be unbreakable.
The pack's [Waystones configuration](https://github.com/AllTheMods/atm9-expert/blob/main/config/waystones-common.toml)
sets `restrictions.generatedWaystonesUnbreakable = true`.

Required behavior:

- Preserve generated Waystones when an AoE tool mines an adjacent block.
- Protect both halves, block entities, and Waystones' registered destination data.
- Reject the operation before item drops or other destructive side effects occur.
- Preserve normal mining of player-placed Waystones and unrelated blocks.
- Respect Waystones configuration; provide an independent switch for this fix.
- Work across tool mods through shared block-removal paths, without a growing list
  of patches for individual pickaxes.
- Avoid tick handlers, world scans, stack inspection, and allocations in ordinary
  block operations. Measure overhead before making performance claims.
- Allow installation without Waystones; future integrations must remain optional.

The proposed scope covers normal Minecraft/Forge breaking and replacement APIs.
Direct chunk mutation and mods that independently generate item drops require
separate investigation; absolute compatibility with arbitrary mods is not promised.
Creative/admin removal, global Waystones, explosions, same-block state changes,
and configuration-disabled behavior need explicit regression coverage before release.

## Build and reproduce

Use Java 17 and a network-enabled Gradle environment:

```sh
./gradlew build
./gradlew runGameTestServer
```

Development dependencies are pinned to Forge 47.3.0, Waystones 14.1.21, and
Balm 7.3.38. The exact ATM9 Expert instance versions still need confirmation.

GameTests are separate from the shipped JAR. The dedicated test run directory is
`run-gametest/`; its Waystones configuration is regenerated with protection enabled.
Tests exercise real Waystone blocks and the level destruction/replacement APIs.
The protection tests are expected to fail on the current unpatched baseline;
the player-placed and protection-disabled controls should pass. Protection tests
check both halves, block entity identity, registered destination preservation,
return values, and absence of dropped items. These are API-level regression tests,
not yet a reproduction with GregTech or Apotheosis tools installed.

Current environment blocker: the Gradle wrapper cannot resolve
`services.gradle.org`. A separate permitted curl download obtained Gradle 8.8,
but the next attempt failed when Gradle opened its local daemon socket:
`java.net.SocketException: Operation not permitted`. Compilation and the GameTests
have not run. Running them requires an environment that permits Gradle's local
socket and dependency downloads.

## Configuration

The scaffold registers `config/allthetweaks_gregedition-common.toml`:

```toml
[tweaks]
waystoneProtection = true
```

This is reserved for the first implementation. It does not protect blocks yet.
Each additional tweak should have its own setting and optional-mod boundary.
