package com.campus.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.book.common.constants.Constants;
import com.campus.book.entity.Book;
import com.campus.book.entity.Report;
import com.campus.book.entity.User;
import com.campus.book.mapper.BookMapper;
import com.campus.book.mapper.ReportMapper;
import com.campus.book.mapper.UserMapper;
import com.campus.book.service.ReportService;
import com.campus.book.util.SecurityUtils;
import com.campus.book.vo.ReportVO;
import org.springframework.beans.BeanUtils;

import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private BookMapper bookMapper;

    @Override
    @Transactional
    public Report createReport(String type, Long reportedUserId, Long bookId, String reason, String description) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);

        Report report = new Report();
        report.setType(type);
        report.setReportUserId(userId);
        report.setReportUsername(user.getUsername());
        report.setReportedUserId(reportedUserId);
        report.setReason(reason);
        report.setDescription(description);
        report.setStatus(Constants.REPORT_STATUS_PENDING);

        if (reportedUserId != null) {
            User reportedUser = userMapper.selectById(reportedUserId);
            if (reportedUser != null) {
                report.setReportedUsername(reportedUser.getUsername());
            }
        }

        if (bookId != null) {
            Book book = bookMapper.selectById(bookId);
            if (book != null) {
                report.setBookId(bookId);
                report.setBookTitle(book.getTitle());
            }
        }

        reportMapper.insert(report);
        return report;
    }

    @Override
    public Page<ReportVO> getReportList(String status, Integer pageNum, Integer pageSize) {
        Page<Report> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();

        if (status != null) {
            wrapper.eq(Report::getStatus, status);
        }

        wrapper.orderByDesc(Report::getCreateTime);
        Page<Report> reportPage = reportMapper.selectPage(page, wrapper);

        Page<ReportVO> voPage = new Page<>(reportPage.getCurrent(), reportPage.getSize(), reportPage.getTotal());
        voPage.setRecords(reportPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    @Transactional
    public void handleReport(Long reportId, String handleResult) {
        Report report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new RuntimeException("举报不存在");
        }

        Long handlerId = SecurityUtils.getCurrentUserId();
        report.setStatus(Constants.REPORT_STATUS_HANDLED);
        report.setHandlerId(handlerId);
        report.setHandleResult(handleResult);
        report.setHandleTime(java.time.LocalDateTime.now());
        reportMapper.updateById(report);

        if ("下架商品".equals(handleResult) && report.getBookId() != null) {
            Book book = bookMapper.selectById(report.getBookId());
            if (book != null) {
                book.setStatus(Constants.STATUS_OFFLINE);
                bookMapper.updateById(book);
            }
        }
    }

    @Override
    public Page<ReportVO> getFeedbackList(Integer pageNum, Integer pageSize) {
        Page<Report> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getType, "FEEDBACK").orderByDesc(Report::getCreateTime);
        Page<Report> reportPage = reportMapper.selectPage(page, wrapper);

        Page<ReportVO> voPage = new Page<>(reportPage.getCurrent(), reportPage.getSize(), reportPage.getTotal());
        voPage.setRecords(reportPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return voPage;
    }
    @Override
    public Page<ReportVO> getMyReports(Integer pageNum, Integer pageSize) {
        Long userId = SecurityUtils.getCurrentUserId();
        Page<Report> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Report> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Report::getReportUserId, userId)
                .orderByDesc(Report::getCreateTime);
        Page<Report> reportPage = reportMapper.selectPage(page, wrapper);

        Page<ReportVO> voPage = new Page<>(reportPage.getCurrent(), reportPage.getSize(), reportPage.getTotal());
        voPage.setRecords(reportPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return voPage;
    }
    private ReportVO convertToVO(Report report) {
        ReportVO vo = new ReportVO();
        BeanUtils.copyProperties(report, vo);
        return vo;
    }
}
