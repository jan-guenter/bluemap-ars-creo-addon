# Changelog

## Unreleased

- Replaced the repository-local installed-GEO compiler, wheel model, and pose
  records with the released first-party source module while keeping Creo's
  wheel contract, animation parsing and sampling, admission, routing, mesh
  emission, and fallback local.

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
