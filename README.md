# BlueMap Ars Creo Add-on

A Java 21 BlueMap add-on for the exact `ars-creo-5.4.0-mc1.21.1` profile in All the Mons
`1.2.0` / Minecraft `1.21.1`.

Status: prototype ready for disposable visual staging. The exact profile reads
the operator-installed Starbuncle Wheel geometry and texture, compiles a fixed
base pose, and routes all six saved `facing` states. Unknown or unsupported
inputs keep BlueMap's stock rendering.

## Build

```bash
gradle --no-daemon -PbluemapSourcePath=../bluemap-backport clean check build
```

`check` is the quick Java/checkstyle/archive gate. `prototypeCheck` additionally
requires every exact candidate JAR property and validates the comparison
gallery. See `provenance/upstreams.json` for immutable artifact identities and
the [execution guide](docs/EXECUTION.md) for the prototype-to-release loop.

## Install

After a renderer exists, place the production JAR in BlueMap's add-on pack
directory and restart the BlueMap JVM. Removal plus one restart restores stock
behavior; the add-on creates no custom world state.

Set `-Dbluemap.arscreo.disabled=true` to leave the exact profile inactive.

## Scope boundary

The initial implementation owns only `ars_creo:starbuncle_wheel`. It freezes
the always-running Starbuncle and wheel animation at the installed geometry's
base pose. Live Create speed, gold-block RPM changes, particles, and moving
contraption behavior are outside this route.

No Ars Creo binary, source, class, asset, captured mesh, or gallery is
bundled in the add-on.
