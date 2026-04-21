package com.campus.book.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.book.common.result.Result;
import com.campus.book.entity.Order;
import com.campus.book.entity.Review;
import com.campus.book.mapper.OrderMapper;
import com.campus.book.mapper.ReviewMapper;
import com.campus.book.service.ReviewService;
import com.campus.book.vo.ReviewVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/review")
public class AdminReviewController {

    @Autowired
    private ReviewMapper reviewMapper;

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private OrderMapper orderMapper;

    @GetMapping("/list")
    public Result<Page<ReviewVO>> getReviewList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        Page<Review> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Review::getStatus, status);
        }
        wrapper.orderByDesc(Review::getCreateTime);
        Page<Review> reviewPage = reviewMapper.selectPage(page, wrapper);

        Page<ReviewVO> voPage = new Page<>(reviewPage.getCurrent(), reviewPage.getSize(), reviewPage.getTotal());
        voPage.setRecords(reviewPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return Result.success(voPage);
    }

    @PutMapping("/{id}/audit")
    public Result<Void> auditReview(@PathVariable Long id, @RequestParam String status) {
        reviewService.auditReview(id, status);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return Result.success();
    }

    private ReviewVO convertToVO(Review review) {
        if (review == null) return null;
        ReviewVO vo = new ReviewVO();
        BeanUtils.copyProperties(review, vo);
        return vo;
    }

    @GetMapping("/{id}/with-order")
    public Result<Map<String, Object>> getReviewWithOrder(@PathVariable Long id) {
        Review review = reviewMapper.selectById(id);
        if (review == null) {
            return Result.error("评价不存在");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("review", review);
        if (review.getOrderId() != null) {
            Order order = orderMapper.selectById(review.getOrderId());
            result.put("order", order);
        }
        return Result.success(result);
    }
}
