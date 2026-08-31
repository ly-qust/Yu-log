package com.yu.blog.module.file.service;

import com.yu.blog.module.file.vo.FileUploadVO;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    FileUploadVO upload(MultipartFile file, String bizType);
}
