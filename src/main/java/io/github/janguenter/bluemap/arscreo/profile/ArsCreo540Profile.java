/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.arscreo.profile;

import java.util.List;

/** Exact All the Mons 1.2.0 profile `ars-creo-5.4.0-mc1.21.1`. */
public final class ArsCreo540Profile {

    public static final String PROFILE_ID = "ars-creo-5.4.0-mc1.21.1";
    public static final ArtifactPin ARS_CREO = new ArtifactPin(
                    "arsCreo",
                    "ars_creo",
                    "5.4.0",
                    "ars_creo-1.21.1-5.4.0.jar",
                    95_973L,
                    "50f0fe5c5f855151c1482c1772ea94c2eaadc2b0c85c963bb9aeb421fc801e4f"
    );
    public static final List<ArtifactPin> ARTIFACTS = List.of(
            ARS_CREO,
            new ArtifactPin(
                    "arsNouveau",
                    "ars_nouveau",
                    "5.13.0",
                    "ars_nouveau-1.21.1-5.13.0.jar",
                    20_096_005L,
                    "90796df69bfb39b1a9c79edbfa01c2425e5b86aea47dc55ebdcbf30e88f47592"
            ),
            new ArtifactPin(
                    "create",
                    "create",
                    "6.0.10",
                    "create-1.21.1-6.0.10.jar",
                    19_123_767L,
                    "ef87fe5709f1ba1f5b8bb20a2925b5afb4669e178fd6d8bf10c167759eefe37a"
            )
    );

    private ArsCreo540Profile() {
    }
}
