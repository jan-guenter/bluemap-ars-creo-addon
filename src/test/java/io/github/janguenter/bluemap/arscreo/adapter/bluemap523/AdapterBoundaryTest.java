/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.arscreo.adapter.bluemap523;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.addon.adapter.api.bluemap523.ResourceExtensionType;
import org.junit.jupiter.api.Test;

class AdapterBoundaryTest {

    @Test
    void usesTheSharedResourceExtensionTypeWithoutLocalCopies() {
        assertInstanceOf(ResourceExtensionType.class, BlueMap523Adapter.extension());
        assertEquals(
                Key.parse("bluemap_ars_creo:exact_profile"),
                BlueMap523Adapter.extension().getKey()
        );
        assertInstanceOf(
                ProfileResourceExtension.class,
                BlueMap523Adapter.extension().create(null)
        );
        assertThrows(ClassNotFoundException.class, () -> Class.forName(
                "io.github.janguenter.bluemap.arscreo.adapter.bluemap523."
                        + "ProfileResourceExtensionType"
        ));
        assertThrows(ClassNotFoundException.class, () -> Class.forName(
                "io.github.janguenter.bluemap.arscreo.adapter.bluemap523.RegistryGuard"
        ));
    }
}
