package com.campus.book.service;

import com.campus.book.entity.Report;
import com.campus.book.vo.ReportVO;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

public interface ReportService {
    Report createReport(String type, Long reportedUserId, Long bookId, String reason, String description);
    Page<ReportVO> getReportList(String status, Integer pageNum, Integer pageSize);
    void handleReport(Long reportId, String handleResult);
    Page<ReportVO> getFeedbackList(Integer pageNum, Integer pageSize);
    Page<ReportVO> getMyReports(Integer pageNum, Integer pageSize);
}