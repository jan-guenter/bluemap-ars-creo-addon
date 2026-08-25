/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.arscreo.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.BlockRendererType;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.arscreo.activation.AddonRuntime;

/** BlueMap 5.22 registration boundary. Family renderer registrations go here. */
public final class BlueMap522Adapter {

    private static final AddonRuntime RUNTIME = AddonRuntime.INSTANCE;
    private static final BlockRendererType RENDERER = new BlockRendererType.Impl(
            Key.parse("bluemap_ars_creo:static_wheel"),
            BlueMap522Adapter::createRenderer
    );
    private static final ResourcePack.Extension<ProfileResourceExtension> EXTENSION =
            new ProfileResourceExtensionType(RENDERER, RUNTIME);

    private BlueMap522Adapter() {
    }

    /** Registers the exact profile extension and its target-only renderer. */
    public static synchronized boolean install() {
        if (!RegistryGuard.canRegister(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.canRegister(ResourcePack.Extension.REGISTRY, EXTENSION)) {
            RUNTIME.fail("registry-collision");
            return false;
        }
        if (!RegistryGuard.register(BlockRendererType.REGISTRY, RENDERER)
                || !RegistryGuard.register(ResourcePack.Extension.REGISTRY, EXTENSION)) {
            RUNTIME.fail("registry-registration-failed");
            return false;
        }
        return true;
    }

    private static BlockRenderer createRenderer(
            ResourcePack pack,
            TextureGallery gallery,
            RenderSettings settings
    ) {
        try {
            return new ArsCreoRenderer(pack, gallery, settings, RUNTIME);
        } catch (RuntimeException exception) {
            RUNTIME.inactive("renderer-construction-"
                    + exception.getClass().getSimpleName());
            return BlockRendererType.DEFAULT.create(pack, gallery, settings);
        }
    }
}
