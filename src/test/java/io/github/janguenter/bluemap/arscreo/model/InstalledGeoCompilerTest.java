/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.arscreo.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.janguenter.bluemap.arscreo.profile.ArsCreo540Profile;
import io.github.janguenter.bluemap.resource.installedgeo.model.InstalledGeoCompiler;
import io.github.janguenter.bluemap.resource.installedgeo.model.InstalledGeoModel;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

class InstalledGeoCompilerTest {

    private static final String GEO =
            "assets/ars_creo/geo/starbuncle_wheel.geo.json";

    @Test
    void compilesTheExactInstalledBasePoseDeterministically() throws IOException {
        byte[] raw = exactGeometry();
        InstalledGeoModel first = InstalledGeoCompiler.compile(
                raw, ArsCreo540Profile.STARBUNCLE_WHEEL
        );
        InstalledGeoModel second = InstalledGeoCompiler.compile(
                raw, ArsCreo540Profile.STARBUNCLE_WHEEL
        );

        assertEquals(ArsCreo540Profile.STARBUNCLE_WHEEL.quads(), first.quads().size());
        assertEquals(first, second);
        assertTrue(first.quads().stream().flatMap(quad -> java.util.stream.Stream.of(
                quad.first(), quad.second(), quad.third(), quad.fourth()
        )).allMatch(vertex -> finite(vertex.position())
                && Float.isFinite(vertex.u()) && Float.isFinite(vertex.v())));
    }

    @Test
    void rejectsAChangedInstalledSchema() throws IOException {
        String geometry = new String(exactGeometry(), StandardCharsets.UTF_8);
        byte[] changed = geometry.replace("\"1.12.0\"", "\"9.99.0\"")
                .getBytes(StandardCharsets.UTF_8);

        assertThrows(IllegalArgumentException.class, () -> InstalledGeoCompiler.compile(
                changed, ArsCreo540Profile.STARBUNCLE_WHEEL
        ));
    }

    private static byte[] exactGeometry() throws IOException {
        String property = System.getProperty("arsCreoJar");
        Assumptions.assumeTrue(property != null && !property.isBlank());
        try (ZipFile zip = new ZipFile(Path.of(property).toFile())) {
            ZipEntry entry = zip.getEntry(GEO);
            Assumptions.assumeTrue(entry != null && !entry.isDirectory());
            return zip.getInputStream(entry).readAllBytes();
        }
    }

    private static boolean finite(InstalledGeoModel.Vec3 value) {
        return Double.isFinite(value.x())
                && Double.isFinite(value.y())
                && Double.isFinite(value.z());
    }
}
