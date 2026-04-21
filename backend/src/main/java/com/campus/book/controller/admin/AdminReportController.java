package com.campus.book.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.book.common.result.Result;
import com.campus.book.service.ReportService;
import com.campus.book.vo.ReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/report")
public class AdminReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/list")
    public Result<Page<ReportVO>> getReportList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        return Result.success(reportService.getReportList(status, pageNum, pageSize));
    }

    @PutMapping("/{id}/handle")
    public Result<Void> handleReport(@PathVariable Long id, @RequestBody Map<String, String> params) {
        String handleResult = params.get("handleResult");
        reportService.handleReport(id, handleResult);
        return Result.success();
    }

    @GetMapping("/feedback/list")
    public Result<Page<ReportVO>> getFeedbackList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reportService.getFeedbackList(pageNum, pageSize));
    }
}
