package com.campus.book.service;

import org.springframework.web.multipart.MultipartFile;

public interface OssService {
    String uploadBookImage(MultipartFile file);
}
