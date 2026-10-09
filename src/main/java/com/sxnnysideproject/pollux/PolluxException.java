package com.sxnnysideproject.pollux;

import java.util.List;

/**
 * Base unchecked exception for all Pollux JVM SDK errors.
 */
public class PolluxException extends RuntimeException {
    public PolluxException(String message) {
        super(message);
    }

    public PolluxException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class ManifestException extends PolluxException {
        public ManifestException(String message) {
            super("Pollux manifest validation failed: " + message);
        }
    }

    public static class OperationException extends PolluxException {
        public OperationException(String message) {
            super("Pollux operation validation failed: " + message);
        }
    }

    public static class EvaluationException extends PolluxException {
        public EvaluationException(String message) {
            super("Pollux evaluation failed: " + message);
        }
    }

    public static class AbiMismatchException extends PolluxException {
        public AbiMismatchException(String expected, String actual) {
            super("Pollux ABI version mismatch. Expected: " + expected + ", Actual: " + actual);
        }
    }

    public static class LibraryNotFoundException extends PolluxException {
        public LibraryNotFoundException(List<String> candidates) {
            super("Could not find native Pollux Core library. Searched:\n  - "
                    + String.join("\n  - ", candidates)
                    + "\nSet POLLUX_CORE_LIB or POLLUX_FFI_PATH to the library path.");
        }
    }

    public static class DisposedException extends PolluxException {
        public DisposedException() {
            super("PolluxEngine instance has already been closed/disposed.");
        }
    }

    public static void checkStatus(int code, String context) {
        PolluxStatus status = PolluxStatus.fromCode(code);
        switch (status) {
            case OK -> { }
            case NULL_ARGUMENT -> throw new NullPointerException("Native call rejected NULL argument: " + context);
            case INVALID_UTF8 -> throw new PolluxException("Native call received invalid UTF-8: " + context);
            case MANIFEST_ERROR -> throw new ManifestException(context);
            case OPERATION_ERROR -> throw new OperationException(context);
            default -> throw new PolluxException("Native Pollux call failed with status (" + status + " / " + code + "): " + context);
        }
    }
}
