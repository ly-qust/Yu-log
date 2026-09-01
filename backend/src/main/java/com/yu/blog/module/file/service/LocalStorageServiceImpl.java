package com.yu.blog.module.file.service;

import com.yu.blog.common.exception.BusinessException;
import com.yu.blog.module.file.config.FileUploadProperties;
import com.yu.blog.module.file.vo.FileUploadVO;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalStorageServiceImpl implements StorageService {
    private static final int FILE_ERROR_CODE = 400;
    private static final int FILE_TOO_LARGE_CODE = 413;
    private static final int FILE_TYPE_CODE = 415;
    private static final DateTimeFormatter YEAR_FORMAT = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MM");
    private static final Set<String> ALLOWED_BIZ_TYPES = Set.of(
            "article-cover",
            "project-cover",
            "avatar",
            "site",
            "other"
    );
    private static final Map<String, Set<String>> CONTENT_TYPE_EXTENSIONS = Map.of(
            "image/jpeg", Set.of("jpg", "jpeg"),
            "image/png", Set.of("png"),
            "image/webp", Set.of("webp"),
            "image/gif", Set.of("gif")
    );

    private final FileUploadProperties properties;

    public LocalStorageServiceImpl(FileUploadProperties properties) {
        this.properties = properties;
    }

    @Override
    public FileUploadVO upload(MultipartFile file, String bizType) {
        validateFile(file);
        String normalizedBizType = normalizeBizType(bizType);
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() == null ? "upload" : file.getOriginalFilename());
        String contentType = normalizeContentType(file.getContentType());
        String extension = extractExtension(originalFilename);
        validateType(contentType, extension);
        validateMagicBytes(file, contentType);

        LocalDate now = LocalDate.now();
        String year = now.format(YEAR_FORMAT);
        String month = now.format(MONTH_FORMAT);
        String filename = normalizedBizType + "-" + UUID.randomUUID().toString().replace("-", "") + "." + extension;

        Path root = Path.of(properties.getLocalPath()).toAbsolutePath().normalize();
        Path targetDir = root.resolve(year).resolve(month).normalize();
        Path target = targetDir.resolve(filename).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException(FILE_ERROR_CODE, "文件存储路径非法");
        }

        try {
            Files.createDirectories(targetDir);
            file.transferTo(target);
        } catch (IOException exception) {
            throw new BusinessException(FILE_ERROR_CODE, "文件上传失败，请稍后重试");
        }

        String url = properties.normalizedPublicPrefix() + "/" + year + "/" + month + "/" + filename;
        return new FileUploadVO(url, filename, originalFilename, contentType, file.getSize());
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(FILE_ERROR_CODE, "上传文件不能为空");
        }
        if (file.getSize() > properties.maxSizeBytes()) {
            throw new BusinessException(FILE_TOO_LARGE_CODE, "文件大小不能超过 " + properties.getMaxSizeMb() + "MB");
        }
    }

    private String normalizeBizType(String bizType) {
        String normalized = bizType == null || bizType.isBlank() ? "other" : bizType.trim();
        if (!ALLOWED_BIZ_TYPES.contains(normalized)) {
            return "other";
        }
        return normalized;
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            throw new BusinessException(FILE_ERROR_CODE, "文件类型不能为空");
        }
        return contentType.toLowerCase(Locale.ROOT);
    }

    private String extractExtension(String originalFilename) {
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) {
            throw new BusinessException(FILE_ERROR_CODE, "文件扩展名不合法");
        }
        return originalFilename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }

    private void validateType(String contentType, String extension) {
        if (!properties.getAllowedTypes().contains(contentType)) {
            throw new BusinessException(FILE_TYPE_CODE, "仅支持 jpg、png、webp、gif 图片");
        }
        Set<String> allowedExtensions = CONTENT_TYPE_EXTENSIONS.get(contentType);
        if (allowedExtensions == null || !allowedExtensions.contains(extension)) {
            throw new BusinessException(FILE_TYPE_CODE, "文件扩展名与类型不匹配");
        }
    }

    private void validateMagicBytes(MultipartFile file, String contentType) {
        try (InputStream input = file.getInputStream()) {
            byte[] header = input.readNBytes(12);
            boolean valid = switch (contentType) {
                case "image/jpeg" -> header.length >= 3
                        && (header[0] & 0xFF) == 0xFF
                        && (header[1] & 0xFF) == 0xD8
                        && (header[2] & 0xFF) == 0xFF;
                case "image/png" -> startsWith(header, new byte[]{(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A});
                case "image/gif" -> startsWith(header, new byte[]{'G', 'I', 'F', '8'})
                        && header.length >= 6 && (header[4] == '7' || header[4] == '9') && header[5] == 'a';
                case "image/webp" -> header.length >= 12
                        && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
                        && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
                default -> false;
            };
            if (!valid) {
                throw new BusinessException(FILE_TYPE_CODE, "文件内容与图片类型不匹配");
            }
        } catch (IOException exception) {
            throw new BusinessException(FILE_TYPE_CODE, "无法读取图片文件");
        }
    }

    private boolean startsWith(byte[] value, byte[] prefix) {
        if (value.length < prefix.length) {
            return false;
        }
        for (int index = 0; index < prefix.length; index++) {
            if (value[index] != prefix[index]) {
                return false;
            }
        }
        return true;
    }
}
