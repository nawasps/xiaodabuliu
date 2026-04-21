package com.campus.book.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReportVO {
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
    private LocalDateTime createTime;
}
