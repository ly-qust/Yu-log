package com.yu.blog.module.file.vo;

public record FileUploadVO(
        String url,
        String filename,
        String originalFilename,
        String contentType,
        long size
) {
}
