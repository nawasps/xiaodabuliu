package com.campus.book.controller.admin;

import com.campus.book.common.result.Result;
import com.campus.book.common.constants.Constants;
import com.campus.book.mapper.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/dashboard")
public class AdminDashboardController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private ReportMapper reportMapper;

    @GetMapping("/stats")
    public Result<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalUsers = userMapper.selectCount(null);
        long totalBooks = bookMapper.selectCount(null);
        long totalOrders = orderMapper.selectCount(null);
        long pendingReports = reportMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.campus.book.entity.Report>()
                        .eq(com.campus.book.entity.Report::getStatus, Constants.REPORT_STATUS_PENDING));
        long pendingBooks = bookMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.campus.book.entity.Book>()
                        .eq(com.campus.book.entity.Book::getStatus, Constants.STATUS_PENDING));

        stats.put("totalUsers", totalUsers);
        stats.put("totalBooks", totalBooks);
        stats.put("totalOrders", totalOrders);
        stats.put("pendingReports", pendingReports);
        stats.put("pendingBooks", pendingBooks);

        return Result.success(stats);
    }

    @GetMapping("/chart")
    public Result<Map<String, Object>> getChartData(@RequestParam String type) {
        Map<String, Object> data = new HashMap<>();
        return Result.success(data);
    }
}
