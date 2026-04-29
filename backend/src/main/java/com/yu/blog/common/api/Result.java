package com.yu.blog.common.api;

import java.time.OffsetDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public record Result<T>(
        int code,
        String message,
        T data,
        String traceId
) {
    private static final int SUCCESS_CODE = 0;
    private static final String SUCCESS_MESSAGE = "OK";

    public static <T> Result<T> ok(T data) {
        return new Result<>(SUCCESS_CODE, SUCCESS_MESSAGE, data, newTraceId());
    }

    public static Result<Void> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(int code, String message) {
        return new Result<>(code, message, null, newTraceId());
    }

    private static String newTraceId() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        return date + "-" + suffix;
    }
}
