# BlueMap Ars Creo Add-on

A Java 21 BlueMap add-on for the exact `ars-creo-5.4.0-mc1.21.1` profile in All the Mons
`1.2.0` / Minecraft `1.21.1`.

Status: prototype ready for disposable visual staging. The exact profile reads
the operator-installed Starbuncle Wheel geometry, `run` animation, and texture.
It samples the continuous 0.56-second body animation into four poses and shows
them through an 11-tick BlueMap texture loop across all six saved `facing`
states. Unknown or unsupported inputs keep BlueMap's stock rendering. A failed
animation parse or mask build keeps the installed static base pose.

## Build

```bash
gradle --no-daemon -PbluemapSourcePath=../bluemap-backport clean check build
```

`check` is the quick Java/checkstyle/archive gate. `prototypeCheck` additionally
requires every exact candidate JAR property and validates the comparison
gallery. See `provenance/upstreams.json` for immutable artifact identities and
the [execution guide](docs/EXECUTION.md) for the prototype-to-release loop.

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
The wheel itself stays in its installed base pose because its client rotation
depends on live Create speed. Gold-block RPM changes, particles, and moving
contraption behavior remain outside this route.

No Ars Creo binary, source, class, asset, captured mesh, or gallery is
bundled in the add-on.
