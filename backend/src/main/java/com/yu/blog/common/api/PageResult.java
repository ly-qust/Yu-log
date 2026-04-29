package com.yu.blog.common.api;

import java.util.List;

public record PageResult<T>(
        List<T> list,
        long pageNum,
        long pageSize,
        long total,
        long totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
    public static <T> PageResult<T> of(List<T> list, long pageNum, long pageSize, long total) {
        long safePageSize = pageSize <= 0 ? 10 : pageSize;
        long safePageNum = pageNum <= 0 ? 1 : pageNum;
        long totalPages = total == 0 ? 0 : (long) Math.ceil((double) total / safePageSize);
        return new PageResult<>(
                list == null ? List.of() : list,
                safePageNum,
                safePageSize,
                total,
                totalPages,
                safePageNum < totalPages,
                safePageNum > 1
        );
    }

    public static <T> PageResult<T> empty(long pageNum, long pageSize) {
        return of(List.of(), pageNum, pageSize, 0);
    }
}
