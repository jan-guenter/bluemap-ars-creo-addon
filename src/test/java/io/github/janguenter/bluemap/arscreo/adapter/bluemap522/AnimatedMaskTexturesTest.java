/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.arscreo.adapter.bluemap522;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.arscreo.model.InstalledRunAnimationCompiler;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnimatedMaskTexturesTest {

    @Test
    void createsFourSynchronizedOnePoseTextureStrips() throws IOException {
        BufferedImage sourceImage = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        sourceImage.setRGB(0, 0, 0xffff0000);
        sourceImage.setRGB(1, 0, 0xff00ff00);
        sourceImage.setRGB(0, 1, 0xff0000ff);
        sourceImage.setRGB(1, 1, 0xffffffff);
        Texture source = Texture.from(Key.parse("test:wheel"), sourceImage);

        List<Texture> generated = AnimatedMaskTextures.create(source);

        assertEquals(InstalledRunAnimationCompiler.POSE_COUNT, generated.size());
        for (int pose = 0; pose < generated.size(); pose++) {
            Texture texture = generated.get(pose);
            BufferedImage strip = texture.getTextureImage();
            assertEquals(2, strip.getWidth());
            assertEquals(8, strip.getHeight());
            for (int frame = 0; frame < InstalledRunAnimationCompiler.POSE_COUNT;
                    frame++) {
                for (int y = 0; y < 2; y++) {
                    for (int x = 0; x < 2; x++) {
                        int expected = frame == pose ? sourceImage.getRGB(x, y) : 0;
                        assertEquals(expected, strip.getRGB(x, frame * 2 + y));
                    }
                }
            }
            assertNotNull(texture.getAnimation());
            assertFalse(texture.getAnimation().isInterpolate());
            assertEquals(3, texture.getAnimation().getFrametime());
            assertEquals(List.of(0, 1, 2, 3), texture.getAnimation().getFrames()
                    .stream().map(frame -> frame.getIndex()).toList());
            assertEquals(List.of(3, 3, 3, 2), texture.getAnimation().getFrames()
                    .stream().map(frame -> frame.getTime()).toList());
        }
    }

    @Test
    void rejectsAChangedNonSquareInstalledTexture() throws IOException {
        Texture source = Texture.from(
                Key.parse("test:wheel"),
                new BufferedImage(2, 3, BufferedImage.TYPE_INT_ARGB)
        );

        assertThrows(IOException.class, () -> AnimatedMaskTextures.create(source));
    }
}
