/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.arscreo.adapter.bluemap523;

import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.arscreo.activation.AddonRuntime;
import io.github.janguenter.bluemap.arscreo.model.InstalledRunAnimationCompiler;
import io.github.janguenter.bluemap.arscreo.model.InstalledRunAnimationCompiler.RunAnimation;
import io.github.janguenter.bluemap.arscreo.profile.ArsCreo540Profile;
import io.github.janguenter.bluemap.arscreo.profile.ExactArtifactDetector;
import io.github.janguenter.bluemap.resource.installedgeo.model.InstalledGeoCompiler;
import io.github.janguenter.bluemap.resource.installedgeo.model.InstalledGeoModel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Exact admission, installed run-pose compilation, and target-only routing. */
final class ProfileResourceExtension implements ResourcePackExtension {

    private static final int MAX_ROOTS = 4_096;
    private static final int MAX_GEO_BYTES = 64 * 1024;
    private static final int MAX_ANIMATION_BYTES = 64 * 1024;
    private static final String GEO_PATH =
            "assets/ars_creo/geo/starbuncle_wheel.geo.json";
    private static final String ANIMATION_PATH =
            "assets/ars_creo/animations/starbuncle_wheel_animation.json";
    private final ResourcePack resourcePack;
    private final BlockRendererType renderer;
    private final AddonRuntime runtime;
    private InstalledGeoModel baseModel;
    private List<InstalledGeoModel> runPoses = List.of();
    private String animationFallback;

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
        baseModel = null;
        runPoses = List.of();
        animationFallback = null;
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
            byte[] geometry = readEntry(
                    artifact, GEO_PATH, MAX_GEO_BYTES, "wheel GEO"
            );
            baseModel = InstalledGeoCompiler.compile(
                    geometry, ArsCreo540Profile.STARBUNCLE_WHEEL
            );
            try {
                RunAnimation run = InstalledRunAnimationCompiler.compile(readEntry(
                        artifact, ANIMATION_PATH, MAX_ANIMATION_BYTES, "wheel animation"
                ));
                runPoses = run.poses().stream()
                        .map(pose -> InstalledGeoCompiler.compile(
                                geometry, ArsCreo540Profile.STARBUNCLE_WHEEL, pose
                        ))
                        .toList();
            } catch (IOException | RuntimeException exception) {
                runPoses = List.of();
                animationFallback = "animation-compile-"
                        + exception.getClass().getSimpleName();
            }
        } catch (IOException | RuntimeException exception) {
            baseModel = null;
            runPoses = List.of();
            runtime.inactive("geo-compile-" + exception.getClass().getSimpleName());
        }
    }

    @Override
    public Set<Key> collectUsedTextureKeys() {
        Set<Key> keys = new LinkedHashSet<>(AnimatedMaskTextures.keys());
        keys.add(WheelMeshEmitter.TEXTURE);
        return Set.copyOf(keys);
    }

    @Override
    public void bake() {
        if (baseModel == null) {
            return;
        }
        Texture texture = resourcePack.getTextures().get(WheelMeshEmitter.TEXTURE);
        if (texture == null) {
            runtime.inactive("installed-wheel-texture-missing");
            return;
        }
        try {
            List<Key> runTextures = List.of();
            if (runPoses.size() == InstalledRunAnimationCompiler.POSE_COUNT) {
                try {
                    runTextures = AnimatedMaskTextures.install(resourcePack, texture);
                } catch (IOException | RuntimeException exception) {
                    runPoses = List.of();
                    animationFallback = "animation-texture-"
                            + exception.getClass().getSimpleName();
                }
            }
            VariantRendererCatalog variants = VariantRendererCatalog.wrap(
                    resourcePack, renderer
            );
            RendererDataRegistry.install(
                    resourcePack, baseModel, runPoses, runTextures, variants
            );
            runtime.activate();
            String mode = runPoses.isEmpty()
                    ? "static base-pose fallback (" + fallbackReason() + ")"
                    : "four-pose, 11-tick installed run loop with synthetic wheel rotation";
            System.out.println("BlueMap Ars Creo add-on active: compiled the installed "
                    + "Starbuncle Wheel " + mode + " and wrapped " + variants.size()
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

    private String fallbackReason() {
        return animationFallback == null ? "animation-unavailable" : animationFallback;
    }

    private static byte[] readEntry(
            Path artifact,
            String path,
            int maxBytes,
            String label
    ) throws IOException {
        try (ZipFile zip = new ZipFile(artifact.toFile())) {
            ZipEntry entry = zip.getEntry(path);
            if (entry == null || entry.isDirectory()
                    || entry.getSize() < 2 || entry.getSize() > maxBytes) {
                throw new IOException("installed " + label + " is missing or outside budget");
            }
            try (InputStream input = zip.getInputStream(entry)) {
                byte[] raw = input.readNBytes(maxBytes + 1);
                if (raw.length > maxBytes) {
                    throw new IOException("installed " + label + " exceeds byte budget");
                }
                return raw;
            }
        }
    }
}
