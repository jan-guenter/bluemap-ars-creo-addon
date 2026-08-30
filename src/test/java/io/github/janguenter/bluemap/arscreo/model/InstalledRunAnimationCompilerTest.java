/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.arscreo.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.janguenter.bluemap.arscreo.model.InstalledRunAnimationCompiler.RunAnimation;
import io.github.janguenter.bluemap.arscreo.profile.ArsCreo540Profile;
import io.github.janguenter.bluemap.resource.installedgeo.model.InstalledGeoCompiler;
import io.github.janguenter.bluemap.resource.installedgeo.model.InstalledGeoModel;
import io.github.janguenter.bluemap.resource.installedgeo.model.InstalledGeoModel.Vec3;
import io.github.janguenter.bluemap.resource.installedgeo.model.InstalledGeoPose;
import io.github.janguenter.bluemap.resource.installedgeo.model.InstalledGeoPose.BoneTransform;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class InstalledRunAnimationCompilerTest {

    private static final double DELTA = 1.0E-9D;
    private static final String ANIMATION =
            "assets/ars_creo/animations/starbuncle_wheel_animation.json";
    private static final String GEO =
            "assets/ars_creo/geo/starbuncle_wheel.geo.json";

    @Test
    void samplesTheExactContinuousRunLoopIntoFourDistinctModels() throws IOException {
        RunAnimation run = InstalledRunAnimationCompiler.compile(exactEntry(ANIMATION));
        List<InstalledGeoModel> models = run.poses().stream()
                .map(pose -> InstalledGeoCompiler.compile(
                        exactEntryUnchecked(GEO), ArsCreo540Profile.STARBUNCLE_WHEEL, pose
                ))
                .toList();

        assertEquals(InstalledRunAnimationCompiler.EXPECTED_LENGTH_SECONDS,
                run.lengthSeconds(), DELTA);
        assertEquals(InstalledRunAnimationCompiler.SAMPLE_TIMES, run.sampleTimes());
        assertEquals(InstalledRunAnimationCompiler.POSE_COUNT, models.size());
        assertEquals(InstalledRunAnimationCompiler.POSE_COUNT, new HashSet<>(models).size());
        for (int index = 0; index < run.poses().size(); index++) {
            assertVector(
                    run.poses().get(index).transform("wheel").rotation(),
                    0D,
                    InstalledRunAnimationCompiler.SYNTHETIC_WHEEL_PHASES.get(index),
                    0D
            );
        }
        assertTrue(models.stream().allMatch(model ->
                model.quads().size() == ArsCreo540Profile.STARBUNCLE_WHEEL.quads()));
        assertNotEquals(InstalledGeoCompiler.compile(
                exactEntry(GEO), ArsCreo540Profile.STARBUNCLE_WHEEL
        ), models.getFirst());
    }

    @Test
    void appliesInstalledSignsUnitsAndTargetKeyframeEasing() throws IOException {
        RunAnimation run = InstalledRunAnimationCompiler.compile(exactEntry(ANIMATION));
        InstalledGeoPose second = run.poses().get(1);
        BoneTransform starbuncle = second.transform("starbuncle");
        BoneTransform head = second.transform("head");

        assertVector(starbuncle.rotation(), 6D, 0D, 0D);
        assertVector(starbuncle.translation(), 0D, 1.4D / 16D, 0D);
        assertVector(head.rotation(), -3.75D, 0D, 0D);
        assertVector(
                head.translation(),
                0D,
                0D,
                (0.5D * (1D - Math.sin(Math.PI / 4D))) / 16D
        );
    }

    @Test
    void rejectsAChangedInstalledRunContract() throws IOException {
        String animation = new String(exactEntry(ANIMATION), StandardCharsets.UTF_8);
        byte[] changed = animation.replace(
                "\"animation_length\": 0.56", "\"animation_length\": 0.57"
        ).getBytes(StandardCharsets.UTF_8);

        assertThrows(
                IllegalArgumentException.class,
                () -> InstalledRunAnimationCompiler.compile(changed)
        );
    }

    private static byte[] exactEntryUnchecked(String path) {
        try {
            return exactEntry(path);
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private static byte[] exactEntry(String path) throws IOException {
        String property = System.getProperty("arsCreoJar");
        Assumptions.assumeTrue(property != null && !property.isBlank());
        try (ZipFile zip = new ZipFile(Path.of(property).toFile())) {
            ZipEntry entry = zip.getEntry(path);
            Assumptions.assumeTrue(entry != null && !entry.isDirectory());
            return zip.getInputStream(entry).readAllBytes();
        }
    }

    private static void assertVector(Vec3 actual, double x, double y, double z) {
        assertEquals(x, actual.x(), DELTA);
        assertEquals(y, actual.y(), DELTA);
        assertEquals(z, actual.z(), DELTA);
    }
}
