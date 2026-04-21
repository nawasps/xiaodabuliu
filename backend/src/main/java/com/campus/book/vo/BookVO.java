package com.campus.book.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookVO {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String coverImage;
    private List<String> images;
    private Long categoryId;
    private String categoryName;
    private String condition;
    private List<String> tags;
    private String status;
    private Long userId;
    private String sellerNickname;
    private String sellerAvatar;
    private Integer sellerCreditScore;
    private Integer viewCount;
    private Integer favoriteCount;
    private Boolean isFavorite;
    private LocalDateTime createTime;
    private Double sortScore;
}
