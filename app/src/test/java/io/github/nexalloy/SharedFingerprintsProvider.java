package io.github.pikoxposed;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SharedFingerprintsProvider {
    public static List<String> getSharedFingerprints(String app) {
        return switch (app) {
            case "youtube", "music" ->
                    Arrays.asList(io.github.pikoxposed.morphe.shared.misc.debugging.FingerprintsKt.class.getName());
            default -> new ArrayList<>();
        };
    }
}
