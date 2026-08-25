/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.arscreo.adapter.bluemap522;

import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;

/** Samples the wheel and the exposed neighbor nearest one compiled face. */
final class FaceLighting {

    private FaceLighting() {
    }

    static Sample sample(BlockNeighborhood block, Direction direction) {
        int x = direction.toVector().getX();
        int y = direction.toVector().getY();
        int z = direction.toVector().getZ();
        LightData own = block.getLightData();
        LightData faced = block.getNeighborBlock(x, y, z).getLightData();
        return new Sample(
                Math.max(own.getSkyLight(), faced.getSkyLight()),
                Math.max(own.getBlockLight(), faced.getBlockLight())
        );
    }

    record Sample(int sunlight, int blocklight) {
    }
}
