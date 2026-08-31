# Third-party material

| Project | Identity | License | Use | Redistributed |
| --- | --- | --- | --- | --- |
| BlueMap | Feature backport `5.22-feature.backport-5.23-stateless-java-web-server-46` / `7e07f4e74ec1e92a6ead9aa1e66054af3e133aac`; API `285c9a60eff3ac2b0cab308ce1058d1565be0971` | MIT | Compile-only internal API | No |
| BlueMap Add-on Adapter API | `0.1.0-alpha.2` / `e81f08bc4bfbf02d810ec8949a019130e2e61634` | MIT | Four exact source files compiled into this add-on | Source only |
| Ars Creo profile | `ars-creo-5.4.0-mc1.21.1` | See `provenance/upstreams.json` | Exact installed-artifact and resource evidence only | No |

The packaged `META-INF/LICENSE-BlueMap` preserves the license notice for the
API patterns used by this project. Candidate license identities and evidence
tiers are recorded per artifact in immutable packaged provenance.

The renderer interprets only the geometry, `run` animation, and texture present
in the exact operator-installed Ars Creo JAR. It packages none of those
resources. The exact JAR and distribution license declarations conflict, so
this project does not adapt Ars Creo source.

The Installed-GEO compiler, model, and pose sources compiled into this add-on
are first-party MIT-licensed source, not third-party material. They are pinned
as a source submodule; its standalone module JAR is not redistributed.
