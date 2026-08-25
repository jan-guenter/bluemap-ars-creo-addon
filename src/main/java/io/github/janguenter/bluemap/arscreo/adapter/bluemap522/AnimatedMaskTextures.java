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

/** Creates four synchronized alpha masks backed by BlueMap texture animation. */
final class AnimatedMaskTextures {

    private static final int MAX_TEXTURE_EDGE = 512;
    private static final List<Integer> FRAME_TICKS = List.of(3, 3, 3, 2);
    private static final List<Key> KEYS = List.of(
            Key.parse("bluemap_ars_creo:block/starbuncle_wheel_run_pose_0"),
            Key.parse("bluemap_ars_creo:block/starbuncle_wheel_run_pose_1"),
            Key.parse("bluemap_ars_creo:block/starbuncle_wheel_run_pose_2"),
            Key.parse("bluemap_ars_creo:block/starbuncle_wheel_run_pose_3")
    );

    private AnimatedMaskTextures() {
    }

    static List<Key> keys() {
        return KEYS;
    }

    static List<Key> install(ResourcePack resourcePack, Texture source) throws IOException {
        for (Key key : KEYS) {
            if (resourcePack.getTextures().containsKey(key)) {
                throw new IOException("animated wheel texture key collision");
            }
        }
        List<Texture> generated = create(source);
        for (int index = 0; index < generated.size(); index++) {
            resourcePack.getTextures().put(KEYS.get(index), generated.get(index));
        }
        return KEYS;
    }

    static List<Texture> create(Texture source) throws IOException {
        BufferedImage image = source.getTextureImage();
        if (image.getWidth() <= 0 || image.getWidth() != image.getHeight()
                || image.getWidth() > MAX_TEXTURE_EDGE) {
            throw new IOException("installed wheel texture dimensions changed");
        }
        int frameSize = image.getWidth();
        int stripHeight = Math.multiplyExact(
                frameSize, InstalledRunAnimationCompiler.POSE_COUNT
        );
        int[] pixels = image.getRGB(0, 0, frameSize, frameSize, null, 0, frameSize);
        AnimationMeta animation = animationMeta(frameSize);
        List<Texture> generated = new ArrayList<>(
                InstalledRunAnimationCompiler.POSE_COUNT
        );
        for (int pose = 0; pose < InstalledRunAnimationCompiler.POSE_COUNT; pose++) {
            BufferedImage strip = new BufferedImage(
                    frameSize, stripHeight, BufferedImage.TYPE_INT_ARGB
            );
            strip.setRGB(0, pose * frameSize, frameSize, frameSize, pixels, 0, frameSize);
            generated.add(Texture.from(KEYS.get(pose), strip, animation));
        }
        return List.copyOf(generated);
    }

    private static AnimationMeta animationMeta(int frameSize) {
        List<FrameMeta> frames = new ArrayList<>(FRAME_TICKS.size());
        for (int index = 0; index < FRAME_TICKS.size(); index++) {
            frames.add(new FrameMeta(index, FRAME_TICKS.get(index)));
        }
        return new AnimationMeta(
                false, frameSize, frameSize, FRAME_TICKS.getFirst(), List.copyOf(frames)
        );
    }
}
