/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.arscreo.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.AnimationMeta;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.AnimationMeta.FrameMeta;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.arscreo.model.InstalledRunAnimationCompiler;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Creates one clocked pose-atlas backed by BlueMap texture animation. */
final class AnimatedMaskTextures {

    private static final int MAX_TEXTURE_EDGE = 512;
    private static final List<Integer> FRAME_TICKS = List.of(3, 3, 3, 2);
    private static final Key KEY = Key.parse(
            "bluemap_ars_creo:block/starbuncle_wheel_run_poses"
    );

    private AnimatedMaskTextures() {
    }

    static List<Key> keys() {
        return List.of(KEY);
    }

    static List<Key> install(ResourcePack resourcePack, Texture source) throws IOException {
        if (resourcePack.getTextures().containsKey(KEY)) {
            throw new IOException("animated wheel texture key collision");
        }
        resourcePack.getTextures().put(KEY, create(source).getFirst());
        return List.of(KEY, KEY, KEY, KEY);
    }

    static List<Texture> create(Texture source) throws IOException {
        BufferedImage image = source.getTextureImage();
        if (image.getWidth() <= 0 || image.getWidth() != image.getHeight()
                || image.getWidth() > MAX_TEXTURE_EDGE) {
            throw new IOException("installed wheel texture dimensions changed");
        }
        int frameSize = image.getWidth();
        int poseCount = InstalledRunAnimationCompiler.POSE_COUNT;
        int stripHeight = Math.multiplyExact(frameSize, poseCount * poseCount);
        int[] pixels = image.getRGB(0, 0, frameSize, frameSize, null, 0, frameSize);
        AnimationMeta animation = animationMeta(frameSize);
        BufferedImage strip = new BufferedImage(
                frameSize, stripHeight, BufferedImage.TYPE_INT_ARGB
        );
        for (int frame = 0; frame < poseCount; frame++) {
            int activeSlot = frame * poseCount + frame;
            strip.setRGB(
                    0, activeSlot * frameSize,
                    frameSize, frameSize, pixels, 0, frameSize
            );
        }
        return List.of(Texture.from(KEY, strip, animation));
    }

    private static AnimationMeta animationMeta(int frameSize) {
        List<FrameMeta> frames = new ArrayList<>(FRAME_TICKS.size());
        for (int index = 0; index < FRAME_TICKS.size(); index++) {
            frames.add(new FrameMeta(
                    index * InstalledRunAnimationCompiler.POSE_COUNT,
                    FRAME_TICKS.get(index)
            ));
        }
        return new AnimationMeta(
                false, frameSize, frameSize, FRAME_TICKS.getFirst(), List.copyOf(frames)
        );
    }
}
