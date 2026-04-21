package com.campus.book.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campus.book.common.constants.Constants;
import com.campus.book.dto.ReviewDTO;
import com.campus.book.entity.Order;
import com.campus.book.entity.Review;
import com.campus.book.entity.User;
import com.campus.book.mapper.OrderItemMapper;
import com.campus.book.mapper.OrderMapper;
import com.campus.book.mapper.ReviewMapper;
import com.campus.book.mapper.UserMapper;
import com.campus.book.service.ReviewService;
import com.campus.book.util.SecurityUtils;
import com.campus.book.vo.ReviewVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewMapper reviewMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public Review createReview(Long orderId, ReviewDTO dto) {
        Long userId = SecurityUtils.getCurrentUserId();
        User user = userMapper.selectById(userId);

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        if (!order.getUserId().equals(userId)) {
            throw new RuntimeException("无权评价此订单");
        }
        if (!Constants.ORDER_STATUS_COMPLETED.equals(order.getStatus())) {
            throw new RuntimeException("只能评价已完成的订单");
        }

        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getOrderId, orderId).eq(Review::getReviewerId, userId);
        if (reviewMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("已评价过此订单");
        }

        Review review = new Review();
        review.setOrderId(orderId);
        Long bookId = dto.getBookId();
        if (bookId == null) {
            com.campus.book.entity.OrderItem orderItem = orderItemMapper.selectOne(
                    new LambdaQueryWrapper<com.campus.book.entity.OrderItem>()
                            .eq(com.campus.book.entity.OrderItem::getOrderId, orderId)
                            .last("LIMIT 1"));
            if (orderItem != null) {
                bookId = orderItem.getBookId();
            }
        }
        review.setBookId(bookId);
        review.setReviewerId(userId);
        review.setReviewerNickname(user.getNickname());
        review.setRevieweeId(order.getSellerId());
        review.setRating(dto.getRating());
        review.setContent(dto.getContent());
        review.setStatus(Constants.REPORT_STATUS_PENDING);

        try {
            review.setImages(objectMapper.writeValueAsString(dto.getImages()));
        } catch (JsonProcessingException e) {
            review.setImages("[]");
        }

        reviewMapper.insert(review);
        return review;
    }

    @Override
    public Page<ReviewVO> getBookReviews(Long bookId, Integer pageNum, Integer pageSize) {
        Page<Review> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getBookId, bookId)
                .eq(Review::getStatus, Constants.REPORT_STATUS_HANDLED)
                .orderByDesc(Review::getCreateTime);
        Page<Review> reviewPage = reviewMapper.selectPage(page, wrapper);

        Page<ReviewVO> voPage = new Page<>(reviewPage.getCurrent(), reviewPage.getSize(), reviewPage.getTotal());
        voPage.setRecords(reviewPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public Page<ReviewVO> getUserReviews(Long userId, Integer pageNum, Integer pageSize) {
        Page<Review> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Review::getRevieweeId, userId)
                .eq(Review::getStatus, Constants.REPORT_STATUS_HANDLED)
                .orderByDesc(Review::getCreateTime);
        Page<Review> reviewPage = reviewMapper.selectPage(page, wrapper);

        Page<ReviewVO> voPage = new Page<>(reviewPage.getCurrent(), reviewPage.getSize(), reviewPage.getTotal());
        voPage.setRecords(reviewPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public void auditReview(Long reviewId, String status) {
        Review review = reviewMapper.selectById(reviewId);
        if (review == null) {
            throw new RuntimeException("评价不存在");
        }
        review.setStatus(status);
        reviewMapper.updateById(review);

        if (Constants.REPORT_STATUS_HANDLED.equals(status)) {
            User reviewee = userMapper.selectById(review.getRevieweeId());
            if (reviewee != null) {
                List<Review> reviews = reviewMapper.selectList(
                        new LambdaQueryWrapper<Review>()
                                .eq(Review::getRevieweeId, reviewee.getId())
                                .eq(Review::getStatus, Constants.REPORT_STATUS_HANDLED));
                double avgRating = reviews.stream().mapToInt(Review::getRating).average().orElse(5.0);
                int creditScore = (int) (avgRating * 20);
                creditScore = Math.max(Constants.CREDIT_SCORE_MIN, Math.min(Constants.CREDIT_SCORE_MAX, creditScore));
                reviewee.setCreditScore(creditScore);
                userMapper.updateById(reviewee);
            }
        }
    }

    @Override
    public void deleteReview(Long reviewId) {
        reviewMapper.deleteById(reviewId);
    }

    private ReviewVO convertToVO(Review review) {
        ReviewVO vo = new ReviewVO();
        BeanUtils.copyProperties(review, vo);
        try {
            vo.setImages(objectMapper.readValue(review.getImages(), new TypeReference<List<String>>() {}));
        } catch (Exception e) {
            vo.setImages(new ArrayList<>());
        }
        return vo;
    }
}
