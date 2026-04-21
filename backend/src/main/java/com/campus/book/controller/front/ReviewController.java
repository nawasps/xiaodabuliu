package com.campus.book.controller.front;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.book.common.result.Result;
import com.campus.book.dto.ReviewDTO;
import com.campus.book.entity.Review;
import com.campus.book.service.ReviewService;
import com.campus.book.vo.ReviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @PostMapping("/{orderId}")
    public Result<Review> createReview(@PathVariable Long orderId, @Validated @RequestBody ReviewDTO dto) {
        return Result.success(reviewService.createReview(orderId, dto));
    }

    @GetMapping("/book/{bookId}")
    public Result<Page<ReviewVO>> getBookReviews(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reviewService.getBookReviews(bookId, pageNum, pageSize));
    }

    @GetMapping("/user/{userId}")
    public Result<Page<ReviewVO>> getUserReviews(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(reviewService.getUserReviews(userId, pageNum, pageSize));
    }
}
