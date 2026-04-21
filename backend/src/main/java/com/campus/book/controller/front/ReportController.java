package com.campus.book.controller.front;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.book.common.result.Result;
import com.campus.book.service.ReportService;
import com.campus.book.vo.ReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/report")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @PostMapping
    public Result<Void> createReport(@RequestBody Map<String, Object> params) {
        String type = (String) params.get("type");
        Long reportedUserId = params.get("reportedUserId") != null ? Long.valueOf(params.get("reportedUserId").toString()) : null;
        Long bookId = params.get("bookId") != null ? Long.valueOf(params.get("bookId").toString()) : null;
        String reason = (String) params.get("reason");
        String description = (String) params.get("description");

        reportService.createReport(type, reportedUserId, bookId, reason, description);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<Page<ReportVO>> getMyReports(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reportService.getMyReports(pageNum, pageSize));
    }
}