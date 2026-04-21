package com.campus.book.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.book.dto.ReviewDTO;
import com.campus.book.entity.Review;
import com.campus.book.vo.ReviewVO;

public interface ReviewService {

    Review createReview(Long orderId, ReviewDTO dto);

    Page<ReviewVO> getBookReviews(Long bookId, Integer pageNum, Integer pageSize);

    Page<ReviewVO> getUserReviews(Long userId, Integer pageNum, Integer pageSize);

    void auditReview(Long reviewId, String status);

    void deleteReview(Long reviewId);
}
