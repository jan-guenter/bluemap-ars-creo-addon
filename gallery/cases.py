#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Seven-cell Starbuncle Wheel facing comparison gallery."""

from __future__ import annotations

from dataclasses import dataclass


NAMESPACE = "ars_creo_gallery"
ENVELOPE = (173, 99, 173, 191, 103, 183)


@dataclass(frozen=True)
class Placement:
    case_id: str
    label: str
    x: int
    y: int
    z: int
    block_state: str
    expected: str


PLACEMENTS = (
    Placement(
        "wheel-north",
        "Starbuncle Wheel facing north",
        176,
        100,
        176,
        "ars_creo:starbuncle_wheel[facing=north]",
        "installed-base-pose-visible",
    ),
    Placement(
        "wheel-south",
        "Starbuncle Wheel facing south",
        180,
        100,
        176,
        "ars_creo:starbuncle_wheel[facing=south]",
        "installed-base-pose-visible",
    ),
    Placement(
        "wheel-west",
        "Starbuncle Wheel facing west",
        184,
        100,
        176,
        "ars_creo:starbuncle_wheel[facing=west]",
        "installed-base-pose-visible",
    ),
    Placement(
        "wheel-east",
        "Starbuncle Wheel facing east",
        188,
        100,
        176,
        "ars_creo:starbuncle_wheel[facing=east]",
        "installed-base-pose-visible",
    ),
    Placement(
        "wheel-up",
        "Starbuncle Wheel facing up",
        176,
        100,
        180,
        "ars_creo:starbuncle_wheel[facing=up]",
        "installed-base-pose-visible",
    ),
    Placement(
        "wheel-down",
        "Starbuncle Wheel facing down",
        180,
        100,
        180,
        "ars_creo:starbuncle_wheel[facing=down]",
        "installed-base-pose-visible",
    ),
    Placement(
        "stock-control",
        "stone stock rendering control",
        184,
        100,
        180,
        "minecraft:stone",
        "stock-visible",
    ),
)
