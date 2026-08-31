# BlueMap Ars Creo Add-on

A Java 21 BlueMap 5.23 feature-backport add-on for the exact
`ars-creo-5.4.0-mc1.21.1` profile in All the Mons `1.2.0` / Minecraft
`1.21.1`.

Version `0.1.0-alpha.3` is an unpublished runtime migration candidate. It
keeps the owner-accepted Starbuncle Wheel behavior from `0.1.0-alpha.2` and
targets only BlueMap feature-backport commit
`7e07f4e74ec1e92a6ead9aa1e66054af3e133aac` with API commit
`285c9a60eff3ac2b0cab308ce1058d1565be0971`. The exact profile reads the
operator-installed wheel geometry, `run` animation, and texture. It samples
the continuous 0.56-second body animation into four poses and shows them
through an 11-tick BlueMap texture loop. The same clock advances the cage
through a representative 45-degree rotation. Unknown or unsupported inputs
keep BlueMap's stock rendering. A failed animation parse or mask build keeps
the installed static base pose.

## Build

Clone with `--recurse-submodules`, or initialize an existing checkout with
`git submodule update --init --recursive -- tooling/bluemap-addon-toolkit
modules/bluemap-addon-adapter-api
modules/bluemap-installed-geo-resource-models`. The settings preflight accepts
only the committed toolkit gitlink at
`6cd34a8368cc4ee8628fbe830a90ec5b14960629` and the Installed-GEO source
module at `c80a83eb6e2cb0bb05a69ace9716ef08b9db14f2`, with Java source tree
`8db87f933557d54c5ede2db70d94f67eaf44c30b`. It also pins Adapter API
`0.1.0-alpha.2` at commit `e81f08bc4bfbf02d810ec8949a019130e2e61634`
and source tree `2f974c9bb2ba13888d69682f86f30f58922d30eb`. It rejects an
uninitialized, changed, or dirty checkout.

```bash
gradle --no-daemon -PbluemapSourcePath=../bluemap-backport clean check build
```

`check` is the quick Java/checkstyle/archive gate. `prototypeCheck` additionally
requires every exact candidate JAR property and validates the comparison
gallery. See `provenance/upstreams.json` for immutable artifact identities and
the [execution guide](docs/EXECUTION.md) for the prototype-to-release loop.

The build compiles the three Installed-GEO and four Adapter API Java source
files directly. Their standalone JARs are neither runtime dependencies nor
nested in the add-on. Creo's wheel contract, `run` animation parser and
sampler, resource admission, routes, mesh emission, and fallback policy stay
local to this repository.

## Install

Place the production JAR in BlueMap's add-on pack directory and restart the
BlueMap JVM. Removal plus one restart restores stock behavior; the add-on
creates no custom world state.

Set `-Dbluemap.arscreo.disabled=true` to leave the exact profile inactive.

## Scope boundary

The initial implementation owns only `ars_creo:starbuncle_wheel`. The
Starbuncle body uses four runtime-compiled samples of the installed looping
`run` animation. BlueMap's 50 ms texture timing makes the displayed loop 550 ms
instead of the source animation's 560 ms. All map instances share that phase.
The cage uses a synchronized, constant representative rotation because BlueMap
cannot read its live Create speed. Gold-block RPM changes, live speed and
direction, particles, and moving contraption behavior remain outside this route.

No Ars Creo binary, source, class, asset, captured mesh, or gallery is
bundled in the add-on.
