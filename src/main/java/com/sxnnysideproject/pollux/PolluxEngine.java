package com.sxnnysideproject.pollux;

import com.sxnnysideproject.pollux.internal.NativeBridge;

import java.io.IOException;
import java.lang.foreign.MemorySegment;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Stateful instance of the Pollux Core capability evaluation engine.
 * Implements {@link AutoCloseable} for use with Java try-with-resources or Kotlin `use { }`.
 */
public final class PolluxEngine implements AutoCloseable {

    public static final String EXPECTED_ABI_VERSION = "pollux-abi/1";

    private final NativeBridge bridge;
    private final MemorySegment handle;
    private final String abiVersion;
    private final String coreVersion;
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private PolluxEngine(NativeBridge bridge, MemorySegment handle) {
        this.bridge = bridge;
        this.handle = handle;
        this.abiVersion = bridge.getAbiVersion();
        this.coreVersion = bridge.getCoreVersion();

        if (!EXPECTED_ABI_VERSION.equals(abiVersion)) {
            close();
            throw new PolluxException.AbiMismatchException(EXPECTED_ABI_VERSION, abiVersion);
        }
    }

    /**
     * Loads and initializes a PolluxEngine with a YAML manifest string.
     */
    public static PolluxEngine load(String manifestYaml) {
        return load(manifestYaml, null);
    }

    /**
     * Loads and initializes a PolluxEngine with an explicit native library path.
     */
    public static PolluxEngine load(String manifestYaml, String customLibraryPath) {
        Objects.requireNonNull(manifestYaml, "Manifest YAML must not be null");
        NativeBridge bridge = new NativeBridge(customLibraryPath);
        MemorySegment handle = bridge.createEngine(manifestYaml);
        return new PolluxEngine(bridge, handle);
    }

    /**
     * Loads a PolluxEngine from a YAML manifest file on the filesystem.
     */
    public static PolluxEngine fromFile(Path manifestPath) {
        try {
            String yaml = Files.readString(manifestPath);
            return load(yaml);
        } catch (IOException e) {
            throw new PolluxException.ManifestException("Failed to read manifest file: " + e.getMessage());
        }
    }

    /**
     * Evaluates a strongly typed capability operation.
     */
    public EvaluationResult evaluate(Operation operation) {
        Objects.requireNonNull(operation, "Operation must not be null");
        return evaluate(operation.toJson());
    }

    /**
     * Evaluates a raw wire JSON operation string.
     */
    public EvaluationResult evaluate(String operationWireJson) {
        ensureNotClosed();
        Objects.requireNonNull(operationWireJson, "Operation wire JSON must not be null");
        String traceJson = bridge.evaluate(handle, operationWireJson);
        return EvaluationResult.fromTraceJson(traceJson);
    }

    public String getAbiVersion() {
        return abiVersion;
    }

    public String getCoreVersion() {
        return coreVersion;
    }

    public boolean isClosed() {
        return closed.get();
    }

    private void ensureNotClosed() {
        if (closed.get()) {
            throw new PolluxException.DisposedException();
        }
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            try {
                bridge.destroyEngine(handle);
            } finally {
                bridge.close();
            }
        }
    }
}
