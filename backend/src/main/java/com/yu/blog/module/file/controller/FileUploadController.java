package com.yu.blog.module.file.controller;

import com.yu.blog.common.api.Result;
import com.yu.blog.module.file.service.StorageService;
import com.yu.blog.module.file.vo.FileUploadVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/files")
@RequiredArgsConstructor
public class FileUploadController {
    private final StorageService storageService;

    @PostMapping("/upload")
    public Result<FileUploadVO> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false, defaultValue = "other") String bizType
    ) {
        return Result.ok(storageService.upload(file, bizType));
    }
}
