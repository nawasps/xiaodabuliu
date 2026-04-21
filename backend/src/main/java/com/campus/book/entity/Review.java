package com.campus.book.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("b_review")
public class Review implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    private Long bookId;

    private Long reviewerId;

    private String reviewerNickname;

    private Long revieweeId;

    private Integer rating;

    private String content;

    private String images;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
