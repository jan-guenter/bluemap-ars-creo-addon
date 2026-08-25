/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.arscreo.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.arscreo.activation.AddonRuntime;
import io.github.janguenter.bluemap.arscreo.model.InstalledGeoCompiler;
import io.github.janguenter.bluemap.arscreo.model.WheelModel;
import io.github.janguenter.bluemap.arscreo.profile.ExactArtifactDetector;
import io.github.janguenter.bluemap.arscreo.profile.ArsCreo540Profile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Exact-artifact admission, installed GEO compilation, and target-only routing. */
final class ProfileResourceExtension implements ResourcePackExtension {

    private static final int MAX_ROOTS = 4_096;
    private static final int MAX_GEO_BYTES = 64 * 1024;
    private static final String GEO_PATH =
            "assets/ars_creo/geo/starbuncle_wheel.geo.json";
    private final ResourcePack resourcePack;
    private final BlockRendererType renderer;
    private final AddonRuntime runtime;
    private WheelModel model;

    ProfileResourceExtension(
            ResourcePack resourcePack,
            BlockRendererType renderer,
            AddonRuntime runtime
    ) {
        this.resourcePack = resourcePack;
        this.renderer = renderer;
        this.runtime = runtime;
    }

    @Override
    public void loadResources(Iterable<Path> roots) {
        if (Boolean.getBoolean("bluemap.arscreo.disabled")) {
            runtime.inactive("operator-disabled");
            return;
        }
        List<Path> candidates = boundedRoots(roots);
        if (candidates == null
                || !ExactArtifactDetector.matchesAll(
                        candidates, ArsCreo540Profile.ARTIFACTS
                )) {
            runtime.inactive("exact-artifact-missing-or-duplicate");
            return;
        }
        Path artifact = ExactArtifactDetector.findExact(
                candidates, ArsCreo540Profile.ARS_CREO
        ).orElse(null);
        if (artifact == null) {
            runtime.inactive("exact-ars-creo-artifact-unavailable");
            return;
        }
        try {
            model = InstalledGeoCompiler.compile(readGeo(artifact));
        } catch (IOException | RuntimeException exception) {
            model = null;
            runtime.inactive("geo-compile-" + exception.getClass().getSimpleName());
        }
    }

    @Override
    public Set<Key> collectUsedTextureKeys() {
        return Set.of(WheelMeshEmitter.TEXTURE);
    }

    @Override
    public void bake() {
        if (model == null) {
            return;
        }
        if (resourcePack.getTextures().get(WheelMeshEmitter.TEXTURE) == null) {
            runtime.inactive("installed-wheel-texture-missing");
            return;
        }
        try {
            VariantRendererCatalog variants = VariantRendererCatalog.wrap(
                    resourcePack, renderer
            );
            RendererDataRegistry.install(resourcePack, model, variants);
            runtime.activate();
            System.out.println("BlueMap Ars Creo add-on active: compiled the installed "
                    + "Starbuncle Wheel base pose and wrapped " + variants.size()
                    + " blockstate variant(s).");
        } catch (RuntimeException exception) {
            runtime.inactive("route-install-" + exception.getClass().getSimpleName());
        }
    }

    private static List<Path> boundedRoots(Iterable<Path> roots) {
        List<Path> result = new ArrayList<>();
        for (Path root : roots) {
            if (Thread.currentThread().isInterrupted() || result.size() >= MAX_ROOTS) {
                return null;
            }
            result.add(root);
        }
        return List.copyOf(result);
    }

    private static byte[] readGeo(Path artifact) throws IOException {
        try (ZipFile zip = new ZipFile(artifact.toFile())) {
            ZipEntry entry = zip.getEntry(GEO_PATH);
            if (entry == null || entry.isDirectory()
                    || entry.getSize() < 2 || entry.getSize() > MAX_GEO_BYTES) {
                throw new IOException("installed wheel GEO is missing or outside budget");
            }
            try (InputStream input = zip.getInputStream(entry)) {
                byte[] raw = input.readNBytes(MAX_GEO_BYTES + 1);
                if (raw.length > MAX_GEO_BYTES) {
                    throw new IOException("installed wheel GEO exceeds byte budget");
                }
                return raw;
            }
        }
    }
}
