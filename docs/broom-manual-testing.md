# Real-client broom teleport comparison — 2026-11-10

The reported persistent infinite-flight behavior was **not reproduced** in this
reduced client, either with the cleanup enabled or disabled. The earlier automated
tests are not sufficient evidence that the original full-pack symptom is fixed.

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

## Interpretation and next reproduction target

The existing cleanup contract and before-first-tick race remain covered by
automated tests. This manual comparison establishes neither a full-pack
reproduction nor a full-pack fix. Other mod interactions, client/server differences,
and timing remain untested explanations. Reproduce the visible symptom in the
installed pack before changing production hooks further.

Launch this reduced comparison environment with:

```sh
./gradlew runClient -PforgeVersion=47.4.10 -PbalmVersion=7.3.44 -PaoeTestClient -PteleportTestClient
```

Use `run/config/allthetweaks_gregedition-common.toml` to select
`broomFlightCleanup=false` or `true`, then restart before the comparison.
The extra command mods are optional development dependencies, never release
dependencies. No production fix was changed for this investigation.
