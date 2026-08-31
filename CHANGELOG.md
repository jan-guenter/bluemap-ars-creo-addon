# Changelog

## 0.1.0-alpha.3 - 2026-08-31

- Target only BlueMap feature-backport commit
  `7e07f4e74ec1e92a6ead9aa1e66054af3e133aac` and API commit
  `285c9a60eff3ac2b0cab308ce1058d1565be0971`.
- Move the local adapter boundary from `bluemap522` to `bluemap523`.
- Compile the four shared bootstrap helpers from Adapter API
  `0.1.0-alpha.2` and remove the three duplicate local helpers.
- Keep the accepted four-pose animal loop, synchronized cage rotation,
  six-facing gallery, installed-resource use, and stock fallback unchanged.

## 0.1.0-alpha.2 - 2026-08-30

- Replaced the private installed-GEO compiler, wheel model, and pose records
  with the pinned `bluemap-installed-geo-resource-models` source module.
- Kept Creo's exact wheel contract, animation parsing and sampling, resource
  admission, routes, mesh emission, fallback policy, and parity fixtures local.

## 0.1.0-alpha.1 - 2026-08-25

- Added exact-gated installed-resource rendering for the Starbuncle Wheel's
  four-pose, 11-tick `run` loop across all six facing states.
- Added a synchronized representative cage rotation for the static BlueMap
  view.
- Kept a deterministic static base-pose fallback when animation parsing or
  runtime texture-mask creation fails.
- Added a seven-cell comparison gallery and focused geometry, animation,
  texture-mask, and facing checks.
- Sealed the exact owner-accepted staging entries for release promotion.
