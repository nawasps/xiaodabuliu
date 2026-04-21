package com.campus.book.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("b_order_item")
public class OrderItem implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long bookId;

    private String bookTitle;

    private String bookImage;

    private BigDecimal price;

    private Integer quantity;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
