package com.sxnnysideproject.pollux;

/**
 * Native status codes returned by Pollux ABI C functions.
 * Field-for-field, value-for-value identical to enum PolluxStatus in pollux.h.
 */
public enum PolluxStatus {
    OK(0),
    NULL_ARGUMENT(1),
    INVALID_UTF8(2),
    MANIFEST_ERROR(3),
    OPERATION_ERROR(4),
    INTERNAL_ERROR(5),
    UNKNOWN(Integer.MIN_VALUE);

    private final int code;

    PolluxStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static PolluxStatus fromCode(int code) {
        for (PolluxStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return UNKNOWN;
    }
}
