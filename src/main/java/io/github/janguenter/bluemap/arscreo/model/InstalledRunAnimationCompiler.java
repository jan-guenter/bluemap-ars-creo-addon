/*
 * SPDX-License-Identifier: MIT
 *
 * Independently authored interpreter for the operator-installed GeckoLib
 * animation resource. No Ars Creo resource is packaged with this add-on.
 */

package io.github.janguenter.bluemap.arscreo.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.janguenter.bluemap.arscreo.model.WheelModel.Vec3;
import io.github.janguenter.bluemap.arscreo.model.WheelPose.BoneTransform;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Samples the exact installed looping {@code run} animation into four poses. */
public final class InstalledRunAnimationCompiler {

    public static final double EXPECTED_LENGTH_SECONDS = 0.56D;
    public static final int POSE_COUNT = 4;
    public static final int DISPLAY_TICKS = 11;
    public static final List<Double> SAMPLE_TIMES = List.of(0D, 0.14D, 0.28D, 0.42D);
    public static final List<Double> SYNTHETIC_WHEEL_PHASES =
            List.of(0D, -11.25D, -22.5D, -33.75D);
    private static final int MAX_BYTES = 64 * 1024;
    private static final int MAX_KEYFRAMES = 32;
    private static final Set<String> EXPECTED_BONES = Set.of(
            "starbuncle", "head", "ear_left", "ear_right", "tail",
            "legs_front", "legs_back"
    );
    private static final Set<String> SUPPORTED_EASINGS = Set.of(
            "linear", "easeInSine", "easeOutSine"
    );
    private static final Vec3 ZERO_VECTOR = new Vec3(0D, 0D, 0D);

    private InstalledRunAnimationCompiler() {
    }

    public static RunAnimation compile(byte[] raw) {
        if (raw.length < 2 || raw.length > MAX_BYTES) {
            throw new IllegalArgumentException("installed animation is outside the byte budget");
        }
        JsonObject root = object(JsonParser.parseString(
                new String(raw, StandardCharsets.UTF_8)
        ), "root");
        if (!"1.8.0".equals(string(root.get("format_version")))) {
            throw new IllegalArgumentException("unsupported installed animation version");
        }
        JsonObject run = object(
                object(root.get("animations"), "animations").get("run"), "run animation"
        );
        if (!bool(run.get("loop"))
                || Double.compare(number(run.get("animation_length")),
                        EXPECTED_LENGTH_SECONDS) != 0) {
            throw new IllegalArgumentException("installed run loop contract changed");
        }
        JsonObject bones = object(run.get("bones"), "run bones");
        if (!bones.keySet().equals(EXPECTED_BONES)) {
            throw new IllegalArgumentException("installed run bone roster changed");
        }

        Map<String, BoneTracks> tracks = new LinkedHashMap<>();
        for (Map.Entry<String, JsonElement> entry : bones.entrySet()) {
            JsonObject channels = object(entry.getValue(), "run bone");
            if (!Set.of("rotation", "position").containsAll(channels.keySet())) {
                throw new IllegalArgumentException("installed run channel roster changed");
            }
            Track rotation = channels.has("rotation")
                    ? track(channels.get("rotation"), "rotation") : Track.ZERO;
            Track position = channels.has("position")
                    ? track(channels.get("position"), "position") : Track.ZERO;
            tracks.put(entry.getKey(), new BoneTracks(rotation, position));
        }

        List<WheelPose> poses = new ArrayList<>(POSE_COUNT);
        for (int index = 0; index < SAMPLE_TIMES.size(); index++) {
            double time = SAMPLE_TIMES.get(index);
            Map<String, BoneTransform> transforms = new LinkedHashMap<>();
            tracks.forEach((bone, boneTracks) -> transforms.put(bone, new BoneTransform(
                    signedRotation(boneTracks.rotation.sample(time)),
                    signedTranslation(boneTracks.position.sample(time))
            )));
            transforms.put("wheel", new BoneTransform(
                    new Vec3(0D, SYNTHETIC_WHEEL_PHASES.get(index), 0D), ZERO_VECTOR
            ));
            poses.add(new WheelPose(transforms));
        }
        return new RunAnimation(EXPECTED_LENGTH_SECONDS, SAMPLE_TIMES, poses);
    }

    private static Track track(JsonElement source, String label) {
        JsonObject values = object(source, label + " keyframes");
        if (values.size() < 2 || values.size() > MAX_KEYFRAMES) {
            throw new IllegalArgumentException("installed run keyframe count changed");
        }
        List<Keyframe> keyframes = new ArrayList<>(values.size());
        for (Map.Entry<String, JsonElement> entry : values.entrySet()) {
            double time;
            try {
                time = Double.parseDouble(entry.getKey());
            } catch (NumberFormatException exception) {
                throw new IllegalArgumentException("malformed installed run timestamp", exception);
            }
            if (!Double.isFinite(time) || time < 0D || time > EXPECTED_LENGTH_SECONDS) {
                throw new IllegalArgumentException("installed run timestamp is out of bounds");
            }
            JsonObject keyframe = object(entry.getValue(), label + " keyframe");
            if (!Set.of("vector", "easing").containsAll(keyframe.keySet())) {
                throw new IllegalArgumentException("installed run keyframe schema changed");
            }
            String easing = keyframe.has("easing")
                    ? string(keyframe.get("easing")) : "linear";
            if (!SUPPORTED_EASINGS.contains(easing)) {
                throw new IllegalArgumentException("unsupported installed run easing");
            }
            keyframes.add(new Keyframe(time, vector(keyframe.get("vector"), label), easing));
        }
        keyframes.sort(Comparator.comparingDouble(Keyframe::time));
        if (Double.compare(keyframes.getFirst().time, 0D) != 0
                || Double.compare(keyframes.getLast().time,
                        EXPECTED_LENGTH_SECONDS) != 0) {
            throw new IllegalArgumentException("installed run keyframe range changed");
        }
        return new Track(keyframes);
    }

    private static Vec3 signedRotation(Vec3 raw) {
        return new Vec3(-raw.x(), -raw.y(), raw.z());
    }

    private static Vec3 signedTranslation(Vec3 raw) {
        return new Vec3(-raw.x() / 16D, raw.y() / 16D, raw.z() / 16D);
    }

    private static double ease(String easing, double progress) {
        return switch (easing) {
            case "linear" -> progress;
            case "easeInSine" -> 1D - Math.cos(progress * Math.PI / 2D);
            case "easeOutSine" -> Math.sin(progress * Math.PI / 2D);
            default -> throw new IllegalArgumentException("unknown run easing");
        };
    }

    private static Vec3 vector(JsonElement value, String label) {
        JsonArray array = array(value, label + " vector");
        if (array.size() != 3) {
            throw new IllegalArgumentException(label + " vector must contain three numbers");
        }
        return new Vec3(number(array.get(0)), number(array.get(1)), number(array.get(2)));
    }

    private static JsonObject object(JsonElement value, String label) {
        if (value == null || !value.isJsonObject()) {
            throw new IllegalArgumentException(label + " must be an object");
        }
        return value.getAsJsonObject();
    }

    private static JsonArray array(JsonElement value, String label) {
        if (value == null || !value.isJsonArray()) {
            throw new IllegalArgumentException(label + " must be an array");
        }
        return value.getAsJsonArray();
    }

    private static String string(JsonElement value) {
        if (value == null || !value.isJsonPrimitive()
                || !value.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("malformed installed animation string");
        }
        String result = value.getAsString();
        if (result.isBlank() || result.length() > 64) {
            throw new IllegalArgumentException("installed animation string is outside budget");
        }
        return result;
    }

    private static boolean bool(JsonElement value) {
        if (value == null || !value.isJsonPrimitive()
                || !value.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException("malformed installed animation boolean");
        }
        return value.getAsBoolean();
    }

    private static double number(JsonElement value) {
        if (value == null || !value.isJsonPrimitive()
                || !value.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException("malformed installed animation number");
        }
        double result = value.getAsDouble();
        if (!Double.isFinite(result) || Math.abs(result) > 16_384D) {
            throw new IllegalArgumentException("installed animation number is out of bounds");
        }
        return result;
    }

    public record RunAnimation(
            double lengthSeconds,
            List<Double> sampleTimes,
            List<WheelPose> poses
    ) {

        public RunAnimation {
            sampleTimes = List.copyOf(sampleTimes);
            poses = List.copyOf(poses);
            if (sampleTimes.size() != POSE_COUNT || poses.size() != POSE_COUNT) {
                throw new IllegalArgumentException("run sample count changed");
            }
        }
    }

    private record BoneTracks(Track rotation, Track position) {
    }

    private record Keyframe(double time, Vec3 value, String easing) {
    }

    private record Track(List<Keyframe> keyframes) {

        private static final Track ZERO = new Track(List.of());

        private Track {
            keyframes = List.copyOf(keyframes);
        }

        Vec3 sample(double time) {
            if (keyframes.isEmpty()) {
                return ZERO_VECTOR;
            }
            if (time <= keyframes.getFirst().time) {
                return keyframes.getFirst().value;
            }
            for (int index = 1; index < keyframes.size(); index++) {
                Keyframe before = keyframes.get(index - 1);
                Keyframe after = keyframes.get(index);
                if (time <= after.time) {
                    double progress = (time - before.time) / (after.time - before.time);
                    return interpolate(before.value, after.value, ease(after.easing, progress));
                }
            }
            return keyframes.getLast().value;
        }

        private static Vec3 interpolate(Vec3 from, Vec3 to, double progress) {
            return from.add(to.subtract(from).scale(progress));
        }
    }
}
