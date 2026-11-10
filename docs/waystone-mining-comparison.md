# Waystone mining comparison — ATM9 Expert 0.7.2

Minecraft 1.20.1, Forge 47.4.10, Waystones 14.1.21, Balm 7.3.44.
This tests server-side protection in separate copies of the full pack. The original
Prism instance was not changed. Both servers retain the pack's original
`allthetweaks-1.20.1-47.2.20-2.4.4-all.jar`. Compared with the original installation,
the only excluded JARs are the client-only EuphoriaPatcher, Mekalus, and Sodium Extras;
the only added JAR is the protection mod being tested.

## Separate configurations

1. Our 0.2.2 build on the server, Unbreakables absent, `waystoneProtection = true`.
2. Unbreakables 20.1.10 on the server, our JAR absent from **both server and client**,
   `rulesets = ["waystones:generated_waystones"]`, `rules = []`.
3. Baseline: our protection disabled, Unbreakables absent.

The same unchanged base-pack client, containing neither protection mod, connects to
each server. This also exercises their server-only installation support.

Waystones' `restrictions.generatedWaystonesUnbreakable = true` in every setup.
The Unbreakables ruleset is supplied by the installed Waystones JAR, not a custom
rule invented for this comparison.

## Method and scope

Each fixture has a 3×3 stone plane with a Waystone replacing the east cell.
Generated lower-half and upper-half targets are separate cases. Checks include
both original block entities, destination registration, loose Waystone drops,
and all eight ordinary stone cells. Player-origin fixtures are compared against
the protection-disabled baseline. Creative lower/upper breaking is checked separately.

Diagnostic calls run on the dedicated server thread using the pack's actual loaded
classes. The 110 our-mod/baseline cases used the connected base-pack client. All 66
Unbreakables cases were run with a server test player and repeated with that same
connected client, producing identical results. Some tests invoke native mining
routines directly; the table distinguishes
these from full tool callbacks and eligibility checks. This is **not** an interactive
playtest of every material, upgrade, AoE size, spell, or automated machine in the pack.
No claim of a powered Digital Miner or Mining Gadgets laser cycle is made.

## Results

The following cells describe generated lower-half targets. Upper-half results and
player-origin controls are recorded separately in the raw logs.

| Tool / path | Test level | Waystones config only | Our 0.2.2 | Unbreakables 20.1.10 |
|---|---|---|---|---|
| GregTech diamond mining hammer | Tool callback | **Removed**; 8/8 stone | Preserved; 8/8 stone | Preserved; 4/8 stone |
| Apotheosis radial affix | 3×3 affix routine | Preserved; 8/8 stone | Preserved; 8/8 stone | Preserved; 8/8 stone |
| ProjectE red matter hammer | Charged `digAOE`; EMC cost set to zero | **Removed**; 8/8 stone | Preserved; 8/8 stone | Preserved; 8/8 stone |
| Mystical Agriculture mining augment | `harvestAOEBlocks` routine | Preserved; 8/8 stone | Preserved; 8/8 stone | Preserved; 8/8 stone |
| RFTools Builder clearing quarry | `quarryBlock` with output chest | **Removed**; 8/8 stone | Preserved; 8/8 stone | Preserved; 8/8 stone |
| Mekanism Digital Miner | `canMine` eligibility only | Accepts generated | Rejects generated / accepts stone | Rejects generated / accepts stone |
| Thermal Extra abyssal hammer | Tool callback / CoFH event | **Removed**; 8/8 stone | Preserved; 8/8 stone | Preserved; 8/8 stone |
| Redstone Arsenal flux hammer | Charged tool / CoFH event | **Removed**; 8/8 stone | Preserved; 8/8 stone | Preserved; 8/8 stone |
| Tinkers’ Construct sledge hammer | Built tool; fixed random material seed | Preserved; 8/8 stone | Preserved; 8/8 stone | Preserved; 8/8 stone |
| Mining Gadgets | Charged `canMineBlock` eligibility only | Accepts generated | Rejects generated / accepts stone | Rejects generated / accepts stone |
| Mekanism AoE/vein mining | `veinMineArea`; supplied energy, zero energy cost | **Removed**; 8/8 stone | Preserved; 8/8 stone | Preserved; 8/8 stone |
| Draconic Evolution chaotic pickaxe | `breakAOEBlock` routine | **Removed**; 8/8 stone | Preserved; 8/8 stone | Preserved; 8/8 stone |
| Direct block replacement | Actorless `Level.setBlock` control | **Removed** | Preserved | **Removed** |
| Direct survival destruction | `ServerPlayerGameMode.destroyBlock`; bypasses mining progress | **Removed** | Preserved | Preserved |
| PneumaticCraft jackhammer | Air, netherite bit, full 3×3 mode | Preserved; 8/8 stone | Preserved; 8/8 stone | Preserved; 8/8 stone |
| Immersive Engineering drill | Steel head and biodiesel | Preserved; 8/8 stone | Preserved; 8/8 stone | Preserved; 8/8 stone |
| Industrial Foregoing infinity drill | Fueled mining worker; targeting invoked explicitly | Preserved; 8/8 stone | Preserved; 8/8 stone | Preserved; 8/8 stone |
| Just Dire Things eclipse alloy pickaxe | Charged, hammer upgrade enabled | Preserved; 8/8 stone | Preserved; 8/8 stone | Preserved; 8/8 stone |
| Silent Gear iron hammer | Native target selection + server destruction; no client packet test | **Removed**; 8/8 stone | Preserved; 8/8 stone | Preserved; 8/8 stone |
| FTB Ultimine area mode | Server-player event; shape index 2 | Preserved; 8/8 stone | Preserved; 8/8 stone | Preserved; 8/8 stone |
| Create mechanical drill | Native `canBreak` + `onBlockBroken`; no powered cycle | **Removed**; 8/8 stone | Preserved; 8/8 stone | **Removed**; 8/8 stone |
| Botania Terra Shatterer | Enabled; 2,000,000 mana | **Removed**; 8/8 stone | Preserved; 8/8 stone | Preserved; 8/8 stone |

## Regression fixed during this comparison

Before 0.2.2, our guard preserved the Waystone but GregTech stopped its AoE loop
at the first rejected removal: only four of eight ordinary cells were mined.
Protected Waystones now report hardness -1, so GregTech excludes them during target
selection and mines all eight ordinary cells. Lower-level replacement and loot guards
still cover callers that bypass hardness checks. Ordinary blocks return through the
fast non-Waystone path; there is no scanning or per-tick protection task.

The hardness check and initial GregTech regression test failed before this change.
Verification after it:
42 default GameTests, 44 with GregTech, 45 with Mekanism, optional-mod-absent smoke test,
and both intentionally failing disabled baselines all behaved as expected.

## Recommendation

For generated-Waystone protection in this pack, use our mod. Both configurations
preserved Waystones in most tested player mining paths, but Unbreakables allowed the
Create mechanical-drill routine and direct block replacement to remove both halves
and unregister the destination. Its GregTech protection also aborted neighboring
mining after four of eight stone cells, while ours completed all eight.

The Create difference was additionally reproduced through the drill's native `tick()`
logic, supplying 512 rotation speed each tick rather than building a kinetic power
network. Both lower- and upper-half targets were removed under Unbreakables.
Our mod preserved both targets at the same supplied speed, and allowed a player-origin Waystone to be
removed. This confirms the drill was operational in the protected configuration.

Our player-origin results matched the disabled baseline, and direct creative breaking
of either half remained allowed. Some native tools skip Waystones independently of
either protection mod; their unchanged behavior is shown in the baseline column.

Unbreakables remains useful for general configurable block rules, conditions and costs.
The linked developer FAQ is for 1.21.1+, but Unbreakables also publishes a Forge
1.20.1 release, which is the version tested here. More hooks do not prove universal
compatibility: direct chunk-section edits, custom loot creation, and untested tool configurations remain outside these results.

## Sources and evidence

- [Waystones developer FAQ recommending Unbreakables](https://mods.twelveiterations.com/minecraft/waystones/1.21.1/guides/faq#make-generated-waystones-unbreakable)
- [Unbreakables 1.20.1 implementation](https://github.com/TwelveIterations/Unbreakables/blob/1.20.1/common/src/main/java/net/blay09/mods/unbreakables/Unbreakables.java)
- [Forge 1.20.1 release](https://modrinth.com/mod/unbreakables/version/2CS0JLRm)

Local raw logs and diagnostic source are retained under
`build/reports/waystones-comparison/`; automated verification logs are under
`build/reports/verification/`. Runtime checks do not measure performance quantitatively.
