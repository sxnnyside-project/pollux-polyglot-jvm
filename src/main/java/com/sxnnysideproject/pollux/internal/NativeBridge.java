package com.sxnnysideproject.pollux.internal;

import com.sxnnysideproject.pollux.PolluxException;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * Low-level Panama Foreign Function and Memory (FFM) binding layer for Pollux ABI.
 */
public final class NativeBridge implements AutoCloseable {

    private final Arena arena;
    private final MethodHandle abiVersionHandle;
    private final MethodHandle coreVersionHandle;
    private final MethodHandle engineCreateHandle;
    private final MethodHandle engineEvaluateHandle;
    private final MethodHandle stringFreeHandle;
    private final MethodHandle engineDestroyHandle;

    public NativeBridge(String customLibraryPath) {
        this.arena = Arena.ofShared();
        Path libPath = LibraryLoader.resolveLibraryPath(customLibraryPath);

        Linker linker = Linker.nativeLinker();
        SymbolLookup lookup = SymbolLookup.libraryLookup(libPath, arena);

        this.abiVersionHandle = linker.downcallHandle(
                lookup.find("pollux_abi_version")
                        .orElseThrow(() -> new PolluxException("Missing symbol: pollux_abi_version")),
                FunctionDescriptor.of(ValueLayout.ADDRESS)
        );

        this.coreVersionHandle = linker.downcallHandle(
                lookup.find("pollux_core_version")
                        .orElseThrow(() -> new PolluxException("Missing symbol: pollux_core_version")),
                FunctionDescriptor.of(ValueLayout.ADDRESS)
        );

        this.engineCreateHandle = linker.downcallHandle(
                lookup.find("pollux_engine_create")
                        .orElseThrow(() -> new PolluxException("Missing symbol: pollux_engine_create")),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS,
                        ValueLayout.JAVA_LONG,
                        ValueLayout.ADDRESS
                )
        );

        this.engineEvaluateHandle = linker.downcallHandle(
                lookup.find("pollux_engine_evaluate")
                        .orElseThrow(() -> new PolluxException("Missing symbol: pollux_engine_evaluate")),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT,
                        ValueLayout.ADDRESS,
                        ValueLayout.ADDRESS,
                        ValueLayout.JAVA_LONG,
                        ValueLayout.ADDRESS
                )
        );

        this.stringFreeHandle = linker.downcallHandle(
                lookup.find("pollux_string_free")
                        .orElseThrow(() -> new PolluxException("Missing symbol: pollux_string_free")),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );

        this.engineDestroyHandle = linker.downcallHandle(
                lookup.find("pollux_engine_destroy")
                        .orElseThrow(() -> new PolluxException("Missing symbol: pollux_engine_destroy")),
                FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
        );
    }

    public String getAbiVersion() {
        try {
            MemorySegment ptr = (MemorySegment) abiVersionHandle.invokeExact();
            return ptr.reinterpret(Long.MAX_VALUE).getString(0, StandardCharsets.UTF_8);
        } catch (Throwable e) {
            throw new PolluxException("Failed to invoke pollux_abi_version", e);
        }
    }

    public String getCoreVersion() {
        try {
            MemorySegment ptr = (MemorySegment) coreVersionHandle.invokeExact();
            return ptr.reinterpret(Long.MAX_VALUE).getString(0, StandardCharsets.UTF_8);
        } catch (Throwable e) {
            throw new PolluxException("Failed to invoke pollux_core_version", e);
        }
    }

    public MemorySegment createEngine(String manifestYaml) {
        try (Arena localArena = Arena.ofConfined()) {
            byte[] yamlBytes = manifestYaml.getBytes(StandardCharsets.UTF_8);
            MemorySegment yamlSegment = localArena.allocateFrom(ValueLayout.JAVA_BYTE, yamlBytes);
            MemorySegment outHandle = localArena.allocate(ValueLayout.ADDRESS);

            int status = (int) engineCreateHandle.invokeExact(
                    yamlSegment,
                    (long) yamlBytes.length,
                    outHandle
            );

            if (status != 0) {
                PolluxException.checkStatus(status, "Engine creation failed");
            }

            return outHandle.get(ValueLayout.ADDRESS, 0);
        } catch (PolluxException e) {
            throw e;
        } catch (Throwable e) {
            throw new PolluxException("Unexpected error creating engine handle", e);
        }
    }

    public String evaluate(MemorySegment engineHandle, String operationWireJson) {
        try (Arena localArena = Arena.ofConfined()) {
            byte[] opBytes = operationWireJson.getBytes(StandardCharsets.UTF_8);
            MemorySegment opSegment = localArena.allocateFrom(ValueLayout.JAVA_BYTE, opBytes);
            MemorySegment outTracePtr = localArena.allocate(ValueLayout.ADDRESS);

            int status = (int) engineEvaluateHandle.invokeExact(
                    engineHandle,
                    opSegment,
                    (long) opBytes.length,
                    outTracePtr
            );

            MemorySegment traceAddress = outTracePtr.get(ValueLayout.ADDRESS, 0);
            if (traceAddress.equals(MemorySegment.NULL)) {
                PolluxException.checkStatus(status, "Evaluation produced NULL trace");
                return "{\"decision\":\"deny\",\"status\":" + status + "}";
            }

            String traceJson = traceAddress.reinterpret(Long.MAX_VALUE).getString(0, StandardCharsets.UTF_8);
            stringFreeHandle.invokeExact(traceAddress);

            if (status != 0) {
                PolluxException.checkStatus(status, traceJson);
            }

            return traceJson;
        } catch (PolluxException e) {
            throw e;
        } catch (Throwable e) {
            throw new PolluxException("Unexpected error during engine evaluation", e);
        }
    }

    public void destroyEngine(MemorySegment engineHandle) {
        if (engineHandle.equals(MemorySegment.NULL)) {
            return;
        }
        try {
            engineDestroyHandle.invokeExact(engineHandle);
        } catch (Throwable e) {
            throw new PolluxException("Failed to destroy native engine handle", e);
        }
    }

    @Override
    public void close() {
        if (arena.scope().isAlive()) {
            arena.close();
        }
    }
}
