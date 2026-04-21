package com.campus.book.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewVO {
    private Long id;
    private Long orderId;
    private Long bookId;
    private Long reviewerId;
    private String reviewerNickname;
    private Long revieweeId;
    private Integer rating;
    private String content;
    private List<String> images;
    private String status;
    private LocalDateTime createTime;
}
