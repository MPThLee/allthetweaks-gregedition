# Real-client broom teleport comparison — 2026-11-10

The reduced-client comparison below did not reproduce persistent flight permission.
The subsequent ATM9 Expert 0.7.2 comparison **did reproduce it** with cleanup
disabled: `/home`, `/back`, and `/spawn` left both client and server with
`mayfly=true` after dismounting. The corrected 0.2.1 dedicated-server build clears
permission on both sides for all three commands. See the full-pack observations below.

## Environment

Minecraft 1.20.1, Forge 47.4.10, EvilCraft 1.3.5, Cyclops Core 1.22.3,
Waystones 14.1.21, Balm 7.3.44, GregTech CEu 7.5.3, JEI 15.59.0.210,
FTB Essentials 2001.2.4, FTB Library 2001.2.13, Architectury 9.2.14.

FTB Essentials and Library were selected from the ATM9 Expert 0.7.1 manifest's
CurseForge file IDs 7609948 and 8226927. Its
`overrides/defaultconfigs/ftbessentials-server.snbt` was used, including zero
teleport warmup and the normal command cooldowns. This was the development
singleplayer client with a real player, not FakePlayer or the entire modpack.

## Procedure and observations

Used the visible client through the computer-use skill, with command results
verified in Minecraft's client/server log. Backed up the development world first.
Restarted between disabled and enabled configurations. Reset only the FTB player
data created by this test between comparisons so cooldowns started fresh.

1. In Survival, set an Overworld home with `/sethome`.
2. Teleport to the Nether roof, mount the broom by right-clicking its item, and
   inspect `/data get entity @s abilities`.
3. Run `/home`, then inspect abilities and dimension again.
4. Mount again, run `/back` to return to the Nether, and repeat the inspections.
5. Mount again, run `/spawn` to return to the Overworld, and repeat the inspections.

The disabled run was repeated with a blood-filled broom. An airborne mounted
player was observed at Y=132.564 above the Nether roof. No forced player-flight
flag was used in this manual comparison. An attempted `/home` during its cooldown
was rejected and was not counted as a teleport; the successful retry was observed
after the cooldown expired.

| Command | Cleanup disabled | Cleanup enabled |
| --- | --- | --- |
| `/home`, Nether → Overworld | Dismounted; `mayfly=0`, `flying=0` | Dismounted; `mayfly=0`, `flying=0` |
| `/back`, Overworld → Nether | Dismounted; `mayfly=0`, `flying=0` | Dismounted; `mayfly=0`, `flying=0` |
| `/spawn`, Nether → Overworld | Dismounted; `mayfly=0`, `flying=0` | Dismounted; `mayfly=0`, `flying=0` |

During these actual broom rides, the server reported `mayfly=1` but `flying=0`.
This differs from `BroomFlightTests`, which explicitly sets `flying=true` to verify
cleanup of active flight. That injected state explains why a failing automated
baseline alone must not be called a reproduction of ordinary visible broom flight.

Local raw evidence is retained in `build/reports/manual-broom/ftb-disabled.log`
and `ftb-enabled.log`. Examples of post-command ability observations:

- Disabled: 11:01:18 (`/home`), 11:01:57 (`/back`), 11:03:01 (`/spawn`).
- Disabled, blood-filled: 11:05:35 (`/spawn`), 11:05:40 (`/back`), 11:06:27 (`/home`).
- Enabled: 11:07:53 (`/home`), 11:08:05 (`/back`), 11:08:10 (`/spawn`).

## Interpretation of the reduced run

The existing cleanup contract and before-first-tick race remain covered by
automated tests. That reduced comparison established neither a full-pack
reproduction nor a full-pack fix. The investigation below subsequently tested the
installed pack and identified the missing interaction.

Launch this reduced comparison environment with:

```sh
./gradlew runClient -PforgeVersion=47.4.10 -PbalmVersion=7.3.44 -PaoeTestClient -PteleportTestClient
```

Use `run/config/allthetweaks_gregedition-common.toml` to select
`broomFlightCleanup=false` or `true`, then restart before the comparison.
The extra command mods are optional development dependencies, never release
dependencies. No production fix was changed for this investigation.

## ATM9 Expert 0.7.2 full-pack comparison

Used an isolated copy of the installed Prism instance, including its mods, config,
defaultconfigs, and KubeJS scripts. The client loaded 347 JARs including this tweak
mod (368 Forge mod entries). Relevant versions: Forge 47.4.10, EvilCraft 1.3.5,
Cyclops Core 1.22.4, Mekanism 10.4.16.80, FTB Essentials 2001.2.4. The client ran
locally as offline player `Dev`; the dedicated server was bound to localhost with
`online-mode=false`. The original installing instance was not used as a test world.

A temporary read-only Java agent sampled the real player's abilities, vehicle, and
dimension on the client/render and server threads. A separate read-only probe
checked `BroomFlightTweak.enabled()` in memory: editing the config file alone did
not change it in this pack, so the enabled comparison used a restarted process.
No probe set ability flags. A Creative → Survival command cycle cleared leaked
permission between cases; every measured ride started in Survival without flight.
Mounting used the actual blood-filled EvilCraft broom item.

| Environment | `/home` Nether → Overworld | `/back` Overworld → Nether | `/spawn` Nether → Overworld |
| --- | --- | --- | --- |
| Full-pack singleplayer, cleanup disabled | Both sides retain `mayfly=true` | Both sides retain `mayfly=true` | Both sides retain `mayfly=true` |
| Full-pack singleplayer, 0.2.0 cleanup enabled | Both sides retain `mayfly=true` | Both sides retain `mayfly=true` | Both sides retain `mayfly=true` |
| Full-pack dedicated server, cleanup disabled | Both sides retain `mayfly=true` | Both sides retain `mayfly=true` | Both sides retain `mayfly=true` |
| Full-pack dedicated server, 0.2.1 cleanup enabled | Both sides clear `mayfly` and `flying` | Both sides clear `mayfly` and `flying` | Both sides clear `mayfly` and `flying` |

In each failed case the vehicle was absent after teleport and permission remained
set on subsequent samples. `flying` remained false during these rides and snapshots;
these observations establish leaked flight permission, not a measured duration of
active flight. Short synthetic jump inputs did not reliably activate flight, and
were not counted as an active-flight assertion.

The enabled 0.2.0 run showed that the broom mixin did acquire a lease with prior
permission `false`, and removed that lease on dismount. Mekanism's
`PlayerState.updateFlightInfo` caches the broom-granted permission each player tick;
`reapplyServerSideOnly` can then restore that stale snapshot during dimension
change. A focused test with the real Mekanism methods reproduced this independently
of the full pack: mounted teleport and dismount-then-teleport both failed before the
fix; preexisting flight passed. No artificial active-flight flag is used by these
three tests.

The final network comparison used 0.2.1 on the server while the running client
still used 0.2.0. It therefore verified the correction from the server alone.
Successful post-command samples were recorded at 03:09:13 UTC (`/home`),
03:09:35 (`/back`), and 03:09:54 (`/spawn`), with no vehicle and both flight flags
false on both sides. Each command followed a verified real broom mount; no
gamemode reset was needed between these final cases. The client instance now has
0.2.1 on disk for its next launch. A separate 0.2.1 singleplayer run was not part
of this final comparison.

Version 0.2.1 refreshes that cache using `Mekanism.playerState.updateFlightInfo` when
an owned broom lease is released, after restoring permission. Mekanism is optional;
this adds no periodic scan or new tick handler. The existing 40 tests pass without
Mekanism, and all 43 tests pass with it. Run the focused profile with:

```sh
./gradlew runGameTestServer -PmekanismBroomTest -PforgeVersion=47.4.10 -PbalmVersion=7.3.44
```

The dedicated copy excludes three client-only JARs that prevent server startup:
Mekalus/Oculus, EuphoriaPatcher, and Sodium Extras. The copied flat development
world stalled in an unrelated Tombstone/EvilCraft structure lookup, so the working
network comparison used a fresh normal world. A temporary native field-watch
attempt made startup slow enough to trigger the watchdog during MEGA Cells recipe
initialization; it produced no useful flight trace and was removed before final
validation. No pack mod was removed to hide a flight failure.

Local evidence is stored under `build/reports/manual-broom/`, including
`pack-mods-sha256.json`, the client/server flight samples, runtime config checks,
and the failed/passing Mekanism regression logs. These runtime probes and native
experiments are temporary test tooling, not shipped in the mod.
