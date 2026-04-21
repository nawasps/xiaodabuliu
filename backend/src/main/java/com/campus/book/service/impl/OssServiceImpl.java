package com.campus.book.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.campus.book.config.OssProperties;
import com.campus.book.service.OssService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.UUID;

@Service
public class OssServiceImpl implements OssService {

    private final OssProperties ossProperties;

    public OssServiceImpl(OssProperties ossProperties) {
        this.ossProperties = ossProperties;
    }

    @Override
    public String uploadBookImage(MultipartFile file) {
        validateConfig();
        validateFile(file);

        String extension = getExtension(file.getOriginalFilename());
        LocalDate today = LocalDate.now();
        String objectKey = String.format(
                "book-images/%d/%02d/%02d/%s.%s",
                today.getYear(),
                today.getMonthValue(),
                today.getDayOfMonth(),
                UUID.randomUUID().toString().replace("-", ""),
                extension
        );

        OSS ossClient = null;
        try {
            ossClient = new OSSClientBuilder().build(
                    normalizeEndpoint(ossProperties.getEndpoint()),
                    ossProperties.getAccessKeyId(),
                    ossProperties.getAccessKeySecret()
            );
            ossClient.putObject(ossProperties.getBucketName(), objectKey, file.getInputStream());
            return buildAccessUrl(objectKey);
        } catch (IOException e) {
            throw new RuntimeException("上传图片失败，请稍后重试");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    private void validateConfig() {
        if (!StringUtils.hasText(ossProperties.getEndpoint())
                || !StringUtils.hasText(ossProperties.getBucketName())
                || !StringUtils.hasText(ossProperties.getAccessKeyId())
                || !StringUtils.hasText(ossProperties.getAccessKeySecret())) {
            throw new RuntimeException("OSS配置未完成，请先在配置文件中填写参数");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("上传文件不能为空");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new RuntimeException("仅支持图片文件上传");
        }
        long maxSize = 5 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new RuntimeException("图片大小不能超过5MB");
        }
    }

    private String buildAccessUrl(String objectKey) {
        if (StringUtils.hasText(ossProperties.getDomain())) {
            return String.format("https://%s/%s", trimSlash(ossProperties.getDomain()), objectKey);
        }
        return String.format(
                "https://%s.%s/%s",
                ossProperties.getBucketName(),
                trimProtocol(ossProperties.getEndpoint()),
                objectKey
        );
    }

    private String getExtension(String fileName) {
        if (StringUtils.hasText(fileName) && fileName.contains(".")) {
            String suffix = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
            if (StringUtils.hasText(suffix)) {
                return suffix;
            }
        }
        return "jpg";
    }

    private String normalizeEndpoint(String endpoint) {
        if (endpoint.startsWith("http://") || endpoint.startsWith("https://")) {
            return endpoint;
        }
        return "https://" + endpoint;
    }

    private String trimProtocol(String value) {
        return value.replaceFirst("^https?://", "");
    }

    private String trimSlash(String value) {
        return value.replaceAll("/$", "").replaceFirst("^https?://", "");
    }
}
