package com.sxnnysideproject.pollux.internal;

import com.sxnnysideproject.pollux.PolluxException;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Resolves the absolute path to the native Pollux Core dynamic library.
 */
public final class LibraryLoader {

    private LibraryLoader() {}

    public static String getPlatformLibraryName() {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("mac") || os.contains("darwin")) {
            return "libpollux_ffi.dylib";
        }
        if (os.contains("win")) {
            return "pollux_ffi.dll";
        }
        return "libpollux_ffi.so";
    }

    public static Path resolveLibraryPath(String explicitPath) {
        if (explicitPath != null && !explicitPath.isBlank()) {
            Path path = Paths.get(explicitPath);
            if (Files.exists(path)) {
                return path.toAbsolutePath();
            }
        }

        String envLib = System.getenv("POLLUX_CORE_LIB");
        if (envLib == null || envLib.isBlank()) {
            envLib = System.getenv("POLLUX_FFI_PATH");
        }
        if (envLib != null && !envLib.isBlank()) {
            Path path = Paths.get(envLib);
            if (Files.exists(path)) {
                return path.toAbsolutePath();
            }
        }

        String libName = getPlatformLibraryName();
        String envDir = System.getenv("POLLUX_CORE_DIR");
        if (envDir != null && !envDir.isBlank()) {
            Path inLib = Paths.get(envDir, "lib", libName);
            if (Files.exists(inLib)) {
                return inLib.toAbsolutePath();
            }
            Path inDir = Paths.get(envDir, libName);
            if (Files.exists(inDir)) {
                return inDir.toAbsolutePath();
            }
        }

        Path userDir = Paths.get(System.getProperty("user.dir", "."));
        Path ecosystemRoot = userDir.getParent() != null ? userDir.getParent().getParent() : null;

        List<String> candidates = new ArrayList<>();
        candidates.add(userDir.resolve("lib").resolve(libName).toString());
        candidates.add(userDir.resolve(libName).toString());
        candidates.add(userDir.resolve("build").resolve("lib").resolve(libName).toString());

        if (ecosystemRoot != null) {
            String arch = System.getProperty("os.arch", "").toLowerCase();
            String targetArch = (arch.contains("aarch64") || arch.contains("arm64")) ? "aarch64" : "x86_64";
            String os = System.getProperty("os.name", "").toLowerCase();
            String targetOs = (os.contains("mac") || os.contains("darwin")) ? "apple-darwin" : "unknown-linux-gnu";
            String targetTriple = "pollux-core-0.1.0-" + targetArch + "-" + targetOs;

            candidates.add(ecosystemRoot.resolve("Native/pollux-polyglot-native-bridge/target/pollux-core-dist-cache")
                    .resolve(targetTriple).resolve("lib").resolve(libName).toString());
            candidates.add(ecosystemRoot.resolve("Native/pollux-polyglot-native-bridge/target/release").resolve(libName).toString());
            candidates.add(ecosystemRoot.resolve("Native/pollux-polyglot-native-bridge/target/debug").resolve(libName).toString());
            candidates.add(ecosystemRoot.resolve("Pollux/target/release").resolve(libName).toString());
            candidates.add(ecosystemRoot.resolve("Pollux/target/debug").resolve(libName).toString());
            candidates.add(ecosystemRoot.resolve("Distribution/lib").resolve(libName).toString());
        }

        // System directories
        candidates.add("/usr/local/lib/" + libName);
        candidates.add("/usr/lib/" + libName);
        candidates.add("/opt/homebrew/lib/" + libName);

        for (String candidate : candidates) {
            Path path = Paths.get(candidate);
            if (Files.exists(path)) {
                return path.toAbsolutePath();
            }
        }

        throw new PolluxException.LibraryNotFoundException(candidates);
    }
}
