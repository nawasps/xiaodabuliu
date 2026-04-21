package com.campus.book.controller.front;

import com.campus.book.common.result.Result;
import com.campus.book.service.OssService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/upload")
public class UploadController {

    private final OssService ossService;

    public UploadController(OssService ossService) {
        this.ossService = ossService;
    }

    @PostMapping("/book-image")
    public Result<Map<String, String>> uploadBookImage(@RequestParam("file") MultipartFile file) {
        String imageUrl = ossService.uploadBookImage(file);
        return Result.success(Collections.singletonMap("url", imageUrl));
    }
}
