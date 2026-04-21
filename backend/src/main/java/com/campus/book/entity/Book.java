package com.campus.book.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("b_book")
public class Book implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private String images;

    private Long categoryId;

    private String categoryName;

    @TableField("`condition`")
    private String condition;

    @TableField("`tags`")
    private String tags;

    @TableField("`status`")
    private String status;

    @TableField("`user_id`")
    private Long userId;

    @TableField("`seller_nickname`")
    private String sellerNickname;

    @TableField("`view_count`")
    private Integer viewCount;

    @TableField("`favorite_count`")
    private Integer favoriteCount;

    @TableField("`is_recommended`")
    private Integer isRecommended;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
