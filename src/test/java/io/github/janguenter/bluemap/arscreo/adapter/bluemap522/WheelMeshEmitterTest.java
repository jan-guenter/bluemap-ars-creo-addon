/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.arscreo.adapter.bluemap522;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.github.janguenter.bluemap.arscreo.model.WheelModel.Vec3;
import org.junit.jupiter.api.Test;

class WheelMeshEmitterTest {

    private static final double DELTA = 1.0E-9D;

    @Test
    void appliesTheTwoClientBasePoseOrientationsAcrossSixFacings() {
        Vec3 point = new Vec3(1D, 2D, 3D);
        Vec3 horizontal = WheelMeshEmitter.transformPoint(point, "north");
        Vec3 vertical = WheelMeshEmitter.transformPoint(point, "up");

        assertVector(horizontal, -2.5D, 2D, 1.5D);
        assertVector(vertical, 1.5D, 2D, 3.5D);
        for (String facing : new String[]{"south", "west", "east"}) {
            assertEquals(horizontal, WheelMeshEmitter.transformPoint(point, facing));
        }
        assertEquals(vertical, WheelMeshEmitter.transformPoint(point, "down"));
    }

    @Test
    void rejectsUnknownFacingInsteadOfGuessing() {
        Vec3 point = new Vec3(1D, 2D, 3D);
        assertThrows(
                IllegalArgumentException.class,
                () -> WheelMeshEmitter.transformPoint(point, "sideways")
        );
    }

    @Test
    void offsetsEachPoseIntoItsAtlasSlot() {
        assertEquals(0.25F, WheelMeshEmitter.poseV(0.25F, 0));
        assertEquals(1.25F, WheelMeshEmitter.poseV(0.25F, 1));
        assertEquals(2.25F, WheelMeshEmitter.poseV(0.25F, 2));
        assertEquals(3.25F, WheelMeshEmitter.poseV(0.25F, 3));
    }

    private static void assertVector(Vec3 actual, double x, double y, double z) {
        assertEquals(x, actual.x(), DELTA);
        assertEquals(y, actual.y(), DELTA);
        assertEquals(z, actual.z(), DELTA);
    }
}
