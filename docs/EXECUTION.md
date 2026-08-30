# Add-on execution

This repository starts inactive and stock-safe. Implement only the smallest
observed Ars Creo rendering defect before staging.

Before running Gradle gates, activate a Python 3.11 or newer virtual
environment, initialize both pinned submodules, and install the exact
development-only toolkit into the environment:

```bash
git submodule update --init --recursive -- \
  tooling/bluemap-addon-toolkit modules/bluemap-installed-geo-resource-models
python -m pip install --disable-pip-version-check --no-deps \
  --require-hashes --only-binary=:all: \
  --requirement requirements/toolkit.txt
```

The requirement locks the 20,585-byte `v0.3.0-alpha.1` wheel at SHA-256
`82f1ec53603646849a7c2d4b58f3fb7000413fe83043a302bee88cc88daeb8f7`.
The Gradle settings preflight separately locks the Installed-GEO module commit
and its `src/main/java` tree, requires a clean checkout, and compiles its three
Java sources directly with Gson 2.8.9 as an explicit compile-only dependency.

## Prototype

Acquire and verify the exact candidate JARs outside Git. Their Gradle
properties are:

- `-ParsCreoJar=/path/to/ars_creo-1.21.1-5.4.0.jar`
- `-ParsNouveauJar=/path/to/ars_nouveau-1.21.1-5.13.0.jar`
- `-PcreateJar=/path/to/create-1.21.1-6.0.10.jar`

Then run:

```bash
gradle --no-daemon -PbluemapSourcePath=../bluemap-backport \
  <exact-candidate-properties> clean prototypeCheck build
bash gallery/package.sh /tmp/ars_creo-gallery.zip
```

Deploy that JAR and gallery only to disposable staging, verify the intended
BlueMap link loads, and compare it with the matching client. Iterate from
observed defects until the owner explicitly accepts one exact staging JAR.

## Acceptance and release

Freeze that accepted JAR's functional entries once; the writer refuses to
overwrite an existing acceptance record:

```bash
bluemap-addon-toolkit jar-entries write \
  --jar /absolute/path/accepted-staging.jar \
  --entries provenance/accepted-staging-entries.sha256
```

Record the manifest in `provenance/release.json` as
`accepted_staging_entries` with exact `path`, `entry_count`, and `sha256`.
Record `visual_acceptance: true` under `owner_accepted_staging`, and record the
production JAR, sources JAR, POM and Gradle module file names, sizes and hashes
under `final_release_artifacts`.

Promote `addon_version` through a pull request, remove every prototype
implementation placeholder, and run with all exact candidate properties:

```bash
gradle --no-daemon -PbluemapSourcePath=../bluemap-backport \
  <exact-candidate-properties> -PreleaseTag=v<version> \
  clean build generatePomFileForAddonPublication \
  generateMetadataFileForAddonPublication verifyReleaseCandidate
```

Merge only after final-version CI passes this gate. Create an annotated
`v<version>` tag at reviewed `main`; the release workflow independently checks
the tag, exact BlueMap checkout, accepted bytes and draft assets before making
the prerelease public. Publication never deploys to production.
