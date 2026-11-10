# Adding a tweak

Use Java 17 and the checked-in Gradle wrapper. Packages belong under
`dev.mpthlee.allthetweaks`.

1. Create `tweak/<name>/` for the fix's policy, integration helpers, and lifecycle
   class. Implement `Tweak`; register event listeners once in `register` and update
   cached configuration in `refresh`. Add the instance to `Tweaks.ALL`.
2. Add an independent, default-enabled switch to `TweaksConfig`. Keep existing
   configuration paths stable. Check optional-mod presence before enabling a fix.
3. Put mixins in that module's `mixin` subpackage. Create a dedicated mixin JSON
   and add its filename to `tweakMixinConfigs` in `build.gradle`; this also includes
   it in development launches and the release manifest. Use the shared refmap.
   A mixin configuration's package must contain only mixins, never ordinary helper
   classes: Mixin reserves that package and rejects normal class loading from it.
4. Keep optional integrations optional: use `compileOnly` dependencies, development
   `runtimeOnly` dependencies inside the optional-mod guard, optional metadata in
   `mods.toml`, and `@Pseudo` for optional mixin targets. Bootstrap code must load
   safely without those mods. Extend the absent-mod smoke test when appropriate.
5. Add real regression tests under `src/gametest/java/.../tweak/<name>/`, using the
   shared `empty` template and this mod's GameTest namespace. First verify that the
   reported behavior fails without the fix. Cover supported behavior that must
   remain intact, the disabled switch, and relevant lifecycle boundaries. Keep
   mod-specific classes out of `src/smoke` so it can run without optional mods.
6. Extend `scripts/verify.sh` with a disabled-fix regression check. Require the
   specific bug assertion, not merely a failed build. Run the full script, update
   README behavior/version/verification details, and inspect release packaging.

Prefer event-driven hooks at shared lifecycle boundaries. Keep integration policy
inside its module; avoid per-tool patches, global player scans, or new work in hot
paths without evidence that it is necessary. State limitations when an upstream
API cannot expose ownership or distinguish actors.

```sh
./gradlew build runGameTestServer
./gradlew runGameTestServer -PwithoutOptionalMods
./scripts/verify.sh
```

The test task resets only generated regression worlds under `run-gametest/` and
checks the discovered passing test count. Tests and their optional dependencies
are not bundled in the release JAR. Verification logs are written to
`build/reports/verification/`.
