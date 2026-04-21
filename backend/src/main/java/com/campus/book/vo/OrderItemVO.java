package com.campus.book.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderItemVO {
    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookImage;
    private BigDecimal price;
    private Integer quantity;
}
