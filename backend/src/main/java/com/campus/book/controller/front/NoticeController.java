package com.campus.book.controller.front;

import com.campus.book.common.result.Result;
import com.campus.book.entity.Notice;
import com.campus.book.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    @GetMapping("/list")
    public Result<Object> getNoticeList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(noticeService.getNoticeList(pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Notice> getNoticeById(@PathVariable Long id) {
        return Result.success(noticeService.getNoticeById(id));
    }
}
