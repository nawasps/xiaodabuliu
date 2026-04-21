package com.campus.book.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.book.common.result.Result;
import com.campus.book.entity.Notice;
import com.campus.book.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/notice")
public class AdminNoticeController {

    @Autowired
    private NoticeService noticeService;

    @GetMapping("/list")
    public Result<Page<Notice>> getNoticeList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(noticeService.getNoticeList(pageNum, pageSize));
    }

    @GetMapping("/{id}")
    public Result<Notice> getNoticeById(@PathVariable Long id) {
        return Result.success(noticeService.getNoticeById(id));
    }

    @PostMapping
    public Result<Notice> createNotice(@RequestBody Notice notice) {
        return Result.success(noticeService.createNotice(notice));
    }

    @PutMapping("/{id}")
    public Result<Notice> updateNotice(@PathVariable Long id, @RequestBody Notice notice) {
        notice.setId(id);
        return Result.success(noticeService.updateNotice(notice));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteNotice(@PathVariable Long id) {
        noticeService.deleteNotice(id);
        return Result.success();
    }
}
