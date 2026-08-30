/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.installedgeo.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParseException;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/** Shared synthetic contract tests for the installed Bedrock GEO compiler family. */
public abstract class InstalledGeoParityHarness {

    private static final int EXPECTED_QUADS = 192;
    private static final int MAX_BYTES = 64 * 1024;
    private static final String VISIBLE_CUBE = """
            {"origin":[0,0,0],"size":[16,16,16],"uv":{
              "west":{"uv":[4,8],"uv_size":[8,16]},
              "east":{"uv":[4,8],"uv_size":[8,16]},
              "north":{"uv":[4,8],"uv_size":[8,16]},
              "south":{"uv":[4,8],"uv_size":[8,16]},
              "up":{"uv":[4,8],"uv_size":[8,16]},
              "down":{"uv":[4,8],"uv_size":[8,16]}
            }}
            """;
    private static final String HIDDEN_CUBE =
            "{\"origin\":[0,0,0],\"size\":[16,16,16],\"uv\":{}}";

    protected abstract MeshSnapshot compile(byte[] raw);

    @Test
    protected final void rejectsMalformedJson() {
        assertThrows(JsonParseException.class, () -> compile(bytes("{]")));
    }

    @Test
    protected final void rejectsMissingParentBone() {
        assertRejected(
                "missing installed GEO parent bone",
                () -> compile(geometry("absent", null, 20))
        );
    }

    @Test
    protected final void mapsUvAndGeometryExactly() {
        MeshSnapshot first = compile(geometry(null, null, 20));
        MeshSnapshot second = compile(geometry(null, null, 20));

        assertEquals(EXPECTED_QUADS, first.quads());
        assertEquals(first, second);
        assertEquals(
                new VertexSnapshot(-1D, 1D, 1D, 0.1875F, 0.25F),
                first.first()
        );
        assertEquals(
                new VertexSnapshot(-1D, 1D, 0D, 0.0625F, 0.25F),
                first.second()
        );
        assertEquals(
                new VertexSnapshot(-1D, 0D, 0D, 0.0625F, 0.75F),
                first.third()
        );
        assertEquals(
                new VertexSnapshot(-1D, 0D, 1D, 0.1875F, 0.75F),
                first.fourth()
        );
    }

    @Test
    protected final void rejectsMalformedUvMapping() {
        String malformed = new String(
                geometry(null, null, 20),
                StandardCharsets.UTF_8
        ).replace("\"uv\":[4,8]", "\"uv\":[4]");
        assertRejected(
                "malformed installed GEO face UV",
                () -> compile(bytes(malformed))
        );
    }

    @Test
    protected final void rejectsHierarchyCycle() {
        assertRejected(
                "cyclic or deep installed GEO hierarchy",
                () -> compile(geometry("bone_1", "bone_0", 20))
        );
    }

    @Test
    protected final void enforcesInputByteBudget() {
        byte[] valid = geometry(null, null, 20);
        assertTrue(
                valid.length <= MAX_BYTES,
                "synthetic installed GEO exceeds compiler byte budget"
        );
        byte[] atLimit = Arrays.copyOf(valid, MAX_BYTES);
        Arrays.fill(atLimit, valid.length, atLimit.length, (byte) ' ');
        assertEquals(EXPECTED_QUADS, compile(atLimit).quads());
        assertRejected(
                "installed GEO is outside the byte budget",
                () -> compile(new byte[0])
        );
        assertRejected(
                "installed GEO is outside the byte budget",
                () -> compile(new byte[MAX_BYTES + 1])
        );
    }

    @Test
    protected final void preservesExactFallbackSignalForContractMismatch() {
        assertRejected(
                "installed GEO bone roster changed",
                () -> compile(geometry(null, null, 19))
        );
    }

    private static void assertRejected(String message, Executable compilation) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                compilation
        );
        assertEquals(message, exception.getMessage());
    }

    private static byte[] geometry(
            String firstParent,
            String secondParent,
            int boneCount
    ) {
        StringBuilder json = new StringBuilder();
        json.append("""
                {"format_version":"1.12.0","minecraft:geometry":[{
                  "description":{"texture_width":64,"texture_height":32},
                  "bones":[
                """);
        for (int bone = 0; bone < boneCount; bone++) {
            if (bone > 0) {
                json.append(',');
            }
            json.append("{\"name\":\"bone_").append(bone).append('"');
            String parent = bone == 0 ? firstParent : bone == 1 ? secondParent : null;
            if (parent != null) {
                json.append(",\"parent\":\"").append(parent).append('"');
            }
            if (bone == 0) {
                json.append(",\"cubes\":[");
                for (int cube = 0; cube < 36; cube++) {
                    if (cube > 0) {
                        json.append(',');
                    }
                    json.append(cube < 32 ? VISIBLE_CUBE : HIDDEN_CUBE);
                }
                json.append(']');
            }
            json.append('}');
        }
        json.append("]}]}");
        return bytes(json.toString());
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    public record MeshSnapshot(
            int quads,
            VertexSnapshot first,
            VertexSnapshot second,
            VertexSnapshot third,
            VertexSnapshot fourth
    ) {
    }

    public record VertexSnapshot(double x, double y, double z, float u, float v) {
    }
}
