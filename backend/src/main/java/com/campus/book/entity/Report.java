package com.campus.book.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("b_report")
public class Report implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String type;

    private Long reportUserId;

    private String reportUsername;

    private Long reportedUserId;

    private String reportedUsername;

    private Long bookId;

    private String bookTitle;

    private String reason;

    private String description;

    private String images;

    private String status;

    private String handleResult;

    private Long handlerId;

    private LocalDateTime handleTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
